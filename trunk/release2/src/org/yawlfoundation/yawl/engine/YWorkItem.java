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

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.*;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.yawlfoundation.yawl.engine.YWorkItemStatus.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/05/2003
 * Time: 15:29:33
 *
 * Refactored for v2.0 by Michael Adams 10/2007 - 12/2009
 * 
 */
public class YWorkItem {

    private static DateFormat _df = new SimpleDateFormat("MMM:dd, yyyy H:mm:ss");
    private static YEngine _engine = YEngine.getInstance();
    private YWorkItemRepository _workItemRepository = _engine.getWorkItemRepository();
    private YWorkItemID _workItemID;
    private String _thisID = null;
    private YSpecificationID _specID;
    private YTask _task;                                // task item is derived from
    private Date _enablementTime;
    private Date _firingTime;
    private Date _startTime;

    private YAttributeMap _attributes;                    // decomposition attributes

    private YWorkItemStatus _status;
    private YWorkItemStatus _prevStatus = null;       // this item's next to last status
    private YClient _externalClient;                     // the 'started by' service/app
    private String _externalClientStr;                   // for persistence
    private boolean _allowsDynamicCreation;
    private boolean _requiresManualResourcing;
    private YWorkItem _parent;                            // this item's parent (if any)
    private Set<YWorkItem> _children;                     // this item's kids (if any)
    private Element _dataList;
    private String _dataString = null;                  // persisted version of datalist

    private String _deferredChoiceGroupID = null ;

    private YTimerParameters _timerParameters ;                      // timer extensions
    private boolean _timerStarted ;
    private long _timerExpiry = 0;                     // set to expiry when timer starts

    private URL _customFormURL ;
    private String _codelet ;
    private String _documentation;
    private String _externalLogPredicate;                 // set by services on checkin


    private final YEventLogger _eventLog = YEventLogger.getInstance();
    private final Logger _log = Logger.getLogger(YWorkItem.class);


    // CONSTRUCTORS //

    public YWorkItem() {}                                  // required for persistence


    /** Creates an enabled WorkItem */
    public YWorkItem(YPersistenceManager pmgr, YSpecificationID specID, YTask task,
                     YWorkItemID workItemID, boolean allowsDynamicCreation,
                     boolean isDeadlocked) throws YPersistenceException {

        _log.debug("Spec =" + specID + " WorkItem =" + workItemID.getTaskID());

        createWorkItem(specID, workItemID, isDeadlocked ? statusDeadlocked : statusEnabled,
                       allowsDynamicCreation); 

        _task = task;
        if (task != null) _documentation = task.getDocumentationPreParsed(); 
        _enablementTime = new Date();
        _eventLog.logWorkItemEvent(pmgr, this, _status, null);
        if ((pmgr != null) && (! isDeadlocked)) pmgr.storeObject(this);
    }


    /** Creates a fired WorkItem */
    private YWorkItem(YPersistenceManager pmgr, YWorkItemID workItemID,
                      YSpecificationID specID, Date workItemCreationTime, YWorkItem parent,
                      boolean allowsDynamicInstanceCreation)
            throws YPersistenceException {

        _log.debug("Spec =" + specID + " WorkItem =" + workItemID.getTaskID());

        createWorkItem(specID, workItemID, statusFired, allowsDynamicInstanceCreation);

        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        _parent = parent;
        _eventLog.logWorkItemEvent(pmgr, this, _status, createLogDataList("fired"));
        if (pmgr != null) pmgr.storeObject(this);
    }


    /********************************************************************************/

    // PRIVATE METHODS //

    /** Called from constructors to set some mutual members */
    private void createWorkItem(YSpecificationID specificationID,
                                YWorkItemID workItemID, YWorkItemStatus status,
                                boolean allowsDynamicInstanceCreation)
                                throws YPersistenceException {
        _workItemID = workItemID;
        addToRepository();
        set_thisID(_workItemID.toString() + "!" + _workItemID.getUniqueID());
        _specID = specificationID;
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        _status = status ;
    }


    /** completes persisting and event logging for a workitem */
    private void completePersistence(YPersistenceManager pmgr,
                                     YWorkItemStatus completionStatus)
            throws YPersistenceException {

        // make sure we can complete this workitem
        if (!(_status.equals(statusExecuting) || _status.equals(statusSuspended))) {
            throw new RuntimeException(this + " [when current status is \""
                   + _status + "\" it cannot be moved to \"" + completionStatus + "\"]");
        }

        // set final status, log event and remove from persistence
        set_status(null, completionStatus);              // don't persist status update
        logAndUnpersist(pmgr, this);
        completeParentPersistence(pmgr) ;
    }


    /** completes persisting and event logging for a parent workitem if required */
    private void completeParentPersistence(YPersistenceManager pmgr)
            throws YPersistenceException {

        synchronized(_parent) {                      // sequentially handle children

            // if all siblings are completed, then the parent is completed too
            boolean parentComplete = true;

            if (_parent.getChildren().size() >  1) {  // short-circuit if not multi-task
                for (YWorkItem mysibling : _parent.getChildren()) {
                    if (mysibling.hasUnfinishedStatus()) {
                        parentComplete = false;
                        break;
                    }
                }
            }

            if (parentComplete) logAndUnpersist(pmgr, _parent);
        }
    }


    private void logAndUnpersist(YPersistenceManager pmgr, YWorkItem item)
            throws YPersistenceException {
        _eventLog.logWorkItemEvent(pmgr, item, _status, createLogDataList(_status.name()));
        if (pmgr != null) pmgr.deleteObject(item);
    }


    /**
     * Finds the net-level param specified, then deconstructs its data to simple
     * timer parameters. The data in the net-level param is a complex type YTimerType
     * consisting of two elements: 'trigger' (either 'OnEnabled' or 'OnExecuting'), and
     * 'expiry': a string that may represent a duration type, a dateTime type, or a long
     * value to be converted to a Date.
     * @param param the name of the YTimerType parameter
     * @param data the case or net-level data object
     * @return true if the param is successfully unpacked.
     */
    private boolean unpackTimerParams(String param, YNetData data) {
        if (data == null) data = _engine.getCaseData(_workItemID.getCaseID());
        if (data == null) return false ;                    // couldn't get case data

        Element eData = JDOMUtil.stringToElement(data.getData());
        Element timerParams = eData.getChild(param) ;
        if (timerParams == null) return false ;            // no var with param's name

        try {
            _timerParameters.parseYTimerType(timerParams);
            return true;
        }
        catch (IllegalArgumentException iae) {
            _log.warn("Unable to set timer for workitem '" + getIDString() +
                      "' - " + iae.getMessage()) ;
            return false ;

        }
    }


    /*****************************************************************************/

    // MISC METHODS //

    public void addToRepository() {
        _workItemRepository.add(this);
        _engine.getInstanceCache().addWorkItem(this);
    }


    public YWorkItem createChild(YPersistenceManager pmgr, YIdentifier childCaseID)
           throws YPersistenceException {
        if (this._parent == null) {

            // don't proceed if child caseid is invalid
            YIdentifier parentCaseID = getWorkItemID().getCaseID();
            if ((childCaseID == null) || (childCaseID.getParent() == null) ||
                    (! childCaseID.getParent().equals(parentCaseID))) return null;

            set_status(pmgr, statusIsParent);

            // if this parent has no children yet, create the set and log it
            if (_children == null) {
                _children = new HashSet<YWorkItem>();
                _eventLog.logWorkItemEvent(pmgr, this, _status, createLogDataList("createChild"));
            }

            YWorkItem childItem = new YWorkItem(pmgr,
                    new YWorkItemID(childCaseID, getWorkItemID().getTaskID()),
                    _specID, getEnablementTime(), this, _allowsDynamicCreation);

            // map relevant (genetic, perhaps?) attributes to child
            childItem.setTask(getTask());
            childItem.setRequiresManualResourcing(requiresManualResourcing());
            childItem.setAttributes(getAttributes());
            childItem.setTimerParameters(getTimerParameters());
            childItem.setCustomFormURL(getCustomFormURL());
            childItem.setCodelet(getCodelet());

            _children.add(childItem);
            if (pmgr != null) pmgr.updateObject(this);
            return childItem;
        }
        return null;
    }

    // set by custom service on checkin, and immediately before workitem completes 
    public void setExternalLogPredicate(String predicate) {
        _externalLogPredicate = predicate;
    }

    /** write data input values to event log */
    public void setData(YPersistenceManager pmgr, Element data)
            throws YPersistenceException {
        _dataList = data;
        _dataString = getDataString();

        if (pmgr != null) pmgr.updateObject(this);

        YLogDataItemList logData = assembleLogDataItemList(data, true);
        _eventLog.logDataEvent(pmgr, this, "DataValueChange", logData);
    }


    /** write output data values to event log */
    public void completeData(YPersistenceManager pmgr, Document output) {
        YLogDataItemList logData = assembleLogDataItemList(output.getRootElement(), false);
        _eventLog.logDataEvent(pmgr, this, "DataValueChange", logData);
    }


    private YLogDataItemList assembleLogDataItemList(Element data, boolean input) {
        YLogDataItemList result = new YLogDataItemList();
        if (data != null) {
            Map<String, YParameter> params =
                    _engine.getParameters(_specID, getTaskID(), input) ;
            String descriptor = (input ? "Input" : "Output") + "VarAssignment";
            for (Element child : data.getChildren()) {
                String name = child.getName();
                String value = child.getValue();
                YParameter param = params.get(name);
                if (param != null) {
                    String dataType = param.getDataTypeNameUnprefixed();

                    // if a complex type, store the structure with the value
                    if (child.getContentSize() > 1) {
                        value = JDOMUtil.elementToString(child);
                    }
                    result.add(new YLogDataItem(descriptor, name, value, dataType));

                    // add any configurable logging predicates for this parameter
                    YLogDataItem dataItem = getDataLogPredicate(param, input);
                    if (dataItem != null) result.add(dataItem);
                }    
            }
        }
        return result;        
    }

    public void restoreDataToNet(Set<YAWLServiceReference> services) throws YPersistenceException {
        if (getDataString() != null) {
            YNet net;
            try {
                net = _engine.getNetRunner(getCaseID().getParent()).getNet();
            }
            catch (Exception e) {
                return;
            }
            YAtomicTask task = (YAtomicTask) net.getNetElement(getTaskID());
            if (task != null) {
                try {
                    task.prepareDataForInstanceStarting(getCaseID());
                    net.addNetElement(task);
                }
                catch (Exception e) {
                    throw new YPersistenceException(e);
                }
            }
            if (_externalClientStr != null) {
                if (_externalClientStr.equals("DefaultWorklist")) {
                    _externalClient = _engine.getDefaultWorklist();
                }
                else {
                    for (YAWLServiceReference service : services) {
                        if (service.getServiceName().equals(_externalClientStr)) {
                            _externalClient = service;
                            break;
                        }
                    }
                }
            }
        }
    }


    /** removes workitems from persistence when cancelled **/
    public void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        if (pmgr != null) {

            //remove the children first
            Set<YWorkItem> children = getChildren();
            if (children != null) {
                for (YWorkItem child : children) {
                    deleteWorkItem(pmgr, child);
                }
            }

            deleteWorkItem(pmgr, this);
        }
    }


    private void deleteWorkItem(YPersistenceManager pmgr, YWorkItem item)
            throws YPersistenceException {
        pmgr.deleteObject(item);
        _eventLog.logWorkItemEvent(pmgr, item, YWorkItemStatus.statusDeleted,
                createLogDataList(YWorkItemStatus.statusDeleted.name()));
        _engine.getAnnouncer().announceCancelledWorkItem(item);
    }


    public void checkStartTimer(YPersistenceManager pmgr, YNetData data)
            throws YPersistenceException {

        if (_timerParameters != null) {

            // get values from net-level var if necessary
            String netParam = _timerParameters.getVariableName();
            if (netParam != null) {
                if (!unpackTimerParams(netParam, data)) return ;
            }

            // if current workitem status equals trigger status, start the timer
            if (_timerParameters.statusMatchesTrigger(_status)) {
                YWorkItemTimer timer = null ;
                switch (_timerParameters.getTimerType()) {
                    case Expiry: {
                        timer = new YWorkItemTimer(_workItemID.toString(),
                                _timerParameters.getDate(), (pmgr != null)) ;
                        break;
                    }
                    case Duration: {
                        timer = new YWorkItemTimer(_workItemID.toString(),
                                _timerParameters.getDuration(), (pmgr != null));
                        break;
                    }
                    case Interval: {
                        timer = new YWorkItemTimer(_workItemID.toString(),
                                _timerParameters.getTicks(), _timerParameters.getTimeUnit(), (pmgr != null)) ;
                    }
                }
                if (timer != null) {
                    _timerExpiry = timer.getEndTime();
                    setTimerActive();
                    _timerStarted = true ;
                    if (pmgr != null) pmgr.storeObject(timer);
                }
            }
        }
    }


    private void setTimerActive() {
        _engine.getNetRunner(this).updateTimerState(_task, YWorkItemTimer.State.active);
    }


    /** @return true if workitem is 'live' */
    public boolean hasLiveStatus() {
        return _status.equals(statusFired) ||
               _status.equals(statusEnabled) ||
               _status.equals(statusExecuting);
    }


    /** @return true if workitem is finished */
    public boolean hasFinishedStatus() {
        return hasCompletedStatus() ||
               _status.equals(statusDeleted)  ||
               _status.equals(statusFailed) ;
    }

    /** @return true if workitem has completed */
    public boolean hasCompletedStatus() {
        return _status.equals(statusComplete) ||
               _status.equals(statusForcedComplete) ;
    }


    /** @return true if workitem is not finished */
    public boolean hasUnfinishedStatus() {
        return hasLiveStatus() || _status.equals(statusSuspended) ||
               _status.equals(statusDeadlocked);
    }


    /** @return true if workitem is suspended from enabled status */
    public boolean isEnabledSuspended() {
        return _status.equals(statusSuspended) && _prevStatus.equals(statusEnabled);
    }


    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof YWorkItem) {         // instanceof = false if other is null
            YWorkItem otherItem = (YWorkItem) other;
            if (this.get_thisID() != null) {
                return this.get_thisID().equals(otherItem.get_thisID());
            }
            else if (this.getWorkItemID() != null) {
                return this.getWorkItemID().equals(otherItem.getWorkItemID());
            }
        }
        return false;
    }

    public int hashCode() {
        return (get_thisID() != null) ? get_thisID().hashCode() :
               (getWorkItemID() != null) ? getWorkItemID().hashCode() : super.hashCode();
    }

    /********************************************************************************/

    // STATUS CHANGE METHODS //

    public void setStatusToStarted(YPersistenceManager pmgr, YClient client)
           throws YPersistenceException {
        if (!_status.equals(statusFired)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be moved to \"" + statusExecuting + "\"]");
        }

        set_status(pmgr, statusExecuting);
        _startTime = new Date();
        _externalClient = client;
        if (! _timerStarted) checkStartTimer(pmgr, null) ;
        if (pmgr != null) pmgr.updateObject(this);
        _eventLog.logWorkItemEvent(pmgr, this, _status, createLogDataList(_status.name()));
    }


    public void setStatusToComplete(YPersistenceManager pmgr,
                                    YEngine.WorkItemCompletion completionFlag)
           throws YPersistenceException {
        YWorkItemStatus completionStatus;
        switch (completionFlag) {
            case Normal : completionStatus = statusComplete; break;
            case Force  : completionStatus = statusForcedComplete; break;
            case Fail   : completionStatus = statusFailed; break;
            default     : completionStatus = statusComplete;
        }
        completePersistence(pmgr, completionStatus) ;
    }


    public void setStatusToDeleted(YPersistenceManager pmgr)
           throws YPersistenceException {
        completePersistence(pmgr, statusDeleted) ;
    }


    /**
     * announces and logs that this workitem has been discarded - ie. left in the net when
     * the net completed
     */
    public void setStatusToDiscarded(YPersistenceManager pmgr) {
        try {
            set_status(null, statusDiscarded);
            _eventLog.logWorkItemEvent(pmgr, this, _status, null);
        }
        catch (YPersistenceException ype) {
            // no action required
        }
    }


    public void rollBackStatus(YPersistenceManager pmgr)
           throws YPersistenceException {
        if (!_status.equals(statusExecuting)) {
            throw new RuntimeException(this + " [when current status is \""
                   + _status + "\" it cannot be rolled back to \"" + statusFired + "\"]");
        }

        set_status(pmgr, statusFired);
        _eventLog.logWorkItemEvent(pmgr, this, _status, createLogDataList(_status.name()));
        _startTime = null;
        _externalClient = null;
        if (pmgr != null) pmgr.updateObject(this);
    }


    public void setStatusToSuspended(YPersistenceManager pmgr)
            throws YPersistenceException {
        if (hasLiveStatus()) {
            _prevStatus = _status ;
            set_status(pmgr, statusSuspended);
            _eventLog.logWorkItemEvent(pmgr, this, _status, createLogDataList(_status.name()));
        }
        else throw new RuntimeException(this + " [when current status is \""
                                + _status + "\" it cannot be moved to \"Suspended\".]");
    }


    public void setStatusToUnsuspended(YPersistenceManager pmgr)
            throws YPersistenceException {
        set_status(pmgr, _prevStatus);
        _prevStatus = null ;
        _eventLog.logWorkItemEvent(pmgr, this, "resume", createLogDataList("resume"));
    }


    /********************************************************************************/

    // GETTERS & SETTERS //

    public void set_parent(YWorkItem parent) { _parent = parent; }

    public YWorkItem get_parent() { return _parent; }

    public Set get_children() { return _children; }

    public boolean hasChildren() { return _children != null; }

    public void add_child(YWorkItem child) { _children.add(child); }

	  public void add_children(Set children) { _children.addAll(children); }

    public void setWorkItemID(YWorkItemID workitemid) { _workItemID = workitemid; } //

    public String get_thisID() { return _thisID; }

    public void set_thisID(String thisID) { _thisID = thisID; }


    public String get_specIdentifier() { return _specID.getIdentifier(); }

    public String get_specVersion() { return _specID.getVersionAsString(); }

    public String get_specUri() { return _specID.getUri(); }


    public void set_specIdentifier(String id) {
        if (_specID == null) _specID = new YSpecificationID((String) null);
        _specID.setIdentifier(id);
    }

    public void set_specUri(String uri) {
        if (_specID != null)
            _specID.setUri(uri);
        else
           _specID = new YSpecificationID(uri);
    }

    public void set_specVersion(String version) {
        if (_specID == null) _specID = new YSpecificationID((String) null);
        _specID.setVersion(version);
    }
    

    public Hashtable<String, String> getAttributes() {
        return _attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        if (attributes != null) {
            _attributes = new YAttributeMap(attributes);
        }
    }

    public boolean requiresManualResourcing() {
        return _requiresManualResourcing;
    }

    public void setRequiresManualResourcing(boolean requires) {
        _requiresManualResourcing = requires;
    }

    public String getCodelet() { return _codelet; }

    public void setCodelet(String codelet) { _codelet = codelet ; }


    public URL getCustomFormURL() { return _customFormURL; }

    public void setCustomFormURL(URL formURL) { _customFormURL = formURL; }


    public String get_deferredChoiceGroupID() { return _deferredChoiceGroupID; }

    public void set_deferredChoiceGroupID(String id) { _deferredChoiceGroupID = id; }

    public Date get_enablementTime() { return _enablementTime; }

    public void set_enablementTime(Date eTime) { _enablementTime = eTime; }

    public Date get_firingTime() { return _firingTime; }

    public void set_firingTime(Date fTime) { _firingTime = fTime; }

    public Date get_startTime() {return _startTime; }

    public void set_startTime(Date sTime) { _startTime = sTime; }


    public String get_status() { return _status.toString(); }

    public void set_status(String status) {                         // for hibernate
        _status = YWorkItemStatus.fromString(status);
    }

    public String get_prevStatus() {
        return (_prevStatus != null) ? _prevStatus.toString() : null;         
    }

    public void set_prevStatus(String status) {
         _prevStatus = (status != null ) ? YWorkItemStatus.fromString(status) : null ;
    }


    private void set_status(YPersistenceManager pmgr, YWorkItemStatus status)
                                                         throws YPersistenceException {
        _engine.getAnnouncer().announceWorkItemStatusChange(this, _status, status);
        _status = status;
        if (pmgr != null) pmgr.updateObject(this);
    }

    public String get_externalClient() {
        return (_externalClient != null) ? _externalClient.getUserName() : null;
    }

    public void set_externalClient(String owner) { _externalClientStr = owner; }

    public boolean get_allowsDynamicCreation() { return _allowsDynamicCreation; }

    public void set_allowsDynamicCreation(boolean a) { _allowsDynamicCreation = a; }

    public String get_dataString() { return _dataString; }

    public void set_dataString(String s) { _dataString = s; }

    public void setInitData(Element data) {
        _dataList = data;
        _dataString = getDataString();
    }

    public void setStatus(YWorkItemStatus status) { _status = status; }

    public YWorkItemID getWorkItemID() { return _workItemID; }

    public Date getEnablementTime() { return _enablementTime; }

    public String getEnablementTimeStr() { return _df.format(_enablementTime); }

    public Date getFiringTime() { return _firingTime; }

    public String getFiringTimeStr() { return _df.format(_firingTime); }

    public Date getStartTime() { return _startTime; }

    public String getStartTimeStr() { return _df.format(_startTime); }

    public YWorkItemStatus getStatus() { return _status; }

    public YWorkItem getParent() { return _parent; }

    public Set<YWorkItem> getChildren() { return _children; }

    public YIdentifier getCaseID() { return _workItemID.getCaseID(); }

    public String getTaskID() { return _workItemID.getTaskID(); }

    public String getIDString() { return _workItemID.toString(); }

    private String getUniqueID() { return _workItemID.getUniqueID(); }

    public String getDeferredChoiceGroupID() { return _deferredChoiceGroupID; }

    public void setDeferredChoiceGroupID(String id) { _deferredChoiceGroupID = id; }

    public String getSpecName() { return _specID.getUri(); }

    public YSpecificationID getSpecificationID() { return _specID ; }

    public YTimerParameters getTimerParameters() { return _timerParameters; }

    public void setTimerParameters(YTimerParameters params) {
        _timerParameters = params;
    }

    public boolean hasTimerStarted() { return _timerStarted; }

    public void setTimerStarted(boolean started) {
        _timerStarted = started;
        if (started) setTimerActive();
    }

    public long getTimerExpiry() { return _timerExpiry; }

    public void setTimerExpiry(long time) { _timerExpiry = time; }

    public String getTimerStatus() {
        if (_timerParameters == null) return "Nil";
        if (_timerExpiry == 0) return "Dormant";
        return "Active";
    }

    public boolean allowsDynamicCreation() { return _allowsDynamicCreation; }

    public String toString() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(
                      fullClassName.lastIndexOf('.') + 1) + ":" + getIDString();
    }


    public YClient getExternalClient() {
            return _externalClient;
    }

    public YNetRunner getNetRunner() {
        return _engine.getNetRunnerRepository().get(this);
    }


    public Element getDataElement() { return _dataList; }


    public String getDataString() {
        return JDOMUtil.elementToString(_dataList) ;
    }

    public YTask getTask() { return _task; }

    public void setTask(YTask task) { _task = task; }


    public String getDocumentation() {
        return (_parent != null) ? _parent.getDocumentation() : _documentation;
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder("<workItem");
        if (_attributes != null) xml.append(_attributes.toXML());
        xml.append(">");
        xml.append(StringUtil.wrap(getTaskID(), "taskid"));
        xml.append(StringUtil.wrap(getCaseID().toString(), "caseid"));
        xml.append(StringUtil.wrap(getUniqueID(), "uniqueid"));
        xml.append(StringUtil.wrap(_task.getName(), "taskname"));
        xml.append(StringUtil.wrap(getDocumentation(), "documentation"));
        if (_specID.getIdentifier() != null)
            xml.append(StringUtil.wrap(_specID.getIdentifier(), "specidentifier"));

        xml.append(StringUtil.wrap(String.valueOf(_specID.getVersion()), "specversion"));
        xml.append(StringUtil.wrap(_specID.getUri(), "specuri"));
        xml.append(StringUtil.wrap(_status.toString(), "status"));
        xml.append(StringUtil.wrap(String.valueOf(_allowsDynamicCreation),
                                                              "allowsdynamiccreation"));
        xml.append(StringUtil.wrap(String.valueOf(_requiresManualResourcing),
                                                              "requiresmanualresourcing"));
        xml.append(StringUtil.wrap(_codelet, "codelet"));
        if (_deferredChoiceGroupID != null)
            xml.append(StringUtil.wrap(_deferredChoiceGroupID, "deferredChoiceGroupID"));
        if (_dataList != null)
            xml.append(StringUtil.wrap(getDataString(), "data"));
        xml.append(StringUtil.wrap(_df.format(getEnablementTime()), "enablementTime"));
        xml.append(StringUtil.wrap(String.valueOf(getEnablementTime().getTime()),
                       "enablementTimeMs")) ;
        if (getFiringTime() != null) {
            xml.append(StringUtil.wrap(_df.format(getFiringTime()), "firingTime"));
            xml.append(StringUtil.wrap(String.valueOf(getFiringTime().getTime()),
                       "firingTimeMs")) ;
        }
        if (getStartTime() != null) {
            xml.append(StringUtil.wrap(_df.format(getStartTime()), "startTime"));
            xml.append(StringUtil.wrap(String.valueOf(getStartTime().getTime()),
                         "startTimeMs")) ;
            if (_externalClient != null) {
                xml.append(StringUtil.wrap(_externalClient.getUserName(), "startedBy"));
            }    
        }
        if (_timerParameters != null) {
            YWorkItemTimer.Trigger trigger = _timerParameters.getTrigger();
            if (trigger != null) {
                String triggerName = trigger.name();
                xml.append(StringUtil.wrap(triggerName, "timertrigger"));
                long expiry = _timerExpiry > 0 ? _timerExpiry : _parent.getTimerExpiry();
                xml.append(StringUtil.wrap(String.valueOf(expiry), "timerexpiry"));
            }    
        }
        if (_customFormURL != null) {
            xml.append(StringUtil.wrap(_customFormURL.toString(), "customform"));
        }
        YDecomposition decomp = _task.getDecompositionPrototype();
        if (decomp != null) {
            YLogPredicate logPredicate = decomp.getLogPredicate();
            if (logPredicate != null) {
                xml.append(logPredicate.toXML());
            }
        }
        xml.append("</workItem>");
        return xml.toString();
    }

    
    private YLogDataItemList createLogDataList(String tag) {
        YLogDataItemList itemList = new YLogDataItemList();
        if (_externalClient != null) {
            itemList.add(new YLogDataItem("OwnerService", tag,
                    _externalClient.getUserName(), "string"));
        }
        if (tag.equals(statusExecuting.name())) {
            YLogDataItem dataItem = getDecompLogPredicate(YWorkItemStatus.valueOf(tag));
            if (dataItem != null) itemList.add(dataItem);
        }
        else if (tag.equals(statusComplete.name()) || tag.equals(statusForcedComplete.name())) {
            itemList.addAll(getCompletionPredicates());
        }
        return (itemList.isEmpty()) ? null : itemList;
    }


    private YLogDataItemList getCompletionPredicates() {
        YLogDataItemList completionList = new YLogDataItemList();
        YLogDataItem completionItem = null;
        if (_externalLogPredicate != null) {
            if (_externalLogPredicate.startsWith("<logdataitemlist>")) {
                completionList.fromXML(_externalLogPredicate);
                for (YLogDataItem item : completionList) {
                    if (item.getName().equals("Complete")) {
                        completionItem = item;
                        break;
                    }
                }
            }
            else if (_externalLogPredicate.startsWith("<logdataitem>")) {
                YLogDataItem item = new YLogDataItem(_externalLogPredicate);
                completionList.add(item);
                if (item.getName().equals("Complete")) completionItem = item;
            }
            else {
                completionItem = new YLogDataItem("Predicate", "External",
                        _externalLogPredicate, "string");
                completionList.add(completionItem);
            }
        }
        if (completionItem != null) {
            completionItem.setValue(
                    new YLogPredicateWorkItemParser(this).parse(completionItem.getValue()));
        }
        else {
            completionItem = getDecompLogPredicate(YWorkItemStatus.statusComplete);
            if (completionItem != null) completionList.add(completionItem);
        }
        return completionList;
    }


    private YLogDataItem getDecompLogPredicate(YWorkItemStatus itemStatus) {
        YLogDataItem dataItem = null;
        String predicate = null;
        YLogPredicate logPredicate = getDecompLogPredicate();
        if (logPredicate != null) {
           if (itemStatus.equals(YWorkItemStatus.statusExecuting)) {
               predicate = logPredicate.getParsedStartPredicate(this);
           }
           else if (itemStatus.equals(YWorkItemStatus.statusComplete)) {
               predicate = logPredicate.getParsedCompletionPredicate(this);
           }
           if (predicate != null) {
                dataItem = new YLogDataItem("Predicate", itemStatus.name(),
                            predicate, "string");
            }
        }
        return dataItem ;
    }


    private YLogPredicate getDecompLogPredicate() {
        YDecomposition decomp = _task.getDecompositionPrototype();
        return (decomp != null) ? decomp.getLogPredicate() : null;
    }


    private YLogDataItem getDataLogPredicate(YParameter param, boolean input) {
        YLogDataItem dataItem = null;
        YLogPredicate logPredicate = param.getLogPredicate();
        if (logPredicate != null) {
            String predicate = input ? logPredicate.getParsedStartPredicate(param) :
                    logPredicate.getParsedCompletionPredicate(param);
            if (predicate != null) {
                dataItem = new YLogDataItem("Predicate", param.getPreferredName(), predicate, "string");
            }
        }
        return dataItem ;
    }

}
