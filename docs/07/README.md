## DAO, Service, Test
이제 간단한 CRUD를 만들어보기로 합니다. 우선 DAO 인터페이스를 정의합니다.
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

현재 Singer와 Album은 @ManyToOne 양방향으로 설정되어 있습니다. Singer를 인서트하면서 그 Singer의 Album 두 개를 함께 저장시키려고 합니다. 이렇게 하기 위해서는 Singer의 매핑 어노테이션에 다음과 같이 `cascade`설정이 필요합니다. `cascade`는 FK 관계에 따라 연관 데이터들을 함께 변경시키는 것을 말합니다. SQL로 말하자면 foreign key ON DELETE CASCADE와 ON UPDATE CASCADE에 해당합니다.
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
이렇게 하면 setter를 매번 써주지 않고 `addAlbum` 호출로 양방향으로 설정된 객체에 서로의 데이터를 넣어줄 수 있습니다. 객체가 준비되었으면 이제 이것은 서비스에 전달하고 서비스는 다시 DAO를 통해 DB에 저장하게 됩니다.

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
여기서 `em.createNamedQuery`를 사용했습니다. 이것은 미리 정해진 쿼리를 만들어놓고 필요할 때 실행할 수 있습니다. `@NamedQuery` 쿼리는 `RecordLabel` 엔티티에 두기로 합니다. 여기에 사용된 쿼리는 SQL이 아니라 JPQL입니다. SQL과 유사하므로 의미를 해석하는 것에 큰 어려움은 없을 것 같습니다. `join fetch`는 연관된 객체를 가져오는 구문입니다.

```
@Entity(name = "RecordLabel")
@Table(name = "record_label")
@NamedQueries(
		{
			@NamedQuery(
				name = "RecordLabel.Find_RecordLabel_With_Singer",
				query = "select r from RecordLabel r left join fetch r.singers s where r.label = ?0"
			)
		}
)
public class RecordLabel extends BaseEntity implements Serializable {
...
}

```
`label`에 해당하는 조건은 변하는 것이므로 외부 파라미터로 받기 위해 순서대로 입력받는 `?0`을 사용했습니다. 이것은 이름을 지정하는 파라미터 `:label`으로 써도 되겠습니다.

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
이때 `label` 속성 값이 같은 것을 동일한 레코드사로 보고 Set에서 삭제할 것이므로 `RecordLabel` 클래스에 `hashCode`와 `equals` 메소드를 오버라이드할 필요가 있습니다.

[처음](../README.md)