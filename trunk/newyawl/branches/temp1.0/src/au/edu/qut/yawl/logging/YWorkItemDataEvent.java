/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving workflow
 *  technology.
 */

package au.edu.qut.yawl.logging;

/**
 * An instantiation of this class represents one row of data in the event logging
 * table 'log_WorkItemDataEvent'. Called by YEventLogger to record input values when
 * a workitem is enabled, and output values when a workitem completes.
 *
 * Refactored for v1.0 by Michael Adams
 * 09/10/2007
 */

public class YWorkItemDataEvent {

    private String _rowkey;                            // auto-generated hibernate key
    private String _workItemEventID;                   // workitem that owns this data
    private String _param;                             // data attribute name
    private String _value;                             // data attribute value
    private char _io;                                  // is attribute input or output?


    // CONSTRUCTORS //

    public YWorkItemDataEvent() {}                     // required for hibernate


    public YWorkItemDataEvent(String rowkey, String workItemEventID,
                              String param, String value, char io) {
        _rowkey = rowkey ;
        _workItemEventID = workItemEventID;
        _param = param;
        _value = value;
        _io = io;
    }

    
    // GETTERS AND SETTERS FOR HIBERNATE //

    public String get_rowkey() { return _rowkey; }

    public void set_rowkey(String rowkey) { _rowkey = rowkey; }


    public String get_workItemEventID() { return _workItemEventID; }

    public void set_workItemEventID(String workItemEventID) {
        _workItemEventID = workItemEventID;
    }


    public String get_param() { return _param; }

    public void set_param(String param) { _param = param; }


    public String get_value() { return _value; }

    public void set_value(String value) { _value = value; }


    public char get_io() { return _io; }

    public void set_io(char io) { _io = io; }

}