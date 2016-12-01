/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.interfce;

import org.jdom2.Element;
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
    public static final String statusDiscarded = "Discarded";

    // workitem resourcing statuses
    public final static String statusResourceOffered = "Offered" ;
    public final static String statusResourceAllocated = "Allocated" ;
    public final static String statusResourceStarted = "Started" ;
    public final static String statusResourceSuspended = "Suspended" ;
    public final static String statusResourceUnoffered = "Unoffered" ;
    public final static String statusResourceUnresourced = "Unresourced" ;

    // item identifiers
    private long _id;                                    // hibernate primary key
    private String _specIdentifier;
    private String _specVersion = "0.1" ;
    private String _specURI;
    private String _caseID;
    private String _taskID;
    private String _uniqueID;                            // used by PDF Forms service
    private String _taskName;                            // the unmodified task name
    private String _documentation;
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
    private String _completedBy;

    // initial data params and values
    private Element _dataList;
    private String _dataListString;

    // interim data store - for use by custom services for temp storage
    private Element _dataListUpdated;
    private String _dataListUpdatedString;

    // configurable logging predicates
    private String _logPredicateStarted;
    private String _logPredicateCompletion;

    private String _customFormURL;                         // path to alternate jsp

    private boolean _docoChanged = false;                  // documentation updated?

    private String _tag;                                   // for user-defined values

    /********************************************************************************/

    // CONSTRUCTORS //

    public WorkItemRecord() {}                     // for reflection

    // called by Marshaller.unmarshallWorkItem
    public WorkItemRecord(String caseID, String taskID, String specURI,
                          String enablementTime, String status) {
        _taskID = taskID;
        _caseID = caseID;
        _specURI = specURI;
        _enablementTime = enablementTime;
        _status = status;
    }

    public void restoreDataList() {
        if (_dataListString != null)
            _dataList = JDOMUtil.stringToElement(_dataListString) ;
        if (_dataListUpdatedString != null)
            _dataListUpdated = JDOMUtil.stringToElement(_dataListUpdatedString) ;
    }

    public void restoreAttributeTable() {
        _attributeTable = attributeStringToTable();
    }

    public void resetDataState() {
        _dataListUpdatedString = null;
        _dataListUpdated = null ; 
    }

    /********************************************************************************/

    // SETTERS //

    public void setSpecIdentifier(String id) { _specIdentifier = id; }

    public void setSpecVersion(String version) { _specVersion = version; }

    public void setSpecURI(String uri) { _specURI = uri; }

    public void setCaseID(String caseID) {_caseID = caseID; }

    public void setTaskID(String taskID) { _taskID = taskID; }

    public void setUniqueID(String uniqueID) { _uniqueID = uniqueID;  }

    public void setTaskName(String name) { _taskName = name;  }

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
        _extendedAttributes = attributeTableToAttributeString() ;
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

    public void setUpdatedData(Element dataListUpdated) {
        _dataListUpdated = dataListUpdated;
        _dataListUpdatedString = JDOMUtil.elementToString(dataListUpdated);
    }

    public void setCustomFormURL(String url) { _customFormURL = url ; }

    public void setLogPredicateStarted(String predicate) {
        _logPredicateStarted = predicate;
    }

    public void setLogPredicateCompletion(String predicate) {
        _logPredicateCompletion = predicate;
    }

    public void setDocumentation(String doco) {
        _documentation = doco;
    }

    public void setDocumentationChanged(boolean added) {
        _docoChanged = added;
    }

    /********************************************************************************/

    // GETTERS //

    public String getSpecIdentifier() { return _specIdentifier; }

    public String getSpecVersion() { return _specVersion ; }

    public String getSpecURI() { return _specURI; }

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

    public Element getDataList() {
        if (_dataList == null) _dataList = JDOMUtil.stringToElement(_dataListString);
        return _dataList;
    }

    public String getDataListString() { return _dataListString; }

    public String getTag() { return _tag ; }

    public Element getUpdatedData() {
        if (_dataListUpdated == null) {
            _dataListUpdated = JDOMUtil.stringToElement(_dataListUpdatedString);
        }
        return _dataListUpdated;
    }

    public String getIDForDisplay() {
        return _caseID + ":" + _taskName ;
    }

    public String getTaskName() { return _taskName; }

    public String getDocumentation() { return _documentation; }

    public boolean hasDocumentation() { return _documentation != null; }

    public boolean isDocumentationChanged() { return _docoChanged; }

    // returns the case id of the root ancestor case
    public String getRootCaseID() {
        if (_caseID != null) {
            int firstDot = _caseID.indexOf('.');
            return (firstDot > -1) ? _caseID.substring(0, firstDot) : _caseID;
        }
        return null;
    }

    public String getNetID() {
        if (_status.equals(statusIsParent)) {
            return _caseID;
        }
        if (_caseID != null) {
            int pos = _caseID.lastIndexOf('.');
            return (pos < 0) ? _caseID : _caseID.substring(0, pos);
        }
        return null;
    }

    public String getParentID() {
        if (isEnabledOrFired()) return null;
        int pos = _caseID.lastIndexOf('.');        
        return (pos < 0) ? null : _caseID.substring(0, pos) + ":" + _taskID;
    }

    public String getLogPredicateStarted() { return _logPredicateStarted; }

    public String getLogPredicateCompletion() { return _logPredicateCompletion; }


    public boolean isEdited() { return (_dataListUpdated != null); }

    public boolean isDeferredChoiceGroupMember() {
        return (_deferredChoiceGroupID != null) ;
    }

    public boolean isAutoTask() {
        return ((getRequiresManualResourcing() != null) &&
                (getRequiresManualResourcing().equalsIgnoreCase("false")));
    }


    public String getCustomFormURL() { return _customFormURL; }

    public boolean hasLiveStatus() {
        return isEnabledOrFired() || _status.equals(statusExecuting);
    }

    public boolean isEnabledOrFired() {
        return _status.equals(statusEnabled) || _status.equals(statusFired);
    }

    public boolean hasStatus(String status) {
        return (_status != null) && _status.equals(status);
    }

    public boolean hasResourceStatus(String status) {
        return (_resourceStatus != null) && _resourceStatus.equals(status);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof WorkItemRecord) {
            WorkItemRecord other = (WorkItemRecord) o;
            return getID().equals(other.getID()) &&
                 _status.equals(other.getStatus()) &&
                 _uniqueID != null ? _uniqueID.equals(other._uniqueID) :
                    other._uniqueID == null;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = _caseID.hashCode();
        result = 31 * result + _taskID.hashCode();
        result = 31 * result + _status.hashCode();
        result = 31 * result + (_uniqueID != null ? _uniqueID.hashCode() : 0);
        return result;
    }

    /********************************************************************************/

    public String toString() {
        return getID();
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<workItemRecord");
        xml.append(_extendedAttributes != null? _extendedAttributes : "")
           .append(">")
           .append(StringUtil.wrap(getID(), "id"))
           .append(StringUtil.wrap(_specVersion, "specversion"))
           .append(StringUtil.wrap(_specURI, "specuri"))
           .append(StringUtil.wrap(_caseID, "caseid"))
           .append(StringUtil.wrap(_taskID, "taskid"))
           .append(StringUtil.wrap(_uniqueID, "uniqueid"))
           .append(StringUtil.wrap(_taskName, "taskname"));
        if (_documentation != null) xml.append(StringUtil.wrap(_documentation, "documentation"));
        xml.append(StringUtil.wrap(_allowsDynamicCreation, "allowsdynamiccreation"))
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
           .append(StringUtil.wrap(_tag, "tag"))
           .append(StringUtil.wrap(_customFormURL, "customform"))
           .append(StringUtil.wrap(_logPredicateStarted, "logPredicateStarted"))
           .append(StringUtil.wrap(_logPredicateCompletion, "logPredicateCompletion"));

        if (_specIdentifier != null)
            xml.append(StringUtil.wrap(_specIdentifier, "specidentifier"));

        xml.append("<data>")
           .append(_dataList != null? JDOMUtil.elementToStringDump(_dataList) : "")
           .append("</data>")
           .append("<updateddata>")
           .append(_dataListUpdated != null? JDOMUtil.elementToStringDump(_dataListUpdated) : "")
           .append("</updateddata>")
           .append("</workItemRecord>");

        return xml.toString() ;
    }


    /**************************************************************************/


    private String attributeTableToAttributeString() {
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

        Hashtable<String, String> table = new Hashtable<String, String>();

        // split into key, value, key, value, ...
        String[] attributes = _extendedAttributes.split("\\s*=\\s*\"|\\s*\"\\s*");
        for (int i=0; i < attributes.length - 1; i=i+2) {
            table.put(attributes[i].trim(), attributes[i+1].trim());
        }
        return table;
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
