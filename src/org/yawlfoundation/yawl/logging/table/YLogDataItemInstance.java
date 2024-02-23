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

package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logDataItem table, representing a data item associated with an event.
 * Note that an event may have a number of data items associated with it.
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogDataItemInstance {

    private long dataItemID;                            // PK - auto generated
    private long eventID;                               // FK to YLogEvent
    private YLogDataItem dataItem ;
    private long dataTypeID;                            // FK to YLogDataType

    public YLogDataItemInstance() {
        dataItem = new YLogDataItem();
    }

    public YLogDataItemInstance(long eventID, YLogDataItem dataItem, long dataTypeID) {
        this.eventID = eventID;
        this.dataItem = dataItem;
        this.dataTypeID = dataTypeID;
    }

    public long getDataItemID() {
        return dataItemID;
    }

    public void setDataItemID(long dataItemID) {
        this.dataItemID = dataItemID;
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public YLogDataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(YLogDataItem dataItem) {
        this.dataItem = dataItem;
    }

    public long getDataTypeID() {
        return dataTypeID;
    }

    public void setDataTypeID(long dataTypeID) {
        this.dataTypeID = dataTypeID;
    }

    public String getName() {
        return dataItem.getName();
    }

    public void setName(String name) {
        dataItem.setName(name);
    }

    public String getValue() {
        return dataItem.getValue();
    }

    public void setValue(String value) {
        dataItem.setValue(value);
    }

    public String getDataType() {
        return dataItem.getDataTypeName() ;
    }

    public void setDataType(String dataType) {
        dataItem.setDataTypeName(dataType);
    }

    public String getDescriptor() {
        return dataItem.getDescriptor();
    }

    public void setDescriptor(String descriptor) {
        dataItem.setDescriptor(descriptor);
    }

    public boolean equals(Object other) {
        return (other instanceof YLogDataItemInstance) &&
                (this.getDataItemID() == ((YLogDataItemInstance) other).getDataItemID());
    }

    public int hashCode() {
        return (int) (31 * getDataItemID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(330);
        xml.append(String.format("<dataitem key=\"%d\">", dataItemID));
        xml.append(dataItem.toXMLShort());
        xml.append(StringUtil.wrap(String.valueOf(eventID), "eventKey"));
        xml.append(StringUtil.wrap(String.valueOf(dataTypeID), "datatypeKey"));
        xml.append("</dataitem>");
        return xml.toString();
    }

}
