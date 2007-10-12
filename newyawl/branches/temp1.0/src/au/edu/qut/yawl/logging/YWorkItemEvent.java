/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.logging;

/**
 * An instantiation of this class represents one row of data in the event logging
 * table 'log_WorkItemEvent'. Called by YEventLogger to record a change in status
 * by a 'live' workitem.
 *
 * Refactored for v1.0 by Michael Adams
 * 09/10/2007
 */

public class YWorkItemEvent implements java.io.Serializable {

    private String _workItemEventID ;                     // primary ID for this event
    private String _caseEventID;                          // foreign key to case event
    private String _caseID;                               // this workitem's id
    private String _taskID;                               // ... and task id
    private String _resourceID;                           // which user generated event?
    private String _eventName;                            // what event was it?
    private long _eventTime;                              // when did it happen?


    // CONSTRUCTORS //

    public YWorkItemEvent() {}                            // required for hibernate


    public YWorkItemEvent(String workItemEventID, String caseEventID, String caseID,
                          String taskID, String resourceID, String eventName,
                          long eventTime) {
        _workItemEventID = workItemEventID;
        _caseEventID = caseEventID;
        _caseID = caseID;
        _taskID = taskID;
        _resourceID = resourceID;
        _eventName = eventName;
        _eventTime = eventTime;
    }


    // GETTERS AND SETTERS FOR HIBERNATE //


    public String get_workItemEventID() { return _workItemEventID; }

    public void set_workItemEventID(String workItemEventID) {
        _workItemEventID = workItemEventID;
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
        if (o instanceof YWorkItemEvent) {
            YWorkItemEvent wie = (YWorkItemEvent) o;
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