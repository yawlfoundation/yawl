/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving workflow
 *  technology.
 */

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * An instantiation of this class represents one row of data in the event logging
 * table 'log_WorkItemDataEvent'. Called by YEventLogger to record input values when
 * a workitem is enabled, and output values when a workitem completes.
 *
 * Refactored for v2.0 by Michael Adams
 * 09/10/2007
 */

public class YWorkItemDataEvent {

    private String _rowkey;                            // auto-generated hibernate key
    private String _childWorkItemEventID;              // workitem that owns this data
    private String _param;                             // data attribute name
    private String _value;                             // data attribute value
    private char _io;                                  // is attribute input or output?


    // CONSTRUCTORS //

    public YWorkItemDataEvent() {}                     // required for hibernate


    public YWorkItemDataEvent(String rowkey, String workItemEventID,
                              String param, String value, char io) {
        _rowkey = rowkey ;
        _childWorkItemEventID = workItemEventID;
        _param = param;
        _value = value;
        _io = io;
    }

    /********************************************************************************/

    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<WorkItemDataEvent id=\"%s\">", _rowkey)) ;
        xml.append(StringUtil.wrap(_childWorkItemEventID, "childWorkItemEventID"));
        xml.append(StringUtil.wrap(_param, "param"));
        xml.append(StringUtil.wrap(_value, "value"));
        xml.append(StringUtil.wrap(String.valueOf(_io), "io"));
        xml.append("</WorkItemDataEvent>");
        return xml.toString() ;
    }

    
    // GETTERS AND SETTERS FOR HIBERNATE //

    public String get_rowkey() { return _rowkey; }

    public void set_rowkey(String rowkey) { _rowkey = rowkey; }


    public String get_childWorkItemEventID() { return _childWorkItemEventID; }

    public void set_childWorkItemEventID(String workItemEventID) {
        _childWorkItemEventID = workItemEventID;
    }


    public String get_param() { return _param; }

    public void set_param(String param) { _param = param; }


    public String get_value() { return _value; }

    public void set_value(String value) { _value = value; }


    public char get_io() { return _io; }

    public void set_io(char io) { _io = io; }

}