/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.datastore;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.resourcing.datastore.persistence.Persister;

import java.util.HashMap;


/**
 * A workitem record hashmap with added persistence.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 15/09/2007
 */

public class WorkItemCache extends HashMap<String, WorkItemRecord> {

    private Persister _persister;
    private WorkItemCache _me ;
    private boolean _persistOn = false ;

    public WorkItemCache() {
        super();
        _me = this ;
    }

    public WorkItemCache(boolean persist) {
        this() ;
        _persistOn = persist ;
        if (_persistOn) _persister = Persister.getInstance();
    }


    public WorkItemCache getInstance() {
        if (_me == null) _me = new WorkItemCache() ;
        return _me ;
    }

    public void setPersist(boolean persist) { _persistOn = persist ; }

    public boolean isPersistOn() { return _persistOn ; }


    public WorkItemRecord add(WorkItemRecord wir) {
        return this.put(wir.getID(), wir) ;
    }

    public WorkItemRecord remove(WorkItemRecord wir) {
        return this.remove(wir.getID());
    }

    public WorkItemRecord update(WorkItemRecord wir) {
        return this.put(wir.getID(), wir) ;
    }


    // OVERRIDES //

    public WorkItemRecord put(String id, WorkItemRecord wir) {
        if (_persistOn) {
            if (containsKey(id))
                _persister.update(wir) ;
            else
                _persister.insert(wir) ;
        }
        return super.put(id, wir);
    }

    public WorkItemRecord remove(String id) {
        if (_persistOn) _persister.delete(get(id)) ;
        return super.remove(id);
    }

}
