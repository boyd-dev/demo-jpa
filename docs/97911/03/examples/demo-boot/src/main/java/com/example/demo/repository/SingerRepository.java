package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Singer;


public interface SingerRepository extends JpaRepository<Singer, Long> {
	
	
		
}
