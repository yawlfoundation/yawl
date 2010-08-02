/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An object representing one resourcing event for logging.
 *
 * @author Michael Adams
 * Date: 23/08/2009
 */
public class ResourceEvent extends BaseEvent implements Cloneable {

    private long _specKey;                                       // FK to SpecLog table
    private String _caseID ;
    private String _taskID ;
    private String _itemID ;
    private String _participantID ;


    public ResourceEvent() {}                                    // for reflection

    /** Constructor for item level events **/
    public ResourceEvent(long specKey, WorkItemRecord wir, String pid, EventLogger.event eType) {
        this(specKey, wir.getCaseID(), pid, eType);
        _taskID = wir.getTaskName(); 
        _itemID = wir.getID();
    }

    /** Constrcutor for case level events **/
    public ResourceEvent(long specKey, String caseID, String pid, EventLogger.event eType) {
        super(eType.name());
        _specKey = specKey;
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

    public String get_caseID() { return _caseID; }

    public void set_caseID(String caseID) { _caseID = caseID; }


    public String get_taskID() { return _taskID; }

    public void set_taskID(String taskID) { _taskID = taskID; }


    public String get_itemID() { return _itemID; }

    public void set_itemID(String itemID) {_itemID = itemID; }


    public String get_participantID() { return _participantID; }

    public void set_participantID(String participantID) { _participantID = participantID;}


    public long get_specKey() { return _specKey; }

    public void set_specKey(long specKey) { _specKey = specKey; }


    public String toXML() {
        StringBuilder xml = new StringBuilder(String.format("<event key=\"%d\">", _id));
        xml.append(StringUtil.wrap(String.valueOf(_specKey), "speckey"))
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
        _specKey = strToLong(xml.getChildText("speckey"));
        _caseID = xml.getChildText("caseid");
        _taskID = xml.getChildText("taskid");
        _itemID = xml.getChildText("itemid");
        _participantID = xml.getChildText("participantid");
        _event = xml.getChildText("eventtype") ;
        _timeStamp = strToLong(xml.getChildText("timestamp"));
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

