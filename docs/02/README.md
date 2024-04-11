## JDK+하이버네이트+JPA 버전
스프링에서 JPA를 쓰기 위해서는 관련 디펜던시 라이브러리를 설치해야 합니다. 자바에서는 사용하려는 JDK에 맞는 라이브러리를 찾는 것이 일반적입니다. JPA 구현체인 하이버네이트는 JPA 표준의 버전이 있기 때문에 어떤 JPA 표준을 구현했는지도 알아야 합니다.

어떤 라이브러리를 써야 하는지는 하이버네이트의 아래 사이트를 참고하면 도움이 될 것 같습니다. 여기서는 하이버네이트 5.3.36.Final을 기준으로 합니다.

[JPA Specification](https://jakarta.ee/specifications/persistence/)  
[Hibernate release](https://hibernate.org/orm/releases/5.3/)

## JDK와 스프링 버전
JDK 17을 사용합니다. 예제에서는 스프링 부트를 쓰지 않고 그냥 스프링 프레임워크를 쓰겠습니다. 버전은 5.3.32 입니다. 

## 관계형DB 
MySQL 8을 사용합니다.

## build.gradle
그레이들 설정은 아래와 같습니다. 앞서 언급한 것처럼 스프링 데이터 JPA는 쓰지 않기 때문에 주석 처리를 했습니다.

```
plugins {
    id 'java'
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = '17'
}


dependencies {

    implementation 'org.springframework:spring-context:5.3.32'    
    
    // Test
    // Junit5
    testImplementation 'org.springframework:spring-test:5.3.32'
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.9.3'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    
    // Data
    implementation 'org.springframework:spring-jdbc:5.3.32'
    implementation 'org.springframework:spring-orm:5.3.32'
    implementation 'org.apache.commons:commons-dbcp2:2.9.0'    
    runtimeOnly 'com.mysql:mysql-connector-j:8.0.33'
    
    // Hibernate
    implementation 'org.hibernate:hibernate-core:5.3.36.Final'
    
    // Spring JPA
    //implementation 'org.springframework.data:spring-data-jpa:2.7.18'
        
    
    // Logging    
    implementation 'org.slf4j:slf4j-api:2.0.12'
    implementation 'ch.qos.logback:logback-classic:1.5.3'    
    
    
}

test {
    useJUnitPlatform()
    testLogging.showStandardStreams=true
    //include '**/*Test.class'
    include 'com/foo/**/*Test.class'    
}

```

[처음](../README.md) | [다음](../03/README.md)