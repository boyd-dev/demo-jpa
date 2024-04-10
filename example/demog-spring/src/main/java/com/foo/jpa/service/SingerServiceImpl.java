package com.foo.jpa.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foo.jpa.dao.SingerDao;
import com.foo.jpa.entity.RecordLabel;
import com.foo.jpa.entity.Singer;

@Service("singerService")
@Transactional
public class SingerServiceImpl implements SingerService {
	
	
	private SingerDao singerDao;
	
	@Autowired	
	public void setSingerDao(SingerDao singerDao) {
		this.singerDao = singerDao;
	}

	@Override
	public List<Singer> findAll() {
		return singerDao.findAll();		
	}

	@Override
	public Singer findById(Long singerId) {
		return singerDao.findById(singerId);
	}
	
	@Override
	public Singer findByIdWithAlbums(Long singerId) {		
		return singerDao.findByIdWithAlbums(singerId);
	}

	@Override
	public Singer findByIdWithRecordLabels(Long singerId) {		
		return singerDao.findByIdWithRecordLabels(singerId);
	}

	@Override
	public Singer insert(Singer singer) {
		return singerDao.insert(singer);
	}	

	@Override
	public Singer update(Singer singer) {
		return singerDao.update(singer);
	}

	@Override
	public void delete(Singer singer) {
		singerDao.delete(singer);
		
	}

	@Override
	public RecordLabel insert(RecordLabel recordLabel) {
		return singerDao.insert(recordLabel);
	}

	@Override
	public RecordLabel findRecordLabel(RecordLabel recordLabel) {
		RecordLabel result = singerDao.findRecordLabel(recordLabel);
		return Objects.isNull(result)?recordLabel:result;
	}

	

}
