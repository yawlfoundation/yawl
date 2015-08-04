/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

public class YWorkItemEvent implements java.io.Serializable {

    public String resource = null;
    public long time = 0;
    public String event = null;
    public String identifier = null;
    public String taskid = null;
    public String description = null;

    private String rowkey = null;

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getRowkey() {
        return this.rowkey;
    }


    public YWorkItemEvent() {
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    public String getResource() {
        return this.resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getTaskid() {
        return this.taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getEvent() {
        return this.event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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