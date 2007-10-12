/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */


package au.edu.qut.yawl.worklist.model;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import au.edu.qut.yawl.util.JDOMConversionTools;

/**
 * A 'stringified' record of a workitem for passing across various HTTP interfaces
 *
 * @author Lachlan Aldred
 * Date: 2/02/2004
 * Time: 18:30:18
 *
 * Last Date: 14/09/2007 extended for version 1.0 MJA
 * 
 */

public class WorkItemRecord {

    // workitem execution statuses
    public static final String statusEnabled = "Enabled";
    public static final String statusFired = "Fired";
    public static final String statusExecuting = "Executing";
    public static final String statusComplete = "Complete";
    public static final String statusIsParent = "Is parent";
    public static final String statusDeadlocked = "Deadlocked";
    public static final String statusForcedComplete = "ForcedComplete";
    public static final String statusFailed = "Failed";
    public static final String statusSuspended = "Suspended";

    // workitem resourcing statuses
    public final static String statusResourceOffered = "Offered" ;
    public final static String statusResourceAllocated = "Allocated" ;
    public final static String statusResourceStarted = "Started" ;
    public final static String statusResourceSuspended = "Suspended" ;
    public final static String statusResourceUnoffered = "Unoffered" ;
    public final static String statusResourceUnresourced = "Unresourced" ;

    // item identifiers
    private long _id;                                  // hibernate primary key
    private String _specificationID;
    private String _caseID;
    private String _taskID;
    private String _uniqueID;                            // used by PDF Forms service

    // life-cycle time stamps
    private String _enablementTime;
    private String _firingTime;
    private String _startTime;
    private String _completionTime;

    // current statuses
    private String _status;
    private String _resourceStatus ;

    // who performed the workitem
    private String _startedBy;
    private String _completedBy ;

    private Element _dataList;
    private String _dataListString;

    private String _tag;                                   // for user-defined values

    private XMLOutputter outPretty = new XMLOutputter(Format.getPrettyFormat());
    private XMLOutputter outCompact = new XMLOutputter(Format.getCompactFormat());

    /********************************************************************************/

    // CONSTRUCTORS //

    public WorkItemRecord() {}                     // for reflection

    // called by Marshaller.unmarshallWorkItem
    public WorkItemRecord(String caseID, String taskID, String specificationID,
                          String enablementTime, String status) {
        _taskID = taskID;
        _caseID = caseID;
        _specificationID = specificationID;
        _enablementTime = enablementTime;
        _status = status;
    }

    // full Constructor called by resourceService restore methods
    public WorkItemRecord(long id, String specID, String caseID, String taskID,
                          String uniqueID, String enablementTime, String firingTime,
                          String startTime, String completionTime, String status,
                          String resourceStatus, String startedBy, String completedBy,
                          String dataListString ) {
        _id = id;
        _specificationID = specID;
        _caseID = caseID;
        _taskID = taskID;
        _uniqueID = uniqueID;
        _enablementTime = enablementTime;
        _firingTime = firingTime;
        _startTime = startTime;
        _completionTime = completionTime;
        _status = status;
        _resourceStatus = resourceStatus;
        _startedBy = startedBy;
        _completedBy = completedBy;
        _dataListString = dataListString;
    }


    /********************************************************************************/

    // SETTERS //

    public void setSpecificationID(String specID) { _specificationID = specID; }

    public void setCaseID(String caseID) {_caseID = caseID; }

    public void setTaskID(String taskID) { _taskID = taskID; }

    public void setUniqueID(String uniqueID) { _uniqueID = uniqueID;  }


    public void setEnablementTime(String time) { _enablementTime = time; }

    public void setFiringTime(String time) { _firingTime = time; }

    public void setStartTime(String time) { _startTime = time; }

    public void setCompletionTime(String time) { _completionTime = time; }


    public void setStatus(String status) {_status = status; }
    
    public void setResourceStatus(String status) {_resourceStatus = status; }

    /** @deprecated */
    public void setAssignedTo(String whoStartedMe) { setStartedBy(whoStartedMe) ; }

    public void setStartedBy(String resource) { _startedBy = resource; }

    public void setCompletedBy(String resource) { _completedBy = resource; }


    public void setDataList(Element dataList) {
        _dataList = dataList;
        _dataListString = outCompact.outputString(_dataList) ;
    }

    public void setDataListString(String dataStr) {
        _dataListString = dataStr ;
        _dataList = JDOMConversionTools.stringToElement(dataStr) ;
    }

    public void setTag(String tag) { _tag = tag ; }


    /********************************************************************************/

    // GETTERS //

    public String getSpecificationID() { return _specificationID; }

    public String getCaseID() { return _caseID; }

    public String getTaskID() { return _taskID; }

    public String getUniqueID() { return _uniqueID; }


    public String getID() { return _caseID + ":" + _taskID; }

    public String getEnablementTime() { return _enablementTime; }

    public String getFiringTime() { return _firingTime; }

    public String getStartTime() { return _startTime; }

    public String getCompletionTime() { return _completionTime; }


    public String getStatus() { return _status; }

    public String getResourceStatus() { return _resourceStatus; }


    /** @deprecated */
    public String getAssignedTo() { return getStartedBy(); }

    /** @deprecated */
    public String getWhoStartedMe() { return getStartedBy(); }

    public String getStartedBy() { return _startedBy; }

    public String getCompletedBy() { return _completedBy; }


    /** @deprecated */
    public Element getWorkItemData() { return getDataList(); }

    public Element getDataList() { return _dataList; }

    public String getDataListString() { return _dataListString; }

    public String getTag() { return _tag ; }

    public boolean hasLiveStatus() {
        return _status.equals(statusFired) || _status.equals(statusEnabled) ||
               _status.equals(statusExecuting);
    }

    /********************************************************************************/

    public String toXML() {
        StringBuilder xml = new StringBuilder("<itemRecord>");
        xml.append(wrap(getID(), "id"))
           .append(wrap(_specificationID, "specid"))
           .append(wrap(_caseID, "caseid"))
           .append(wrap(_taskID, "taskid"))
           .append(wrap(_caseID, "caseid"))
           .append(wrap(_uniqueID, "uniqueID"))
           .append(wrap(_enablementTime, "enablementTime"))
           .append(wrap(_firingTime, "firingTime"))
           .append(wrap(_startTime, "startTime"))
           .append(wrap(_completionTime, "completionTime"))
           .append(wrap(_status, "status"))
           .append(wrap(_resourceStatus, "resourceStatus"))
           .append(wrap(_startedBy, "startedBy"))
           .append(wrap(_completedBy, "completedBy"))
           .append("<data>")
           .append(_dataList != null? outCompact.outputString(_dataList) : "")
           .append("</data>")
           .append("</itemRecord>");
        return xml.toString() ;
    }


    private String wrap(String core, String tag) {
        return String.format("<%s>%s</%s>", tag, core, tag) ;
    }
 
    /********************************************************************************/

    // hibernate mapping

    private void set_dataList(String data) { _dataListString = data; }

    private String get_dataList() { return _dataListString ; }

    private void set_id(long id) { _id = id; }

    private long get_id() { return _id; }
}
