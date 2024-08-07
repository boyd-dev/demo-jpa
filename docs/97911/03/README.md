## 페치

- 다이렉트 페치  
다이렉트 페치는 기본 `FetchType`에 따라 엔티티를 가져오는 것을 말한다. 기본 `FetchType`이 eager일 수 있으므로 연관관계에서는 명시적으로 lazy로 설정하는 것이 좋다(하지만 `@OneToOne`은 lazy로 해도 eager로 가져온다).  

- 세션 수준의 반복읽기  
하이버네이트는 "session-level repeatable read"를 보장하기 때문에 항상 1차 캐시(persistence context)에 해당 엔티티가 있는지 먼저 검사한다. 여기서 알아둘 것은 설령 해당 엔티티를 조회하는 select를 직접 실행하더라도 데이터베이스에서는 실행이 되지만 여전히 캐시에 있는 엔티티를 반환한다는 점이다. 따라서 가장 최신 스냅샷을 가져오기 위해 조회하는 것은 무의미한 일이 될 수 있다(EntityManager에서 제공하는 `refresh`를 사용하면 다시 조회한 결과를 가져올 수 있기는 하다).  

  책에서는 이를 보여주기 위해 `Propagation.REQUIRES_NEW`로 새로운 트랜잭션을 시작해서 데이터를 업데이트한 후 재개되는 트랜잭션에서 다시 select한 결과를 비교한다. 앞서 업데이트가 되면서 커밋이 되었지만 엔티티는 여전히 처음 가져온 데이터가 유지된다. 참고로 MySQL의 경우 기본 격리 레벨이 repeatable read이므로 하이버네이트의 세션 수준의 반복읽기가 아니더라도 처음 조회한 데이터가 나오기 때문에 MySQL을 시작할 때 격리모드를 read-committed로 변경하고 테스트해야 확실하다.

  ```
  mysqld --console --transaction-isolation=READ-COMMITTED
  ```
  테스트를 단순화하기 위해 아래와 같이 해도 된다.
  ```
  public Singer findById(Long singerId) {		
		
       Singer singer1 = singerRepository.findById(singerId).get();
       
       Singer singer2 = singerService2.insertOrUpdate(Long.parseLong("2"));
       
       Singer singer3 = singerRepository.Find_Singer(singerId);
		
       assert(singer1 != singer2);
       assert(singer1 == singer3);
       
       return singer1;
  }
  ```
  `singerRepository.Find_Singer`는 JPQL로 직접 조회한 것으로 데이터베이스에서 select가 실행되지만 `singerService2.insertOrUpdate`에서 업데이트한 것을 가져오지 않는다. 

- 엔티티를 나중에 변경할 때도 읽기 전용으로 가져오는 이유  
보통 업데이트를 위해서 엔티티를 가져오는 경우 그냥 조회하면 read-write 모드로 엔티티가 캐시에 저장된다. 이러한 경우 두 가지 단점이 있다. 

  - 더티 체킹을 위한 메모리 소비한다.
  - 플러시 시점에 스캔 대상이 된다.

  위의 단점을 피하기 위해 처음에는 읽기 전용으로 엔티티를 가져오고 detach 상태에서 수정한 엔티티를 다시 새로운 트랜잭션의 업데이트로 실행해야 한다(하지만 꼭 그렇게 할 필요가 있나 싶기는 하다).

- 속성 지연 로딩  
연관관계의 지연 로딩은 잘 알고 있지만 특정 속성에 대해서 지연 로딩을 할 수도 있다(기본적으로 모든 속성들은 eager 로딩한다). 예를 들어 `byte[]` 타입의 아바타 이미지를 가져오려면 부하가 생기므로 지연 로딩으로 지정할 수 있다.  

  지연 로딩은 말 그대로 초기에 가져오지 않고 나중에 (필요한 시점에) 가져온다는 것이므로 필연적으로 N+1 문제가 발생할 수 있다. 즉 처음 조회 후 지연된 속성을 가져오게 되면 추가적인 select를 실행하기 때문에 비효율적일 수 있다.  

- 속성 지연 로딩의 부작용  
속성 지연 로딩을 사용하기 위해서는 bytecode enhancement를 활성화시켜야 한다. 그런데 이 경우에 스프링 부트의 JSON 처리 라이브러리인 Jackson에서 문제가 발생할 소지가 있다.  

  책의 예제를 살펴보면, 스프링 부트에서는 "OSIV(Open Session In View)"가 기본으로 true로 설정되어 있고 또 Jackson 라이브러리가 컨트롤러에서 엔티티를 JSON으로 직렬화할 때 지연된 속성(그리고 연관관계)을 자동으로 가져오도록 동작한다. 따라서 lazy로 설정하더라도 Jackson이 그것을 가져오기 때문에 지연 설정이 의미가 없게 된다(지연된 속성을 가져오기 위한 추가 select가 실행된다).  

  만약 OSIV를 false로 설정하면 어떻게 될까? Jackson이 지연 속성을 가져올 때 세션이 없으므로 지연 초기화 에러가 발생한다. 

  ```
  Resolved [org.springframework.http.converter.HttpMessageNotWritableException: Could not write JSON: Unable to perform requested lazy initialization [com.example.demo.entity.Singer.photo] - no session and settings disallow loading outside the Session]
  ``` 
  책에서는 이것을 해결하는 두 가지 방법을 소개하고 있는데, 첫 번째는 지연 속성을 미리 초기화해두는 것이다. 즉 select로 받은 결과의 지연 속성을 null로 초기화해서 리턴하면 Jackson은 초기화된 지연 속성을 또 로드하지 않으므로 OSIV가 false라고 해도 위와 같은 예외는 발생하지 않는다(추가 select를 실행하지 않는다).  

  그런데 어차피 초기에 가져오지 않을 속성이고 Jackson이 JSON으로 변환하면서 null이 된다면 아예 Jackson에게 이 속성을 제외하라고 설정하는 편이 좋다. 따라서 엔티티에 `@JsonInclude(value = Include.NON_NULL)` 어노테이션을 추가한다. 이렇게 하면 null인 속성을 제외하고 JSON을 리턴한다.  

  스프링 부트 3.3.2와 그레이들 기반의 예제는 [여기](./examples/demo-boot/)에 있다. bytecode enhancement는 빌드할때 그레이들 플러그인에 의해 적용되기 때문에 JUnit Test를 실행하지 말고 Gradle Test를 실행해야 한다. 

  두 번째는 필터를 이용하는 방법인데 복잡해서 생략한다(필터까지 만들 필요가?).  

  bytecode enhancement를 쓰는 것이 번거롭다면 상속을 이용하여 서브 엔티티를 이용하는 방법이 있다. 즉 지연 로드 속성만 가진 엔티티(서브 엔티티) 그렇지 않은 속성을 가진 엔티티(부모 엔티티)로 분리하는 것인데 어떻게 보면 이것이 가장 간단한 방법일 수 있다.

- 프로젝션으로 데이터 가져오기  
엔티티는 기본적으로 읽기-쓰기 모드로 캐시에 저장되고 플러시 시점에 더티 체킹 메커니즘으로 변경 사항을 추적하여 데이터베이스에 반영한다. 이러한 일련의 처리들은 리소스를 소모하고 특히 로드된 엔티티들이 많을 때는 성능에 큰 영향을 미친다. 따라서 그냥 조회만 하는 엔티티는 반드시 읽기 전용 모드로 가져와야 하고 더구나 수정할 일도 없다면 엔티티를 직접 가져올 필요도 없다.  
  
  페치에서 가장 중요한 것은 필요한 컬럼들만 조회하는 것이고 프로젝션이라는 것은 바로 그렇게 필요한 데이터만 가져오는 것을 의미한다. 하이버네이트 공식 문서에서는 프로젝션을 "DTO 프로젝션"이라고 표현했는데 필요한 속성들로 만드는 DTO와 유사한 개념이기 때문이다. 

  >For read-only transactions, you should fetch DTO projections because they allow you to select just as many columns as you need to fulfill a certain business use case. This has many benefits like reducing the load on the currently running Persistence Context because DTO projections don’t need to be managed.

  프로젝션은 기본적으로 persistent context에서 관리되지 않으므로 리소스를 절약할 수 있다. 프로젝션으로 데이터를 페치하는 방법은 여러가지가 있고 책에서 다소 많은 지면에 걸쳐 설명하고 있다. 

- 닫힌(closed) 인터페이스  
필요한 속성만으로 get 메소드를 가지는 인터페이스를 생성한다. 메소드는 속성이름에 대응되어야 한다. "closed"는 인터페이스가 엔티티의 속성만을 포함한다는 것을 의미한다. 따라서 그 엔티티의 레포지토리 안에 nested 인터페이스로 정의될 수도 있다.  

  이렇게 정의한 타입을 리턴 타입으로 하는 쿼리 메소드를 작성하면 되고 이렇게 하면 하나의 엔티티를 전부 가져오는 것보다 리소스를 많이 절약할 수 있다.  

- 네임드 쿼리  
필요한 속성들만 select 하는 쿼리를 직접 작성하는 것이 오히려 직관적일 수 있다. 이때 사용할 것이 네임드 쿼리이다. 네임드 쿼리는 JPQL 또는 네이티브 쿼리로 작성할 수 있다. 

- 프로젝션 클래스  
앞서 프로젝션을 위해 인터페이스를 정의한 것처럼 클래스를 정의할 수도 있다. 이 클래스는 생성자 인자로 가져올 속성들을 받아야 하고 인자의 이름은 엔티티의 속성명과 일치해야 한다.  

- 프로젝션 재사용  
필요한 속성들이 경우에 따라 일부는 동일하고 일부는 차이가 나는 경우 각각 프로젝션 인터페이스나 클래스를 만드는 것이 비효율적일 수 있다. 그래서 필요한 모든 속성을 포함하는 프로젝션을 하나만 만들어서 사용할 수 있다. `@Query`에서 필요한 속성을 조회하는 메소드를 작성하면 다른 속성들은 null이 되는데, 이때 `@JsonInclude(value = Include.NON_NULL)`을 사용하여 실제 필요한 데이터만 JSON으로 변환될 수 있도록 할 수 있다. 

- Dynamic Projection  
select 하는 속성만 다른 동일 쿼리가 많다면? 즉 리턴되는 결과셋만 다르다면? 앞서 프로젝션 재사용처럼 일단 모든 속성을 포함하는 프로젝션을 만드는 것을 생각해볼 수 있다. 하지만 스프링 데이터 JPA에서는 dynamic projection을 제공하는데 결과셋에 해당하는 여러 타입들을 받을 수 있도록 쿼리 메소드가 `Class<T>` 타입의 파라미터를 받을 수 있도록 정의하는 것이다. 

  ```
  interface PersonRepository extends Repository<Person, UUID> {
     <T> Collection<T> findByLastname(String lastname, Class<T> type);
  }
  ```
  타입 파라미터는 컬렉션의 요소 타입일 수도 있고 단일 타입일 수도 있다. 타입은 서비스 클래스에서 호출할 때 전달하면 된다. 

- 프로젝션에 엔티티가 포함되는 경우  
  JPQL을 사용하여 가져온 엔티티를 수정하게 되면 플러시 시점에 업데이트가 트리거 된다(당연한 것 같은데?)




[처음](../README.md) | [이전](../02/README.md) | [다음](../04/README.md) 