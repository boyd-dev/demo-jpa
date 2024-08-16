## Criteria API  

크라이테리아 API는 JPA 스펙에 다음과 같이 설명되어 있습니다.

>The Jakarta Persistence Criteria API is used to define queries through the construction of object-based query definition objects, rather than use of the string-based approach of the Jakarta Persistence query language...

이제까지 살펴본 쿼리는 크게 세 가지 종류로 구분될 수 있습니다. 

- 메소드명으로 자동 생성되는 쿼리 빌더
- JPQL
- Native SQL

이제 여기에 다른 방식으로 쿼리를 작성하는 "Criteria API"가 추가되는 것입니다. 위의 설명에도 있는 것처럼 기존의 쿼리들은 대개 문자열로 작성되는, SQL과 유사한 형태의 static한 쿼리라고 할 수 있었습니다. 문자열 기반의 쿼리의 경우 동적으로 쿼리를 만들어내는 것은 한계가 있을 수 밖에 없습니다.

크라이테리어 API는 동적인 쿼리를 만들 때 활용할 수 있는 것으로, 이를테면 조회할 컬럼들은 동일하지만 조건절이 매번 달라지는 경우에 적용될 수 있습니다. 


[처음](../README.md)