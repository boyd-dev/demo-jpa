package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class DemoBootApplicationTests {
	
	@Autowired
    private ApplicationContext context;


	@Test
	void contextLoads() {
		assertNotNull(context);
	}
	
	@Test
	@DisplayName("CoffeeOrder Test")
	void testOrder() {
		Order order = context.getBean("coffeeOrder", Order.class);
		order.brew();		
		assertNotNull(order);				
	}


}
