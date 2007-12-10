/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;

import java.util.Set;

/**
 * A repository of work queues belonging to a participant
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.1, 23/08/2007
 */

public class QueueSet {

    // participant queues
    private WorkQueue _qOffered ;
    private WorkQueue _qAllocated ;
    private WorkQueue _qStarted ;
    private WorkQueue _qSuspended ;

    // administrator queues
    private WorkQueue _qUnoffered ;
    private WorkQueue _qWorklisted ;

    private String _ownerID ;
    private setType _type ;
    private boolean _persisting ;
    private Persister _persist = Persister.getInstance();

    public enum setType { participantSet, adminSet }


    public QueueSet() {}

    public QueueSet(String pid, setType sType, boolean persisting) {
        _type = sType ;
        _persisting = persisting ;
        _ownerID = (_type == setType.participantSet) ? pid : "admin" ;
    }

    public String getID() { return _ownerID; }

    public void setID(String id) {
        _ownerID = id ;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            if (! isNullQueue(queue)) getQueue(queue).setOwnerID(id);
    }

    public void setPersisting(boolean persisting) { _persisting = persisting ; }

    public boolean getPersisting() { return _persisting; }
    

    public int getQueueSize(int queue) {
        if (isNullQueue(queue)) return 0 ;
        else return getQueue(queue).getQueueSize() ;
    }

    /*****************************************************************************/

    // Between Queue Actions //

    public void movetoSuspend(WorkItemRecord wir) {
        _qStarted.remove(wir);
        _qSuspended.add(wir);
    }

    public void movetoStarted(WorkItemRecord wir) {
        _qAllocated.remove(wir);
        _qStarted.add(wir) ;
    }

    public void movetoUnsuspend(WorkItemRecord wir) {
        _qSuspended.remove(wir);
        _qStarted.add(wir);
    }


    /*****************************************************************************/

    private boolean isNullQueue(int queue) {
        return getQueue(queue) == null ;
    }
    
    /** instantiates the queue if it is not yet instantiated */
    private void checkQueueExists(int queue) {
        System.out.println("checkQueueExists");
        if (isNullQueue(queue)) {
            switch (queue) {
                case WorkQueue.OFFERED    :
                    _qOffered  = new WorkQueue(_ownerID, queue, _persisting); break;
                case WorkQueue.ALLOCATED  :
                    _qAllocated = new WorkQueue(_ownerID, queue, _persisting); break;
                case WorkQueue.STARTED    :
                    _qStarted = new WorkQueue(_ownerID, queue, _persisting); break;
                case WorkQueue.SUSPENDED  :
                    _qSuspended = new WorkQueue(_ownerID, queue, _persisting); break;
                case WorkQueue.WORKLISTED :
                    _qWorklisted = new WorkQueue(_ownerID, queue, _persisting); break;
                case WorkQueue.UNOFFERED  :
                    _qUnoffered = new WorkQueue(_ownerID, queue, _persisting);
            }
        }
    }


    private WorkQueue getQueue(int queue) {
        switch (queue) {
            case WorkQueue.OFFERED    : return _qOffered;
            case WorkQueue.ALLOCATED  : return _qAllocated;
            case WorkQueue.STARTED    : return _qStarted;
            case WorkQueue.SUSPENDED  : return _qSuspended;
            case WorkQueue.WORKLISTED : return _qWorklisted;
            case WorkQueue.UNOFFERED  : return _qUnoffered;
        }
        return null ;
    }


    public void addToQueue(WorkItemRecord wir, int queue) {
        checkQueueExists(queue) ;
        getQueue(queue).add(wir);
    }


    public void addToQueue(int queue, WorkQueue queueToAdd) {
        checkQueueExists(queue) ;
        getQueue(queue).addQueue(queueToAdd);
    }


    public void removeFromQueue(WorkItemRecord wir, int queue) {
        if (! isNullQueue(queue)) getQueue(queue).remove(wir);
    }


    public Set<WorkItemRecord> getQueuedWorkItems(int queue) {
        if (isNullQueue(queue)) return null ;
        else return getQueue(queue).getAll();
    }

    public WorkQueue getCombinedQueues() {
        WorkQueue result = new WorkQueue() ;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            if (! isNullQueue(queue)) result.addQueue(getQueue(queue));
        return result ;
    }


    public void removeFromAllQueues(WorkItemRecord wir) {
        int max, min;
        if (_type == setType.adminSet) {
            min = WorkQueue.UNOFFERED ;
            max = WorkQueue.WORKLISTED ;            
        }
        else {
            min = WorkQueue.OFFERED;
            max = WorkQueue.SUSPENDED;
        }
        for (int queue = min; queue <= max; queue++)
            removeFromQueue(wir, queue);
    }

    
    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache) {
        q.restore(cache) ;
        switch (q.getQueueType()) {
            case WorkQueue.OFFERED    : _qOffered = q; break;
            case WorkQueue.ALLOCATED  : _qAllocated = q; break;
            case WorkQueue.STARTED    : _qStarted = q; break;
            case WorkQueue.SUSPENDED  : _qSuspended = q; break;
            case WorkQueue.WORKLISTED : _qWorklisted = q; break;
            case WorkQueue.UNOFFERED  : _qUnoffered = q;
        }
    }



    // hibernate mappings
    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private String get_type() { return _type.name(); }

    private void set_type(String type) { _type = setType.valueOf(type); }

}
