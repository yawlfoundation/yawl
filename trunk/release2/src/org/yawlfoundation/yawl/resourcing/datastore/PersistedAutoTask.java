package org.yawlfoundation.yawl.resourcing.datastore;

import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

/**
 * Simple class to facilitate the persistence of a checkedout automated task.
 * 
 * Author: Michael Adams
 * Creation Date: 2/09/2008
 */
public class PersistedAutoTask {

    private long _id;                                    // hibernate primary key
    private String _wirStr;
    private String _wirID;


    public PersistedAutoTask() {}

    public PersistedAutoTask(WorkItemRecord wir) {
        _wirID = wir.getID();
        _wirStr = wir.toXML();
        Persister.getInstance().insert(this);                
    }

    public void unpersist() {
        Persister.getInstance().delete(this);
    }

    public WorkItemRecord getWIR() {
        if (_wirStr != null)
            return Marshaller.unmarshalWorkItem(_wirStr);
        else
            return null;
    }


    public String get_wirStr() { return _wirStr; }

    public void set_wirStr(String wirStr) { _wirStr = wirStr; }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_wirID() { return _wirID; }

    public void set_wirID(String wirID) { _wirID = wirID; }
}
