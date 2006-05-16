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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import au.edu.qut.yawl.persistence.PersistableObject;

@Entity
public class YWorkItemEvent implements PersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

    public String resource = null;
    public long time;
    public String event = null;
    public String identifier = null;
    public String taskid = null;
    public String description = null;

    public YWorkItemEvent() {
    }
    
    private Long id;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	public void setId( Long id ) {
		this.id = id;
	}

    public void setTime(long time) {
        this.time = time;
    }

    @Basic
    public long getTime() {
        return this.time;
    }

    @Basic
    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Basic
    public String getTaskid() {
        return this.taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    @Basic
    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Basic
    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Basic
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object o) {
        if (o instanceof YWorkItemEvent) {
            YWorkItemEvent wie = (YWorkItemEvent) o;
            if (wie.getTaskid().equals(getTaskid()) &&
                    wie.getIdentifier().equals(getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return getTaskid().hashCode() + getIdentifier().hashCode();
    }

}