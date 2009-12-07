package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

/**
 * Author: Michael Adams
 * Creation Date: 22/10/2009
 */
public class AuditEvent {

    private long _id ;                                           // hibernate PK
    private String _userid ;
    private String _event ;
    private long _timeStamp ;

    public AuditEvent() { }

    public AuditEvent(String userid, EventLogger.audit event) {
        _userid = userid;
        _event = event.name();
        _timeStamp = System.currentTimeMillis();
    }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_userid() { return _userid; }

    public void set_userid(String userid) { _userid = userid; }

    public String get_event() { return _event; }

    public void set_event(String event) {_event = event; }

    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) {_timeStamp = timeStamp; }
    
}
