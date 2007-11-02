/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of 
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

public class YLogIdentifier {

    private String identifier = null;
    private long cancelled = 0;
    private long created = 0;
    private long completed = 0;
    private String createdby = null;
    private String specification = null;
    private String parent = null;
    private long timetaken = 0;
    private long count = 0;
    
    public void setCount(long count) {
	this.count = count;
    }	

    public long getCount() {
	return this.count;
    }	
    
    public void setTime(long time) {
	this.timetaken = time;
    }
    public long getTime() {
	return this.timetaken;
    }	

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setCancelled(long cancelled) {
        this.cancelled = cancelled;
    }

    public long getCancelled() {
        return this.cancelled;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getCreated() {
        return this.created;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getCompleted() {
        return this.completed;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getCreatedby() {
        return this.createdby;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getSpecification() {
        return this.specification;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getParent() {
        return this.parent;
    }


}