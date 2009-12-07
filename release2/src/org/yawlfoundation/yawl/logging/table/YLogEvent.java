package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logEvent table, representing a single net or task instance runtime event
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogEvent {

    private long eventID;                   // PK - auto generated
    private long instanceID;                // FK to YLogTaskInstance OR YLogNetInstance
    private String descriptor ;             // the type of event
    private long timestamp ;
    private long serviceID;                 // the service that created the event
    private long rootNetInstanceID;         // convenience for queries

    public YLogEvent() { }

    public YLogEvent(long instanceID, String descriptor, long timestamp,
                     long serviceID, long rootNetInstanceID) {
        this.instanceID = instanceID;
        this.descriptor = descriptor;
        this.timestamp = timestamp;
        this.serviceID = serviceID;
        this.rootNetInstanceID = rootNetInstanceID;
    }

    public long getEventID() {
        return eventID;
    }

    public void setEventID(long eventID) {
        this.eventID = eventID;
    }

    public long getInstanceID() {
        return instanceID;
    }

    public void setInstanceID(long instanceID) {
        this.instanceID = instanceID;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getServiceID() {
        return serviceID;
    }

    public void setServiceID(long serviceID) {
        this.serviceID = serviceID;
    }

    public long getRootNetInstanceID() {
        return rootNetInstanceID;
    }

    public void setRootNetInstanceID(long rootNetInstanceID) {
        this.rootNetInstanceID = rootNetInstanceID;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(240);
        xml.append(String.format("<event key=\"%s\">", eventID));
        xml.append(StringUtil.wrap(String.valueOf(instanceID), "instanceKey"));
        xml.append(StringUtil.wrap(descriptor, "descriptor"));
        xml.append(StringUtil.wrap(String.valueOf(timestamp), "timestamp"));
        xml.append(StringUtil.wrap(String.valueOf(serviceID), "serviceKey"));
        xml.append(StringUtil.wrap(String.valueOf(rootNetInstanceID), "rootNetInstanceKey"));
        xml.append("</event>");
        return xml.toString();
    }

}
