package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logTask table, representing a unique task 'template' of a parent net
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogTask {

    private long taskID;                          // PK - auto generated
    private String name ;
    private long parentNetID ;                    // FK to YLogNet
    private long childNetID;                      // FK to YLogNet (composite tasks only)

    public YLogTask() { }

    public YLogTask(String name, long parentNetID, long childNetID) {
        this.name = name;
        this.parentNetID = parentNetID;
        this.childNetID = childNetID;
    }

    public long getTaskID() {
        return taskID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getParentNetID() {
        return parentNetID;
    }

    public void setParentNetID(long parentNetID) {
        this.parentNetID = parentNetID;
    }

    public long getChildNetID() {
        return childNetID;
    }

    public void setChildNetID(long childNetID) {
        this.childNetID = childNetID;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(130);
        xml.append(String.format("<task key=\"%d\">", taskID));
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(String.valueOf(parentNetID), "parentNetKey"));
        xml.append(StringUtil.wrap(String.valueOf(childNetID), "childNetKey"));
        xml.append("</task>");
        return xml.toString();
    }
}
