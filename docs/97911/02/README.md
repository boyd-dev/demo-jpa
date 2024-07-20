## 엔티티

- 엔티티 작성시 "플루언트" 스타일 적용하기  
엔티티를 하나 생성하여 등록을 할 때 각 속성의 setter를 통해 값을 넣어주어야 한다. 속성이 많은 경우 이와 같은 작업이 번거로울 수 있기 때문에 "플루언트" 스타일로 값을 넣을 수 있께 만들어주는 방법을 소개한다. 이 방법은 흔히 말하는 "빌더 패턴"과 유사한데, 빌더 패턴이 플루언트 스타일과 함께 쓰이는 경우가 많기 때문인 것 같다. 

   ```
   Singer singer = new Singer();
   singer.setFirstName("John");
   singer.setLastName("Lennon");
   ...
   ```
   플루언트 스타일은 아래와 같은 형태가 된다.
   ```
   Singer singer = new Singer()
                    .firstName("John")
                    .lastName("Lennon");
   ```
   빌드 메소드를 별도로 두는 빌더 패턴의 경우는 아래와 같은 형태가 된다.

   ```
   Singer singer = new Singer.Builder()
                       .firstName("John")
                       .lastName("Lennon")
                       .build();
   ```
- 자식을 등록할 때 FK컬럼에 부모의 키 채우기  
단방향 `@ManyToOne`에서는 자식을 등록할 때 연관된 부모의 PK를 알아야 한다. 보통 부모를 `findById`로 조회한 후 부모를 참조한다. 

   ```
   Author author = authorRepository.findById(1L).get();

   Book book = new Book();
   book.setAuthor(author);
   ```
   이렇게 하면 당연히 Author 엔티티를 조회하는 쿼리가 먼저 실행되고 인서트가 실행된다. 엔티티를 전부 가져오는 것은 이 목적에서는 불필요한 일이기 때문에 단지 부모 키가 필요하다면 `getReferenceById`를 쓰면 select 쿼리 없이 인서트만 실행되므로 효율적이다. 책에서는 `getOne`을 사용했지만 deprecated 되었다.
   







[처음](../README.md) | [다음](../03/README.md)