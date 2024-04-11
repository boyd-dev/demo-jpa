## Entity
이제 예제로 제시한 Singer, Album, RecordLabel들을 매핑해보겠습니다.  
JPA에서 엔티티는 관계형DB의 테이블에 대응되는 `@Entity` 어노테이션을 가진 클래스로 아래와 같은 형태입니다. 엔티티 클래스는 인자 없는 public 생성자를 가져야 합니다(자바에서 생성자를 생략하면 자동으로 만들어집니다).

```
@Entity(name = "Singer")
@Table(name = "singer")
public class Singer extends BaseEntity implements Serializable {

}
```
다수의 엔티티가 공통 속성을 가지고 있다면 `BaseEntity` 클래스에 넣고 상속받을 수 있습니다. 이때 `@MappedSuperclass`를 사용합니다.
```
@MappedSuperclass
public abstract class BaseEntity {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}	

}
```
엔티티의 속성은 테이블의 컬럼에 해당합니다. 테이블은 PK가 있는데 엔티티에서는 `@Id`로 PK 컬럼에 해당하는 속성을 지정합니다. `@GeneratedValue(strategy = GenerationType.IDENTITY)`은 자동증가 컬럼을 의미합니다.

엔티티 이름에 대응되는 테이블 이름을 `name` 속성으로 명시적으로 지정할 수 있습니다. 이것을 생략하는 경우는 디폴트 네이밍 전략 `ImplicitNamingStrategyJpaCompliantImpl`이 적용됩니다. 보통 엔티티 이름과 동일하게 소문자로 테이블이 생성됩니다.

```
@Entity(name = "Singer")
@Table(name = "singer")
public class Singer extends BaseEntity implements Serializable {
	
	@Column(name = "first_name")	
	private String firstName;

	@Column(name = "last_name")
	private String lastName;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "birth_date", nullable = true)
	private Date birthDate;
	
	@Column(name = "version")
	@Version
	@ColumnDefault("0")
	private int version;

    // getter, setter

}
```
속성에 지정할 수 있는 어노테이션은 다양합니다. 어노테이션 종류는 아래 링크를 참조하세요.  
[Mapping annotations](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#annotations)

JPA의 EntityManager가 엔티티에 "access" 하는 방식은 두 가지입니다. 내부적으로 필드에 직접 접근하거나(introspection) getter를 통하는 것입니다. 이것은 매핑 어노테이션을 어디에 설정하느냐에 따라 달라질 수 있습니다. 보통 엔티티 필드에 어노테이션 하는 것이 일반적이지만 getter 메소드에 지정할 수도 있습니다.

```
@Column(name = "first_name")
public String getFirstName() {
	return firstName;
}
```

 필드에 어노테이션을 주면 getter/setter를 만들지 않아도 됩니다(필요 없다면). getter에 주는 경우는 setter도 반드시 만들어야 하는데 엔티티가 로딩될 때 setter가 호출됩니다. access 방식에 대한 보다 자세한 설명은 [여기](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#access)를 참조하세요.

Album과 RecordLabel 엔티티도 마찬가지로 정의할 수 있습니다.

```
@Entity(name = "Album")
@Table(name = "album")
public class Album extends BaseEntity implements Serializable {
	
	@Column(name = "title")
	private String title;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "release_date")	
	private Date releaseDate;
	
	@Column(name = "version")
	@ColumnDefault("0")
	@Version
	private int version;

	// getter, setter

}
```

```
public class RecordLabel extends BaseEntity implements Serializable {
	
	@Column(name="label", unique = true)
	private String label;	

	// getter, setter
}
```

엔티티의 정의가 끝났습니다. 그런데 관계형DB의 테이블들이 서로 relation을 가지고 있는 것처럼 엔티티들도 서로 연관 관계(association)을 갖도록 해야 합니다. 이러한 관계는 데이터베이스에서 FK 같은 것으로 생성됩니다.

[처음](../README.md) | [다음](../06/README.md)
