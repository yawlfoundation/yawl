package au.edu.qut.yawl.resourcing.datastore.eventlog;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;

/**
 * Created by IntelliJ IDEA. User: Default Date: 23/08/2007 Time: 20:40:55 To change this
 * template use File | Settings | File Templates.
 */
public class ResourceEvent {

    private long _id ;                                           // hibernate PK
    private String _specID ;
    private String _caseID ;
    private String _taskID ;
    private String _itemID ;
    private String _participantID ;
    private String _event ;
    private long _timeStamp ;

    public ResourceEvent() {}                                    // for reflection


    public ResourceEvent(WorkItemRecord wir, String pid, EventLogger.event eType) {
        _specID = wir.getSpecificationID();
        _caseID = wir.getCaseID();
        _taskID = wir.getTaskID();
        _itemID = wir.getID();
        _participantID = pid;
        _event = eType.name() ;
        _timeStamp = System.currentTimeMillis();

    }

    // GETTERS & SETTERS

    public String get_specID() { return _specID; }

    public void set_specID(String specID) { _specID = specID; }


    public String get_caseID() { return _caseID; }

    public void set_caseID(String caseID) { _caseID = caseID; }


    public String get_taskID() { return _taskID; }

    public void set_taskID(String taskID) { _taskID = taskID; }


    public String get_itemID() { return _itemID; }

    public void set_itemID(String itemID) {_itemID = itemID; }


    public String get_participantID() { return _participantID; }

    public void set_participantID(String participantID) { _participantID = participantID;}


    public String get_event() { return _event; }

    public void set_event(String event) { _event = event; }


    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) { _timeStamp = timeStamp; }


    private long get_id() { return _id; }

    private void set_id(long _id) { this._id = _id; }
}

