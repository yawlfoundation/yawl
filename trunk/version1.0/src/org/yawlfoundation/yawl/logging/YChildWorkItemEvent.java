/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.logging;

/**
 * An instantiation of this class represents one row of data in the event logging
 * table 'log_WorkItemEvent'. Called by YEventLogger to record a change in status
 * by a 'live' workitem.
 *
 * Refactored for v1.0 by Michael Adams
 * 09/10/2007
 */

public class YChildWorkItemEvent implements java.io.Serializable {

    private String _childWorkItemEventID ;                // primary ID for this event
    private String _parentWorkItemEventID;                // foreign key to parent event
    private String _caseID;                               // this workitem's id
    private String _resourceID;                           // which user generated event?
    private String _eventName;                            // what event was it?
    private long _eventTime;                              // when did it happen?


    // CONSTRUCTORS //

    public YChildWorkItemEvent() {}                            // required for hibernate


    public YChildWorkItemEvent(String workItemEventID, String parentEventID, String caseID,
                               String resourceID, String eventName, long eventTime) {
        _childWorkItemEventID = workItemEventID;
        _parentWorkItemEventID = parentEventID;
        _caseID = caseID;
        _resourceID = resourceID;
        _eventName = eventName;
        _eventTime = eventTime;
    }


    // GETTERS AND SETTERS FOR HIBERNATE //


    public String get_childWorkItemEventID() { return _childWorkItemEventID; }

    public void set_childWorkItemEventID(String workItemEventID) {
        _childWorkItemEventID = workItemEventID;
    }


    public String get_parentWorkItemEventID() { return _parentWorkItemEventID; }

    public void set_parentWorkItemEventID(String caseEventID) {
        _parentWorkItemEventID = caseEventID;
    }


    public String get_caseID() { return _caseID; }

    public void set_caseID(String itemID) { _caseID = itemID; }


    public String get_resourceID() { return _resourceID; }

    public void set_resourceID(String resourceID) { _resourceID = resourceID; }


    public String get_eventName() { return _eventName; }

    public void set_eventName(String eventName) { _eventName = eventName; }


    public long get_eventTime() { return _eventTime; }

    public void set_eventTime(long eventTime) { _eventTime = eventTime; }


    // SERIALIZABLE METHODS //

    public boolean equals(Object o) {
        if (o instanceof YChildWorkItemEvent) {
            YChildWorkItemEvent wie = (YChildWorkItemEvent) o;
            if (wie.get_caseID().equals(get_caseID()) &&
                    wie.get_parentWorkItemEventID().equals(get_parentWorkItemEventID())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return get_caseID().hashCode() + get_parentWorkItemEventID().hashCode();
    }

}