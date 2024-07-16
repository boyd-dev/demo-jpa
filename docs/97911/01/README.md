## 연관관계

- 저자(author)와 도서(book) 관계를 통해 `@OneToMany` 관계를 살펴본다. `@OneToMany`는 관계형 데이터베이스에서 흔히 보는 관계지만 JPA에서는 여러 가지 문제를 지닌 관계라서 처음에 언급되는 것 같다.

- 결론부터 말하면 일반적으로 단방향 `@OneToMany`는 피해야 한다. 왜 그런지 비교적 길게 기술되어 있다. 단방향 `@OneToMany`는 중간 테이블이 생성되므로 CRUD에서 번거롭고 부가적인 SQL문이 실행될 수 밖에 없다. 예를 들어 기존 저자의 새로운 도서를 등록하는 경우 book에 인서트하고 중간 테이블에서 해당 저자를 모두 삭제한 후 다시 저자-도서 데이터를 저장하기 때문에 매우 비효율적이다.

   author에 `@JoinColumn`을 추가하면 중간 테이블이 생성되지 않지만 위의 비효율적인 SQL 실행이 없어지는 것은 아니다.

- author(부모)와 book(자식) 엔티티는 FK를 통해 서로 "동기화(synchronizing)"되어 있다고 표현하고 있다. 상식적으로 저자 없는 책은 없다고 보기 때문에 항상 부모인 author가 먼저 등록되고 book이 등록되는 흐름으로 가야 한다. 이것은 단뱡향이나 양방향의 문제로 생각하는 것이 아니라 양방향이라고 해도 부모에서 자식으로 전이가 일어나야 한다.

  관계형 데이터베이스에서는 자식을 먼저 삭제해야 부모의 데이터를 지울 수 있다. 하지만 `@OneToMany`와 같은 연관 관계에서는 부모인 author를 지우면서 자식인 book이 cascade로 삭제된다. 어떻게 보면 순서가 다르다는 느낌을 받을 수 있겠지만 실제 SQL 실행 순서는 데이터베이스와 동일하다.

- `mappedBy`의 규칙  
이것은 양방향 관계에서 부모 엔티티에 있는 관계의 속성이다. 양방향에서는 "owning side"는 흔히 말하는 연관 관계의 주인이라고 표현을 하지만 사실은 FK를 소유하는 엔티티를 의미한다. 반면 "inverse side"는 `mappedBy`로 자식을 참조한다. 즉 `mappedBy`가 있으면 그 상대 엔티티는 "owning side"라고 판단할 수 있다. 관계의 주인이라고 해서 그것을 부모 엔티티라고 생각하면 안될 것 같다(오히려 그 반대다).

- `equals`와 `hashCode`의 오버라이딩  
일반적으로 식별자(PK)가 같으면 그 엔티티는 같아야 한다(equals == true)고 본다. 하이버네이트의 공식 문서에서는 이것이 일반적인 자바 프로그래밍에서와 다른 점이라고 말하고 있다. 

  > Normally, most Java objects provide a built-in equals() and hashCode() based on the object’s identity, so each new object will be different from all others. This is generally what you want in ordinary Java programming. Conceptually however this starts to break down when you start to think about the possibility of multiple instances of a class representing the same data.

  `Object` 클래스의 `equals`와 `hashCode` 기본 구현을 그대로 사용하면 같지 않기 때문에 반드시 엔티티의 특성에 맞게 오버라이딩 해야 한다. 참고로 자바에서 `equals`가 true가 되려면 `hashCode`도 반드시 일치해야 한다. 책에서는 아마 이런 이유로 `hashCode`가 항상 상수를 리턴하도록 만들어야 한다고 설명한다.
  
  본문에서는 엔티티는 모든 상태 전이에서 동등(equals == true)해야 한다고 설명한다. 여기서 상태 전이란 "persistence context"와의 관계, 또는 데이터베이스와의 관계를 나타내는 상태들 `transient`, `managed`, `detached` 등을 의미한다. 이러한 상태 전이가 일어나도 해당 엔티티는 equals == true 해야 한다.

  그런데 하이버네이트에서는 내부적으로 "더티 체킹"으로 엔티티의 변경을 감지하기 때문에 equals를 사용하지는 않는다. 엔티티의 속성 값이 변경되어 업데이트가 되더라도 그 엔티티는 상태 전이에 상관없이 equals == true 이어야 한다.

  특히 컬렉션 타입으로 `Set`을 사용하는 경우 중복 등록이 될 수 있으므로 `equals`와 `hashCode`의 구현이 중요하다. 기본 원칙은 불변인 비지니스 key(natural key)가 같으면 equals == true가 되도록 오버라이딩하는 것이다. 

- 연관 관계에서 부모 또는 자식에 따라 디폴트 지연 로딩이 다르다. `@OneToMany`는 디폴트로 `FetchType.LAZY`이지만 `@ManyToOne`은 `FetchType.EAGER`이다. 기본적으로 모든 패치 타입은 `FetchType.LAZY`로 설정하는 것이 바람직하다.

- `toString`을 오버라이딩 할 때 자식 엔티티의 속성을 넣으면 안된다. 그렇게 되면 의도치 않게 자식 엔티티를 함께 패치하게 되므로 성능상 좋지 않다.

- 단방향 `@ManyToOne`은 일반적으로 효율적이다. 결론적으로 양방향 `@OneToMany`가 필요없다면 단방향 `@ManyToOne`을 사용하는 것이 좋다.

- `@ManyToMany`에서 컬렉션 타입은 `Set`을 사용하는 것이 좋다. 그런데 `@OneToMany`에서도 Many 쪽은 특별한 사정이 없다면 `Set`을 쓰는 것이 좋을까? 
  이 관계는 양쪽이 모두 부모가 될 수 있으므로 상식적으로 `CascadeType.ALL`을 사용하면 안된다. 특히 자동 삭제는 말이 안된다. 예를 들어 가수와 레코드사의 관계에서 레코드사가 삭제되면 해당 레코드사에서 앨범을 발매한 가수까지 삭제될 수는 없다.

- `CascadeType.REMOVE`와 `orphanRemoval=true`의 차이  
부모가 삭제되면 자동으로 자식 데이터가 삭제되는 것은 둘 다 같으므로 둘 중 하나만 쓰면 된다. 이 경우에 보통 부모가 등록되면 자식을 같이 등록 또는 삭제하는 경우가 많기 때문에 `CascadeType.ALL`을 쓰는 것이 일반적이다.  
  자동 삭제를 위해서는 연관 관계 자식들이 1차 캐시에 로드되어야 한다. 따라서 삭제 전에 자식들을 로드하는 select가 실행된다. 또 그렇게 로드된 엔티티들을 개별적으로 삭제한다. 예를 들어 저자가 삭제되고 그 저자의 책이 3건이라면 `delete`문이 3번 호출된다.
  
  부모가 삭제되면 자식을 전부 삭제하는 `CascadeType.REMOVE`와 비슷하면서도 다른 옵션인 `orphanRemoval`은 자식의 일부를 삭제할 때 필요하다. 예를 들어 `orphanRemoval=false`가 되면 부모 엔티티에서 컬렉션 타입의 자식을 일부 삭제하더라도 자식 데이터는 삭제되지 않는다. `orphanRemoval=true`이면 부모에서 컬렉션의 모든 자식을 삭제하면 해당 자식 데이터는 당연히 전부 삭제되므로 결과적으로 `CascadeType.REMOVE`와 같아진다. 그러나 부모는 그대로 남는다.

- 영속성 컨텍스트에서 유효하지 않은 엔티티  
한번 패치된 엔티티는 "persistent context"라고 부르는 1차 캐시에 `managed` 상태로 존재하고 상태 전이가 일어날 수 있다(`transient`, `managed`, `detached` 등). 그런데 단순히 엔티티를 삭제하는 경우, 즉 delete(또는 remove)로 엔티티를 삭제하는 경우는 "persistent context"에 반영되지만 그렇지 않은 경우도 있다는 것이다. 

  유효하지 않은(outdated) 엔티티가 캐시에 남을 수 있는 경우는, 책에서 예로 든 `JpaRepository` 기본 제공 메소드인 `deleteInBatch`가 그 중 하나다. 이 메소드는 이름 그대로 다건을 벌크로 지울 때 유용하다(delete가 한번만 호출). 문제가 될 소지가 있는 이유는 그렇게 삭제된 엔티티가 캐시에 (아마 managed 상태로) 남아 있기 때문에 `author.setGenre("Anthology")`라고 하면 예외가 발생한다(물론 삭제된 것을 업데이트할 일은 없을 것 같은데?). 이것은 `@Modifying` 어노테이션으로 직접 DML을 작성했을 때도 마찬가지 맥락의 예외가 발생한다. 데이터베이스에서는 삭제가 되었지만 "persistent context"에는 이것이 아직 유효한 엔티티로 판단하므로 다시 업데이트하려고 하는 것이다. 그래서 보통 `@Modifying(clearAutomatically=true, flushAutomatically=true)`으로 1차 캐시를 현행화시킨다.
   
   다음 두 가지 메소드를 살펴보자.
   ```
   public interface SingerRepository extends JpaRepository<Singer, Long> {

     @Query(value = "delete from Singer s where s.id = ?1")
     @Modifying
     public void removeSingerById(Long id);
	
   }
   ```
   서비스 메소드에서 다음 차이를 비교하면 된다.
   ```
   public void removeSingerByName(String first, String last) {
     
     Singer singer = singerRepository.findSingerById(Long.parseLong("2"));		
     
     singerRepository.delete(singer); // (1)
     //singerRepository.removeSingerById(Long.parseLong("2")); // (2)
		
     singer.setLastName("Rennon");
		
  }
   ```
   (1)의 경우 패치한 singer를 삭제하고 바로 `setLastName`을 해도 아무런 문제가 발생하지 않는다. 이미 "persistent context"에서 singer는 managed 상태가 아니기 때문이다. 그러나 (2)에서는 캐시가 현행화 되지 않았기 때문에 이미 삭제된 데이터를 플러시하게 되면서 예외를 발생시킨다. 


- 엔티티 그래프로 연관 관계 데이터 가져오기  
연관 관계의 데이터, 이를테면 가수-앨범(singer-album) 관계에서 album을 가져오려면 JOIN FETCH를 쓰는 방법을 생각할 수 있다. JOIN FETCH는 사실상 "left outer join" 이다. 왜냐하면 가수를 가져오는데 앨범이 있을 수도 없을 수도 있기 때문이다. 또 JOIN FETCH는 직접 쿼리를 작성해야 한다.  
  연관 관계를 가져오는 다른 방법으로 엔티티 그래프를 이용할 수 있다. 이것은 쿼리와 독립적으로 적용될 수 있다는 장점이 있다. 다시 말해 재사용이 가능하다. 엔티티 그래프는 엔티티에 정의한다.

  ```
  @NamedEntityGraph(
		name = "singer_albums_graph",
		attributeNodes = {
         @NamedAttributeNode("albums")
		}
  )
  public class Singer extends BaseEntity implements Serializable {
     ...
  }
  ```

  스프링 데이터 JPA에서는 `@EntityGraph` 어노테이션으로 정의된 엔티티 그래프를 사용할 수 있다. 그냥 `findById`는 단일한 `Singer`를 조회하지만 엔티티 그래프를 다음과 같이 추가하면 left outer join 쿼리가 실행되면서 `Album` 컬렉션을 즉시 가져온다. 

  ```
  @EntityGraph(value = "singer_albums_graph", type = EntityGraphType.FETCH)
  public Optional<Singer> findById(Long id);
  ```
  
  ```
  SELECT singer0_.id AS id1_2_0_, singer0_.birth_date AS birth_da2_2_0_, 
  ...
  FROM singer singer0_ 
  LEFT OUTER JOIN album albums1_ 
  ON singer0_.id=albums1_.singer_id WHERE singer0_.id=3
  ```
  `EntityGraphType.FETCH` 속성은 `attributeNodes`에 지정된 속성을 eager 패치하고 나머지는 lazy로 가져온다. 다른 타입으로 ``EntityGraphType.LOAD`가 있는데 `attributeNodes`에 지정된 속성을 eager 패치하고 나머지는 지정된 패치 타입이나 기본값을 적용한다.
  
  엔티티 그래프는 다른 메소드(쿼리)에도 재사용이 가능하다.
  ``` 
  @EntityGraph(value = "singer_albums_graph", type = EntityGraphType.FETCH)
  public List<Singer> findByBirthDateGreaterThan(Date d);
  ```
  엔티티 그래프는 JPQL에서 사용 가능하지만 네이티브 쿼리에서는 쓸 수 없다.  

- 일반적으로 다수의 연관 관계 데이터를 동시에 즉시 가져오기 하는 경우 `MultipleBagFetchException`이 발생할 수 있다.




