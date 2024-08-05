package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.demo.service.SingerService;

@SpringBootApplication
public class DemoBootApplication {
	
	@Autowired
	private SingerService singerService;

	public static void main(String[] args) {
		SpringApplication.run(DemoBootApplication.class, args);
	}
	
	@Bean
    ApplicationRunner init() {
        return args -> {
            System.out.println("Insert test data..."); 
            singerService.insertTestData();                        
        };
    }

}
