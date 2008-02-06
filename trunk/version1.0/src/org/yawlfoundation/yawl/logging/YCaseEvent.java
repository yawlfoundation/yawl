/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An instantiation of this class represents one row of data in the event logging
 * table 'log_CaseEvent'. Called by YEventLogger to record the event and time when
 * a case is started and when it completes or is cancelled.
 *
 * Refactored for v2.0 by Michael Adams
 * 09/10/2007
 */

public class YCaseEvent {

    private String _caseEventID ;                         // primary key for hibernate
    private String _caseID;
    private long _eventTime;                              // when the event occurred
    private String _eventName;                            // what type of event it was
    private String _resourceID;                           // who triggered the event
    private String _specID;                               // specification of the case
    private String _subnetID;                             // if this is a sub-net

    // constants for the case events logged
    public static String START = "started" ;
    public static String COMPLETE = "completed";
    public static String CANCEL = "cancelled";
    

    // CONSTRUCTORS //

    public YCaseEvent() {}                                // required for persistence


    public YCaseEvent(String eventID, String caseID, long eventTime, String eventName,
                      String resourceID, String specID, String subnetID) {
        _caseEventID = eventID ;
        _caseID = caseID;
        _eventTime = eventTime;
        _eventName = eventName;
        _resourceID = resourceID;
        _specID = specID;
        _subnetID = subnetID;
    }

    /****************************************************************************/

    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<CaseEvent id=\"%s\">", _caseEventID)) ;
        xml.append(StringUtil.wrap(_caseID, "caseID"));
        xml.append(StringUtil.wrap(String.valueOf(_eventTime), "eventTime"));
        xml.append(StringUtil.wrap(_eventName, "eventName"));
        xml.append(StringUtil.wrap(_resourceID, "resourceID"));
        xml.append(StringUtil.wrap(_specID, "specID"));
        xml.append(StringUtil.wrap(_subnetID, "subnetID"));
        xml.append("</CaseEvent>");
        return xml.toString() ;
    }


    // GETTERS & SETTERS FOR HIBERNATE //

    public String get_caseEventID() { return _caseEventID; }

    public void set_caseEventID(String caseEventID) { _caseEventID = caseEventID; }


    public String get_caseID() { return _caseID; }

    public void set_caseID(String caseID) { _caseID = caseID; }


    public long get_eventTime() { return _eventTime; }

    public void set_eventTime(long eventTime) { _eventTime = eventTime; }


    public String get_eventName() { return _eventName; }

    public void set_eventName(String eventType) { _eventName = eventType; }


    public String get_resourceID() { return _resourceID; }

    public void set_resourceID(String resourceID) { _resourceID = resourceID; }


    public String get_specID() { return _specID; }

    public void set_specID(String specID) { _specID = specID; }


    public String get_subnetID() { return _subnetID; }

    public void set_subnetID(String subnetID) { _subnetID = subnetID; }

}