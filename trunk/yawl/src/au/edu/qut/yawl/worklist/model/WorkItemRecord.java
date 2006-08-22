/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import au.edu.qut.yawl.engine.domain.YWorkItem;

/**
 * 
 * @author Lachlan Aldred
 * Date: 2/02/2004
 * Time: 18:30:18
 * 
 */
public class WorkItemRecord {
    private String _taskID;
    private String _caseID;
    private String _enablementTime;
    private String _firingTime;
    private String _startTime;
    private YWorkItem.Status _status;
    private String _whoStartedMe;
    private Element _dataList;
    private String _specificationID;
    private String _uniqueID;

    private XMLOutputter outPretty = new XMLOutputter(Format.getPrettyFormat());
    private XMLOutputter outCompact = new XMLOutputter(Format.getCompactFormat());

    /*
      Inserted for the admin tool
      Required to generate an empty record
     */
    private String _completionTime;
    private Category _logger = Logger.getLogger(getClass());


    public void setTaskID(String taskID) {
    this._taskID = taskID;
    }

    public void setCaseID(String caseID) {
        this._caseID = caseID;
    }

    public void setSpecificationID(String specificationID) {
    this._specificationID = specificationID;
    }

    public void setEnablementTime(String enablementTime) {
    this._enablementTime = enablementTime;
    }

    public void setStatus(YWorkItem.Status status) {
        _status = status;
    }


    public WorkItemRecord() {
    }

    public WorkItemRecord(String caseID, String taskID, String specificationID,
                          String enablementTime, String status) {
        _taskID = taskID;
        _caseID = caseID;
        _specificationID = specificationID;
        _enablementTime = enablementTime;
        _status = YWorkItem.Status.valueOf(status);
    }

    public void setFiringTime(String firingTime) {
        this._firingTime = firingTime;
    }
    public void setCompletionTime(String completionTime) {
        this._completionTime = completionTime;
    }

    public void setStartTime(String startTime) {
        this._startTime = startTime;
    }

    public void setAssignedTo(String whoStartedMe) {
        this._whoStartedMe = whoStartedMe;
    }

    public String getAssignedTo() {
    return this._whoStartedMe;
    }

    public void setDataList(Element dataList) {
        this._dataList = dataList;
    }

    public String getTaskID() {
        return _taskID;
    }

    public String getCaseID() {
        return _caseID;
    }

    public String getEnablementTime() {
        return _enablementTime;
    }

    public String getFiringTime() {
        return _firingTime;
    }
    public String getCompletionTime() {
        return _completionTime;
    }

    public String getStartTime() {
        return _startTime;
    }

    public YWorkItem.Status getStatus() {
        return _status;
    }

    public String getWhoStartedMe() {
        return _whoStartedMe;
    }


    public Element getWorkItemData() {
        return _dataList;
    }


    public String getDataListString() {
        return outPretty.outputString(_dataList);
    }


    public String getOutputDataString() {
        return "stubbed data";
    }


    public String getSpecificationID() {
        return _specificationID;
    }

    public String getID() {
        return _caseID + ":" + _taskID;
    }

    public String toXML() {
        return
                "<itemRecord>" +
                "<id>" + _taskID + ":" + _caseID + "</id>" +
                "<status>" + _status + "</status>" +
                "<user>" + _whoStartedMe + "</user>" +
                ((_dataList == null) ?
                        "<data/>" :
                        "<data>" + outCompact.outputString(_dataList) + "</data>") +
                "<specid>" + _specificationID + "</specid>" +
                "</itemRecord>";
    }

    public void setUniqueID(String uniqueID) {
        _uniqueID = uniqueID;
    }

    public String getUniqueID() {
        return _uniqueID;
    }

    //added method
    public boolean hasLiveStatus() {
        return _status.equals(YWorkItem.Status.Fired) ||
               _status.equals(YWorkItem.Status.Enabled) ||
               _status.equals(YWorkItem.Status.Executing);
    }
    
}

