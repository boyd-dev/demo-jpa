package com.foo.jpa.service;

import java.util.List;

import com.foo.jpa.entity.RecordLabel;
import com.foo.jpa.entity.Singer;

public interface SingerService {
	
	public List<Singer> findAll();
	public Singer findById(Long singerId);
	public Singer findByIdWithAlbums(Long singerId);
	public Singer findByIdWithRecordLabels(Long singerId);
	public RecordLabel findRecordLabel(RecordLabel recordLabel);	
		
	public Singer insert(Singer singer);
	public Singer update(Singer singer);
	public void delete(Singer singer);
	
	public RecordLabel insert(RecordLabel recordLabel);
	
	

}
