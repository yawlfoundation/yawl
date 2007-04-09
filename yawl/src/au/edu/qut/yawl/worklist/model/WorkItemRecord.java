/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import java.io.IOException;
import java.io.StringReader;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.engine.domain.YWorkItem;

/**
 * 
 * @author Lachlan Aldred
 * Date: 2/02/2004
 * Time: 18:30:18
 * 
 * This class is made persistence-enabled in case that a
 * custom yawl service needs to store it in case of a 
 * failure
 * 
 */
@Entity
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

    @Basic
    public String getTaskID() {
        return _taskID;
    }

    @Basic
    public String getCaseID() {
        return _caseID;
    }

    @Basic
    public String getEnablementTime() {
        return _enablementTime;
    }

    @Basic
    public String getFiringTime() {
        return _firingTime;
    }
    @Basic
    public String getCompletionTime() {
        return _completionTime;
    }

    @Basic
    public String getStartTime() {
        return _startTime;
    }

    @Basic
    public String getStatusString() {
    	return _status.name();
    }
    public void setStatusString(String status) {
    	_status = YWorkItem.Status.valueOf(status);
    }
    
    
    @Transient
    public YWorkItem.Status getStatus() {
        return _status;
    }

    @Basic
    public String getWhoStartedMe() {
        return _whoStartedMe;
    }
    public void setWhoStartedMe(String person) {
    	_whoStartedMe = person;
    }

    @Transient
    public Element getWorkItemData() {
        return _dataList;
    }


    @Basic
    public String getDataListString() {
    	if(_dataList != null)
    		return outPretty.outputString(_dataList);
    	return "";
    }
    public void setDataListString(String s) {
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

    @Transient
    public String getOutputDataString() {
        return "stubbed data";
    }


    @Basic
    public String getSpecificationID() {
        return _specificationID;
    }

    @Transient
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
    
    public String toYWorkItemXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<workItem>");
        xmlBuff.append("<taskID>").append(getTaskID()).append("</taskID>");
        xmlBuff.append("<caseID>").append(this.getCaseID()).append("</caseID>");
        xmlBuff.append("<uniqueID>").append(getUniqueID()).append("</uniqueID>");
        xmlBuff.append("<specID>").append(_specificationID).append("</specID>");
        xmlBuff.append("<status>").append(getStatus()).append("</status>");
        if (_dataList != null) {
            xmlBuff.append("<data>").append(getDataListString())
                    .append("</data>");
        }
        xmlBuff.append("<enablementTime>")
                .append(getEnablementTime())
                .append("</enablementTime>");
        if (this.getFiringTime() != null) {
            xmlBuff.append("<firingTime>")
                    .append(getFiringTime())
                    .append("</firingTime>");
        }
        if (this.getStartTime() != null) {
            xmlBuff.append("<startTime>")
                    .append(getStartTime())
                    .append("</startTime>");
            xmlBuff.append("<assignedTo>")
                    .append(this.getWhoStartedMe())
                    .append("</assignedTo>");
        }
        xmlBuff.append("</workItem>");
        return xmlBuff.toString();
    }

    public void setUniqueID(String uniqueID) {
        _uniqueID = uniqueID;
    }

    @Id
    @Basic
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

