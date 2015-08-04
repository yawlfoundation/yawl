package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the YLogTaskInstance table, representing a runtime instance of a task
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogTaskInstance {

    private long taskInstanceID;                // PK - auto generated
    private String engineInstanceID;            // either workitem or composite task id
    private long taskID;                        // FK to YLogTask
    private long parentNetInstanceID;           // FK to YLogNetInstance
    private long parentTaskInstanceID;          // null if this is not a child workitem

    public YLogTaskInstance() { }

    public YLogTaskInstance(String engineInstanceID, long taskID,
                            long parentTaskInstanceID, long parentNetInstanceID) {
        this.engineInstanceID = engineInstanceID;
        this.taskID = taskID;
        this.parentTaskInstanceID = parentTaskInstanceID;
        this.parentNetInstanceID = parentNetInstanceID;
    }

    public long getTaskInstanceID() {
        return taskInstanceID;
    }

    public void setTaskInstanceID(long taskInstanceID) {
        this.taskInstanceID = taskInstanceID;
    }

    public String getEngineInstanceID() {
        return engineInstanceID;
    }

    public void setEngineInstanceID(String engineInstanceID) {
        this.engineInstanceID = engineInstanceID;
    }

    public long getTaskID() {
        return taskID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public long getParentTaskInstanceID() {
        return parentTaskInstanceID;
    }

    public void setParentTaskInstanceID(long parentTaskInstanceID) {
        this.parentTaskInstanceID = parentTaskInstanceID;
    }

    public long getParentNetInstanceID() {
        return parentNetInstanceID;
    }

    public void setParentNetInstanceID(long parentNetInstanceID) {
        this.parentNetInstanceID = parentNetInstanceID;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(170);
        xml.append(String.format("<taskInstance key=\"%d\">", taskInstanceID));
        xml.append(StringUtil.wrap(engineInstanceID, "caseid"));
        xml.append(StringUtil.wrap(String.valueOf(taskID), "taskKey"));
        xml.append(StringUtil.wrap(String.valueOf(parentNetInstanceID), "parentNetKey"));
        xml.append(StringUtil.wrap(String.valueOf(parentTaskInstanceID), "parentTaskKey"));
        xml.append("</taskInstance>");
        return xml.toString();
    }

}
