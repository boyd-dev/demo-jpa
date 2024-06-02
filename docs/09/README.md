## JpaRepository 

보통 `JpaRepository`를 상속하여 인터페이스를 정의하는 경우는 "domain class-specific repository"를 만드는 것에 해당합니다. 즉 하나의 엔티티에 대한 CRUD를 간소화하려는 목적입니다. `JpaRepository`를 상속한 인터페이스만 정의해도 기본적인 CRUD를 포함한 여러 가지 입출력 메소드가 제공됩니다. 

예를 들어 아래와 같이 다음과 같이 `SingerRepository`를 정의하면

```
public interface SingerRepository extends JpaRepository<Singer, Long> {
    // No need to add method declaration
}
```

`Singer`에 대해 `public List<Singer> findAll()`이나 `public Optional<Singer> findById(Long singerId)`등의 메소드들이 제공됩니다. 특별히 오버라이드할 일이 없다면 그대로 사용하면 되고, 내부적으로 실제 구현체인 [`SimpleJpaRepository`](https://docs.spring.io/spring-data/jpa/docs/2.7.9/api/org/springframework/data/jpa/repository/support/SimpleJpaRepository.html)의 메소드로 자동으로 라우트됩니다.

`JpaRepository`에서 제공되지 않는 query는 다양한 방법으로 추가할 수 있습니다. 그중에 하나는 정해진 규칙에 의해 메소드 이름으로부터 query를 자동 생성하는 기능입니다. 정해진 규칙이란 메소드 이름의 형식입니다. 메소드 이름을 크게 두 개 부분으로 나누어 query를 생성합니다. 예를 들어 다음과 같은 메소드를 선언하면

```
 public List<Singer> findByFirstNameAndLastName(String first, String last); 
```
여기서 `findBy...`는 "subject"에 해당합니다. 조건에 해당하는 `FirstName`와 `LastName`을 `And` 로 연결했습니다. 조건에 해당하는 부분을 "predicate"이라고 합니다. 메소드 이름에 들어가는 `FirstName`와 `LastName`은 당연히 `Singer` 엔티티의 속성 `firstName`과 `lastName`에 대응되어야 합니다. 파라미터로 전달된 `String first`와 `String last`가 조건 값으로 들어가는 query가 생성됩니다(파라미터 이름은 속성과 관련이 없습니다).  

실제 실행되는 query는 아래와 같습니다.

```
select 
	singer0_.id as id1_2_, 
	singer0_.birth_date as birth_da2_2_, 
	singer0_.first_name as first_na3_2_, 
	singer0_.last_name as last_nam4_2_, 
	singer0_.version as version5_2_ 
from singer singer0_ 
where singer0_.first_name=? and singer0_.last_name=?
```
"subject"와 "predicate"으로 사용될 수 있는 키워드는 아래 링크를 참조하면 되겠습니다.

[Subject](https://docs.spring.io/spring-data/jpa/docs/2.7.x/reference/html/#appendix.query.method.subject)  
[Predicate](https://docs.spring.io/spring-data/jpa/docs/2.7.x/reference/html/#appendix.query.method.predicate)

레포지토리가 만들어질 때 정해진 규칙으로 query가 생성되지 않으면 빈 생성 오류가 발생하기 때문에 컨테이너가 구동되는 과정에서 미리 알 수 있습니다. 

메소드 이름을 기반으로 생성하기 어려운 query는 레포지토리 인터페이스에 `@Query` 어노테이션으로 직접 query를 작성할 수 있습니다. 예를 들어 아래와 같이 query를 정의할 수 있습니다. JPQL을 쓸 수도 있고 `nativeQuery = true` 속성을 추가하여 일반 SQL을 사용할 수 있습니다.

```
@Query("from Singer s join fetch s.albums a where s.id = :singerId")
public Singer findByIdWithAlbums(@Param("singerId") Long id);
```

참고적으로, 스프링 데이터 JPA가 제공하는 `JpaRepository`를 쓰지 않는 "plain" JPA에서는 `TypedQuery<X>`를 통해서 직접 query를 만들 수 있습니다. EntityManager의 `createQuery` 또는 `createNamedQuery`를 사용하여 JPQL이나 일반 SQL(native query)로 query를 작성합니다.  

예를 들어 Named Query는 해당 엔티티에 `@NamedQueries` 어노테이션을 사용하여 작성할 수 있습니다. DAO 클래스에서 아래와 같이 EntityManager의 `createNamedQuery`로 불러올 수 있습니다.

```
// Entity
@Entity(name = "RecordLabel")
@Table(name = "record_label")
@NamedQueries(
	{
		@NamedQuery(
			name = "RecordLabel.Find_RecordLabel_With_Singer",
			query = "select r from RecordLabel r left join fetch r.singers s "
					+ "where lower(r.label) like lower('%' || ?1 || '%')"
		)
	}
)
public class RecordLabel implements Serializable {
   ...
}

// DAO
public List<RecordLabel> findRecordLabel(String label) {
		return em.createNamedQuery("RecordLabel.Find_RecordLabel_With_Singer", RecordLabel.class)
			.setParameter(1, label)
			.getResultList();
}
```

## Projection

스프링 데이터 JPA에서 프로젝션(Projection)은 다음과 같이 설명되어 있습니다.

>Spring Data query methods usually return one or multiple instances of the aggregate root managed by the repository. However, it might sometimes be desirable to create projections based on certain attributes of those types. Spring Data allows modeling dedicated return types, to more selectively retrieve partial views of the managed aggregates.

간단히 말하면 필요한 속성들만으로 결과셋을 만드는 것을 의미합니다. 스프링 데이터 JPA를 사용하지 않는 경우는 EntityManager의 `createQuery`를 사용하면 `getResultList`나 `getSingleResult`를 사용할 수 있지만 필요한 속성만을 선택적으로 받게 되므로 결과 타입을 직접 명시하기 어렵습니다(단순히 `Object[]`로 받는 것을 스칼라 프로젝션이라고 합니다).  

인터페이스를 이용하는 프로젝션은 필요한 컬럼으로 인터페이스 타입을 정의합니다. 예를 들어 `Singer`의 모든 컬럼을 조회하지 않고 `firstName`과 `lastName`만을 가진 결과 타입을 리턴받기 위해서는 아래와 같은 인터페이스를 작성합니다.

```
public interface SingerName {	
	String getFirstName();
	String getLastName();
}
```
`SingerName` 인터페이스의 getter 메소드는 각각 `firstName`과 `lastName`과 일치하도록 해야 합니다. 그리고 레포지토리에는 아래와 같은 메소드를 선언합니다.

```
public interface SingerRepository extends JpaRepository<Singer, Long> {
    ...
	public SingerName findSingerById(Long id);
	...
}
```
메소드 이름인 `findSingerById`으로부터 query가 생성되고 그 결과는 `SingerName` 타입으로 리턴됩니다. 실제 실행되는 query 역시 해당 컬럼만 조회합니다.

```
select 
	singer0_.first_name as col_0_0_, 
	singer0_.last_name as col_1_0_ 
from singer singer0_ 
where singer0_.id=?
```

보통 여러 엔티티들을 조인한 결과셋을 가져올 때 프로젝션을 이용할 수 있습니다. 예를 들어 `Singer`와 `Album`을 조인하면서 `Singer`의 `firstName`과 `lastName` 그리고 연관된 `Album`의 `title`을 가져오는 경우를 생각해 보겠습니다.  

아래와 같이 인터페이스를 만들 수 있습니다. 

```
public interface SingerAlbums {	
	String getFirstName();
	String getLastName();
	String getTitle();	
}
```
레포지토리 인터페이스에서는 아래와 같은 메소드를 선언합니다. 

```
@Query(value = "SELECT s.firstName as firstName, s.lastName as lastName, a.title as title "
			+ "FROM Singer s JOIN s.albums a WHERE s.id = :singerId")
	public List<SingerAlbums> fetchSingerAlbumsById(@Param("singerId") Long id);
```
인터페이스의 getter가 모두 "aggregate root" 엔티티의 속성과 일치하면 이것을 "닫힌(closed)" 프로젝션이라고 합니다. 

클래스 기반의 프로젝션은 "DTO 프로젝션"이라고도 말합니다. 인터페이스 대신 POJO 형태의 DTO를 사용합니다. 앞서 `SingerAlbums` 인터페이스는 다음과 같은 클래스로 대체할 수 있습니다.  

```
public class SingerAlbums {
	
	private final String firstName;
	private final String lastName;
	private final String title;
		
	public SingerAlbums(String firstName, String lastName, String title) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.title = title;		
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getTitle() {
		return title;
	}	
}
```
레포지토리 인터페이스에는 "생성자 표현식(constructor expression)"을 사용한 query를 정의합니다.

```
@Query(value = "SELECT new com.foo.jpa.projection.SingerAlbums(s.firstName, s.lastName, a.title) "
			+ "FROM Singer s JOIN s.albums a WHERE s.id = :singerId")
	public List<SingerAlbums> fetchSingerAlbumsById(@Param("singerId") Long id);
```
생성자 표현식을 사용하기 때문 native query에서는 사용할 수 없다는 단점이 있습니다. 프로젝션 인터페이스나 DTO 없이 간단하게 `Tuple`을 이용하는 방법도 있습니다. 

```
@Query(value = "SELECT s.firstName as firstName, s.lastName as lastName, a.title as title "
			+ "FROM Singer s JOIN s.albums a WHERE s.id = :singerId")
	public List<Tuple> fetchSingerAlbumsById(@Param("singerId") Long id);
```
