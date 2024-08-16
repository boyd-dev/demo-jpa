## Custom Repository 

앞서 살펴본 것처럼 스프링 데이터 JPA는 쿼리 메서드를 만들 수 있는 다양한 옵션을 제공합니다. 그러나 이러한 옵션이 요구 사항에 맞지 않는 경우 리포지토리 메서드에 대한 고유한 사용자 지정 구현을 제공할 수도 있습니다. 커스텀 레포지토리는 꼭 JPA를 써야하는 것이 아니기 때문에 `JdbcTemplate`을 쓸 수도 있습니다. 

커스텀 레포지토리는 과거 DAO 클래스를 만드는 것과 유사합니다. 우선 인터페이스(fragment interface)를 정의합니다. 

```
public interface SingerCustomRepository {
	
	public Singer fetchSinger(Long singerId);

}
```
`SingerCustomRepository`의 구현 클래스의 이름은 반드시 인터페이스 이름 + `Impl`로 끝나야 합니다(접미사는 `@EnableJpaRepositories` 속성에서 변경 가능합니다).

```
public class SingerCustomRepositoryImpl implements SingerCustomRepository {
	
	@PersistenceContext
	EntityManager em;
	
	@Override
	public Singer fetchSinger(Long singerId) {		
        ...
		return singer;
	}
}
```
스프링이 제공하는 `JpaRepository`가 아니기 때문에 정의된 모든 메소드를 구현해야 하고 JPA를 사용하려면 `EntityManager`를 주입받아서 사용할 수 있습니다. 앞서 말한 것처럼 반드시 JPA를 써야 한다는 제약은 없습니다. 이렇게 커스텀 레포지토리 구현체를 만들게 되면 아래와 같이 `JpaRepository`와 함께 상속받는 인터페이스를 다음과 같이 정의하면 되겠습니다.

```
public interface SingerRepository extends JpaRepository<Singer, Long>, SingerCustomRepository {

   ...

}
```
`SingerRepository`는 `JpaRepository`가 제공하는 CRUD, 쿼리 빌더에 의한 자동생성, `@Query`, 네임드 쿼리 등과 함께 커스텀 레포지토리인 `SingerCustomRepository`가 제공하는 CRUD도 포함하게 됩니다. 같은 메소드 시그너처(메소드 이름과 인자 타입)가 있는 경우 커스텀 레포지토리가 우선합니다. 

커스텀 레포지토리는 스프링 데이터 JPA에서 `EntityManager`를 직접 활용하려고 할 때 또는 JPA에 국한하지 않고 다양한 데이터 액세스 방식을 적용할 때 고려할 수 있겠습니다.


[처음](../README.md) | [다음](../11/README.md)