package com.example.demo.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;


@Entity(name = "Singer")
@Table(name = "singer")
@JsonInclude(value = Include.NON_NULL)
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
	
	@Lob
    @Basic(fetch = FetchType.LAZY)
	@Column(name = "photo", length = 5000) // max 5000 bytes
    private byte[] photo;

		
	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

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
	
		
	@Override
	public int hashCode() {		
		return 2024;
	}

	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == this) return true;
		
		if (obj == null || getClass() != obj.getClass() ) {
			return false;
		}		
		Singer s = (Singer)obj;		
		return Objects.equals(this.firstName+this.lastName, s.firstName+s.lastName);		
	}	
		
	
	@Override
	public String toString() {		
		return getId() + ":" + this.firstName + " " + this.lastName + " " + this.birthDate;
	}

}
