## 스프링 부트 JPA 모범 사례

2020년 출판된 안겔 레오나르드(Anghel Leonard)가 쓴 "Spring Boot Persistence Best Practice" 번역판(한성곤 역)에 대한 스터디

책의 예제는 JDK 12, 스프링 부트 2.2.2.release를 기준으로 되어 있음

스터디에 사용된 것은 JDK 17과 아래 버전
```
 // Hibernate
implementation 'org.hibernate:hibernate-core:5.3.36.Final'
    
// Spring JPA
implementation 'org.springframework.data:spring-data-jpa:2.7.18'
```


목차 

1. [연관관계](01/README.md)
2. [엔티티](02/README.md)
3. [페치](03/README.md)
4. 배치 처리
5. 컬렉션
6. 커넥션과 트랜잭션


