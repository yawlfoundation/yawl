package au.edu.qut.yawl.util;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import au.edu.qut.yawl.elements.state.YIdentifier;

@Entity
public class MultiInstanceData {

	public MultiInstanceData() {
		
	}

	public MultiInstanceData(String data) {
		this.data = data;
	}

	private String data = null;

	private Long id = null;
	
	private String identifierlink = null;

	@Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Basic
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Lob
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Basic
	public String getIdentifierlink() {
		return identifierlink;
	}

	public void setIdentifierlink(String identifierlink) {
		this.identifierlink = identifierlink;
	}
	
	
}
