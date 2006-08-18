package au.edu.qut.yawl.events;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Event {
	@Basic
	private Serializable data;
	@Id
	private Long id;
	
	public Serializable getData() {
		return data;
	}
	
	public void setData( Serializable data ) {
		this.data = data;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId( Long id ) {
		this.id = id;
	}
}
