/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.edu.qut.yawl.persistence.PersistableObject;

@Entity
public class YLogIdentifier implements PersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

    private String identifier = null;
    private long cancelled = 0;
    private long created = 0;
    private long completed = 0;
    private String createdby = null;
    private String specification = null;
    private String parent = null;
    private long timetaken = 0;
    private long count = 0;

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Id
    public String getIdentifier() {
        return this.identifier;
    }

    public void setCancelled(long cancelled) {
        this.cancelled = cancelled;
    }

    @Basic
    public long getCancelled() {
        return this.cancelled;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Basic
    public long getCreated() {
        return this.created;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    @Basic
    public long getCompleted() {
        return this.completed;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    @Basic
    public String getCreatedby() {
        return this.createdby;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    @Basic
    public String getSpecification() {
        return this.specification;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Basic
    public String getParent() {
        return this.parent;
    }

	@Basic
	public long getCount() {
		return count;
	}

	
	public void setCount( long count ) {
		this.count = count;
	}

	@Basic
	public long getTime() {
		return timetaken;
	}

	
	public void setTime( long timetaken ) {
		this.timetaken = timetaken;
	}
}