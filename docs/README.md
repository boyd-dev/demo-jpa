## 스프링 "plain" JPA

이 글은 스프링에서 JPA를 사용하는 방법에 대해 간단히 알아보려는 목적으로 작성되었습니다. 그래서(?) 스프링 부트와 "스프링 데이터 JPA"를 쓰지 않았습니다.
예제의 소스는 [여기](https://github.com/boyd-dev/demo-jpa/tree/main/example)를 참고하면 되겠습니다.  

후반부에는 스프링 데이터 JPA의 `JpaRepository`에 관한 설명이 추가되어 있습니다. 

가능하면 하이버네이트와 스프링의 공식 문서를 인용했습니다. 예제 데이터 모델은 "Pro Spring 5, 5th(번역서는 전문가를 위한 스프링 5)"를 약간 변형해서 사용했습니다(Singer-Album-RecordLabel).

예제 환경은 다음과 같습니다.

- JDK 17
- Spring Framework 5.3.32
- Hibernate 5.3.36.Final
- MySQL 8.0
- Junit 5.9.3
- Gradle 8.4
- IDE - STS 4.20.1

목차

1. [개요](01/README.md)
2. [JDK+하이버네이트+JPA 버전](02/README.md)
3. [데이터 모델](03/README.md)
4. [스프링 설정(Gradle)](04/README.md)
5. [엔티티 매핑](05/README.md)
6. [연관 관계](06/README.md)
7. [CRUD 테스트](07/README.md)
8. [스프링 데이터 JPA](08/README.md)
9. [JPA Repository](09/README.md)
10. [Custom Repository](10/README.md)

기타  

1. 스프링 부트 JPA 모범 사례


참고  
[Hibernate User Guide](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html)  
[Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/2.7.x/reference/html/)
