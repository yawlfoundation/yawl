/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Hashtable;

/**
 * A 'stringified' record of a workitem for passing across various HTTP interfaces
 *
 * @author Lachlan Aldred
 * Date: 2/02/2004
 * Time: 18:30:18
 *
 * Extended & refactored for version 2.0 by Michael Adams
 * Last Date: 27/05/2008
 * 
 */

public class WorkItemRecord implements Cloneable {

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
    private long _id;                                    // hibernate primary key
    private String _specificationID;
    private String _specVersion = "0.1" ;
    private String _caseID;
    private String _taskID;
    private String _uniqueID;                            // used by PDF Forms service
    private String _allowsDynamicCreation;
    private String _requiresManualResourcing;
    private String _codelet;

    // task/decomp level attribs
    private Hashtable<String, String> _attributeTable;
    private String _extendedAttributes = "";

    // identifies this item as a member of a group of deferred choice items
    private String _deferredChoiceGroupID = null;

    // life-cycle time stamps
    private String _enablementTime;
    private String _firingTime;
    private String _startTime;
    private String _completionTime;

    // ... and their millisecond equivalents
    private String _enablementTimeMs;
    private String _firingTimeMs;
    private String _startTimeMs ;
    private String _completionTimeMs ;

    // timer values (if item has a timer enabled)
    private String _timerTrigger;
    private String _timerExpiry;

    // current statuses
    private String _status;
    private String _resourceStatus = statusResourceUnresourced;

    // who performed the workitem
    private String _startedBy;
    private String _completedBy ;

    // initial data params and values
    private Element _dataList;
    private String _dataListString;

    // interim data store - for use by custome services for temp storage
    private Element _dataListUpdated ;
    private String _dataListUpdatedString ;

    private String _customFormURL ;                         // path to alternate jsp

    private boolean _edited ;                       // for use on custom service side

    private String _tag;                                   // for user-defined values

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
    public WorkItemRecord(long id, String specID, String specVersion, String caseID, 
                          String taskID, String uniqueID, String enablementTime,
                          String firingTime, String startTime, String completionTime,
                          String status, String resourceStatus, String startedBy,
                          String completedBy, String dataListString ) {
        _id = id;
        _specificationID = specID;
        _specVersion = specVersion ;
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

    public void restoreDataList() {
        if (_dataListString != null)
            _dataList = JDOMUtil.stringToElement(_dataListString) ;
        if (_dataListUpdatedString != null)
            _dataListUpdated = JDOMUtil.stringToElement(_dataListUpdatedString) ;
    }

    public void resetDataState() {
        _dataListUpdatedString = null;
        _dataListUpdated = null ; 
    }

    /********************************************************************************/

    // SETTERS //

    public void setSpecificationID(String specID) { _specificationID = specID; }

    public void setSpecVersion(String version) { _specVersion = version; }

    public void setCaseID(String caseID) {_caseID = caseID; }

    public void setTaskID(String taskID) { _taskID = taskID; }

    public void setUniqueID(String uniqueID) { _uniqueID = uniqueID;  }

    public void setAllowsDynamicCreation(String allows) {
        _allowsDynamicCreation = allows ;
    }

    public void setRequiresManualResourcing(String manual) {
        _requiresManualResourcing = manual;
    }

    public void setCodelet(String codelet) { _codelet = codelet; }

    public void setDeferredChoiceGroupID(String id) { _deferredChoiceGroupID = id ; }

    public void setExtendedAttributes(Hashtable<String, String> attribs) {
        _attributeTable = attribs ;
        _extendedAttributes = attributeTableToXMLString() ;
    }

    public void setExtendedAttributes(String attribStr) {
        _extendedAttributes = attribStr ;
        _attributeTable = attributeStringToTable();
    }

    public void setEnablementTime(String time) { _enablementTime = time; }

    public void setFiringTime(String time) { _firingTime = time; }

    public void setStartTime(String time) { _startTime = time; }

    public void setCompletionTime(String time) { _completionTime = time; }

    public void setEnablementTimeMs(String time) { _enablementTimeMs = time; }

    public void setFiringTimeMs(String time) { _firingTimeMs = time; }

    public void setStartTimeMs(String time) { _startTimeMs = time; }

    public void setCompletionTimeMs(String time) {_completionTimeMs = time; }

    public void setTimerTrigger(String trigger) { _timerTrigger = trigger; }

    public void setTimerExpiry(String expiry) { _timerExpiry = expiry; }
    

    public void setStatus(String status) {_status = status; }
    
    public void setResourceStatus(String status) {_resourceStatus = status; }

    /** @deprecated */
    public void setAssignedTo(String whoStartedMe) { setStartedBy(whoStartedMe) ; }

    public void setStartedBy(String resource) { _startedBy = resource; }

    public void setCompletedBy(String resource) { _completedBy = resource; }


    public void setDataList(Element dataList) {
        _dataList = dataList;
        _dataListString = JDOMUtil.elementToStringDump(_dataList) ;
    }

    public void setDataListString(String dataStr) {
        _dataListString = dataStr ;
        _dataList = JDOMUtil.stringToElement(dataStr) ;
    }

    public void setTag(String tag) { _tag = tag ; }

    public void setEdited(boolean edited) { _edited = edited ; }

    public void setUpdatedData(Element dataListUpdated) {
        _dataListUpdated = dataListUpdated;
        _dataListUpdatedString = JDOMUtil.elementToString(dataListUpdated);
    }

    public void setCustomFormURL(String url) { _customFormURL = url ; }


    /********************************************************************************/

    // GETTERS //

    public String getSpecificationID() { return _specificationID; }

    public String getSpecVersion() { return _specVersion ; }

    public String getCaseID() { return _caseID; }

    public String getTaskID() { return _taskID; }

    public String getUniqueID() { return _uniqueID; }

    public String getAllowsDynamicCreation() { return _allowsDynamicCreation ; }

    public String getDeferredChoiceGroupID() { return _deferredChoiceGroupID ; }

    public String getRequiresManualResourcing() { return _requiresManualResourcing; }

    public String getCodelet() { return _codelet; }

    public String getExtendedAttributes() { return _extendedAttributes ; }

    public Hashtable<String, String> getAttributeTable() {
        if ((_extendedAttributes.length() > 0) && (_attributeTable == null))
            _attributeTable = attributeStringToTable();
        return _attributeTable;
    }

    public String getID() { return _caseID + ":" + _taskID; }

    public String getEnablementTime() { return _enablementTime; }

    public String getFiringTime() { return _firingTime; }

    public String getStartTime() { return _startTime; }

    public String getCompletionTime() { return _completionTime; }

    public String getEnablementTimeMs() { return _enablementTimeMs; }

    public String getFiringTimeMs() { return _firingTimeMs; }

    public String getStartTimeMs() { return _startTimeMs; }

    public String getCompletionTimeMs() { return _completionTimeMs; }

    public String getTimerTrigger() { return _timerTrigger; }

    public String getTimerExpiry() { return _timerExpiry; }

    public String getStatus() { return _status; }

    public String getResourceStatus() { return _resourceStatus; }


    /** @deprecated - use getStartedBy() */
    public String getAssignedTo() { return getStartedBy(); }

    /** @deprecated - use getStartedBy() */
    public String getWhoStartedMe() { return getStartedBy(); }

    public String getStartedBy() { return _startedBy; }

    public String getCompletedBy() { return _completedBy; }


    /** @deprecated - use getDataList() */
    public Element getWorkItemData() { return getDataList(); }

    public Element getDataList() { return _dataList; }

    public String getDataListString() { return _dataListString; }

    public String getTag() { return _tag ; }

    public Element getUpdatedData() { return _dataListUpdated; }

    public String getIDForDisplay() {
        return _caseID + ":" + getTaskIDForDisplay() ;
    }

    public String getTaskIDForDisplay() {
        String exSuffix = getTaskName();
        return exSuffix.replaceAll("_", " ");
    }

    public String getTaskName() {
        String result;
        char c = _taskID.charAt(0);
        if ((c >= '0') && (c <= '9')) {                        // taskid startswith digit
            result = _taskID.substring(_taskID.indexOf("_") + 1);        // pre b7.1 task
        }
        else {
            result = _taskID.substring(0, _taskID.lastIndexOf("_"));     // post b7.1
        }
        return result;
    }

    // returns the case id of the root ancestor case
    public String getRootCaseID() {
        String result = _caseID;
        if ((_caseID != null) && (_caseID.indexOf(".") > 0)) {
            result = _caseID.split("\\.")[0] ;
        }
        return result ;
    }

    public boolean isEdited() { return _edited; }

    public boolean isDeferredChoiceGroupMember() {
        return (_deferredChoiceGroupID != null) ;
    }

    public String getCustomFormURL() { return _customFormURL; }

    public boolean hasLiveStatus() {
        return _status.equals(statusFired) || _status.equals(statusEnabled) ||
               _status.equals(statusExecuting);
    }

    public boolean hasStatus(String status) {
        return (_status != null) && _status.equals(status);
    }

    public boolean hasResourceStatus(String status) {
        return (_resourceStatus != null) && _resourceStatus.equals(status);
    }

    
    /********************************************************************************/

    public String toXML() {
        StringBuilder xml = new StringBuilder("<workItemRecord");
        xml.append(_extendedAttributes != null? _extendedAttributes : "")
           .append(">")
           .append(StringUtil.wrap(getID(), "id"))
           .append(StringUtil.wrap(_specificationID, "specid"))                
           .append(StringUtil.wrap(_specVersion, "specversion"))
           .append(StringUtil.wrap(_caseID, "caseid"))
           .append(StringUtil.wrap(_taskID, "taskid"))
           .append(StringUtil.wrap(_uniqueID, "uniqueid"))
           .append(StringUtil.wrap(_allowsDynamicCreation, "allowsdynamiccreation"))
           .append(StringUtil.wrap(_requiresManualResourcing, "requiresmanualresourcing"))
           .append(StringUtil.wrap(_codelet, "codelet"))
           .append(StringUtil.wrap(_deferredChoiceGroupID, "deferredChoiceGroupid"))
           .append(StringUtil.wrap(_enablementTime, "enablementTime"))
           .append(StringUtil.wrap(_firingTime, "firingTime"))
           .append(StringUtil.wrap(_startTime, "startTime"))
           .append(StringUtil.wrap(_completionTime, "completionTime"))
           .append(StringUtil.wrap(_enablementTimeMs, "enablementTimeMs"))
           .append(StringUtil.wrap(_firingTimeMs, "firingTimeMs"))
           .append(StringUtil.wrap(_startTimeMs, "startTimeMs"))
           .append(StringUtil.wrap(_completionTimeMs, "completionTimeMs"))
           .append(StringUtil.wrap(_timerTrigger, "timertrigger"))
           .append(StringUtil.wrap(_timerExpiry, "timerexpiry"))
           .append(StringUtil.wrap(_status, "status"))
           .append(StringUtil.wrap(_resourceStatus, "resourceStatus"))
           .append(StringUtil.wrap(_startedBy, "startedBy"))
           .append(StringUtil.wrap(_completedBy, "completedBy"))
           .append(StringUtil.wrap(String.valueOf(_edited), "edited"))
           .append(StringUtil.wrap(String.valueOf(_tag), "tag"))
           .append(StringUtil.wrap(_customFormURL, "customform"))
           .append("<data>")
           .append(_dataList != null? JDOMUtil.elementToStringDump(_dataList) : "")
           .append("</data>")
           .append("</workItemRecord>");
        return xml.toString() ;
    }


    private String attributeTableToXMLString() {
        if ((_attributeTable == null) || _attributeTable.isEmpty()) return "" ;
        
        StringBuilder xml = new StringBuilder();
        for (String key : _attributeTable.keySet()) {
            xml.append(" ")
               .append(key)
               .append("=\"")
               .append(_attributeTable.get(key))
               .append("\"");
        }
        return xml.toString();
    }


    private Hashtable<String, String> attributeStringToTable() {
        if ((_extendedAttributes == null) || (_extendedAttributes.length() == 0))
            return null;

        Hashtable<String, String> result = new Hashtable<String, String>();
        String attribStr = _extendedAttributes.trim() ;
        String[] attribs = attribStr.split(" ");
        for (int i = 0; i < attribs.length; i++) {
            String[] attrib = attribs[0].split("=");
            result.put(attrib[0], attrib[1]);
        }
        return result;
    }


    /*******************************************************************************/

    public WorkItemRecord clone() throws CloneNotSupportedException {
        return (WorkItemRecord) super.clone() ;
    }

 
    /********************************************************************************/

    // hibernate mappings

    private void set_dataList(String data) { _dataListString = data; }

    private String get_dataList() { return _dataListString ; }

    private void set_dataListUpdated(String data) { _dataListUpdatedString = data; }

    private String get_dataListUpdated() { return _dataListUpdatedString ; }

    private void set_id(long id) { _id = id; }

    private long get_id() { return _id; }

    /********************************************************************************/

}
