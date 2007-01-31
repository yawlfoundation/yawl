/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.events.YawlEventLogger;
import au.edu.qut.yawl.exceptions.YPersistenceException;

/**
 * 
 * @hibernate.class table = "workitem_event"
 * @author Lachlan Aldred
 * Date: 28/05/2003
 * Time: 15:29:33
 * 
 */
@Entity
public class YWorkItem {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    public static enum Status {
        Enabled,Fired,Executing,
        Complete,IsParent,Deadlocked,Cancelled,
        ForcedComplete,Failed,Suspended}

//    public static final String statusEnabled = "Enabled";
//    public static final String statusFired = "Fired";
//    public static final String statusExecuting = "Executing";
//    public static final String statusComplete = "Complete";
//    public static final String statusIsParent = "Is parent";
//    public static final String statusDeadlocked = "Deadlocked";
//    public static final String statusDeleted = "Cancelled";

//    private YWorkItemID _workItemID;
    private String _specificationID;
    private Date _enablementTime;
    private Date _firingTime;
    private Date _startTime;

    private Status _status;
    private Status _prevStatus;                   //added
    private String _whoStartedMe;
    private boolean _allowsDynamicCreation;
    private Element _dataList;

    private YWorkItem _parent;
    private Set<YWorkItem> _children = new HashSet<YWorkItem>();

    private static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private static DateFormat _df = new SimpleDateFormat("MMM:dd H:mm:ss");


    /**********************************/
    /* INSERTED VARIABLES AND METHODS */
    /*********************************/
    private String lastevent = "0";
    public String thisId = null;
    private String data_string = null;

    /**
     * Constructor<P>
     *
     * Default constructor is required for use by Hibernate (reflection?)
     */
    public YWorkItem() {
        super();
    }

//    public void addToRepository() {
//        Logger.getLogger(this.getClass()).debug("--> addToRepository");
//        if ((_workItemRepository.getWorkItem(this.getWorkItemID().toString())) != null) {
//
//            YWorkItem work = _workItemRepository.getWorkItem(this.getWorkItemID().toString());
//
//        }
//        _workItemRepository.addNewWorkItem(this);
//    }
//    public void setWorkItemID(YIdentifier anIdentifier, String aTaskID) {
//        this.setYIdentifier(anIdentifier);
//        this.setIdentifierString(anIdentifier.getId());
//        this.setTaskID(aTaskID);
//    }

    private Long _id;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getId() {
		return _id;
	}
    
	public void setId( Long id ) {
		_id = id;
	}
    
    @Basic
    public String getThisId() {
        return thisId;
    }

    public void setThisId(String a) {
        thisId = a;
    }

    public void setInitData(Element data) {
        _dataList = data;
        data_string = getDataString();
    }

    public void completeData(Document output) throws YPersistenceException {

        Element root = output.getRootElement();

        java.util.List list = root.getChildren();
        Iterator iter = list.listIterator();

        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            YawlEventLogger.getInstance().logData(child.getName(), child.getValue(), lastevent, "o");
        }

    }

    //added method
    public boolean hasLiveStatus() {
        return _status.equals(Status.Fired) || _status.equals(Status.Enabled) ||
               _status.equals(Status.Executing);
    }


    /***********************************/


    public YWorkItem(String specificationID, YIdentifier anIdentifier, String aTaskID,
                     boolean allowsDynamicCreation, boolean isDeadlocked) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("Spec=" + specificationID + " WorkItem=" + aTaskID);
        setYIdentifier(anIdentifier);
        setIdentifierString(getYIdentifier().toString());
        setTaskID(aTaskID);
        setIDString(getYIdentifier().toString() + ":" + getTaskID());
        _specificationID = specificationID;
        _enablementTime = new Date();
        _status = isDeadlocked ? Status.Deadlocked : Status.Enabled;
        _allowsDynamicCreation = allowsDynamicCreation;
//        _workItemRepository.addNewWorkItem(this);

        /***************************/
        /* INSERTED FOR LOGGING/PERSISTANCE */
        /********************/

        try {

            YawlEventLogger.getInstance().logWorkItemEvent(getYIdentifier().toString(),
                    getTaskID()
                    , _status, _whoStartedMe, _specificationID);
	    setThisId(this.getIDString());
//        YPersistance.getInstance().storeData(this);
// TODO           if (pmgr != null) {
//                pmgr.storeObject(this);
//            }



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
    private YWorkItem(YIdentifier anIdentifier, String aTaskID, String specificationID,
                      Date workItemCreationTime, YWorkItem parent,
                      boolean allowsDynamicInstanceCreation) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("Spec=" + specificationID + " WorkItem=" + getTaskID());

//        _workItemID = workItemID;
        setYIdentifier(anIdentifier);
        setIdentifierString(getYIdentifier().toString());
        setTaskID(aTaskID);
        setIDString(getYIdentifier().toString() + ":" + getTaskID());
        _specificationID = specificationID;
        _enablementTime = workItemCreationTime;
        _firingTime = new Date();
        _parent = parent;
        _status = Status.Fired;
//        _workItemRepository.addNewWorkItem(this);
        _allowsDynamicCreation = allowsDynamicInstanceCreation;
        /***************************/
        /* INSERTED FOR LOGGING/PERSISTANCE */
        /********************/
        YawlEventLogger.getInstance().logWorkItemEvent(getYIdentifier().toString(),
                getTaskID()
                , _status, _whoStartedMe, _specificationID);
	setThisId(getIDString());
    }

    public YWorkItem createChild(YIdentifier childCaseID) throws YPersistenceException {
        if (this._parent == null) {
            YIdentifier parentCaseID = getYIdentifier();
            if (childCaseID.getParent() != parentCaseID) {
                return null;
            }
            YWorkItem childItem = new YWorkItem(childCaseID, getTaskID(),
                    _specificationID,
                    getEnablementTime(),
                    this,
                    _allowsDynamicCreation
            );

            /*
              MODIFIED FOR PERSISTANCE
             */
            setStatus(Status.IsParent);
            _children.add(childItem);
            if (_children.size() == 1) {
                YawlEventLogger.getInstance().logWorkItemEvent(getYIdentifier().toString(),
                        getTaskID()
                        , _status, _whoStartedMe, _specificationID);
            }
            return childItem;
        }
    	System.out.println("already has parent: " + _parent);
        return null;
    }

    public void setStatusToDelete() throws YPersistenceException {
        setStatus(Status.Cancelled);
    }

    public void setStatusToStarted(String userName) throws YPersistenceException {
        if (_status != Status.Fired) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + Status.Executing + "\"]");
        }
        setStatus(Status.Executing);

        _startTime = new Date();
        _whoStartedMe = userName;

        lastevent = YawlEventLogger.getInstance().logWorkItemEvent(getYIdentifier().toString(),
                getTaskID()
                , _status, _whoStartedMe, _specificationID);
        /****************************/
    }


    public void setStatusToComplete(boolean force) throws YPersistenceException {
        Status completionStatus = force ? Status.ForcedComplete : Status.Complete ;

        if (!((_status == Status.Executing) || (_status == Status.Suspended))) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + completionStatus + "\"]");
        }

        setStatus(Status.Complete);

        YawlEventLogger.getInstance().logWorkItemEvent(_parent.getYIdentifier().toString(),
                _parent.getTaskID()
                , _status, _whoStartedMe, _specificationID);

        YawlEventLogger.getInstance().logWorkItemEvent(getYIdentifier().toString(),
                getTaskID()
                , _status, _whoStartedMe, _specificationID);
        /************************************/
    }
    
    @Transient
    public boolean isParentComplete() {
    	if( getParent() != null ) {
	        /*
	         * Check if all siblings are completed, if so then
	         * the parent is completed too.
	         * */
	        Set<YWorkItem> siblings = getParent().getChildren();
	
	        for (YWorkItem sibling : siblings) {
	            if ((sibling.getStatus() != Status.Complete) &&
	                (sibling.getStatus() != Status.ForcedComplete))
	                return false;
	        }
	        return true;
    	}
        return false;
    }


    public void setStatusToDeleted(boolean fail) throws YPersistenceException {

        Status completionStatus = fail ? Status.Failed : Status.Cancelled ;

        if (!(_status.equals(Status.Executing) || _status.equals(Status.Suspended))) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be changed to \"" + completionStatus + "\"]");
        }
        setStatus(completionStatus);

        YawlEventLogger.getInstance().logWorkItemEvent(_parent.getYIdentifier().toString(),
                _parent.getTaskID()
                , _status, _whoStartedMe, _specificationID);
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
//            if ((! mysibling.getStatus().equals(YWorkItem.statusComplete)) &&
//                (! mysibling.getStatus().equals(YWorkItem.statusForcedComplete)))
//                parentcomplete = false;
//        }
//
//  todo      if (pmgr != null) {
//            pmgr.deleteObject(this);
//            if (parentcomplete) {
//                pmgr.deleteObject(_parent);
//            }
//        }

        YawlEventLogger.getInstance().logWorkItemEvent(
                _parent.getYIdentifier().toString(), _parent.getTaskID(),
                _status, _whoStartedMe, _specificationID);

        YawlEventLogger.getInstance().logWorkItemEvent(
                getYIdentifier().toString(), getTaskID(),
                _status, _whoStartedMe, _specificationID);
    }




    public void rollBackStatus() throws YPersistenceException {
        if (!_status.equals(Status.Executing)) {
            throw new RuntimeException(this + " [when current status is \""
                    + _status + "\" it cannot be rolledBack to \"" + Status.Fired + "\"]");
        }
        setStatus(Status.Fired);
        YawlEventLogger.getInstance().logWorkItemEvent(getYIdentifier().toString(),
                getTaskID()
                , _status, _whoStartedMe, _specificationID);
        /*************************/

        _startTime = null;
        _whoStartedMe = null;
    }
    
    //added method
    public void setStatusToSuspended() throws YPersistenceException {
        if (hasLiveStatus()) {
            _prevStatus = _status ;
            setStatus(Status.Suspended);

            YawlEventLogger.getInstance().logWorkItemEvent(
                    getYIdentifier().toString(), getTaskID(),
                    _status, _whoStartedMe, _specificationID);
        }
        else throw new RuntimeException(this + " [when current status is \""
                                + _status + "\" it cannot be changed to \"Suspended\".]");
    }

    //added method
    public void setStatusToUnsuspended() throws YPersistenceException {
        setStatus(_prevStatus);
        _prevStatus = null ;

        YawlEventLogger.getInstance().logWorkItemEvent(
                getYIdentifier().toString(), getTaskID(),
                _status, _whoStartedMe, _specificationID);
    }


    public void setData(Element data) throws YPersistenceException {
        _dataList = data;

        data_string = getDataString();

        // XXX: FIXME: is this expected behavior?
        if( data == null ) {
        	throw new RuntimeException( "is this expected behavior?" );
        }
        java.util.List list = data.getChildren();
        Iterator iter = list.listIterator();
        /*
          FOR LOGGING
         */
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            YawlEventLogger.getInstance().logData(child.getName(), child.getValue(), lastevent, "i");
        }
    }


    //#################################################################################
    //                              accessors
    //#################################################################################
    private String identifierString;

	public String getIdentifierString() {
		return identifierString;
	}

	private void setIdentifierString(String identifierString) {
		this.identifierString = identifierString;
	}

    private YIdentifier identifier;
    
    
    @OneToOne(fetch = FetchType.EAGER)
    public YIdentifier getYIdentifier() {
    	return identifier;
    }
    
    private void setYIdentifier(YIdentifier anIdentifier) {
    	this.identifier = anIdentifier;
    }
    
    private String taskID;
    
    @Basic
    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String aTaskID) {
    	this.taskID = aTaskID;
    }

    
    
//    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
//    @OnDelete(action=OnDeleteAction.CASCADE)
//    public YWorkItemID getWorkItemID() {
//        return _workItemID;
//    }

    @Basic
    public Date getEnablementTime() {
        return _enablementTime;
    }
    
    /**
     * Inserted for hibernate
     * @param time
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setEnablementTime(Date time) {
    	_enablementTime = time;
    }

    @Transient
    public String getEnablementTimeStr() {
        return _df.format(_enablementTime);
    }

    @Basic
    public Date getFiringTime() {
        return _firingTime;
    }
    
    /**
     * Inserted for hibernate 
     * @param time
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setFiringTime(Date time) {
    	_firingTime = time;
    }

    @Transient
    public String getFiringTimeStr() {
        return _df.format(_firingTime);
    }


    /**
     * @hibernate.property column="start_time"
     */
    @Basic
    public Date getStartTime() {
        return _startTime;
    }
    
    /**
     * Inserted for hibernate
     * @param time
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setStartTime(Date time) {
    	_startTime = time;
    }

    @Transient
    public String getStartTimeStr() {
        return _df.format(_startTime);
    }

    @Basic
    public Status getStatus() {
        return _status;
    }

    public void setStatus(Status status) {
        this._status = status;
    }

    @Basic
    public Status getPrevStatus() {
        return _prevStatus;
    }

    public void setPrevStatus(Status status) {
        this._prevStatus = status;
    }

    @ManyToOne
    public YWorkItem getParent() {
        return _parent;
    }
    
    /**
     * Inserted for hibernate
     * @param item
     */
    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setParent(YWorkItem item) {
    	_parent = item;
    }

    @OneToMany(mappedBy="parent", fetch=FetchType.EAGER)
    public Set<YWorkItem> getChildren() {
        return _children;
    }

    @SuppressWarnings({"UNUSED_SYMBOL"})
    private void setChildren(Set<YWorkItem> children) {
    	_children = children;
    }


//    @Transient
//    public YIdentifier getCaseID() {
//        return _workItemID.getCaseID();
//    }

//    @Transient
//    public String getTaskID() {
//        return _workItemID.getTaskID();
//    }
//

    public String getIDString() {
    	return idString;
//        return getYIdentifier().toString() + ":" + getTaskID();
    }

    private String idString;
    
    private void setIDString(String anIDString) {
    	this.idString = anIDString;
    }
    

    public String toString() {
        String fullClassName = getClass().getName();
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1) + ":" + getIDString();
    }

    @Basic
    public String getUserWhoIsExecutingThisItem() {
        if (_status == Status.Executing) {
            return _whoStartedMe;
        } else
            return null;
    }
    
    //todo Q by LA: do we need this method 4 hibernate? otherwise delete
    private void setUserWhoIsExecutingThisItem(String person) {
    	_whoStartedMe = person;
    }

    
    public boolean allowsDynamicCreation() {
        return _allowsDynamicCreation;
    }
    
    @Basic
    public boolean getAllowsDynamicCreation() {
        return _allowsDynamicCreation;
    }
    
    //todo Q by LA: do we need this method 4 hibernate? otherwise delete
    private void setAllowsDynamicCreation(boolean b) {
    	_allowsDynamicCreation = b;
    }

    @Transient
    public String getDataString() {
        if (_dataList != null) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(_dataList);
        }
        return null;
    }
    
    /**
     * Inserted for hibernate
     * @return datalist
     */
    @Lob
    private String getDataList() {
        if (_dataList != null) {        	
        	Document d = new Document((Element) _dataList.clone());
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            return outputter.outputString(d);
        }
        return null;
    }
    
    /**
     * Inserted for hibernate
     * @param s
     */
    private void setDataList(String s) {
    	if (s!=null) {
    		try {
				SAXBuilder builder = new SAXBuilder();
				Document d = builder.build( new StringReader(s));
				_dataList = d.getRootElement();
			}
			catch( JDOMException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch( IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }


    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<workItem>");
        xmlBuff.append("<taskID>").append(getTaskID()).append("</taskID>");
        xmlBuff.append("<caseID>").append(getYIdentifier()).append("</caseID>");
        xmlBuff.append("<uniqueID>").append(getId()).append("</uniqueID>");
        xmlBuff.append("<specID>").append(_specificationID).append("</specID>");
        xmlBuff.append("<status>").append(getStatus()).append("</status>");
        if (_dataList != null) {
            xmlBuff.append("<data>").append(getDataString())
                    .append("</data>");
        }
        xmlBuff.append("<enablementTime>")
                .append(_df.format(getEnablementTime()))
                .append("</enablementTime>");
        if (this.getFiringTime() != null) {
            xmlBuff.append("<firingTime>")
                    .append(_df.format(getFiringTime()))
                    .append("</firingTime>");
        }
        if (this.getStartTime() != null) {
            xmlBuff.append("<startTime>")
                    .append(_df.format(getStartTime()))
                    .append("</startTime>");
            xmlBuff.append("<assignedTo>")
                    .append(getUserWhoIsExecutingThisItem())
                    .append("</assignedTo>");
        }
        xmlBuff.append("</workItem>");
        return xmlBuff.toString();
    }

//    @Transient
//    private String getUniqueID() {
////        return _workItemID.getUniqueID();
//    	return getId();
//    }

    @Basic
    public String getSpecificationID() {
        return _specificationID;
    }
    
    //todo Q by LA: needed 4 hibernate ?
    private void setSpecificationID(String specificationID) {
    	_specificationID = specificationID;
    }

    @Transient
    public boolean isEnabledSuspended() {
        return _status.equals(Status.Suspended) && _prevStatus.equals(Status.Enabled);
    }
    
    @Transient
    public YIdentifier getCaseID() {
    	return getYIdentifier();
    }
}
