package com.foo.jpa.dao;

import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.foo.jpa.entity.RecordLabel;
import com.foo.jpa.entity.Singer;

@Repository("singerDao")
public class SingerDaoImpl implements SingerDao {
	
	@PersistenceContext
	private EntityManager em;
	

	@Override
	public List<Singer> findAll() {
		return em.createQuery("from Singer s", Singer.class).getResultList();
	}

	@Override
	public Singer findById(Long id) {
		return em.find(Singer.class, id);
	}
	
	@Override
	public Singer findByIdWithAlbums(Long id) {
		return em.createQuery("from Singer s join fetch s.albums a where s.id = ?0", Singer.class)
				.setParameter(0, id)
				.getSingleResult();
	}
	
	@Override
	public Singer findByIdWithRecordLabels(Long id) {
		return em.createQuery("from Singer s left join fetch s.recordLabels r where s.id = ?0", Singer.class)
				.setParameter(0, id)
				.getSingleResult();
	}
	
	@Override
	public RecordLabel findRecordLabel(RecordLabel recordLabel) {
		return em.createNamedQuery("RecordLabel.Find_RecordLabel_With_Singer", RecordLabel.class)
				.setParameter(0, recordLabel.getLabel())
				.getResultStream().findFirst().orElse(null);
	}

	@Override
	public Singer insert(Singer singer) {
		System.out.println(em.contains(singer));
		em.persist(singer);
		System.out.println(em.contains(singer));
		return singer;
	}

	@Override
	public Singer update(Singer singer) {		
//		if (!Objects.isNull(singer.getId())) {
//			return em.merge(singer);
//		}
//		return null;
		return em.merge(singer);
	}

	@Override
	public void delete(Singer singer) {		
		em.remove(singer);		
	}

	@Override
	public RecordLabel insert(RecordLabel recordLabel) {
		
		if (Objects.isNull(findRecordLabel(recordLabel))) {
			em.persist(recordLabel);
		}
		
		return recordLabel;
	}		

}
