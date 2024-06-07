
## Association

관계형DB의 relation은 JPA에서 연관 관계(association)로 표시됩니다.

앞서 정의한 엔티티들에 연관 관계를 설정하려면 각 관계의 종류에 따라 어노테이션을 추가해야 합니다. 네 가지 어노테이션이 있습니다.

- ManyToOne
- OneToMany
- OneToOne
- ManyToMany

엔티티들 사이의 관계는 해당 엔티티를 속성에 포함시키고 그 속성에 위 어노테이션 중 하나를 적용하는 형식으로 이루어집니다. 연관 관계의 설명은 다소 복잡하기 하기 때문에 하이버네이트 [사용자 가이드](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#associations)를 중심으로 정리하겠습니다. 

## 단방향 @ManyToOne, @OneToMany
우선 Singer와 Album을 살펴보겠습니다. Singer는 여러 개의 Album을 가집니다. 반대로 다수의 Album은 한 명의 Singer에 매핑됩니다. DB 관점에서 보면 parent-child 처럼 일대다 관계를 떠올릴 수 있을 것입니다.  

parent-child 관계에서 보통 FK는 child에 설정됩니다. Singer가 없는 Album은 존재할 수 없기 때문에 Singer의 PK를 참조하도록 Album의 컬럼을 FK로 설정합니다. 일대다와 다대일은 어느 테이블에서 그 관계를 바라볼 것이냐 차이일 뿐 사실상 동일합니다. 

그런데 하이버네이트는 이러한 관계를 다대일 관계인 ManyToOne으로 해석합니다. 하이버네이트 가이드의 설명은 아래와 같습니다.
>@ManyToOne is the most common association, having a direct equivalent in the relational database as well (e.g. foreign key), and so it establishes a relationship between a child entity and a parent.

즉 통상적인 FK로 맺어지는 관계는 @ManyToOne을 먼저 고려하면 되겠습니다. 
이제 `@ManyToOne` 어노테이션을 Album 엔티티에 아래와 같이 추가합니다. "Many"에 해당하는 것이 Album이기 때문입니다.

```
@Entity
public class Album
...
@ManyToOne
@JoinColumn(name = "singer_id")
private Singer singer;
```

`@JoinColumn`으로 Album 테이블에 생기는 FK컬럼 이름을 지정할 수 있습니다. Album 엔티티는 Singer를 참조하고 있으므로 Album을 통해 Singer를 알 수 있습니다. 반대로 Singer로부터 Album을 알 수 있을까요? DB의 테이블이라면 당연히 알 수 있지만 엔티티 관계에서는 Singer에 Album에 대한 어떠한 참조도 추가되지 않았으므로 알 수 없습니다. 이러한 관계를 "단방향(unidirectional)"이라고 합니다.  

그렇다면 Singer와 Album의 관계를 OneToMany로 설정할 수도 있지 않을까요? 맞습니다. 그런데 하이버네이트는 다소 비효율적인 방식으로 OneToMany를 설정합니다. 이제 `@OneToMany` 어노테이션을 Singer에 추가해봅니다(Album에 설정한 `@ManyToOne`은 제거합니다).

```
@Entity
public class Singer 
...
@OneToMany
private Set<Album> albums = new HashSet<>();
```
"One"이 Singer가 되고 "ToMany"는 Album이 됩니다. Singer는 컬렉션 타입의 `albums`를 가지게 되었습니다.
하지만 이렇게 정의된 관계는 "중간 테이블(junction table)"을 생성합니다. Singer와 Album은 서로 직접적인 relation을 맺지 않고 중간 테이블인 `singer_album`이라는 테이블과 FK로 설정됩니다. 

>When using a unidirectional @OneToMany association, Hibernate resorts to using a link table between the two joining entities.

중간 테이블을 생성시키지 않고 FK만 맺으려면 아래와 같이 `@JoinColumn`을 사용할 수 있습니다. 이렇게 하면 <b>`Album`에 FK가 생깁니다.</b> 
```
@Entity
public class Singer 
...
@OneToMany
@JoinColumn(name = "singer_id")
private Set<Album> albums = new HashSet<>();
```

일반적으로 단방향 OneToMany는 부가적인 쿼리를 많이 수행하기 때문에 권장하지 않습니다. 단방향 OneToMany는 가능하면 피하고 ManyToOne의 관계로(어차피 관계 자체는 달라지지 않으므로) 변경하고 parent에서 child를 가져올 때는 쿼리를 직접 작성해서 해결하는 방안을 생각할 수 있습니다.  

하이버네이트 가이드에서는 차라리 양방향 OneToMany를 설정하는 것이 바람직하다고 설명합니다.
>On the other hand, a bidirectional @OneToMany association is much more efficient because the child entity controls the association.

## 양방향 @OneToMany

일대다와 다대일은 관점에 따라 다를 뿐이므로 `@OneToMany`과 `@ManyToOne`의 두 어노테이션을 모두 사용하여 연관 관계를 설정합니다. 즉 Singer와 Album을 양방향으로 설정하면 다음과 같습니다.

```
@Entity
public class Singer 
...
@OneToMany(mappedBy = "singer")
private Set<Album> albums = new HashSet<>();
```

```
@Entity
public class Album
...
@ManyToOne
@JoinColumn(name = "singer_id")
private Singer singer;
```

Singer와 Album의 양방향은 Singer에 `mappedBy`로 child의 속성명을 지정합니다. 나머지는 `@ManyToOne`과 `@OneToMany` 어노테이션을 각각 추가한 것과 동일합니다. FK는 `Album`에 생깁니다. DB의 parent-child는 하이버네이트에서 `@ManyToOne` 또는 `@OneToMany`의 양방향 관계라고 볼 수 있겠습니다.

하이버네이트에서 모든 양방향은 FK가 생기는 child와 그 반대 쪽에 `mappedBy`로 그것을 참조하는 형태로 설정됩니다. 

>Every bidirectional association must have one owning side only (the child side), the other one being referred to as the inverse (or the mappedBy) side.

위의 설명에서 "owning side"는 흔히 말하는 연관 관계의 주인이라고 표현을 하지만 사실은 <b>FK를 소유</b>하는 엔티티를 의미합니다. 반면 "inverse side"는 `mappedBy`로 참조하는 쪽을 말합니다. 양방향 OneToMany에서 Album이 owning side이고 Singer가 inverse side가 되겠습니다. 양방향 OneToMany에서 many 쪽에는 `mappedBy`가 설정될 수 없다는 규칙이 있습니다. 따라서 OneToMany과 ManyToOne의 양방향은 위의 경우 한 가지 밖에 없습니다.

당연한 말이지만 단방향의 경우 "owning side"만을 가지게 되고 양방향은 "owning side"와 "inverse side"를 가지게 됩니다.  "owning side"는 연관 관계를 기반으로 어떻게 데이터를 변경할 지를 결정하는 기준이 됩니다. [Java EE 7 Persistence](https://docs.oracle.com/javaee/7/tutorial/partpersist.htm#BNBPY) 설명을 인용해보겠습니다. 

>The owning side of a relationship determines how the Persistence runtime makes updates to the relationship in the database


## FetchType
양방향 OneToMany에서 Singer를 조회하면서 Singer의 Album 데이터들을 즉시 가져오는 경우가 있습니다. 관계 어노테이션에는 `FetchType`이라는 속성이 있는데, 이것은 연관된 데이터를 함께 가져올 것인지 아니면 필요한 시점에 가져올 것인지에 대한 옵션입니다.

```
@Entity
public class Singer 
...
@OneToMany(mappedBy = "singer", fetch = FetchType.LAZY)
private Set<Album> albums = new HashSet<>();
```

양방향 `@OneToMany`는 `FetchType.LAZY`가 기본으로 적용됩니다. 반대로 한꺼번에 가져올 때는 `FetchType.EAGER`를 사용할수 있습니다. 하지만 기본값을 사용하고 연관 데이터가 필요한 경우는 JPQL을 사용할 것을 권장하고 있습니다.  

연관 관계에 따라서 기본 `FetchType`이 정해져 있습니다. 단방향 ManyToOne는 기본적으로 "ToOne"에 해당하는 엔티티를 JOIN 해서 가져옵니다. 하지만 `FetchType.LAZY`로 변경하면, 예를 들어 `Album`에서 `getSinger()`를 호출하지 않는다면 추가적으로 `Singer`를 조회하지 않습니다. 반면에 양방향 OneToOne은 항상 `FetchType.EAGER`로 동작합니다. 


## @OneToOne
이 관계는 DB 관점에서 master-detail 관계에 해당합니다. Phone과 PhoneDetail을 예로 들겠습니다.  

Phone은 전화기 고유번호를 저장하는 master에 해당하고 PhoneDetail은 통신사업자 같은 가변적인 데이터를 저장하는 것이라고 하겠습니다. 물론 둘을 합쳐서 하나의 테이블로 할 수도 있지만 여기서는 OneToOne 관계를 살펴보기 위해 두 개로 나누어 저장합니다.

OneToOne에도 단방향과 양방향이 있습니다. DB 관점에서는 detail에 FK가 생기는 것이 일반적입니다. 그런데 하이버네이트에서는 Phone에서 `@OneToOne`을 추가하면 Phone에 FK가 생깁니다. 하이버네이트 가이드에는 이러한 이상한 상황에 대해 다음과 같이 설명합니다.

>But then, it’s unusual to consider the Phone as a client-side and the PhoneDetails as the parent-side because the details cannot exist without an actual phone. A much more natural mapping would be the Phone were the parent-side, therefore pushing the foreign key into the PhoneDetails table. This mapping requires a bidirectional @OneToOne association as you can see in the following example:

master-detail 관계에서 master에 데이터가 없으면 detail에도 없는 것이 당연하기 때문에 FK는 detail 쪽에 생기는 것이 맞습니다. detail에 `@JoinColumn`으로 FK를 만들어줄 수 있지만 master에 detail을 참조할 수 있는 관계가 없으므로 추가적인 쿼리가 필요합니다. 그래서 양방향 OneToOne을 설정하는 것이 낫습니다. 양방향에서는 owning side는 FK가 있는 `PhoneDetail`이 되고 inverse side는 `Phone`이 됩니다. 한 가지 유념할 것은 양방향 OneToOne은 기본적으로 `FetchType.EAGER`로 동작하기 때문에 성능 측면에서 좋지 않을 수 있습니다. 

```
@Entity
public class Phone 
...
@OneToOne(mappedBy = "phone")
private PhoneDetail detail;
```

```
@Entity
public class PhoneDetail
...
@OneToOne
@JoinColumn(name = "phone_id")
private Phone phone;
```

단방향을 유지하면서 detail에 FK를 두려면 PK공유방식을 사용할 수도 있습니다. 아래와 같이 `@MapsId` 어노테이션을 사용합니다. 이렇게 하면 master의 PK를 detail의 PK로 사용하면서 둘은 FK 관계를 가지게 됩니다. 원래 있던 `@Id`에서 `@GeneratedValue`은 삭제합니다.

```
@Entity
public class PhoneDetail

@Id
private Long id;

@OneToOne
@MapsId
@JoinColumn(name = "id")
private Person person;

```


## @ManyToMany
예제 데이터 모델에서 Singer와 RecordLabel은 다대다 관계입니다. 다대다 관계는 모두 중간 테이블 `singer_record_label`이 생성됩니다. 중간 테이블은 Singer와 RecordLabel의 각 PK들과 FK 관계를 맺고 있습니다. 다시 말해서 두 엔티티가 중간 테이블을 통해 양방향으로 연관되어 있고 사실상 양방향 밖에 없으므로 어느 엔티티를 owning side로 할 것인지 결정할 필요가 있습니다. 아래는 양방향 설정을 했을 때 중간 테이블 두 개 컬럼의 FK 제약조건입니다.

```
CONSTRAINT `FK7...5vp` FOREIGN KEY (`singers_id`) REFERENCES `singer` (`id`),
CONSTRAINT `FKe...y71` FOREIGN KEY (`recordLabels_id`) REFERENCES `record_label` (`id`)
```

```
@Entity
public class Singer
...
@ManyToMany(fetch = FetchType.LAZY)
private List<RecordLabel> recordLabels = new ArrayList<>();
```

```
@Entity
public class RecordLabel
...
@ManyToMany(mappedBy = "recordLabels", fetch = FetchType.LAZY)
private List<Singer> singers = new ArrayList<>();
```
여기서는 논리적으로 `Singer`가 owning side로 하고 `RecordLabel`을 inverse side로 정한다면 `RecordLabel`에 `mappedBy`를 지정해야 합니다. ManyToMany 관계는 기본적으로 `FetchType.LAZY`로 동작합니다.  

이제까지 살펴 본 연관 관계를 정리하면: 네 개의 관계에 대해서 단방향 양방향을 생각하면 8개의 조합이지만 `@OneToMany`와 `@ManyToOne`의 양방향은 한 가지이고 단방향 `@OneToMany`는 매우 비효율적이므로 제외하며, 또 `@ManyToMany`는 양방향 밖에 없기 때문에 사실상 의미있는 관계는 다섯 가지로 생각할 수 있습니다.

- 단방향 ManyToOne
- 양방향 OneToMany(+ManyToOne)
- 단방향 OneToOne
- 양방향 OneToOne
- 양방향 ManyToMany


[처음](../README.md) | [다음](../07/README.md)