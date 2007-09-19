/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Set;
import java.util.HashSet;

/**
 * A repository of work queues belonging to a participant
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
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

    public enum setType { participantSet, adminSet }


    public QueueSet() {}

    public QueueSet(String pid, setType sType) {
        _type = sType ;
        if (_type == setType.participantSet) {
            _ownerID = pid ;
            _qOffered = new WorkQueue(pid);
            _qAllocated = new WorkQueue(pid);
            _qStarted = new WorkQueue(pid);
            _qSuspended = new WorkQueue(pid);
        }
        else {
            _ownerID = "admin";
            _qUnoffered = new WorkQueue(pid);
            _qWorklisted = new WorkQueue(pid);
        }
    }

    public String getID() { return _ownerID; }

    public int getQueueSize(int queue) {
        return getQueue(queue).getQueueSize() ;
    }

    /*****************************************************************************/

    // User-Task Privileges Actions //

    public void toSuspend(WorkItemRecord wir) {
        _qStarted.remove(wir);
        _qSuspended.add(wir);
    }


    /*****************************************************************************/

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
        getQueue(queue).add(wir);
    }


    public void addToQueue(int queue, WorkQueue queueToAdd) {
        getQueue(queue).addQueue(queueToAdd);
    }


    public void removeFromQueue(WorkItemRecord wir, int queue) {
        getQueue(queue).remove(wir);
    }


    public Set getQueuedWorkItems(int queue) {
        return getQueue(queue).getAll();
    }

    public WorkQueue getCombinedQueues() {
        WorkQueue result = new WorkQueue() ;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            result.addQueue(getQueue(queue));
        return result ;
    }

    public void removeFromAllQueues(WorkItemRecord wir) {
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.WORKLISTED; queue++)
            removeFromQueue(wir, queue);
    }


    // hibernate mappings
    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private String get_type() { return _type.name(); }

    private void set_type(String type) { _type = setType.valueOf(type); }
}
