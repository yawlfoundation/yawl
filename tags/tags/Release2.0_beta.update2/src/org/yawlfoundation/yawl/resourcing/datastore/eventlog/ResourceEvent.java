/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

/**
 * An object representing one resourcing event for logging.
 *
 * @author: Michael Adams
 * Date: 23/08/2007
 */
public class ResourceEvent {

    private long _id ;                                           // hibernate PK
    private String _specID ;
    private String _specVersion;
    private String _caseID ;
    private String _taskID ;
    private String _itemID ;
    private String _participantID ;
    private String _event ;
    private long _timeStamp ;

    public ResourceEvent() {}                                    // for reflection


    public ResourceEvent(WorkItemRecord wir, String pid, EventLogger.event eType) {
        _specID = wir.getSpecificationID();
        _specVersion = wir.getSpecVersion();
        _caseID = wir.getCaseID();
        _taskID = wir.getTaskID();
        _itemID = wir.getID();
        _participantID = pid;
        _event = eType.name() ;
        _timeStamp = System.currentTimeMillis();

    }

    // GETTERS & SETTERS

    public String get_specID() { return _specID; }

    public void set_specID(String specID) { _specID = specID; }


    public String get_caseID() { return _caseID; }

    public void set_caseID(String caseID) { _caseID = caseID; }


    public String get_taskID() { return _taskID; }

    public void set_taskID(String taskID) { _taskID = taskID; }


    public String get_itemID() { return _itemID; }

    public void set_itemID(String itemID) {_itemID = itemID; }


    public String get_participantID() { return _participantID; }

    public void set_participantID(String participantID) { _participantID = participantID;}


    public String get_event() { return _event; }

    public void set_event(String event) { _event = event; }


    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) { _timeStamp = timeStamp; }


    public String get_specVersion() { return _specVersion; }

    public void set_specVersion(String specVersion) { _specVersion = specVersion; }


    private long get_id() { return _id; }

    private void set_id(long _id) { this._id = _id; }
}

