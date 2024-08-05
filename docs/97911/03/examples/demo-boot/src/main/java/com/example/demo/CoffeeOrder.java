package com.example.demo;

import org.springframework.stereotype.Component;

@Component("coffeeOrder")
public class CoffeeOrder implements Order {
	
	@Override
	public void brew() {		
		System.out.println("Coffee is brewing");
	}
	
}
