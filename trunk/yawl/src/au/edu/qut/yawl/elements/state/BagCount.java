/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.elements.state;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class BagCount implements Serializable {
	public BagCount() {
		super();
	}
	
	private Integer count = 0;
	
	@Basic
	public Integer getCount() {
		return count;
	}
	
	public void setCount( Integer count ) {
		this.count = count;
	}
	
	public void incrementCount() {
		setCount(count + 1);
	}
	
	public void decrementCount() {
		setCount(count - 1);
	}

	private Long id;
	
	@Id
	@Column(name="count_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}
	
	public void setId( Long id ) {
		this.id = id;
	}
	

}
