/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.logging.YawlLogServletInterface;
import static au.edu.qut.yawl.engine.YWorkItemStatus.*;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/05/2003
 * Time: 15:29:33
 * 
 */
public class YWorkItem {

    static final double INITIAL_VERSION = 0.1;//MLR (30/10/07): added after merge

	private YWorkItemID _workItemID;
    private String _specificationID;
    private double _version = INITIAL_VERSION;
    private YSpecificationID specID;
    private Date _enablementTime;
    private Date _firingTime;
    private Date _startTime;

    private YWorkItemStatus _status;
    private YWorkItemStatus _prevStatus = null;                   //added by MJA for worklet service. TODO: see if this can be removed by using the new methods in YEngine
    private String _whoStartedMe;
    private boolean _allowsDynamicCreation;
    private Element _dataList;

    private YWorkItem _parent;
    private Set _children;

    private static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private static DateFormat _df = new SimpleDateFormat("MMM:dd H:mm:ss");


    /**********************************/
    /* INSERTED VARIABLES AND METHODS */
    /*********************************/
    private String lastevent = "0";
    public String thisId = null;
    private String data_string = null;
    private String taskname = null;

    /**
     * Constructor<P>
     *
     * Default constructor is required for use by Hibernate (reflection?)
     */
    public YWorkItem() {
        super();
    }

    protected void finalize() {
    }

    public void addToRepository() {

        Logger.getLogger(this.getClass()).debug("--> addToRepository");
        if ((_workItemRepository.getWorkItem(this.getWorkItemID().toString())) != null) {

            YWorkItem work = _workItemRepository.getWorkItem(this.getWorkItemID().toString());

        }
        _workItemRepository.addNewWorkItem(this);
    }

    public YWorkItem get_parent() {
        return _parent;
    }

    public void set_parent(YWorkItem parent) {
        _parent = parent;
    }

    public Set get_children() {
        return _children;
    }

    public void add_child(YWorkItem child) {
        _children.add(child);
    }
    
	public void add_children(Set children) {
        _children.addAll(children);
    }

    public void setWorkItemID(YWorkItemID workitemid) {
        this._workItemID = workitemid;
    }

    public String getThisId() {
        return thisId;
    }

    public void setThisId(String a) {
        thisId = a;
    }

    public String get_specificationID() {
        return _specificationID;
    }

    public void set_specificationID(String a) {
        _specificationID = a;
        specID = new YSpecificationID(_specificationID, _version);     //MLF
    }

    public double get_version()
    {
        return _version;
    }

    public void set_version(double _version)
    {
        this._version = _version;
        if(_specificationID != null && _specificationID.trim().length() > 0)
        {
            specID = new YSpecificationID(_specificationID, _version);
        }
    }

    public Date get_enablementTime() {
        return _enablementTime;
    }

    public void set_enablementTime(Date a) {
        _enablementTime = a;
    }

    public Date get_firingTime() {
        return _firingTime;
    }

    public void set_firingTime(Date a) {
        _firingTime = a;
    }

    public Date get_startTime() {
        return _startTime;
    }

    public void set_startTime(Date a) {
        _startTime = a;
    }

    public String get_status() {
        return _status.toString();
    }

    /**
     * Used by hibernate on restore
     * @param a
     * @throws YPersistenceException
     */
    public void set_status(String a) throws YPersistenceException {
        _status = YWorkItemStatus.fromString(a);
    }

    private void set_Status(YPersistenceManager pmgr, YWorkItemStatus a) throws YPersistenceException {
        YWorkItemStatus oldStatus = _status;
        _status = a;
//	YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObject(this);
        }
        YEngine.getInstance().announceWorkItemStatusChange(this, oldStatus, _status);
    }

    public String get_whoStartedMe() {
        return _whoStartedMe;
    }

    public void set_whoStartedMe(String a) {
        _whoStartedMe = a;
    }

    public boolean get_allowsDynamicCreation() {
        return _allowsDynamicCreation;
    }

    public void set_allowsDynamicCreation(boolean a) {
        _allowsDynamicCreation = a;
    }

    public String getData_string() {
        return data_string;
    }

    public void setData_string(String s) {
        this.data_string = s;
    }

    public void setInitData(Element data) {
        _dataList = data;
        data_string = getDataString();
    }

    public void setStatus(YWorkItemStatus status) {
        this._status = status;
    }

    public void completeData(YPersistenceManager pmgr, Document output) throws YPersistenceException {

        Element root = output.getRootElement();

        java.util.List list = root.getChildren();
        Iterator iter = list.listIterator();

        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            YawlLogServletInterface.getInstance().logData(pmgr, child.getName(), child.getValue(), lastevent, "o");
        }

    }

    //this method added by MJA for worklet service: returns true if the status is Fired, Enabled or Executing
    public boolean hasLiveStatus() {
        return _status.equals(statusFired) || _status.equals(statusEnabled) ||
               _status.equals(statusExecuting);
    }


    /***********************************/


    public YWorkItem(YPersistenceManager pmgr, YSpecificationID specID, YWorkItemID workItemID,
                     boolean allowsDynamicCreation, boolean isDeadlocked) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("Spec=" + specID + " WorkItem=" + workItemID.getTaskID());
        _workItemID = workItemID;
        this.specID = specID;//MLR (30/10/07): changed name of param specificationID to specID after the merge
        _specificationID = specID.getSpecID();
        _version = specID.getVersion();
        _enablementTime = new Date();
        _status = isDeadlocked ? statusDeadlocked : statusEnabled;
        _allowsDynamicCreation = allowsDynamicCreation;
        _workItemRepository.addNewWorkItem(this);

        /***************************/
        /* INSERTED FOR LOGGING/PERSISTANCE */
        /********************/

        try {

            YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, workItemID.getCaseID().toString(),
                    workItemID.getTaskID()
                    , _status.toString(), _whoStartedMe, _specificationID);
	    setThisId(_workItemID.toString() + "!" + _workItemID.getUniqueID());
//        YPersistance.getInstance().storeData(this);
            if (pmgr != null) {
                pmgr.storeObject(this);
            }



            /*******************************/
        } catch (YPersistenceException e) {
            throw e;
        } catch (RuntimeException e) {
            // igtnore
        } catch (Exception e2) {
            // ignore
        }
    }


    /*
     * Creates a fired WorkItem.  Private method.
     */
    private YWorkItem(YPersistenceManager pmgr, YWorkItemID workItemID, YSpecificationID specID,
                      Date workItemCreationTime, YWorkItem parent,
                      boolean allowsDynamicInstanceCreation) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("Spec=" + specID + " WorkItem=" + workItemID.getTaskID());

        _workItemID = workItemID;
        this.specID = specID;
        _specificationID = specID.getSpecID();
        _version = specID.getVersion();
        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        _parent = parent;
        _status = statusFired;
        _workItemRepository.addNewWorkItem(this);
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        /***************************/
        /* INSERTED FOR LOGGING/PERSISTANCE */
        /********************/
        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, workItemID.getCaseID().toString(),
                workItemID.getTaskID()
                , _status.toString(), _whoStartedMe, _specificationID);
	setThisId(_workItemID.toString() + "!" + _workItemID.getUniqueID());

//	YPersistance.getInstance().storeData(this);
        if (pmgr != null) {
            pmgr.storeObject(this);
        }
        /*******************************/
    }


    public YWorkItem createChild(YPersistenceManager pmgr, YIdentifier childCaseID) throws YPersistenceException {
        if (this._parent == null) {
            YIdentifier parentCaseID = getWorkItemID().getCaseID();
            if (childCaseID.getParent() != parentCaseID) {
                return null;
            }
            YWorkItem childItem = new YWorkItem(pmgr,
                    new YWorkItemID(childCaseID, getWorkItemID().getTaskID()),
                    specID, getEnablementTime(), this, _allowsDynamicCreation);

            if (_children == null) {
                _children = new HashSet();
            }

            /*
              MODIFIED FOR PERSISTANCE
             */
            _children.add(childItem);
            set_Status(pmgr, statusIsParent);
            if (_children.size() == 1) {
                YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, _workItemID.getCaseID().toString(),
                        _workItemID.getTaskID()
                        , _status.toString(), _whoStartedMe, _specificationID, null);

            }
            return childItem;
        }
        return null;
    }

    public void setStatusToDelete(YPersistenceManager pmgr) throws YPersistenceException {
        /*MODIFIED FOR PERSISTANCE*/
        set_Status(pmgr, statusDeleted);
        //_status = statusDeleted;
    }

    public void setStatusToStarted(YPersistenceManager pmgr, String userName) throws YPersistenceException {
        if (!_status.equals(statusFired)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + statusExecuting + "\"]");
        }
        /*
          MODIFIED FOR PERSISTANCE
         */
        set_Status(pmgr, statusExecuting);
        //_status = statusExecuting;

        _startTime = new Date();
        _whoStartedMe = userName;

        /*
          INSERTED FOR PERSISTANCE
         */
//	YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObject(this);
        }


        lastevent = YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, _workItemID.getCaseID().toString(),
                _workItemID.getTaskID()
                , _status.toString(), _whoStartedMe, _specificationID);
        /****************************/
    }


    public void setStatusToComplete(YPersistenceManager pmgr, boolean force) throws YPersistenceException {

        YWorkItemStatus completionStatus = force ? statusForcedComplete : statusComplete;

        if (!(_status.equals(statusExecuting) || _status.equals(statusSuspended))) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + completionStatus + "\"]");
        }
        /*
          MODIFIED FOR PERSISTANCE
         */
        set_Status(pmgr, completionStatus);
        //_status = statusComplete;

        /*
         * Check if all siblings  are completed, if so then
         * the parent is completed too.
         * */
        boolean parentcomplete = true;
        Set siblings = _parent.getChildren();

        Iterator iter = siblings.iterator();

        while (iter.hasNext()) {
            YWorkItem mysibling = (YWorkItem) iter.next();
            if ((! mysibling.getStatus().equals(statusComplete)) &&
                (! mysibling.getStatus().equals(statusForcedComplete)))
                parentcomplete = false;
        }

        /********************************/
        /* INSERTED FOR PERSISTANCE */
        /**********************************/
//	YPersistance.getInstance().removeData(this);
//        if (parentcomplete) {
//              YPersistance.getInstance().removeData(_parent);
//        }
        if (pmgr != null) {
            pmgr.deleteObject(this);
            if (parentcomplete) {
                pmgr.deleteObject(_parent);
            }
        }


        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, _parent.getCaseID().toString(),
                _parent.getTaskID()
                , _status.toString(), _whoStartedMe, _specificationID);

        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, _workItemID.getCaseID().toString(),
                _workItemID.getTaskID()
                , _status.toString(), _whoStartedMe, _specificationID);
        /************************************/
    }

//this method added by MJA for worklet service
    public void setStatusToDeleted(YPersistenceManager pmgr, boolean fail) throws YPersistenceException {

        YWorkItemStatus completionStatus = fail ? statusFailed : statusDeleted;

        if (!(_status.equals(statusExecuting) || _status.equals(statusSuspended))) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + completionStatus + "\"]");
        }
        set_Status(pmgr, completionStatus);

        /*
         * Check if all siblings  are completed, if so then
         * the parent is completed too.
         * */
        boolean parentcomplete = true;
//        Set siblings = _parent.getChildren();
//
//        Iterator iter = siblings.iterator();
//
//        while (iter.hasNext()) {
//            YWorkItem mysibling = (YWorkItem) iter.next();
//            if ((! mysibling.getStatus().equals(statusComplete)) &&
//                (! mysibling.getStatus().equals(statusForcedComplete)))
//                parentcomplete = false;
//        }
//
        if (pmgr != null) {
            pmgr.deleteObject(this);
            if (parentcomplete) {
                pmgr.deleteObject(_parent);
            }
        }

        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr,
                _parent.getCaseID().toString(), _parent.getTaskID(),
                _status.toString(), _whoStartedMe, _specificationID);

        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr,
                _workItemID.getCaseID().toString(), _workItemID.getTaskID(),
                _status.toString(), _whoStartedMe, _specificationID);
    }

    public void rollBackStatus(YPersistenceManager pmgr) throws YPersistenceException {
        if (!_status.equals(statusExecuting)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be rolledBack to \"" + statusFired + "\"]");
        }
        //_status = statusFired;
        /*
	  MODIFIED FOR PERSISTANCE
	 */
        set_Status(pmgr, statusFired);
        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr, _workItemID.getCaseID().toString(),
                _workItemID.getTaskID()
                , _status.toString(), _whoStartedMe, _specificationID);
        /*************************/

        _startTime = null;
        _whoStartedMe = null;

        /*
          INSERTED FOR PERSISTANCE
         */
//	YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObject(this);
        }

    }

//this method added by MJA for worklet service
    public void setStatusToSuspended(YPersistenceManager pmgr) throws YPersistenceException {
        if (hasLiveStatus()) {
            _prevStatus = _status ;
            set_Status(pmgr, statusSuspended);

            YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr,
                    _workItemID.getCaseID().toString(), _workItemID.getTaskID(),
                    _status.toString(), _whoStartedMe, _specificationID);
        }
        else throw new RuntimeException(this + " [when current status is \""
                                + _status + "\" it cannot be changed to \"Suspended\".]");
    }

//this method added by MJA for worklet service
    public void setStatusToUnsuspended(YPersistenceManager pmgr) throws YPersistenceException {
        set_Status(pmgr, _prevStatus);
        _prevStatus = null ;

        YawlLogServletInterface.getInstance().logWorkItemEvent(pmgr,
                _workItemID.getCaseID().toString(), _workItemID.getTaskID(),
                _status.toString(), _whoStartedMe, _specificationID);
    }


    public void setData(YPersistenceManager pmgr, Element data) throws YPersistenceException {
        _dataList = data;

        /**********************/
        /* FOR PERSISTANCE */
        /*********************/
        data_string = getDataString();
//	YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObject(this);
        }


        java.util.List list = data.getChildren();
        Iterator iter = list.listIterator();
        /*
          FOR LOGGING
         */
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            YawlLogServletInterface.getInstance().logData(pmgr, child.getName(), child.getValue(), lastevent, "i");
        }

    }


    //#################################################################################
    //                              accessors
    //#################################################################################
    public YWorkItemID getWorkItemID() {
        return _workItemID;
    }


    public Date getEnablementTime() {
        return _enablementTime;
    }

    public String getEnablementTimeStr() {
        return _df.format(_enablementTime);
    }


    public Date getFiringTime() {
        return _firingTime;
    }


    public String getFiringTimeStr() {
        return _df.format(_firingTime);
    }


    public Date getStartTime() {
        return _startTime;
    }


    public String getStartTimeStr() {
        return _df.format(_startTime);
    }


    public YWorkItemStatus getStatus() {
        return _status;
    }


    public YWorkItem getParent() {
        return _parent;
    }


    public Set getChildren() {
        return _children;
    }


    public YIdentifier getCaseID() {
        return _workItemID.getCaseID();
    }


    public String getTaskID() {
        return _workItemID.getTaskID();
    }


    public String getIDString() {
        return _workItemID.toString();
    }


    public String toString() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1) + ":" + getIDString();
    }


    public String getUserWhoIsExecutingThisItem() {
        if (_status == statusExecuting) {
            return _whoStartedMe;
        } else
            return null;
    }


    public boolean allowsDynamicCreation() {
        return _allowsDynamicCreation;
    }


    public String getDataString() {
        if (_dataList != null) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(_dataList);
        }
        return null;
    }


    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<workItem>");
        xmlBuff.append("<taskID>" + getTaskID() + "</taskID>");
        xmlBuff.append("<caseID>" + getCaseID() + "</caseID>");
        xmlBuff.append("<uniqueID>" + getUniqueID() + "</uniqueID>");
        xmlBuff.append("<specID>" + _specificationID + "</specID>");
        xmlBuff.append("<status>" + getStatus() + "</status>");
        if (_dataList != null) {
            xmlBuff.append("<data>" + getDataString() + "</data>");
        }
        xmlBuff.append("<enablementTime>" + _df.format(getEnablementTime()) + "</enablementTime>");
        if (this.getFiringTime() != null) {
            xmlBuff.append("<firingTime>" + _df.format(getFiringTime()) + "</firingTime>");
        }
        if (this.getStartTime() != null) {
            xmlBuff.append("<startTime>" + _df.format(getStartTime()) + "</startTime>");
            xmlBuff.append("<assignedTo>" + getUserWhoIsExecutingThisItem() + "</assignedTo>");
        }
        xmlBuff.append("</workItem>");
        return xmlBuff.toString();
    }

    private String getUniqueID() {
        return _workItemID.getUniqueID();
    }


    public YSpecificationID getSpecificationID() {
        return specID;
    }

//this method added by MJA for worklet service
    public boolean isEnabledSuspended() {
        return _status.equals(statusSuspended) && _prevStatus.equals(statusEnabled);
    }
}
