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
