package au.edu.qut.yawl.elements;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;

@Entity
public class KeyValue implements PolymorphicPersistableObject{

	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

	private String key;
	private String value;
	private Long id = null;

	public KeyValue() {
		super();
	}
	
	public KeyValue(String key, String value) {
		this.value = value;
		this.key = key;
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


	@Basic
	public String getValue() {
		return value;
	}
	
	public void setValue( String value ) {
		this.value = value;
	}

	@Basic
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
		if (obj instanceof KeyValue && ((KeyValue) obj).getKey().equals(this.getKey())) {
			return true;
		} else {
			return false;
		}
	}
}
