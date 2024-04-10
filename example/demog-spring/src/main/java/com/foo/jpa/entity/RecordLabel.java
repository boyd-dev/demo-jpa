package com.foo.jpa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity(name = "RecordLabel")
@Table(name = "record_label")
@NamedQueries(
		{
			@NamedQuery(
				name = "RecordLabel.Find_RecordLabel_With_Singer",
				query = "select r from RecordLabel r left join fetch r.singers s where r.label = ?0"
			)
		}
)
public class RecordLabel extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Column(name="label", unique = true)
	private String label;	

	@ManyToMany(mappedBy = "recordLabels", fetch = FetchType.EAGER)
	private List<Singer> singers = new ArrayList<>();
		
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<Singer> getSingers() {
		return singers;
	}

	public void setSingers(List<Singer> singers) {
		this.singers = singers;
	}
	
	@Override
	public int hashCode() {		
		return Objects.hash(this.label);
	}

	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == this) return true;
		
		if (obj == null || getClass() != obj.getClass() ) {
			return false;
		}		
		RecordLabel r = (RecordLabel)obj;		
		return Objects.equals(this.label, r.label);		
	}	
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.label;
	}
	
}
