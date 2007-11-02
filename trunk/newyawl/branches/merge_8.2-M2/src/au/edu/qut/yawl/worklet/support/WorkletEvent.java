/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package au.edu.qut.yawl.worklet.support;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  The sole purpose of this class is to generate an event log record via Persistence.
 *  An instance is created via the constructor (from the EventLogger class), then the
 *  object is persisted to create one event log record.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8, 04-09/2006
 */

public class WorkletEvent {
    private String _event, _caseId, _specId, _taskId, _parentCaseId, _stamp;
    private int _xType;
    private long _id ;

    private	SimpleDateFormat _sdfe = new SimpleDateFormat("yyy.MM.dd hh:mm:ss:SS");


    private WorkletEvent() {}                  // empty cons. required for persistence

    /** the one and only constructor */
    public WorkletEvent(String event, String caseId, String specId,
                         String taskId, String parentCaseId, int xType) {
        _event = event;
        _caseId = caseId ;
        _specId = specId ;
        _taskId = taskId ;
        _parentCaseId = parentCaseId ;
        _xType = xType ;
        _stamp = _sdfe.format(new Date());
        _id = new Date().getTime();
    }

    /** getters & setters used by persistence */
    private String get_event() { return _event; }
    private String get_caseId() { return _caseId; }
    private String get_specId() { return _specId; }
    private String get_taskId() { return _taskId; }
    private String get_parentCaseId() { return _parentCaseId; }
    private int get_xType() { return _xType; }
    private String get_stamp() { return _stamp; }
    private long get_id() { return _id; }

    private void set_event(String s) { _event = s; }
    private void set_caseId(String s) { _caseId = s; }
    private void set_specId(String s) { _specId = s; }
    private void set_taskId(String s) { _taskId = s; }
    private void set_parentCaseId(String s) { _parentCaseId = s; }
    private void set_xType(int i) { _xType = i; }
    private void set_stamp(String s) { _stamp = s; }
    private void set_id(long lg) { _id = lg; }

}
