/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

public class YLogData {
    private String eventid = null;

    private String port = null;

    private String value = null;

    private String io = null;

    private String rowkey = null;

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getRowkey() {
        return this.rowkey;
    }

    public void setEventid(String eventid) {
        this.eventid = eventid;
    }

    public String getEventid() {
        return this.eventid;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return this.port;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }


    public void setIo(String io) {
        this.io = io;
    }

    public String getIo() {
        return this.io;
    }

}