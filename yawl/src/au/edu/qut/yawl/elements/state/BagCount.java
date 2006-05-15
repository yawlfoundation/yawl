package au.edu.qut.yawl.elements.state;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BagCount {
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
