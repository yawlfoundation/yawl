package au.edu.qut.yawl.elements;

import java.io.Serializable;


import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class KeyValue implements Serializable, Parented<YTask>{

	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

	private String key;
	private String value;
	private Long id;
	private YTask parent;
	private String type;
	public static final String COMPLETION = "completion";
	public static final String ENABLEMENT = "enablement";
	public static final String STARTING = "starting";

	public KeyValue() {
		super();
	}
	
	public KeyValue(String type, String key, String value, YTask parent) {
		this.type = type;
		this.key = key;
		this.value = value;
		this.parent = parent;
	}
	
	@Id
	@Column(name="key_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	
	public void setId( Long id ) {
		this.id = id;
	}


	@Lob
	public String getValue() {
		return value;
	}
	
	public void setValue( String value ) {
		this.value = value;
	}

	@Lob
	public String getKey() {
		return key;
	}
	
	public void setKey( String key ) {
		this.key = key;
	}
	
	public String toString() {
		return value;
	}
	
	public int hashCode() {
		return this.getKey() == null ? 0 : this.getKey().hashCode();
	}
	
	public boolean equals(Object obj) {
		return (obj instanceof KeyValue 
			&& ((KeyValue) obj).getKey().equals(this.getKey()));
	}

	@Transient
	public void setParent(YTask parent) {
		this.parent = parent;
	}

	@Transient
	public YTask getParent() {
		return parent;
	}

	@ManyToOne(cascade = {CascadeType.ALL})
	public void setTask(YTask parent) {
		this.parent = parent;
	}

	@ManyToOne(cascade = {CascadeType.ALL})
	public YTask getTask() {
		return parent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
