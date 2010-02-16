/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An object representing one resourcing event for logging.
 *
 * @author: Michael Adams
 * Date: 23/08/2009
 */
public class ResourceEvent extends BaseEvent implements Cloneable {

    private YSpecificationID _specID ;
    private String _caseID ;
    private String _taskID ;
    private String _itemID ;
    private String _participantID ;


    public ResourceEvent() {}                                    // for reflection

    /** Constructor for item level events **/
    public ResourceEvent(WorkItemRecord wir, String pid, EventLogger.event eType) {
        this(new YSpecificationID(wir), wir.getCaseID(), pid, eType);
        _taskID = wir.getTaskName(); 
        _itemID = wir.getID();
    }

    /** Constrcutor for case level events **/
    public ResourceEvent(YSpecificationID specID, String caseID, String pid, EventLogger.event eType) {
        super(eType.name());
        _specID = specID;
        _caseID = caseID;
        _participantID = pid;
    }

    /** Constructor for unmarshalling from xml **/
    public ResourceEvent(Element xml) {
        super();
        fromXML(xml);
    }

    public final ResourceEvent clone() {
        try {
            return (ResourceEvent) super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            return null;
        }
    }


    // GETTERS & SETTERS

    public YSpecificationID get_specID() { return _specID; }

    public void set_specID(YSpecificationID specID) { _specID = specID; }


    public String get_caseID() { return _caseID; }

    public void set_caseID(String caseID) { _caseID = caseID; }


    public String get_taskID() { return _taskID; }

    public void set_taskID(String taskID) { _taskID = taskID; }


    public String get_itemID() { return _itemID; }

    public void set_itemID(String itemID) {_itemID = itemID; }


    public String get_participantID() { return _participantID; }

    public void set_participantID(String participantID) { _participantID = participantID;}


    public String toXML() {
        StringBuilder xml = new StringBuilder(String.format("<event key=\"%d\">", _id));
        xml.append(_specID.toXML())
           .append(StringUtil.wrap(_caseID, "caseid"))
           .append(StringUtil.wrap(_taskID, "taskid"))
           .append(StringUtil.wrap(_itemID, "itemid"))
           .append(StringUtil.wrap(_participantID, "participantid"))
           .append(super.toXML())
           .append("</event>") ;
        return xml.toString();
    }


    public void fromXML(Element xml) {
        _id = strToLong(xml.getAttributeValue("key"));
        _caseID = xml.getChildText("caseid");
        _taskID = xml.getChildText("taskid");
        _itemID = xml.getChildText("itemid");
        _participantID = xml.getChildText("participantid");
        _event = xml.getChildText("eventtype") ;
        _timeStamp = strToLong(xml.getChildText("timestamp"));

        Element specid = xml.getChild("specificationid") ;
        if (specid != null) {
            _specID = new YSpecificationID(specid.getChildText("identifier"),
                                           specid.getChildText("version"),
                                           specid.getChildText("uri"));
        }
    }


    private long strToLong(String value) {
        try {
            return new Long(value);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }

}

