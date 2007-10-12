/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving workflow
 * technology.
 */

package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.logging.YEventLogger;
import au.edu.qut.yawl.util.JDOMConversionTools;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/05/2003
 * Time: 15:29:33
 *
 * Refactored for v1.0 by Michael Adams 11/10/2007
 * 
 */
public class YWorkItem {

    // the various statuses
    public static final String statusEnabled = "Enabled";
    public static final String statusFired = "Fired";
    public static final String statusExecuting = "Executing";
    public static final String statusComplete = "Complete";
    public static final String statusIsParent = "Is parent";
    public static final String statusDeadlocked = "Deadlocked";
    public static final String statusDeleted = "Cancelled";
    public static final String statusForcedComplete = "ForcedComplete";
    public static final String statusFailed = "Failed";
    public static final String statusSuspended = "Suspended";

    private YWorkItemID _workItemID;
    private String _thisID = null;
    private String _startEventID ;
    private String _endEventID ;
    private String _specificationID;
    private Date _enablementTime;
    private Date _firingTime;
    private Date _startTime;
    private String _status;
    private String _prevStatus = null;                     // status pre-suspended
    private String _whoStartedMe;                          // user who executed workitem
    private boolean _allowsDynamicCreation;
    private YWorkItem _parent;                             // this item's parent (if any)
    private Set _children;                                 // this item's kids (if any)
    private Element _dataList;
    private String _dataString = null;                // stringified version of datalist

    private YEventLogger _eventLog = YEventLogger.getInstance();
    private Logger _log = Logger.getLogger(YWorkItem.class);
    private static DateFormat _df = new SimpleDateFormat("MMM:dd H:mm:ss");
    private static YWorkItemRepository _workItemRepository =
                                             YWorkItemRepository.getInstance();


    // CONSTRUCTORS //

    public YWorkItem() {}                                  // required for persistence


    /** Creates an enabled WorkItem */
    public YWorkItem(YPersistenceManager pmgr, String specID, YWorkItemID workItemID,
                     boolean allowsDynamicCreation, boolean isDeadlocked)
                                                        throws YPersistenceException {

        _log.debug("Spec =" + specID + " WorkItem =" + workItemID.getTaskID());

        createWorkItem(pmgr, specID, workItemID,
                       isDeadlocked ? statusDeadlocked : statusEnabled,
                       allowsDynamicCreation);

        _enablementTime = new Date();
        if (pmgr != null) pmgr.storeObject(this);
    }


    /** Creates a fired WorkItem */
    private YWorkItem(YPersistenceManager pmgr, YWorkItemID workItemID, String specID,
                     Date workItemCreationTime, YWorkItem parent,
                     boolean allowsDynamicInstanceCreation) throws YPersistenceException {

        _log.debug("Spec =" + specID + " WorkItem =" + workItemID.getTaskID());

        createWorkItem(pmgr, specID, workItemID, statusFired,
                       allowsDynamicInstanceCreation);

        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        _parent = parent;
        if (pmgr != null) pmgr.storeObject(this);
    }


    /********************************************************************************/

    // PRIVATE METHODS //

    /** Called from constructors to set some mutual members */
    private void createWorkItem(YPersistenceManager pmgr, String specificationID,
                                YWorkItemID workItemID, String status,
                                boolean allowsDynamicInstanceCreation)
                                throws YPersistenceException {
        _workItemID = workItemID;
        _specificationID = specificationID;
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        _status = status ;
        set_thisID(_workItemID.toString() + "!" + _workItemID.getUniqueID());
        _workItemRepository.addNewWorkItem(this);
        _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);

    }


    /** completes persisting and event logging for a workitem */
    private void completePersistence(YPersistenceManager pmgr, String completionStatus)
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
            _eventLog.logWorkItemEvent(pmgr, _parent, _status, _whoStartedMe);
            if (pmgr != null) pmgr.deleteObject(_parent);
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
                _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
            }

            YWorkItem childItem = new YWorkItem(pmgr,
                    new YWorkItemID(childCaseID, getWorkItemID().getTaskID()),
                    _specificationID, getEnablementTime(), this, _allowsDynamicCreation);

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


    /** @return true if workitem is 'live' */
    public boolean hasLiveStatus() {
        return _status.equals(statusFired) ||
               _status.equals(statusEnabled) ||
               _status.equals(statusExecuting);
    }


    /** @return true if workitem is finsihed */
    public boolean hasFinishedStatus() {
        return _status.equals(statusComplete) ||
               _status.equals(statusDeleted)  ||
               _status.equals(statusForcedComplete) ||
               _status.equals(statusFailed) ;
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
        if (pmgr != null) pmgr.updateObject(this);
        _startEventID = _eventLog.logWorkItemEvent(pmgr, this, _status, _whoStartedMe);
    }


    public void setStatusToComplete(YPersistenceManager pmgr, boolean force)
                                                        throws YPersistenceException {
        String completionStatus = force ? statusForcedComplete : statusComplete ;
        completePersistence(pmgr, completionStatus) ;
    }


    public void setStatusToDeleted(YPersistenceManager pmgr, boolean fail)
                                                      throws YPersistenceException {
        String completionStatus = fail ? statusFailed : statusDeleted ;
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

    public void add_children(YWorkItem child) { _children.add(child); }

    public void setWorkItemID(YWorkItemID workitemid) { _workItemID = workitemid; } //

    public String get_thisID() { return _thisID; }

    public void set_thisID(String thisID) { _thisID = thisID; }

    public String get_specificationID() { return _specificationID; }

    public void set_specificationID(String specID) { _specificationID = specID; }

    public Date get_enablementTime() { return _enablementTime; }

    public void set_enablementTime(Date eTime) { _enablementTime = eTime; }

    public Date get_firingTime() { return _firingTime; }

    public void set_firingTime(Date fTime) { _firingTime = fTime; }

    public Date get_startTime() {return _startTime; }

    public void set_startTime(Date sTime) { _startTime = sTime; }

    public String get_status() { return _status; }

    public void set_status(YPersistenceManager pmgr, String status)
                                                         throws YPersistenceException {
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

    public void setStatus(String status) { _status = status; }

    public YWorkItemID getWorkItemID() { return _workItemID; }

    public Date getEnablementTime() { return _enablementTime; }

    public String getEnablementTimeStr() { return _df.format(_enablementTime); }

    public Date getFiringTime() { return _firingTime; }

    public String getFiringTimeStr() { return _df.format(_firingTime); }

    public Date getStartTime() { return _startTime; }

    public String getStartTimeStr() { return _df.format(_startTime); }

    public String getStatus() { return _status; }

    public YWorkItem getParent() { return _parent; }

    public Set getChildren() { return _children; }

    public YIdentifier getCaseID() { return _workItemID.getCaseID(); }

    public String getTaskID() { return _workItemID.getTaskID(); }

    public String getIDString() { return _workItemID.toString(); }

    private String getUniqueID() { return _workItemID.getUniqueID(); }

    public String getSpecificationID() { return _specificationID; }

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
        return JDOMConversionTools.elementToString(_dataList) ;
    }


    public String toXML() {
        StringBuilder xmlBuff = new StringBuilder("<workItem>");
        xmlBuff.append(wrap(getTaskID(), "taskID"));
        xmlBuff.append(wrap(getCaseID().toString(), "caseID"));
        xmlBuff.append(wrap(getUniqueID(), "uniqueID"));
        xmlBuff.append(wrap(_specificationID, "specID"));
        xmlBuff.append(wrap(_status, "status"));
        if (_dataList != null) {
            xmlBuff.append(wrap(getDataString(), "data"));
        }
        xmlBuff.append(wrap(_df.format(getEnablementTime()), "enablementTime"));
        if (getFiringTime() != null) {
            xmlBuff.append(wrap(_df.format(getFiringTime()), "firingTime"));
        }
        if (getStartTime() != null) {
            xmlBuff.append(wrap(_df.format(getStartTime()), "startTime"));
            xmlBuff.append(wrap(getUserWhoIsExecutingThisItem(), "assignedTo"));
        }
        xmlBuff.append("</workItem>");
        return xmlBuff.toString();
    }

    /** encases a string with a pair of xml tags */
    private String wrap(String core, String wrapTag) {
        return String.format("<%s>%s</%s>", wrapTag, core, wrapTag) ;
    }


}
