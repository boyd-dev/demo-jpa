## 속성 지연 로딩 예제

- Spring Boot 3.3.2

- Gradle 8.6

  Bytecode Enhancement를 사용하기 위해 플러그인과 설정을 추가

  ```
  plugins {
    ...
	id 'org.hibernate.orm' version '6.5.2.Final'
  }
  
  hibernate {
      enhancement {
          lazyInitialization true
          dirtyTracking false
          associationManagement false
          extendedEnhancement false
      }
  }
  ```

- 테스트  
컨트롤러를 호출하여 `Singer` 엔티티를 직접 조회하는데 `byte[] photo` 속성은 lazy 로딩으로 설정되어 있다. OSIV도 false로 설정하기 때문에 Jackson이 `Singer`를 직렬화할 때 `photo` 속성을 로드하면서 지연 초기화 예외가 발생해야 한다. 하지만 조회할 때 미리 null로 초기화시키고 또 `@JsonInclude(value = Include.NON_NULL)`을 추가해서 오류가 나지 않는다.

  ```
  @SpringBootTest
  @AutoConfigureMockMvc
  public class SingerControllerTest {
	
	  @Autowired
	  private MockMvc mockMvc;
	
	
	  @Test
	  void testFetchSinger() throws Exception {
		
		  mockMvc.perform(MockMvcRequestBuilders.get("/singer/{id}", 1))
		  .andExpect(status().isOk())
		  .andExpect(jsonPath("$.firstName").value("Adele"))		
		  .andExpect(jsonPath("$.photo").doesNotHaveJsonPath());
		
	  }
  }
  ```
  