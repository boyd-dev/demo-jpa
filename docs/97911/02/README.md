## 엔티티

- 엔티티 작성시 "플루언트(fluent)" 스타일 적용하기  
엔티티를 하나 생성하여 등록을 할 때 각 속성의 setter를 통해 값을 넣어주어야 한다. 속성이 많은 경우 이와 같은 작업이 번거로울 수 있기 때문에 "플루언트" 스타일로 값을 넣을 수 있도록 만들어주는 방법을 소개한다. 이 방법은 흔히 말하는 "빌더 패턴"과 유사한데, 빌더 패턴이 플루언트 스타일과 함께 쓰이는 경우가 많기 때문인 것 같다. 

   ```
   Singer singer = new Singer();
   singer.setFirstName("John");
   singer.setLastName("Lennon");
   ...
   ```
   플루언트 스타일은 아래와 같은 형태가 된다.
   ```
   Singer singer = new Singer()
                    .firstName("John")
                    .lastName("Lennon");
   ```
   빌드 메소드를 별도로 두는 빌더 패턴의 경우는 아래와 같은 형태가 된다.

   ```
   Singer singer = new Singer.Builder()
                       .firstName("John")
                       .lastName("Lennon")
                       .build();
   ```
- 자식을 등록할 때 FK컬럼에 부모의 키 채우기  
단방향 `@ManyToOne`에서는 자식을 등록할 때 연관된 부모의 PK를 알아야 한다. 보통 부모를 `findById`로 조회한 후 부모를 참조한다. 

   ```
   Author author = authorRepository.findById(1L).get();

   Book book = new Book();
   book.setAuthor(author);
   ```
   이렇게 하면 당연히 Author 엔티티를 조회하는 쿼리가 먼저 실행되고 인서트가 실행된다. 엔티티를 전부 가져오는 것은 이 목적에서는 불필요한 일이기 때문에 단지 부모 키가 필요하다면 `getReferenceById`를 쓰면 select 쿼리 없이 자식 인서트만 실행되므로 효율적이다. 책에서는 `getOne`을 사용했지만 deprecated 되었다.
   
- Optional 사용  
자바 8부터 도입된 `Optional<T>` 클래스는 타입 `T` 객체가 있음 또는 없음을 나타낼 때 사용하는 것으로, 단순히 null인지 아닌지를 판별해야 했던 기존 방식을 개선하려는 목적으로 사용한다. 
`JpaRepository`가 제공하는 `findById`도 리턴 타입으로 `Optional`을 사용하고 있다.  
따라서 엔티티의 getter나 레포지토리 메소드에서 리턴 타입으로 `Optional`을 쓰는 것을 먼저 고려해보는 것이 좋겠다. 

- 불변(immutable) 엔티티  
불변 엔티티의 조건은 연관관계를 포함하지 않고 `@Immutable`어노테이션이 있는 엔티티이다. 하이버네이트 공식 문서에서는 불변 엔티티에 대해 다음과 같은 두 가지 특성에 대해 설명하고 있다.

  - 더티체크 메커니즘을 적용할 필요가 없기 때문에 메모리 사용을 절감한다.
  - 더티체크를 하지 않으므로 플러시 속도를 높일 수 있다.

  생성하고나면 수정이 되지 않기 때문에 캐시에 저장하는 경우가 있는데 이때 엔티티에 `@Cache`(org.hibernate.annotations.Cache)를 지정할 수 있다. 이 캐시는 1차 캐시인 "persistent context"가 아니라 애플리케이션 레벨에서 제공하는 "2차 캐시(second level cache)"이다. `@Cache`를 사용하기 위해서는 별도의 라이브러리가 필요한데 책에서는 EhCache를 사용한다.  

  여기서는 아래와 같이 EhCache 2.x을 기반으로 하는 라이브러리를 추가하고 캐시 설정이 필요하다. 
  ```
  implementation 'org.hibernate:hibernate-ehcache:5.3.36.Final'
  ```
  하이버네이트 속성에서 다음과 같은 설정을 한다.
  ```
  props.setProperty("hibernate.cache.use_reference_entries", "true");		
  props.setProperty("hibernate.cache.use_second_level_cache", "true");
  props.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
  ```
  엔티티를 다음과 같이 불변으로 설정한 후에 `findById`로 조회해본다. 
  ```
  @Entity
  @Immutable
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  public class Person implements Serializable {

  }
  ```
  첫 번째 처음 가져올 때는 캐시에 저장하고 L2C puts, 두 번째 조회에서는 L2C hits가 카운트되면서 2차 캐시에서 가져온다는 것을 알 수 있다.
  ```
  673500 nanoseconds spent performing 1 L2C puts; // 1
  83900 nanoseconds spent performing 1 L2C hits;  // 2
  ``` 
  
  참고로 `hibernate-ehcache`는 deprecated 되었고 JCache 구현체인 EhCache 3를 쓰려면 `hibernate-jcache`를 사용해야 하는 것으로 보인다. 관련 이슈는 [여기](https://hibernate.atlassian.net/browse/HHH-12441)를 참조


- 엔티티 복제(연관관계 포함)  
author-book을 `@ManyToMany` 관계로 보고 공동저자를 추가할 때 기존 저자를 복제하여 새로운 저자를 등록하는 경우를 설명한다. 보통 새로운 엔티티를 등록할 때는 엔티티를 생성하고 필요한 데이터를 setter를 통해 넣고 저장하지만 여기서는 엔티티의 (인자 없는)기본 생성자를 `private`로 하고(이렇게 하면 최초 저장할 때는 어떻게?) 대신에 기존 엔티티를 인자로 받아서 필요한 속성을 복제하는 새로운 생성자를 추가한다.  

  ```
  @Entity
  public class Author implements Serializable {
      
      ...
      private Author() { }

      public Author(Author author) {
         this.genre = author.getGenre();
         this.books.addAll(author.getBooks());
      }

      ...
  }  
  ```
  이 생성자를 통해 만들어진 엔티티의 다른 속성을 입력하고 저장하면 복제된 속성과 새로 입력된 속성으로 엔티티를 추가할 수 있다.이것은 부모의 일부 속성을 복제하면서 동시에 연관관계에 있는 컬렉션 엔티티까지 복제할 때 유용할 수 있다. 특히 `@ManyToMany` 관계에서는 의미가 있다(다른 연관관계에서는 부모 엔티티 복제하여 추가하면서 동일한 연관관계의 데이터를 동기화하는 것은 말이 안된다). 그래서 복제는 일반적으로 사용되는 경우는 아니라고 한다. 책에서는 컬렉션 타입인 도서를 복제하는 예제도 있는데 동일한 도서가 중복되어 저장되므로 다소 이해가 안되는 예제로 보인다.
  
- 더티 트래킹  
하이버네이트는 managed 엔티티의 변경 감지를 더티 체킹을 통해 수행한다. 플러시 시점에 처음 상태와 현재 상태를 비교하여 변경 여부를 검사한다. 이렇게 되면 persistent context 내의 모든 엔티티에 대해 더티 체킹을 해야 하고 엔티티가 많은 경우는 성능이 떨어질 수 있다.  
  더티 트래킹은 하이버네이트 5부터 적용된 "bytecode enhancement"를 사용하여 엔티티 자체에서 변경 여부를 직접 판단할 수 있도록 엔티티 클래스의 바이트 코드를 조작한다. 이렇게 하면 플러시 시점에 하이버네이트가 상태 변경 여부를 검사할 필요 없이 엔티티의 더티 트래킹을 검사하기만 하면 된다(하이버네이트 문서에서는 이것을 "in-line dirty tracking"이라고 표현).  

  더티 트래킹은 디폴트로 비활성화되어 있다. 이것을 활성화시키기 위해서는 "bytecode enhancement"를 적용해야 하고 빌드 타임에 플러그인을 설정해야 한다. 또 더티 트래킹 옵션인 `hibernate.enhancer.enableDirtyTracking`을 true로 설정해야 한다. 플러그인은 gradle용과 maven용이 있고 책에서는 후자를 예제로 들었다.

- 속성 타입 변환기(AttributeConverter)  
자바의 타입과 데이터베이스 컬럼 타입은 당연히 차이가 있다. 하이버네이트에서는 "기본 타입(Basic Type)"이라는 범위 안에 이러한 타입들을 [매핑](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#basic)하고 있다. 예를 들어 `java.lang.Boolean`은 하이버네이트에 의해, MySQL의 경우 `bit(1)`으로 생성된다. 기본 타입의 속성들은 `@javax.persistence.Basic`어노테이션을 붙여야 하지만 보통은 생략한다. 대부분의 엔티티 속성들은 기본 타입으로 취급된다.  

  그런데 이미 기존에 존재하는 테이블의 컬럼 타입이 정해져 있고 이것을 매핑해야 하는 경우, 기본 타입에서 벗어나게 되면 어떻게 해야 할까? 책에서 예를 든 것처럼 베스트셀러 작가여부를 나타내는 컬럼 best_selling varchar(3)이면 기본 타입 어느 것도 해당되지 않는다. 아마도 원래 의도한 것은 "Yes"와 "No"를 저장하려고 했지만 엔티티 속성을 불리언으로 매핑하기 위해서는 뭔가 변환이 필요하다. 이때 사용할 수 있는 것이 커스텀 속성 컨버터인 `AttributeConverter`를 이용하는 방법이다. 
  



[처음](../README.md) | [이전](../01/README.md) | [다음](../03/README.md) 