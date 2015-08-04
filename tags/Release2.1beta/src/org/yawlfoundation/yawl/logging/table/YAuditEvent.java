package org.yawlfoundation.yawl.logging.table;

/**
 * Author: Michael Adams
 * Creation Date: 22/10/2009
 */
public class YAuditEvent {

    public static enum Action { logon, logoff, invalid, unknown, shutdown, expired }

    private long _id ;                                           // hibernate PK
    private String _username;
    private String _event ;
    private long _timeStamp ;

    public YAuditEvent() { }

    public YAuditEvent(String username, Action event) {
        _username = username;
        _event = event.name();
        _timeStamp = System.currentTimeMillis();
    }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_username() { return _username; }

    public void set_username(String userid) { _username = userid; }

    public String get_event() { return _event; }

    public void set_event(String event) {_event = event; }

    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) {_timeStamp = timeStamp; }

}