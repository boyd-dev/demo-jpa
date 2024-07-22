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
불변 엔티티의 조건은 연관관계를 포함하지 않고 `@Immutable`어노테이션이 있는 엔티티이다.  생성하면 수정이 되지 않기 때문에 캐시에 저장하는 경우가 있는데 이때 엔티티에 `@Cache`(org.hibernate.annotations.Cache)를 지정할 수 있다. 이 캐시는 1차 캐시인 "persistent context"가 아니라 애플리케이션 레벨에서 제공하는 "2차 캐시(second level cache)"이다. `@Cache`를 사용하기 위해서는 별도의 라이브러리가 필요한데 책에서는 EhCache를 사용한다.  

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


[처음](../README.md) | [다음](../03/README.md)