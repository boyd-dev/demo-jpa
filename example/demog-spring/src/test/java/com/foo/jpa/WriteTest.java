package com.foo.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.foo.jpa.config.AppConfig;
import com.foo.jpa.entity.Album;
import com.foo.jpa.entity.RecordLabel;
import com.foo.jpa.entity.Singer;
import com.foo.jpa.service.SingerService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WriteTest {

	@Autowired
	private ApplicationContext ctx;
		
	SingerService service;
	
	@BeforeAll
	void getContext() {
		service = ctx.getBean("singerService", SingerService.class);
	}	

	@Test	
	@DisplayName("setup")
	@Order(1)
	void setup() {		
		assertNotNull(service);
	}
	
	@Test
	@DisplayName("insert Singer with Album")
	@Order(2)
	void testInsert() {
		
		Singer singer = new Singer();
		
		// 1.
		singer.setFirstName("Adele");
		singer.setLastName("Adkins");
		Date bd = Date.from(Instant.parse("1988-05-05T00:00:00Z"));
		singer.setBirthDate(bd);
				
		Album album = new Album();
		album.setTitle("Easy On Me");
		Date d = Date.from(Instant.parse("2021-10-14T00:00:00Z"));
		album.setReleaseDate(d);
		singer.addAlbum(album);
		
		album = new Album();
		album.setTitle("Hello");
		d = Date.from(Instant.parse("2015-10-23T00:00:00Z"));
		album.setReleaseDate(d);
		singer.addAlbum(album);
		
		service.insert(singer);
		
		//2. 
		singer = new Singer();
		singer.setFirstName("John");
		singer.setLastName("Lennon");
		bd = Date.from(Instant.parse("1940-10-09T00:00:00Z"));
		singer.setBirthDate(bd);
		
		album = new Album();
		album.setTitle("Imagine");
		d = Date.from(Instant.parse("1971-09-09T00:00:00Z"));
		album.setReleaseDate(d);
		singer.addAlbum(album);
		
		service.insert(singer);
		
		//3. 
		singer = new Singer();
		singer.setFirstName("Billie");
		singer.setLastName("Eilish");
		bd = Date.from(Instant.parse("2001-12-18T00:00:00Z"));
		singer.setBirthDate(bd);
				
		album = new Album();
		album.setTitle("No Time To Die");
		d = Date.from(Instant.parse("2020-02-13T00:00:00Z"));
		album.setReleaseDate(d);
		singer.addAlbum(album);
		
		album = new Album();
		album.setTitle("Bad Guy");
		d = Date.from(Instant.parse("2019-07-19T00:00:00Z"));
		album.setReleaseDate(d);
		singer.addAlbum(album);
		
		service.insert(singer);
		
		Singer result = service.findByIdWithAlbums(Long.parseLong("1"));
		result.getAlbums().forEach(System.out::println);
		
		assertEquals(result.getAlbums().size(),2);
	}
	
	
	@Test
	@DisplayName("insert RecordLabel")
	@Order(3)
	void insertRecordLabel() {
		
		//1.
		RecordLabel recordLabel = new RecordLabel();
		recordLabel.setLabel("Columbia Records");		
		RecordLabel result = service.insert(recordLabel);
		
		//2.
		recordLabel = new RecordLabel();
		recordLabel.setLabel("Sony Music Entertainment");
		result = service.insert(recordLabel);
		
		assertNotNull(result.getId());		
	}
	
	@Test
	@DisplayName("update Singer with existing RecordLabel")
	@Order(4)
	void updateSingerWithRecordLabel() {
		
		RecordLabel recordLabel = new RecordLabel();
		recordLabel.setLabel("Columbia Records");	
		
		RecordLabel result = service.findRecordLabel(recordLabel);

		Singer singer = new Singer();
		
		singer = service.findByIdWithRecordLabels(Long.parseLong("1"));
		singer.addRecordLabel(result);
		service.update(singer);		
		
		singer = new Singer();
		singer = service.findByIdWithRecordLabels(Long.parseLong("3"));
		singer.addRecordLabel(result);
		Singer s = service.update(singer);
		
		assertTrue(s.getRecordLabels().size()>0);
		
	}
	
	@Test
	@DisplayName("insert new RecordLabel and update Singer")
	@Order(4)
	void insertRecordLabelAndUpdateSinger() {
		
		RecordLabel recordLabel = new RecordLabel();
		recordLabel.setLabel("Darkroom");	
		
		RecordLabel result = service.findRecordLabel(recordLabel);

		Singer singer = new Singer();
		singer = service.findByIdWithRecordLabels(Long.parseLong("3"));
		singer.addRecordLabel(result);
		service.update(singer);	
		
		assertNull(result.getId());
	}
	
	
	@Test
	@DisplayName("delete RecordLabel by Singer")
	@Order(5)
	void deleteRecordLabelbySinger() {
		 
		 Singer singer = service.findByIdWithRecordLabels(Long.parseLong("3"));
		 
		 RecordLabel recordLabel = new RecordLabel();
		 recordLabel.setLabel("Columbia Records");
		 boolean result = singer.getRecordLabels().remove(recordLabel);
		 
		 service.update(singer);
		 assertTrue(result);
		 		
	}	

}
