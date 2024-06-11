/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.stateless.engine;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.stateless.elements.YTimerParameters;
import org.yawlfoundation.yawl.engine.WorkItemCompletion;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.stateless.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.YDecomposition;
import org.yawlfoundation.yawl.stateless.elements.YTask;
import org.yawlfoundation.yawl.stateless.elements.data.YParameter;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.listener.event.YEventType;
import org.yawlfoundation.yawl.stateless.listener.event.YLogEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YWorkItemEvent;
import org.yawlfoundation.yawl.stateless.listener.predicate.YLogPredicate;
import org.yawlfoundation.yawl.stateless.listener.predicate.YLogPredicateWorkItemParser;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.stateless.util.SaxonUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    private static final DateFormat _df = new SimpleDateFormat("MMM:dd, yyyy H:mm:ss");

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
    private boolean _allowsDynamicCreation;
    private boolean _requiresManualResourcing;
    private YWorkItem _parent;                            // this item's parent (if any)
    private Set<YWorkItem> _children;                     // this item's kids (if any)

    private String _deferredChoiceGroupID = null ;

    private YTimerParameters _timerParameters ;                      // timer extensions
    private boolean _timerStarted ;
    private long _timerExpiry = 0;                     // set to expiry when timer starts

    private URL _customFormURL ;
    private String _codelet ;
    private String _documentation;
    private String _externalLogPredicate;                 // set by services on checkin
    private Element _data;

    private boolean _suppressTimerEvents = false;

    private final Logger _log = LogManager.getLogger(YWorkItem.class);


    // CONSTRUCTORS //

    public YWorkItem() {}                                  // required for persistence


    /** Creates an enabled WorkItem */
    public YWorkItem(YSpecificationID specID, YTask task,
                     YWorkItemID workItemID, boolean allowsDynamicCreation,
                     boolean isDeadlocked) {

        _log.debug("Spec={} WorkItem={}", specID, workItemID.getTaskID());

        _task = task;

        createWorkItem(specID, workItemID, isDeadlocked ? statusDeadlocked : statusEnabled,
                       allowsDynamicCreation); 

        if (task != null) _documentation = task.getDocumentationPreParsed();
        _enablementTime = new Date();
        logStatusChange(null);
    }


    /** Creates a fired WorkItem */
    private YWorkItem(YWorkItemID workItemID,
                      YSpecificationID specID, Date workItemCreationTime, YWorkItem parent,
                      boolean allowsDynamicInstanceCreation) {

        _log.debug("Spec={} WorkItem={}", specID, workItemID.getTaskID());

        _parent = parent;
        createWorkItem(specID, workItemID, statusFired, allowsDynamicInstanceCreation);

        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        logStatusChange(createLogDataList("fired"));
    }


    public void logStatusChange(YLogDataItemList logList) {
        logStatusChange(this, logList);
    }


    public void logStatusChange(YWorkItem item, YLogDataItemList logList) {
        YLogEvent event = new YLogEvent(YEventType.ITEM_STATUS_CHANGE, item, logList);
        getNetRunner().getAnnouncer().announceLogEvent(event);
    }


    /********************************************************************************/

    // PRIVATE METHODS //

    /** Called from constructors to set some mutual members */
    private void createWorkItem(YSpecificationID specificationID,
                                YWorkItemID workItemID, YWorkItemStatus status,
                                boolean allowsDynamicInstanceCreation) {
        _workItemID = workItemID;
        addToRepository();
        set_thisID(_workItemID.toString() + "!" + _workItemID.getUniqueID());
        _specID = specificationID;
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        _status = status ;
    }


    /** completes persisting and event logging for a workitem */
    private void completePersistence(YWorkItemStatus completionStatus) {

        // make sure we can complete this workitem
        if (!(_status.equals(statusExecuting) || _status.equals(statusSuspended))) {
            return;
        }

        // set final status, log event and remove from persistence
        set_status(completionStatus);              // don't persist status update

        YEventType eventType = completionStatus == statusDeleted ?
                YEventType.ITEM_CANCELLED : YEventType.ITEM_COMPLETED;
        YWorkItemEvent event = new YWorkItemEvent(eventType, this);
        getNetRunner().getAnnouncer().announceWorkItemEvent(event);
        YLogEvent lEvent = new YLogEvent(eventType, this, null);
        getNetRunner().getAnnouncer().announceLogEvent(lEvent);
        completeParentPersistence() ;
    }


    /** completes persisting and event logging for a parent workitem if required */
    private void completeParentPersistence() {

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

            if (parentComplete) {
                logStatusChange(_parent, createLogDataList(_status.name()));
            }
        }
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
        if (data == null) data = getNetRunner().getNetData();
        if (data == null) return false ;                    // couldn't get case data

        Element eData = JDOMUtil.stringToElement(data.getData());
        Element timerParams = eData.getChild(param) ;
        if (timerParams == null) return false ;            // no var with param's name

        try {
            timerParams = evaluateParamQuery(timerParams, new Document(eData.detach()));
            return _timerParameters.parseYTimerType(timerParams);
        }
        catch (Exception iae) {
            _log.warn("Unable to set timer for workitem '" + getIDString() +
                      "' - " + iae.getMessage()) ;
            return false ;

        }
    }


    /**
     * Evaluates any XQueries embedded in timer parameters
     * @param timerParams the parameters to evaluate
     * @param data the current net data document
     * @return the evaluated element
     * @throws SaxonApiException if queries are malformed
     */
    private Element evaluateParamQuery(Element timerParams, Document data)
            throws SaxonApiException {
        String result = SaxonUtil.evaluateQuery(JDOMUtil.elementToString(timerParams), data);
        return JDOMUtil.stringToElement(result);
    }


    /*****************************************************************************/

    // MISC METHODS //

    public void addToRepository() {
        getNetRunner().getWorkItemRepository().add(this);
    }


    public YWorkItem createChild(YIdentifier childCaseID) {
        if (this._parent == null) {

            // don't proceed if child caseid is invalid
            YIdentifier parentCaseID = getWorkItemID().getCaseID();
            if ((childCaseID == null) || (childCaseID.getParent() == null) ||
                    (! childCaseID.getParent().equals(parentCaseID))) return null;

            set_status(statusIsParent);

            // if this parent has no children yet, create the set and log it
            if (_children == null) {
                _children = new HashSet<>();
                logStatusChange(createLogDataList("createChild"));
            }

            YWorkItem childItem = new YWorkItem(
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
            return childItem;
        }
        return null;
    }

    // set by custom service on checkin, and immediately before workitem completes 
    public void setExternalLogPredicate(String predicate) {
        _externalLogPredicate = predicate;
    }

    /** write data input values to event log */
    public void logCompletionData() {
        Element dataList = _task != null ? _task.getData() : null;
        YLogDataItemList logData = assembleLogDataItemList(dataList, true);
        YLogEvent event = new YLogEvent(YEventType.ITEM_DATA_VALUE_CHANGE, this, logData);
        getNetRunner().getAnnouncer().announceLogEvent(event);
    }


    /** write output data values to event log */
    public void logCompletionData(Document output) {
        YLogDataItemList logData = assembleLogDataItemList(output.getRootElement(), false);
        YLogEvent event = new YLogEvent(YEventType.ITEM_DATA_VALUE_CHANGE, this, logData);
        getNetRunner().getAnnouncer().announceLogEvent(event);
    }


    private YLogDataItemList assembleLogDataItemList(Element data, boolean input) {
        YLogDataItemList result = new YLogDataItemList();
        if (data != null) {
            Map<String, YParameter> params = getTask().getDecompositionPrototype().getInputParameters();
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

//    public void restoreDataToNet(Set<YAWLServiceReference> services) throws YPersistenceException {
//        if (getDataString() != null) {
//            YNet net;
//            try {
//                net = _task.getNetRunner(getCaseID().getParent()).getNet();
//            }
//            catch (Exception e) {
//                return;
//            }
//            YAtomicTask task = (YAtomicTask) net.getNetElement(getTaskID());
//            if (task != null) {
//                try {
//                    task.prepareDataForInstanceStarting(getCaseID());
//                    net.addNetElement(task);
//                }
//                catch (Exception e) {
//                    throw new YPersistenceException(e);
//                }
//            }
//            if (_externalClientStr != null) {
//                if (_externalClientStr.equals("DefaultWorklist")) {
//                    _externalClient = _engine.getDefaultWorklist();
//                }
//                else {
//                    for (YAWLServiceReference service : services) {
//                        if (service.getServiceName().equals(_externalClientStr)) {
//                            _externalClient = service;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//    }


    /** removes workitems from persistence when cancelled **/
    public void cancel() {

        //remove the children first
        Set<YWorkItem> children = getChildren();
        if (children != null) {
            for (YWorkItem child : children) {
                deleteWorkItem(child);
            }
        }

        deleteWorkItem(this);

    }


    private void deleteWorkItem(YWorkItem item) {
        item.setStatusToDeleted();
        logStatusChange(createLogDataList(YWorkItemStatus.statusDeleted.name()));
        getNetRunner().getAnnouncer().announceCancelledWorkItem(item);
    }


    public void checkStartTimer(YNetData data) {

        if (_timerParameters != null) {

            // get values from net-level var if necessary
            String netParam = _timerParameters.getVariableName();
            if (netParam != null) {
                if (!unpackTimerParams(netParam, data)) return ;
            }

            // if current workitem status equals trigger status, start the timer
            if (_timerParameters.triggerMatchesStatus(_status)) {
                YWorkItemTimer timer = null ;
                switch (_timerParameters.getTimerType()) {
                    case Expiry: {
                        timer = new YWorkItemTimer(this,
                                _timerParameters.getDate()) ;
                        break;
                    }
                    case Duration: {
                        timer = new YWorkItemTimer(this,
                                _timerParameters.getWorkDayDuration());
                        break;
                    }
                    case Interval: {
                        timer = new YWorkItemTimer(this,
                                _timerParameters.getTicks(), _timerParameters.getTimeUnit()) ;
                    }
                }
                if (timer != null) {
                    _timerExpiry = timer.getEndTime();
                    setTimerActive();
                    _timerStarted = true ;
                }
            }
        }
    }


    public void cancelTimer() {
        if (hasTimerStarted()) {
            YTimer.getInstance().cancelTimerTask(getIDString());
        }
        YWorkItem parent = getParent();
        if (parent != null && parent.hasTimerStarted()) {
            Set<YWorkItem> children = parent.getChildren();
            if (children != null) {
                for (YWorkItem child : children) {
                    if (! (child.equals(this) || child.hasFinishedStatus())) {
                        return;          // parent still has active child
                    }
                }
            }

            // if suppressing timer cancel events, do it in the parent too
            parent.setSuppressTimerEventNotifications(_suppressTimerEvents);
            YTimer.getInstance().cancelTimerTask(parent.getIDString());
        }
    }


    private void setTimerActive() {
        getNetRunner().updateTimerState(getTask(), YWorkItemTimer.State.active);
    }


    public YNetRunner getNetRunner() { return getTask().getNetRunner(); }


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


    public boolean isParent() {
        return _status.equals(statusIsParent);
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

    public void setStatusToStarted() {
        if (!_status.equals(statusFired)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be moved to \"" + statusExecuting + "\"]");
        }

        set_status(statusExecuting);
        _startTime = new Date();
        if (! _timerStarted) checkStartTimer(null) ;
        logStatusChange(createLogDataList(_status.name()));
    }


    public void setStatusToComplete(WorkItemCompletion completionFlag) {
        YWorkItemStatus completionStatus;
        switch (completionFlag) {
            case Force  : completionStatus = statusForcedComplete; break;
            case Fail   : completionStatus = statusFailed; break;
            default     : completionStatus = statusComplete;
        }
        completePersistence(completionStatus) ;
    }


    public void setStatusToDeleted() {
        completePersistence(statusDeleted) ;
    }


    /**
     * announces and logs that this workitem has been discarded - ie. left in the net when
     * the net completed
     */
    public void setStatusToDiscarded() {
        set_status(statusDiscarded);
        logStatusChange(null);
    }


    public void rollBackStatus() {
        if (!_status.equals(statusExecuting)) {
            throw new RuntimeException(this + " [when current status is \""
                   + _status + "\" it cannot be rolled back to \"" + statusFired + "\"]");
        }

        set_status(statusFired);
        logStatusChange(createLogDataList(_status.name()));
        _startTime = null;
    }


    public void setStatusToSuspended() throws YStateException {
        if (hasLiveStatus()) {
            _prevStatus = _status ;
            set_status(statusSuspended);
            logStatusChange(createLogDataList(_status.name()));
        }
        else throw new YStateException(this + " [when current status is \""
                                + _status + "\" it cannot be moved to \"Suspended\".]");
    }


    public void setStatusToUnsuspended() {
        set_status(_prevStatus);
        _prevStatus = null ;
        logStatusChange(createLogDataList("resume"));
    }


    /********************************************************************************/

    // GETTERS & SETTERS //

    public void set_parent(YWorkItem parent) { _parent = parent; }

    public YWorkItem get_parent() { return _parent; }

    public Set get_children() { return _children; }

    public boolean hasChildren() { return _children != null; }

    public void add_child(YWorkItem child) { _children.add(child); }

    public void add_children(Set children) { _children.addAll(children); }

    public void setChildren(Set<YWorkItem> children) { _children = children; }

    public void setWorkItemID(YWorkItemID workitemid) { _workItemID = workitemid; } //

    public String get_thisID() { return _thisID; }

    public void set_thisID(String thisID) { _thisID = thisID; }


    public String get_specIdentifier() { return _specID.getIdentifier(); }

    public String get_specVersion() { return _specID.getVersionAsString(); }

    public String get_specUri() { return _specID.getUri(); }


    public void setSpecID(YSpecificationID specID) {
        _specID = specID;
    }


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
    

    public Map<String, String> getAttributes() {
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


    private void set_status(YWorkItemStatus status) {
 //       _engine.getAnnouncer().announceWorkItemStatusChange(this, _status, status);
        _status = status;
    }


    public boolean get_allowsDynamicCreation() { return _allowsDynamicCreation; }

    public void set_allowsDynamicCreation(boolean a) { _allowsDynamicCreation = a; }


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
        String idString = getWorkItemID() != null ? getWorkItemID().toString() :
                get_thisID() != null ? get_thisID() : "";
        return getClass().getSimpleName() + " : " + idString;
    }


    public Element getDataElement() {
        if (_data != null) {
            return _data;
        }
        return _task != null ? _task.getData() : null;
    }


    public String getDataString() {
        return JDOMUtil.elementToString(getDataElement()) ;
    }

    public void setDataElement(Element data) {
        _data = data;
    }

    public YTask getTask() {
        return (_task == null && _parent != null) ? _parent.getTask() : _task;
    }

    public void setTask(YTask task) { _task = task; }


    public String getDocumentation() {
        return (_parent != null) ? _parent.getDocumentation() : _documentation;
    }

    public void setSuppressTimerEventNotifications(boolean suppress) {
        _suppressTimerEvents = suppress;
    }

    public boolean isSuppressingTimerEvents() { return _suppressTimerEvents; }

    public String toXML() {
        StringBuilder xml = new StringBuilder("<workItem");
        if (_attributes != null) xml.append(_attributes.toXML());
        xml.append(">");
        xml.append(StringUtil.wrap(getTaskID(), "taskid"));
        xml.append(StringUtil.wrap(getCaseID().toString(), "caseid"));
        xml.append(StringUtil.wrap(getUniqueID(), "uniqueid"));
        xml.append(StringUtil.wrapEscaped(getTask().getName(), "taskname"));
        xml.append(StringUtil.wrapEscaped(getDocumentation(), "documentation"));
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
        Element dataList = _task != null ? _task.getData() : null;
        if (dataList != null)
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
        }
        if (_timerParameters != null) {
            long expiry = _timerExpiry > 0 ? _timerExpiry : _parent != null ?
                    _parent.getTimerExpiry() : 0;
            YWorkItemTimer.Trigger trigger = _timerParameters.getTrigger();
            if (trigger != null && expiry > 0) {
                String triggerName = trigger.name();
                xml.append(StringUtil.wrap(triggerName, "timertrigger"));
                xml.append(StringUtil.wrap(String.valueOf(expiry), "timerexpiry"));
            }    
        }
        if (_customFormURL != null) {
            xml.append(StringUtil.wrap(_customFormURL.toString(), "customform"));
        }
        YDecomposition decomp = getTask().getDecompositionPrototype();
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
        YDecomposition decomp = getTask().getDecompositionPrototype();
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
