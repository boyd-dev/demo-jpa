## Spring Data JPA

이제까지 작성한 예제는 스프링 JPA이지만 스프링 "데이터" JPA는 아니었습니다. 스프링 데이터 JPA는 "스프링 데이터"라는 큰 [프로젝트](https://spring.io/projects/spring-data)에 속해 있는 하위 프로젝트입니다. 스프링 데이터 프로젝트에는 스프링 데이터 JDBC, 스프링 데이터 MongoDB, 스프링 데이터 Redis 등이 포함되어 있습니다.

스프링 데이터 JPA를 사용하려면 해당 라이브러리를 디펜던시에 추가해야 합니다. `build.gradle`에 아래와 같이 추가합니다.

```
dependencies {
    
    ...
    implementation 'org.springframework.data:spring-data-jpa:2.7.18'
}
```

여기서는 JPA 2를 기준으로 설명하기 때문에 스프링 데이터 JPA 버전도 2를 사용합니다. 구성 클래스의 설정은 `@EnableJpaRepository` 어노테이션을 추가하면 됩니다. 이때 `basePackages` 속성에 앞으로 작성할 `JpaRepository` 인터페이스가 있는 패키지를 지정합니다.

```
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.foo.jparepository"})
@ComponentScan(basePackages = {"com.foo.jpa"})
public class AppConfig {

}
```

스프링 데이터 JPA의 가장 큰 특징은 중복되는 코드를 줄여준다는 것입니다. 앞서 예제에서 살펴본 것처럼 DAO 클래스를 작성할 유사한 CRUD 코드가 나열되는 경우가 많이 있습니다. 즉 전달된 엔티티를 받고 EntityManager를 사용하여 DB로부터 읽거나 쓰거나 하는 코드들이 대부분입니다. 

스프링 데이터 JPA는 이것을 "Repository 추상화"라는 이름으로 단순화시켰습니다. DAO인터페이스를 정의하고 DAO 구현 객체를 통해서 데이터를 처리하는 방식을 `JpaRepository`라는 인터페이스 하나만을 정의하는 것으로 간소화했습니다. 예를 들어 `SingerDao`, `SingerDaoImpl`은 아래와 같이 하나의 인터페이스 정의로 대체할 수 있습니다.

```
public interface SingerRepository extends JpaRepository<Singer, Long> {	
	
	public List<Singer> findAll();
	
	public Optional<Singer> findById(Long singerId);
	
	@Query("from Singer s join fetch s.albums a where s.id = :singerId")
	public Singer findByIdWithAlbums(@Param("singerId") Long singerId);
	
	@Query("from Singer s left join fetch s.recordLabels r where s.id = :singerId")
	public Singer findByIdWithRecordLabels(@Param("singerId") Long singerId);
	
}
```
읽기 메소드만 정의한 것 같지만 사실 `persist`와 `remove`는 내부적으로 포함되어 있습니다. 서비스 클래스에서는 아래와 같이 사용합니다.

```
@Service("singerService")
@Transactional
public class SingerServiceImpl implements SingerService {

    private SingerRepository singerRepository;
	
	@Autowired
	public void setSingerRepository(SingerRepository singerRepository) {
		this.singerRepository = singerRepository;
    }

    @Override
	public Singer findById(Long singerId) {		
		return singerRepository.findById(singerId).orElse(null);
	}

    @Override
	public Singer insertOrUpdate(Singer singer) {
		return singerRepository.save(singer);
	}

	@Override
	public void delete(Singer singer) {
		singerRepository.delete(singer);		
	}
    ...
}
```

이렇게 `SingerDao` 대신 `SingerRepository`로 바꾸면 됩니다. 물론 서비스 클래스 인터페이스 변경없이 서비스 구현 클래스만 변경하거나 새로 하나 만들어서 교체하면 될 것입니다. `JpaRepository` 인터페이스는 엔티티 타입 파라미터를 하나 받기 때문에 엔티티 단위로 `JpaRepository`를 만들어야 합니다.

이것 외에도 스프링 데이터 JPA는 스프링에서 JPA를 보다 수월하게(?) 사용할 수 있는 여러가지 기능을 제공합니다. 자세한 내용은 [공식문서](https://spring.io/projects/spring-data-jpa)를 참고하세요.

