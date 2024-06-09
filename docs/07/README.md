## DAO, Service, Test
이제 간단한 CRUD를 만들어보기로 합니다. DAO 인터페이스를 정의합니다.
```
public interface SingerDao {	
	
	public List<Singer> findAll();
	public Singer findById(Long singerId);
	public Singer findByIdWithAlbums(Long singerId);
	public Singer findByIdWithRecordLabels(Long singerId);	
	public RecordLabel findRecordLabel(RecordLabel recordLabel);
	
	public Singer insert(Singer singer);
	public Singer update(Singer singer);
	public void delete(Singer singer);
	
	public RecordLabel insert(RecordLabel recordLabel);
	
}
```
우선 신규 건을 인서트하는 것부터 시작하겠습니다. 예제 데이터 모델은 Singer를 중심으로 데이터가 생성된다고 하겠습니다. 

`SingerDao`를 구현하는 클래스 `SingerDaoImpl`는 아래와 같은 형태입니다. `@Repository` 어노테이션으로 DAO 객체임을 표시합니다.
```
@Repository("singerDao")
public class SingerDaoImpl implements SingerDao {
	
	@PersistenceContext
	private EntityManager em;

    // override
}
```
`EntityManager`는 스프링에서 설정한 `EntityManagerFactory`가 만든 CRUD 관리자라고 생각하면 되겠습니다. 트랜잭션이 시작되고 종료되는 동안 그 사이에 벌어지는 모든 CRUD를 관리합니다. `@PersistenceContext` 어노테이션을 사용하면 DAO 객체에 자동으로 주입됩니다.

예를 들어 Singer를 저장하려면 다음과 같이 `persist` 메소드를 사용하면 됩니다.
```
@Override
public Singer insert(Singer singer) {
	em.persist(singer);
	return singer;
}
```
`SingerDaoImpl`에는 단순하게 데이터 입출력을 위한 기능만 넣기로 합니다. 그 외에 로직은 앞으로 작성할 `@Service` 클래스에 넣기로 하겠습니다. 서비스 클래스를 위한 인터페이스는 아래와 같습니다.

```
public interface SingerService {
	
	public List<Singer> findAll();
	public Singer findById(Long singerId);
	public Singer findByIdWithAlbums(Long singerId);
	public Singer findByIdWithRecordLabels(Long singerId);
	public RecordLabel findRecordLabel(RecordLabel recordLabel);	
		
	public Singer insert(Singer singer);
	public Singer update(Singer singer);
	public void delete(Singer singer);
	
	public RecordLabel insert(RecordLabel recordLabel);
	
}
```
이것을 구현하는 `SingerServiceImpl` 클래스는 다음과 같습니다. 반드시 `@Transactional`을 추가해야 합니다. 
```
@Service("singerService")
@Transactional
public class SingerServiceImpl implements SingerService {
		
	private SingerDao singerDao;
	
	@Autowired	
	public void setSingerDao(SingerDao singerDao) {
		this.singerDao = singerDao;
	}

    // override

}
```
`SingerDao`를 setter 방식으로 주입받습니다. 신규 데이터 입력 메소드는 그냥 `SingerDao.insert`를 호출합니다.

```
@Override
public Singer insert(Singer singer) {
	return singerDao.insert(singer);
}	
```
이제 테스트 케이스를 작성하도록 하겠습니다. 새로운 Singer를 저장하면서 동시에 그 Singer의 Album도 저장할 것입니다. 눈여겨 볼 것은 SQL을 전혀 작성하지 않는다는 점입니다. JPA의 persist를 통해 자동으로 SQL이 생성되어 Singer 테이블에 저장될 것입니다. 

```
@Test
void testInsert() {
		
	Singer singer = new Singer();
		
	singer.setFirstName("Adele");
	singer.setLastName("Adkins");
	Date bd = Date.from(Instant.parse("1988-05-05T00:00:00Z"));
	singer.setBirthDate(bd);
				
	Album album = new Album();
	album.setTitle("Easy On Me");
	Date d = Date.from(Instant.parse("2021-10-14T00:00:00Z"));
	album.setReleaseDate(d);
	singer.addAlbum(album);
		
	album = new Album();
	album.setTitle("Hello");
	d = Date.from(Instant.parse("2015-10-23T00:00:00Z"));
	album.setReleaseDate(d);
	singer.addAlbum(album);
		
	service.insert(singer);

}
```
우선 Singer를 하나 생성합니다. 이렇게 new로 생성된 객체는 "transient" 상태로 아직 DB에 반영되지 않았습니다.  Singer의 각 속성들을 입력합니다. 그리고 Album도 함께 생성합니다.  

현재 Singer와 Album은 @ManyToOne 양방향으로 설정되어 있습니다. Singer를 인서트하면서 그 Singer의 Album 두 개를 함께 저장시키려고 합니다. 이렇게 하기 위해서는 Singer의 매핑 어노테이션에 다음과 같이 `cascade`설정이 필요합니다. `cascade`는 FK 관계에 따라 연관 데이터들을 함께 변경시키는 것을 말합니다. SQL로 말하자면 foreign key ON DELETE CASCADE와 ON UPDATE CASCADE과 유사합니다.
```
@OneToMany(
    mappedBy = "singer", 
    cascade = CascadeType.ALL, 
    orphanRemoval = true, 
    fetch = FetchType.LAZY
    )
private Set<Album> albums = new HashSet<>();
```

양방향 관계에서는 setter를 통해 상대 객체를 넣어주어야 합니다. 다시 말해서 Singer 객체를 생성하고 나서 각 Album 객체에 아래와 같이 Singer를 넣습니다.

```
Singer singer = new Singer();
		
singer.setFirstName("Adele");
...

Album album = new Album();
album.setTitle("Easy On Me");
...
album.setSinger(singer);
```
반대로 Singer에는 컬렉션 타입인 `Set<Album>`을 넣어주어야 합니다.

```
Set<Album> albums = new HashSet<>();
...
albums.add(album);
...
albums.add(album);

singer.setAlbums(albums);
```

그런데 이렇게 하면 번거롭기 때문에 Singer 엔티티에 아래와 같은 `addAlbum` 메소드를 추가합니다.
```
public boolean addAlbum(Album album) {
	album.setSinger(this);
	return getAlbums().add(album);		
}	
```
이렇게 하면 setter를 매번 써주지 않고 `addAlbum` 호출로 양방향으로 설정된 객체에 서로의 데이터를 넣어줄 수 있습니다. 객체가 준비되었으면 이것을 서비스에 전달하고 서비스는 다시 DAO를 거쳐 DB에 저장하게 됩니다.

```
service.insert(singer);
```
표면적으로는 Singer 객체 하나만 전달되었지만 이미 그 안에는 Album 객체가 포함되어 있습니다. 로그에 나타난 실제 SQL을 볼 수 있습니다.

```
insert into singer (birth_date, first_name, last_name, version) values (?, ?, ?, ?)
...
insert into album (release_date, singer_id, title, version) values (?, ?, ?, ?)
TRACE ... - binding parameter [1] as [DATE] - [Thu Oct 14 09:00:00 KST 2021]
TRACE ... - binding parameter [2] as [BIGINT] - [1]
TRACE ... - binding parameter [3] as [VARCHAR] - [Easy On Me]
TRACE ... - binding parameter [4] as [INTEGER] - [0]
insert into album (release_date, singer_id, title, version) values (?, ?, ?, ?)
...
```
album 테이블의 `singer_id`에는 직전에 인서트된 singer의 PK가 저장됩니다. 이 과정을 통해 두 테이블에 설정된 FK의 정합성이 보장됩니다. JPA 트랜잭션 관리자에 의해 메소드가 종료되면 자동으로 DB에 커밋됩니다.

리턴되는 Singer 객체는 이제 PK에 해당하는 `id` 값이 들어 있고 `transient`에서 비로소 `managed` 상태가 됩니다. `managed` 상태의 엔티티 객체들은 소위 말하는 "persistence context"에 캐시(cache)됩니다. 하이버네이트의 가이드에 이에 대한 [설명](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#pc)이 잘 나와 있습니다.

>transient  
the entity has just been instantiated and is not associated with a persistence context. It has no persistent representation in the database and typically no identifier value has been assigned (unless the assigned generator was used).

>managed, or persistent  
the entity has an associated identifier and is associated with a persistence context. It may or may not physically exist in the database yet.

>detached  
the entity has an associated identifier but is no longer associated with a persistence context (usually because the persistence context was closed or the instance was evicted from the context)

>removed  
the entity has an associated identifier and is associated with a persistence context, however, it is scheduled for removal from the database.

Singer와 RecordLabel 역시 같은 방식으로 저장할 수 있습니다. 이 경우는 양방향 `@ManyToMany`이기 때문에 두 테이블이 FK와 같은 직접적인 relation은 없습니다. 하지만 연관 관계에 따라 중간 테이블에 자동으로 인서트가 이루어집니다. 

`@ManyToMany`의 `cascade`는 관계의 특성상 연관된 데이터지만 Singer를 삭제할 때 RecordLabel까지 삭제하는 것은 말이 안되기 때문에 아래와 같이 REMOVE를 제외하고 설정합니다. 

```
@ManyToMany(
    cascade = {CascadeType.PERSIST, CascadeType.MERGE}, 
    fetch = FetchType.LAZY
)
private List<RecordLabel> recordLabels = new ArrayList<>();
```
하이버네이트 가이드에는 아래와 같이 설명되어 있습니다.
>For @ManyToMany associations, the REMOVE entity state transition doesn’t make sense to be cascaded because it will propagate beyond the link table. Since the other side might be referenced by other entities on the parent-side, the automatic removal might end up in a ConstraintViolationException.

가수가 레코드사와 새로운 계약을 맺게 되는 경우 Singer는 이미 등록된 RecordLabel을 참조할 수 있도록 중간 테이블인 `singer_record_label`에 인서트되어야 합니다. 이것은 update에 해당하는데 여기서는 RecordLabel 테이블에 동일한 이름 `label`이 존재하는 경우 그것을 리턴해서 Singer를 업데이트하기로 합니다.

동일한 이름의 RecordLabel이 있는지를 조회하는 메소드는 다음과 같이 만들 수 있습니다.

```
@Override
public RecordLabel findRecordLabel(RecordLabel recordLabel) {
	return em.createNamedQuery("RecordLabel.Find_RecordLabel_With_Singer", RecordLabel.class)
			.setParameter(0, recordLabel.getLabel())
			.getResultStream().findFirst().orElse(null);
}
```
여기서 `em.createNamedQuery`를 사용했습니다. 이것은 미리 정해진 쿼리를 만들어놓고 필요할 때 실행할 수 있습니다. `@NamedQuery` 쿼리는 `RecordLabel` 엔티티에 두기로 합니다. 여기에 사용된 쿼리는 SQL이 아니라 JPQL입니다. `join fetch`는 연관된 컬렉션 객체를 가져올 때 사용하는 구문입니다.

```
@Entity(name = "RecordLabel")
@Table(name = "record_label")
@NamedQueries(
	{
		@NamedQuery(
			name = "RecordLabel.Find_RecordLabel_With_Singer",
			query = "select r from RecordLabel r left join fetch r.singers s where r.label = ?1"
		)
	}
)
public class RecordLabel extends BaseEntity implements Serializable {
...
}

```
`label`에 해당하는 조건은 변하는 것이므로 외부 파라미터로 받기 위해 순서대로 입력받는 `?1`을 사용했습니다. 이것은 이름을 지정하는 파라미터 `:label`으로 써도 되겠습니다.

테스트 케이스는 아래와 같이 작성할 수 있습니다. 1번 Singer의 레코드사에 Columbia Records를 추가합니다.

```
@Test
@DisplayName("update Singer with RecordLabel")
@Order(4)
void updateSingerWithRecordLabel() {
		
	RecordLabel recordLabel = new RecordLabel();
	recordLabel.setLabel("Columbia Records");	
		
	RecordLabel result = service.findRecordLabel(recordLabel);

	Singer singer = new Singer();		
	singer = service.findByIdWithRecordLabels(Long.parseLong("1"));
	singer.addRecordLabel(result);
	service.update(singer);		
	...
		
}
```
`SingerDao`에서는 `em.merge`를 사용합니다.

```
@Override
public Singer update(Singer singer) {		
	return em.merge(singer);
}
```

마지막으로 Singer가 참조하는 RecordLabel 중 하나를 삭제하는 방법을 알아보겠습니다. 이것은 RecordLabel에 있는 레코드사 데이터는 남겨두고(다른 Singer와 연관되어 있을 수 있으므로) 중간 테이블인 `singer_record_label`에서만 삭제하는 것입니다.

Singer를 조회한 후에 `recordLabels`에서 해당 레코드사를 삭제한 후 다시 Singer를 업데이트하면 되겠습니다.

```
@Test
@DisplayName("delete RecordLabel by Singer")
@Order(5)
void deleteRecordLabelbySinger() {
		 
	Singer singer = service.findByIdWithRecordLabels(Long.parseLong("3"));
		 
	RecordLabel recordLabel = new RecordLabel();
	recordLabel.setLabel("Columbia Records");
	boolean result = singer.getRecordLabels().remove(recordLabel);
		 
	service.update(singer);
	assertTrue(result);		 		
}
```
이때 `label` 속성 값이 같은 것을 동일한 레코드사로 보고 컬렉션에서 삭제시킬 것이므로 이를 위해 `RecordLabel` 클래스에 `hashCode`와 `equals` 메소드를 오버라이드할 필요가 있습니다.

## 트랜잭션 기반의 persistence context

보통 트랜잭션은 서비스 메소드 단위로 적용합니다. 다시 말해서 `@Transactional`은 서비스 클래스의 메소드 단위로 적용하는 경우가 대부분 입니다. `EntityManager`가 동작하려면 반드시 트랜잭션이 시작되어야 하는데 이 말은 결국 persistence context가 트랜잭션 단위라는 의미입니다. 이것을 "Transaction-scoped persistence context"라고 표현합니다(하이버네이트에서는 "Session-per-request pattern"이라고 합니다). 서비스 메소드가 리턴되면 트랜잭션이 종료되고 persistence context에 있던 모든 "managed" 엔티티들은 "flush"되고 커밋됩니다. 그리고 `EntityManager`는 종료됩니다.  

따라서 persistence context의 엔티티들이 공유되기 위해서는 단일 트랜잭션, 즉 하나의 서비스 메소드 내에서 CRUD가 이루어져야 하며 다른 서비스 메소드 호출시 트랜잭션 전파가 이어지면 persistence context 역시 지속됩니다. 동일한 엔티티를 조회하면 persistence context에 존재하는 엔티티를 계속 참조합니다. 하지만 `Propagation.REQUIRES_NEW`처럼 새로운 트랜잭션에서 조회한 엔티티는 다른 엔티티가 됩니다. 여기서 유념할 것은 persistence context 내에서 한번 엔티티를 가져온 후 다시 JPQL로 조회하더라도 실행되지만 해당 엔티티가 이미 있으므로 그것을 반환한다는 점입니다. 즉 다른 트랜잭션에 의해 그 엔티티의 속성이 업데이트 되더라도 "repeatable read"처럼 변경 전 엔티티를 리턴한다는 말이 되겠습니다.

## NamedQuery
네임드 쿼리는 마치 JDBC의 `PreparedStatement`처럼 사전에 정의된 쿼리문입니다. 보통 직접 쿼리를 작성하려면 DAO 클래스에서 EntityManager의 `createQuery`를 사용할 수 있는데, 네임드 쿼리를 사용하면 쿼리 문자열을 DAO 클래스(또는 레포지토리)로부터 분리하여 쿼리 구성을 중앙화할 수 있습니다(물론 분리할 필요가 없다고 생각할 수도 있습니다). 예를 들어 네임드 쿼리를 별도의 파일, META-INF/orm.xml이나 jpa-named-queries.properties 파일로 분리할 수 있습니다.  

여기서는 `@NamedQuery` 어노테이션을 사용하여 관련된 엔티티 클래스에 정의하는 방법을 살펴보겠습니다(이미 앞에서 사용한 적이 있습니다). 

```
@Entity(name = "RecordLabel")
@Table(name = "record_label")
@NamedQueries(
	{
		@NamedQuery(
			name = "RecordLabel.Find_RecordLabel_With_Singer",
			query = "select r from RecordLabel r left join fetch r.singers s where r.label = ?1"
		)
	}
)
public class RecordLabel extends BaseEntity implements Serializable {
...
}
```
`@NamedQuery`의 `name` 속성의 이름은 임의로 줄 수 있지만 스프링 데이터 JPA에서 이름을 메소드명 그대로 사용하려면 `{Entity Name}.{Method Name}` 형식을 지켜야 합니다. 이렇게 정의된 네임드 쿼리는 다음과 같이 참조하면 되겠습니다. 

```
TypedQuery nq = em.createNamedQuery("RecordLabel.Find_RecordLabel_With_Singer", RecordLabel.class);
nq.setParameter(1, "Columbia Records");
List<RecordLabel> result = nq.getResultList();
```
네임드 쿼리는 "네이티브(native)" SQL로 작성할 수도 있는데 이때는 `@NamedNativeQuery` 어노테이션을 사용합니다.

## NativeQuery
엔티티의 일부 컬럼이나 조인으로 조회하는 데이터는 컬럼 값들을 가져오는 스칼라 쿼리입니다. 이때는 엔티티를 그대로 가져오기 보다는 [프로젝션](https://github.com/boyd-dev/demo-jpa/blob/main/docs/09/README.md#projection)이나 데이터베이스에 종속적인 네이티브 쿼리를 이용할 때가 많습니다. 여기서는 `@NamedNativeQuery`를 사용하고 그 결과셋을 매핑해주는 방법에 대해 알아보겠습니다.  

아래와 같은 네임드 네이티브 쿼리가 있다고 생각해보겠습니다. 

```
@NamedNativeQueries(
		{
			@NamedNativeQuery(
				name = "Singer.Find_Singer_Native",
				query = "select first_name, last_name from singer where id = ?1",
				resultSetMapping = "singerNameMapping"
			)
		}		
)
```
`resultSetMapping` 속성은 결과셋의 매핑입니다. 네이티브 쿼리 결과셋의 타입은 포괄적인 타입인 `List<Object[]>`이 되는데 이때 컬럼 값을 `Object[]` 배열에 매핑해주게 됩니다. 
따라서 `singerNameMapping`은 아래와 같이 `@SqlResultSetMapping` 어노테이션을 사용하여 정의할 수 있습니다. 

```
@SqlResultSetMapping(name = "singerNameMapping", 
                    columns = {@ColumnResult(name="first_name"), @ColumnResult(name="last_name")})
```

조회된 각 컬럼 값을 매핑해주는 것은 `@ColumnResult`입니다. 컬럼 값들의 타입을 지정해서 좀더 명확한 매핑을 할 수도 있습니다.

```
columns = {@ColumnResult(name="first_name", type = String.class), @ColumnResult(name="last_name", type = String.class)}
```
위와 같이 각 컬럼들의 매핑을 해줄 수도 있지만 아예 하나의 DTO를 지정할 수도 있습니다. 이 경우는  `@SqlResultSetMapping`에서 `classes` 속성을 지정합니다. `Singer`와 `Album`을 
조인한 결과를 `SingerAlbumsDto` 타입에 담는 것을 예로 보겠습니다.

```
@NamedNativeQueries(
		{
			...
			@NamedNativeQuery(
				name = "Singer.Find_Singer_Albums",
				query = "select s.first_name as firstName, s.last_name as lastName, a.title as title "
						+ "from singer s join album a on a.singer_id = s.id where s.id = ?1",
				resultSetMapping = "SingerAlbumsMapping"
			)	
		}
		
)
```
`SingerAlbumsMapping`은 아래와 같이 정의합니다.

```
@SqlResultSetMapping(name = "SingerAlbumsMapping",
			classes = @ConstructorResult(
			                targetClass = SingerAlbumsDto.class,
			        	    columns = {
					               @ColumnResult(name="firstName"), 
					               @ColumnResult(name="lastName"),
					               @ColumnResult(name="title")
			                }				
			)
)
```
`SingerAlbumsDto`는 조회 컬럼들에 대응되는 속성들을 차례대로 입력받는 생성자를 가진 클래스입니다.

```
public class SingerAlbumsDto {
	
	private final String firstName;
	private final String lastName;
	private final String title;	
	
	public SingerAlbumsDto(String firstName, String lastName, String title) {
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


참고  
[Native SQL Queries](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#sql)


[처음](../README.md) | [다음](../08/README.md)