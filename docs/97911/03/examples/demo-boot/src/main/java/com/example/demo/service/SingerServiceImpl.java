package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Singer;
import com.example.demo.repository.SingerRepository;


@Service("singerService")
@Transactional
public class SingerServiceImpl implements SingerService {
	
	
	private final SingerRepository singerRepository;	

	public SingerServiceImpl(SingerRepository singerRepository) {
		this.singerRepository = singerRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Singer findById(Long singerId) {		
		
		Singer result = this.singerRepository.findById(singerId).get();
		result.setPhoto(null);		
		return result;
	}

	@Override
	public Singer insertOrUpdate(Singer singer) {		
		return this.singerRepository.save(singer);
	}

	@Override
	public void insertTestData() throws IOException {
		
		Singer singer = new Singer();
		singer.setFirstName("Adele");
		singer.setLastName("Adkins");
		Date bd = Date.from(Instant.parse("1988-05-05T00:00:00Z"));
		singer.setBirthDate(bd);
		singer.setPhoto(Files.readAllBytes(new File("image.png").toPath()));
		
		
		this.singerRepository.save(singer);		
		
	}

}
