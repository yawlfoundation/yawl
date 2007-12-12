package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An instantiation of this class represents one row of data in the event logging
 * table 'log_ParentWorkItemEvent'. Called by YEventLogger to record a change in status
 * by a parent workitem.
 *
 * Refactored for v1.0 by Michael Adams
 * 09/10/2007
 */

public class YParentWorkItemEvent implements java.io.Serializable {

    private String _parentWorkItemEventID ;               // primary ID for this event
    private String _caseEventID;                          // foreign key to case event
    private String _caseID;                               // this workitem's id
    private String _taskID;                               // ... and task id
    private String _resourceID;                           // which user generated event?
    private String _eventName;                            // what event was it?
    private long _eventTime;                              // when did it happen?


    // CONSTRUCTORS //

    public YParentWorkItemEvent() {}                            // required for hibernate


    public YParentWorkItemEvent(String workItemEventID, String caseEventID, String caseID,
                          String taskID, String resourceID, String eventName,
                          long eventTime) {
        _parentWorkItemEventID = workItemEventID;
        _caseEventID = caseEventID;
        _caseID = caseID;
        _taskID = taskID;
        _resourceID = resourceID;
        _eventName = eventName;
        _eventTime = eventTime;
    }


    /****************************************************************************/

    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<ParentWorkItemEvent id=\"%s\">", _parentWorkItemEventID)) ;
        xml.append(StringUtil.wrap(_caseEventID, "caseEventID"));
        xml.append(StringUtil.wrap(_caseID, "caseID"));
        xml.append(StringUtil.wrap(_taskID, "taskID"));
        xml.append(StringUtil.wrap(_resourceID, "resourceID"));
        xml.append(StringUtil.wrap(_eventName, "eventName"));
        xml.append(StringUtil.wrap(String.valueOf(_eventTime), "eventTime"));
        xml.append("</ParentWorkItemEvent>");
        return xml.toString() ;
    }


    // GETTERS AND SETTERS FOR HIBERNATE //


    public String get_parentWorkItemEventID() { return _parentWorkItemEventID; }

    public void set_parentWorkItemEventID(String workItemEventID) {
        _parentWorkItemEventID = workItemEventID;
    }


    public String get_caseEventID() { return _caseEventID; }

    public void set_caseEventID(String caseEventID) { _caseEventID = caseEventID; }


    public String get_caseID() { return _caseID; }

    public void set_caseID(String itemID) { _caseID = itemID; }


    public String get_taskID() { return _taskID; }

    public void set_taskID(String taskID) { _taskID = taskID; }


    public String get_resourceID() { return _resourceID; }

    public void set_resourceID(String resourceID) { _resourceID = resourceID; }


    public String get_eventName() { return _eventName; }

    public void set_eventName(String eventName) { _eventName = eventName; }


    public long get_eventTime() { return _eventTime; }

    public void set_eventTime(long eventTime) { _eventTime = eventTime; }


    // SERIALIZABLE METHODS //

    public boolean equals(Object o) {
        if (o instanceof YParentWorkItemEvent) {
            YParentWorkItemEvent wie = (YParentWorkItemEvent) o;
            if (wie.get_taskID().equals(get_taskID()) &&
                    wie.get_caseEventID().equals(get_caseEventID())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return get_taskID().hashCode() + get_caseEventID().hashCode();
    }

}
