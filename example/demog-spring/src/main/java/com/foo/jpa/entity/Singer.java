package com.foo.jpa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.ColumnDefault;


@Entity(name = "Singer")
@Table(name = "singer")
public class Singer extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "first_name")	
	private String firstName;

	@Column(name = "last_name")
	private String lastName;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "birth_date", nullable = true)
	private Date birthDate;
	
	@Column(name = "version")
	@Version
	@ColumnDefault("0")
	private int version;
	
	@OneToMany(
			mappedBy = "singer", 
			cascade = CascadeType.ALL, 
			orphanRemoval = true, 
			fetch = FetchType.LAZY)
	private Set<Album> albums = new HashSet<>();
	
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
	private List<RecordLabel> recordLabels = new ArrayList<>();
	
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}

	public Date getBirthDate() {
		return birthDate;
	}
	
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}		

	public Set<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(Set<Album> albums) {
		this.albums = albums;
	}
	
	public boolean addAlbum(Album album) {
		album.setSinger(this);
		return getAlbums().add(album);		
	}	
	
	public boolean addRecordLabel(RecordLabel recordLabel) {		
		recordLabel.getSingers().add(this);		
		return getRecordLabels().add(recordLabel);		
	}
	
	
	public List<RecordLabel> getRecordLabels() {
		return recordLabels;
	}

	public void setRecordLabels(List<RecordLabel> recordLabels) {
		this.recordLabels = recordLabels;
	}

	@Override
	public String toString() {		
		return getId() + ":" + this.firstName + " " + this.lastName + " " + this.birthDate;
	}

}
