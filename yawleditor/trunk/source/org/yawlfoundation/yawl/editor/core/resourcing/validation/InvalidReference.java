package org.yawlfoundation.yawl.editor.core.resourcing.validation;

/**
 * Author: Michael Adams
 */
public abstract class InvalidReference {

    private String _id;
    private String _msgName;
    private String _netID;
    private String _taskID;


    protected InvalidReference(String id, String msgName) {
        _id = id;
        _msgName = msgName;
    }

    public String getID() { return _id; }

    public void setNetID(String id) { _netID = id; }

    public void setTaskID(String id) { _taskID = id; }

    public String getMessage() {
        StringBuilder s = new StringBuilder();
        s.append("Task [").append(_taskID).append("]");
        s.append(" in net [").append(_netID).append("]");
        s.append(" contains an invalid ");
        s.append(_msgName);
        s.append(" reference [").append(_id).append("].");
        return s.toString();
    }
}
