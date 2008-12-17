/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import static org.yawlfoundation.yawl.engine.YWorkItemStatus.*;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/05/2003
 * Time: 15:29:33
 *
 * Refactored for v2.0 by Michael Adams 11/10/2007
 * 
 */
public class YWorkItem {

    private static final String INITIAL_VERSION = "0.1";
    private static DateFormat _df = new SimpleDateFormat("MMM:dd, yyyy H:mm:ss");
    private static YWorkItemRepository _workItemRepository =
                                             YWorkItemRepository.getInstance();
    private YWorkItemID _workItemID;
    private String _thisID = null;
    private String _startEventID ;
    private String _endEventID ;
    private YSpecificationID _specID;
    private Date _enablementTime;
    private Date _firingTime;
    private Date _startTime;

    private Hashtable<String, String> _attributes;          // decomposition attributes

    private YWorkItemStatus _status;
    private YWorkItemStatus _prevStatus = null;             // for worklet service.
    private String _whoStartedMe;
    private boolean _allowsDynamicCreation;
    private boolean _requiresManualResourcing;
    private YWorkItem _parent;                             // this item's parent (if any)
    private Set<YWorkItem> _children;                      // this item's kids (if any)
    private Element _dataList;
    private String _dataString = null;                  // persisted version of datalist

    private String _deferredChoiceGroupID = null ;

    private Map _timerParameters ;                         // timer extensions
    private boolean _timerStarted ;
    private long _timerExpiry = 0;                     // set to expiry when timer starts

    private URL _customFormURL ;
    private String _codelet ;

    private YEventLogger _eventLog = YEventLogger.getInstance();
    private Logger _log = Logger.getLogger(YWorkItem.class);


    // CONSTRUCTORS //

    public YWorkItem() {}                                  // required for persistence


    /** Creates an enabled WorkItem */
    public YWorkItem(YPersistenceManager pmgr, YSpecificationID specID,
                     YWorkItemID workItemID, boolean allowsDynamicCreation,
                     boolean isDeadlocked) throws YPersistenceException {

        _log.debug("Spec =" + specID + " WorkItem =" + workItemID.getTaskID());

        createWorkItem(pmgr, specID, workItemID,
                       isDeadlocked ? statusDeadlocked : statusEnabled,
                       allowsDynamicCreation);

        _enablementTime = new Date();
        _eventLog.logParentWorkItemEvent(pmgr, this, _status, _whoStartedMe);
        if ((pmgr != null) && (! isDeadlocked)) pmgr.storeObject(this);
    }


    /** Creates a fired WorkItem */
    private YWorkItem(YPersistenceManager pmgr, YWorkItemID workItemID,
                      YSpecificationID specID, Date workItemCreationTime, YWorkItem parent,
                     boolean allowsDynamicInstanceCreation) throws YPersistenceException {

        _log.debug("Spec =" + specID + " WorkItem =" + workItemID.getTaskID());

        createWorkItem(pmgr, specID, workItemID, statusFired,
                       allowsDynamicInstanceCreation);

        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        _parent = parent;
        _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
        if (pmgr != null) pmgr.storeObject(this);
    }


    /********************************************************************************/

    // PRIVATE METHODS //

    /** Called from constructors to set some mutual members */
    private void createWorkItem(YPersistenceManager pmgr, YSpecificationID specificationID,
                                YWorkItemID workItemID, YWorkItemStatus status,
                                boolean allowsDynamicInstanceCreation)
                                throws YPersistenceException {
        _workItemID = workItemID;
        _specID = specificationID;
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        _status = status ;
        set_thisID(_workItemID.toString() + "!" + _workItemID.getUniqueID());
        
        _workItemRepository.addNewWorkItem(this);
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
        _status = completionStatus;
        _endEventID = _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
        if (pmgr != null) pmgr.deleteObject(this);

        completeParentPersistence(pmgr) ;
    }


    /** completes persisting and event logging for a parent workitem if required */
    private void completeParentPersistence(YPersistenceManager pmgr)
                                                      throws YPersistenceException {

        // if all siblings are completed, then the parent is completed too
        boolean parentcomplete = true;
        Set siblings = _parent.getChildren();

        Iterator iter = siblings.iterator();
        while (iter.hasNext()) {
            YWorkItem mysibling = (YWorkItem) iter.next();
            if (mysibling.hasUnfinishedStatus()) {
                parentcomplete = false;
                break;
            }
        }

        if (parentcomplete) {
            _eventLog.logParentWorkItemEvent(pmgr, _parent, _status, _whoStartedMe);
            if (pmgr != null) pmgr.deleteObject(_parent);
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
    private boolean unpackTimerParams(String param, YCaseData data) {
        if (data == null)
            data = YEngine.getInstance().getCaseData(_workItemID.getCaseID());

        if (data == null) return false ;                    // couldn't get case data

        Element eData = JDOMUtil.stringToElement(data.getData());

        Element timerParams = eData.getChild(param) ;
        if (timerParams == null) return false ;            // no var with param's name

        String trigger = timerParams.getChildText("trigger");
        if (trigger == null) {
            _log.warn("Unable to set timer for workitem: " + getIDString() +
                      ". Missing 'trigger' parameter." ) ;
            return false ;                                 // no trigger value set
        }
        _timerParameters.put("trigger", YWorkItemTimer.Trigger.valueOf(trigger));

        String expiry = timerParams.getChildText("expiry");
        if (expiry == null) {
            _log.warn("Unable to set timer for workitem: " + getIDString() +
                      ". Missing 'expiry' parameter." ) ;
            return false ;                                 // no expiry value set            
        }

        if (expiry.startsWith("P")) {                    // duration types start with P
            try {
                Duration duration = DatatypeFactory.newInstance().newDuration(expiry) ;
                _timerParameters.put("duration", duration);
                return true ;                        // OK - trigger & duration set
            }
            catch (DatatypeConfigurationException dce) {
                // do nothing here - trickle down
            }
        }

        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = sdf.parse(expiry);                        // test for dateTime
            _timerParameters.put("expiry", date);
            return true;
        }
        catch (ParseException pe) {
            // do nothing here - trickle down                
        }

        try {
            long time = Long.parseLong(expiry);                 // test for long value
            _timerParameters.put("expiry", new Date(time));
            return true ;                                 // OK - trigger & expiry set
        }
        catch (NumberFormatException nfe) {
            _log.warn("Unable to set timer for workitem: " + getIDString() +
                      ". Invalid 'expiry' parameter." ) ;
            return false ;                             // not duration, dateTime or long
        }
    }


    /*****************************************************************************/

    // MISC METHODS //

    public void addToRepository() {
        if ((_workItemRepository.getWorkItem(_workItemID.toString())) == null) {
            _workItemRepository.addNewWorkItem(this);
        }
    }


    public YWorkItem createChild(YPersistenceManager pmgr, YIdentifier childCaseID)
                                                        throws YPersistenceException {
        if (this._parent == null) {
            YIdentifier parentCaseID = getWorkItemID().getCaseID();
            if (childCaseID.getParent() != parentCaseID) return null;

            _status = statusIsParent;

            // if this parent has no children yet, create the set and log it
            if (_children == null) {
                _children = new HashSet();
                _eventLog.logParentWorkItemEvent(pmgr, this, _status, _whoStartedMe);
            }

            YWorkItem childItem = new YWorkItem(pmgr,
                    new YWorkItemID(childCaseID, getWorkItemID().getTaskID()),
                    _specID, getEnablementTime(), this, _allowsDynamicCreation);

            // map relevant (genetic, perhaps?) attributes to child
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


    /** write data input values to event log */
    public void setData(YPersistenceManager pmgr, Element data) throws YPersistenceException {
        _dataList = data;
        _dataString = getDataString();

        if (pmgr != null) pmgr.updateObject(this);

        Iterator iter = data.getChildren().listIterator();
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            _eventLog.logData(pmgr, child.getName(), child.getValue(), _startEventID, 'i');
        }
    }


    /** write output data values to event log */
    public void completeData(YPersistenceManager pmgr, Document output)
                                                        throws YPersistenceException {
        Element root = output.getRootElement();

        Iterator iter = root.getChildren().listIterator();
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            _eventLog.logData(
                    pmgr, child.getName(), child.getValue(), _endEventID, 'o');
        }
    }

      public void restoreDataToNet() throws YPersistenceException {
        if (getDataString() != null) {
            YNet net;
            try {
                net = YWorkItemRepository.getInstance()
                                         .getNetRunner(getCaseID().getParent()).getNet();
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
        }
    }


    /** removes workitems from persistence when cancelled **/
    public void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        if (pmgr != null) {

            //remove the children first
            Set children = getChildren();
            if (children != null) {
                for(Object o : getChildren()) pmgr.deleteObject(o);
            }

            pmgr.deleteObject(this);
        }
    }


    public void checkStartTimer(YPersistenceManager pmgr, YCaseData data) {
        YWorkItemTimer timer = null ;

        if (_timerParameters != null) {

            // get values from net-level var if necessary
            String netParam = (String) _timerParameters.get("netparam") ;
            if (netParam != null)
                if (!unpackTimerParams(netParam, data)) return ;

            YWorkItemTimer.Trigger trigger =
                          (YWorkItemTimer.Trigger) _timerParameters.get("trigger") ;

            // if current workitem status equals trigger status, start the timer
            if (((trigger == YWorkItemTimer.Trigger.OnEnabled) &&
                                (_status.equals(statusEnabled))) ||
                ((trigger == YWorkItemTimer.Trigger.OnExecuting) &&
                                (_status.equals(statusExecuting)))) {

                // try expiry type first
                Date expiryTime = (Date) _timerParameters.get("expiry");
                if (expiryTime != null) {
                    timer = new YWorkItemTimer(_workItemID.toString(), expiryTime, (pmgr != null)) ;
                    _timerStarted = true ;
                }
                else {
                    // try duration type
                    Duration duration = (Duration) _timerParameters.get("duration");
                    if (duration != null) {
                        timer = new YWorkItemTimer(_workItemID.toString(), duration, (pmgr != null));
                        _timerStarted = true ;
                    }
                    else {
                        // other duration settings
                        long ticks = (Long) _timerParameters.get("ticks");
                        if (ticks > 0) {
                            YTimer.TimeUnit interval = (YTimer.TimeUnit)
                                                      _timerParameters.get("interval") ;

                            if (interval == null) interval = YTimer.TimeUnit.MSEC ;
                            timer = new YWorkItemTimer(_workItemID.toString(),
                                                       ticks, interval, (pmgr != null)) ;
                            _timerStarted = true ;
                        }
                    }    
                }
            }
            if (_timerStarted && (timer != null))
                _timerExpiry = timer.getEndTime();
        }
    }


    /** @return true if workitem is 'live' */
    public boolean hasLiveStatus() {
        return _status.equals(statusFired) ||
               _status.equals(statusEnabled) ||
               _status.equals(statusExecuting);
    }


    /** @return true if workitem is finished */
    public boolean hasFinishedStatus() {
        return _status.equals(statusComplete) ||
               _status.equals(statusDeleted)  ||
               _status.equals(statusForcedComplete) ||
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


    /********************************************************************************/

    // STATUS CHANGE METHODS //

    public void setStatusToStarted(YPersistenceManager pmgr, String userName)
                                                        throws YPersistenceException {
        if (!_status.equals(statusFired)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be moved to \"" + statusExecuting + "\"]");
        }

        _status = statusExecuting;
        _startTime = new Date();
        _whoStartedMe = userName;
        if (! _timerStarted) checkStartTimer(pmgr, null) ;
        if (pmgr != null) pmgr.updateObject(this);
        _startEventID = _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
    }


    public void setStatusToComplete(YPersistenceManager pmgr, boolean force)
                                                        throws YPersistenceException {
        YWorkItemStatus completionStatus = force ? statusForcedComplete : statusComplete ;
        completePersistence(pmgr, completionStatus) ;
    }


    public void setStatusToDeleted(YPersistenceManager pmgr, boolean fail)
                                                      throws YPersistenceException {
        YWorkItemStatus completionStatus = fail ? statusFailed : statusDeleted ;
        completePersistence(pmgr, completionStatus) ;
    }


    public void rollBackStatus(YPersistenceManager pmgr) throws YPersistenceException {
        if (!_status.equals(statusExecuting)) {
            throw new RuntimeException(this + " [when current status is \""
                   + _status + "\" it cannot be rolled back to \"" + statusFired + "\"]");
        }

        _status = statusFired;
        _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
        _startTime = null;
        _whoStartedMe = null;
        if (pmgr != null) pmgr.updateObject(this);
    }


    public void setStatusToSuspended(YPersistenceManager pmgr) throws YPersistenceException {
        if (hasLiveStatus()) {
            _prevStatus = _status ;
            set_status(pmgr, statusSuspended);
            _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
        }
        else throw new RuntimeException(this + " [when current status is \""
                                + _status + "\" it cannot be moved to \"Suspended\".]");
    }


    public void setStatusToUnsuspended(YPersistenceManager pmgr) throws YPersistenceException {
        set_status(pmgr, _prevStatus);
        _prevStatus = null ;
        _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
    }


    /********************************************************************************/

    // GETTERS & SETTERS //

    public void set_parent(YWorkItem parent) { _parent = parent; }

    public YWorkItem get_parent() { return _parent; }

    public Set get_children() { return _children; }

    public void add_child(YWorkItem child) { _children.add(child); }

	  public void add_children(Set children) { _children.addAll(children); }

    public void setWorkItemID(YWorkItemID workitemid) { _workItemID = workitemid; } //

    public String get_thisID() { return _thisID; }

    public void set_thisID(String thisID) { _thisID = thisID; }

    public String get_specName() { return _specID.getSpecName(); }

    public void set_specName(String specID) {
        _specID = new YSpecificationID(specID, new YSpecVersion(get_specVersion()));
    }

    public String get_specVersion() {
        if (_specID == null) return INITIAL_VERSION;
        return _specID.getVersion().toString();
    }

    public void set_specVersion(String version) {
        if (_specID != null)
            _specID.setVersion(version);
    }

    public Hashtable<String, String> getAttributes() {
        return _attributes;
    }

    public void setAttributes(Hashtable<String, String> attributes) {
        _attributes = attributes;
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


    public void set_status(YPersistenceManager pmgr, YWorkItemStatus status)
                                                         throws YPersistenceException {
        YEngine.getInstance().announceWorkItemStatusChange(this, _status, status);
        _status = status;
        if (pmgr != null) pmgr.updateObject(this);
    }

    public String get_whoStartedMe() { return _whoStartedMe; }

    public void set_whoStartedMe(String who) { _whoStartedMe = who; }

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

    public String getSpecName() { return _specID.getSpecName(); }

    public YSpecificationID getSpecificationID() { return _specID ; }

    public Map getTimerParameters() { return _timerParameters; }

    public void setTimerParameters(Map params) {
        _timerParameters = params;
    }

    public boolean hasTimerStarted() { return _timerStarted; }

    public void setTimerStarted(boolean started) { _timerStarted = started; }

    public long getTimerExpiry() { return _timerExpiry; }

    public void setTimerExpiry(long time) { _timerExpiry = time; }

    public boolean allowsDynamicCreation() { return _allowsDynamicCreation; }

    public String toString() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(
                      fullClassName.lastIndexOf('.') + 1) + ":" + getIDString();
    }


    public String getUserWhoIsExecutingThisItem() {
        if (_status.equals(statusExecuting))
            return _whoStartedMe;
        else
            return null;
    }


    public String getDataString() {
        return JDOMUtil.elementToString(_dataList) ;
    }


    public String toXML() {
        StringBuilder xmlBuff = new StringBuilder("<workItem");
        if ((_attributes != null) && ! _attributes.isEmpty())
            xmlBuff.append(attributesToXML());
        xmlBuff.append(">");
        xmlBuff.append(StringUtil.wrap(getTaskID(), "taskid"));
        xmlBuff.append(StringUtil.wrap(getCaseID().toString(), "caseid"));
        xmlBuff.append(StringUtil.wrap(getUniqueID(), "uniqueid"));
        xmlBuff.append(StringUtil.wrap(_specID.getSpecName(), "specid"));
        xmlBuff.append(StringUtil.wrap(String.valueOf(_specID.getVersion()), "specversion"));
        xmlBuff.append(StringUtil.wrap(_status.toString(), "status"));
        xmlBuff.append(StringUtil.wrap(String.valueOf(_allowsDynamicCreation),
                                                              "allowsdynamiccreation"));
        xmlBuff.append(StringUtil.wrap(String.valueOf(_requiresManualResourcing),
                                                              "requiresmanualresourcing"));
        xmlBuff.append(StringUtil.wrap(_codelet, "codelet"));
        if (_deferredChoiceGroupID != null)
            xmlBuff.append(StringUtil.wrap(_deferredChoiceGroupID, "deferredChoiceGroupID"));
        if (_dataList != null)
            xmlBuff.append(StringUtil.wrap(getDataString(), "data"));
        xmlBuff.append(StringUtil.wrap(_df.format(getEnablementTime()), "enablementTime"));
        xmlBuff.append(StringUtil.wrap(String.valueOf(getEnablementTime().getTime()),
                       "enablementTimeMs")) ;
        if (getFiringTime() != null) {
            xmlBuff.append(StringUtil.wrap(_df.format(getFiringTime()), "firingTime"));
            xmlBuff.append(StringUtil.wrap(String.valueOf(getFiringTime().getTime()),
                       "firingTimeMs")) ;
        }
        if (getStartTime() != null) {
            xmlBuff.append(StringUtil.wrap(_df.format(getStartTime()), "startTime"));
            xmlBuff.append(StringUtil.wrap(String.valueOf(getStartTime().getTime()),
                         "startTimeMs")) ;
            xmlBuff.append(StringUtil.wrap(getUserWhoIsExecutingThisItem(), "startedBy"));
        }
        if (_timerParameters != null) {
            YWorkItemTimer.Trigger trigger = (YWorkItemTimer.Trigger) _timerParameters.get("trigger");
            if (trigger != null) {
                String triggerName = trigger.name();
                xmlBuff.append(StringUtil.wrap(triggerName, "timertrigger"));
                xmlBuff.append(StringUtil.wrap(String.valueOf(_timerExpiry), "timerexpiry"));
            }    
        }
        if (_customFormURL != null)
            xmlBuff.append(StringUtil.wrap(_customFormURL.toString(), "customform"));

        xmlBuff.append("</workItem>");
        return xmlBuff.toString();
    }

    public String attributesToXML() {
        StringBuilder xml = new StringBuilder();
        for (String key : _attributes.keySet()) {
            xml.append(" ")
               .append(key)
               .append("=\"")
               .append(_attributes.get(key))
               .append("\"");
        }
        return xml.toString();
    }

}
