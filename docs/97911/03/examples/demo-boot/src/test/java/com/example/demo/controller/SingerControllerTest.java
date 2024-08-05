package com.example.demo.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


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
		//.andExpect(jsonPath("$.photo").isEmpty());
		.andExpect(jsonPath("$.photo").doesNotHaveJsonPath());
		
	}

}
