package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Singer;
import com.example.demo.service.SingerService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class SingerController {
	
	@Autowired
	private SingerService singerService;

    	
	@GetMapping(value = {"/singer/{id}"})
	public Singer fetchSinger(@PathVariable(name = "id") long id) {
		
		Singer result = singerService.findById(id);
		return result;
	}
	
	@GetMapping(value = "/home")
	public void home(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_OK);
	}


}
