package com.example.demo.service;

import java.io.IOException;

import com.example.demo.entity.Singer;

public interface SingerService {
	
	public Singer findById(Long singerId);
	
	public Singer insertOrUpdate(Singer singer);
	
	public void insertTestData() throws IOException;

}
