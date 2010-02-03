/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An object representing one resourcing event for logging.
 *
 * @author: Michael Adams
 * Date: 23/08/2007
 */
public class ResourceEvent {

    private long _id ;                                           // hibernate PK
    private YSpecificationID _specID ;
    private String _caseID ;
    private String _taskID ;
    private String _itemID ;
    private String _participantID ;
    private String _event ;
    private long _timeStamp ;

    public ResourceEvent() {}                                    // for reflection

    /** Constructor for item level events **/
    public ResourceEvent(WorkItemRecord wir, String pid, EventLogger.event eType) {
        this(new YSpecificationID(wir), wir.getCaseID(), pid, eType);
        _taskID = wir.getTaskName(); 
        _itemID = wir.getID();
    }


    /** Constrcutor for case level events **/
    public ResourceEvent(YSpecificationID specID, String caseID, String pid, EventLogger.event eType) {
        _specID = specID;
        _caseID = caseID;
        _participantID = pid;
        _event = eType.name() ;
        _timeStamp = System.currentTimeMillis();
    }



    // GETTERS & SETTERS

    public YSpecificationID get_specID() { return _specID; }

    public void set_specID(YSpecificationID specID) { _specID = specID; }


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


    private long get_id() { return _id; }

    private void set_id(long _id) { this._id = _id; }


    public String toXML() {
        StringBuilder xml = new StringBuilder(String.format("<event key=\"%d\">", _id));
        xml.append(_specID.toXML())
           .append(StringUtil.wrap(_caseID, "caseid"))
           .append(StringUtil.wrap(_taskID, "taskid"))
           .append(StringUtil.wrap(_itemID, "itemid"))
           .append(StringUtil.wrap(_participantID, "participantid"))
           .append(StringUtil.wrap(_event, "eventtype"))
           .append(StringUtil.wrap(String.valueOf(_timeStamp), "timestamp"))
           .append("</event>") ;
        return xml.toString();
    }
}

