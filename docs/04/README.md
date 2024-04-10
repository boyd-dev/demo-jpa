## 스프링 설정
이 예제에서는 스프링 설정을 위해 구성 클래스 `@Configuration`을 사용합니다.  
스프링에서 JPA를 쓰기 위해서는 (세가지 옵션 중) `LocalContainerEntityManagerFactoryBean`을 사용하겠습니다. 이 빈을 정의하기 위해서는 몇 가지 속성을 주입해야 합니다. 주요 설정은 아래와 같습니다.
- DataSource
- JpaProperties
- JpaVendorAdaptor

  ```
  @Bean
  public EntityManagerFactory entityManagerFactory() {
		
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setPackagesToScan(new String[]{"com.foo.jpa.entity"});
		factoryBean.setDataSource(dataSource());
		factoryBean.setJpaProperties(getHibernateProperties());
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
		factoryBean.afterPropertiesSet();
		
		return factoryBean.getNativeEntityManagerFactory();
  }	
  ```
  `setPackagesToScan`은 정의된 엔티티가 있는 패키지를 배열로 나열합니다. EntityManagerFactory는 이렇게 스캔한 엔티티를 기준으로 테이블과 매핑하게 됩니다.
- `DataSource`는 관계형DB로 사용할 MySQL과의 연결 설정입니다. 
  ```
  @Bean
  public DataSource dataSource() {
		
		BasicDataSource dataSource = new BasicDataSource();		
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");		
		dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/mydb");	
		dataSource.setConnectionProperties("serverTimezone=UTC;characterEncoding=UTF-8");
		dataSource.setUsername("scott");
		dataSource.setPassword("1234");
		dataSource.setDefaultAutoCommit(false);
		
		return dataSource;		
  }
  ```
  데이터소스로 사용하는 `BasicDataSource`는 커넥션 풀을 지원하는 데이터소스로 이를 위해 디펜던시에 
`org.apache.commons:commons-dbcp2:2.9.0`을 추가했습니다. 참고로 스프링 부트에서는 Hikari를 사용합니다.

  MySQL과 연결하기 위해서는 MySQL 커넥터 드라이버가 필요한데 그것이 `com.mysql.cj.jdbc.Driver`입니다. 나머지는 DB연결할 때 필요한 설정들입니다.

- JPA 구현체 JpaVendorAdaptor  
JPA는 표준 스펙일 뿐이므로 이를 구현한 실제 라이브러리가 필요합니다. 보통 하이버네이트를 사용합니다. 그래서 `HibernateJpaVendorAdapter`를 사용합니다.

  ```
  @Bean
  public JpaVendorAdapter jpaVendorAdapter() {		
  		return new HibernateJpaVendorAdapter();
  }
  ```

- JpaProperties 설정(하이버네이트 설정)  
하이버네이트의 설정은 hibernate.properties 파일을 통해서 설정할 수도 있지만 여기서는 구성 클래스에 넣기로 합니다.
   ```
   private Properties getHibernateProperties() {
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		props.setProperty("hibernate.show_sql", "false");		
		props.setProperty("hibernate.jdbc.batch_size", "3");
		props.setProperty("hibernate.jdbc.fetch_size", "100");
		props.setProperty("hibernate.hbm2ddl.auto", "create");
		
		return props;		
	}
    ```
  하이버네이트의 설정 속성들은 아래 링크를 참조하면 됩니다.  
  [Hibernate configuration](https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#configurations)

  여기서 중요한 설정은 `hibernate.hbm2ddl.auto`입니다. 이 옵션을 `create`로 설정하면 정의된 엔티티를 기준으로 테이블을 생성하고 각 테이블의 relation도 설정합니다. `create`는 매번 테이블을 drop 하고(데이터도 모두 삭제) 새로 생성하므로 엔티티가 거의 확정되었으면 `update`나 `none`으로 변경할 필요가 있습니다. 개발 상황에 따라서 이 옵션을 적절하게 변경하여 사용하면 되겠습니다.

- 트랜잭션 관리자 설정  
데이터 저장에는 트랜잭션 관리가 필요하고 스프링에 트랜잭션 관리자 설정을 해주어야 합니다. 스프링은 JPA 전용 트랜잭션 관리자로 `JpaTransactionManager`를 제공합니다. 아래와 같이 `transactionManager`라는 이름의 빈을 설정합니다.
  ```
  @Bean
  public PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager(entityManagerFactory());
		
  }
  ```
  또 트랜잭션 경계설정을 위해 구성 클래스에 `@EnableTransactionManagement`을 추가합니다. 
  
  전체 구성 클래스는 [여기]()를 참조하면 되겠습니다.

  [처음](../README.md) | [다음](../05/README.md)