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

    public void addToQueue(WorkItemRecord wir, int queue) {
        switch (queue) {
            case WorkQueue.OFFERED    : _qOffered.add(wir);
            case WorkQueue.ALLOCATED  : _qAllocated.add(wir);
            case WorkQueue.STARTED    : _qStarted.add(wir);
            case WorkQueue.SUSPENDED  : _qSuspended.add(wir);
            case WorkQueue.WORKLISTED : _qWorklisted.add(wir);
            case WorkQueue.UNOFFERED  : _qUnoffered.add(wir);
        }
    }


    public void addToQueue(int queue, WorkQueue queueToAdd) {
        switch (queue) {
            case WorkQueue.OFFERED    : _qOffered.addQueue(queueToAdd);
            case WorkQueue.ALLOCATED  : _qAllocated.addQueue(queueToAdd);
            case WorkQueue.STARTED    : _qStarted.addQueue(queueToAdd);
            case WorkQueue.SUSPENDED  : _qSuspended.addQueue(queueToAdd);
            case WorkQueue.WORKLISTED : _qWorklisted.addQueue(queueToAdd);
            case WorkQueue.UNOFFERED  : _qUnoffered.addQueue(queueToAdd);
        }
    }

    public void removeFromQueue(WorkItemRecord wir, int queue) {
        switch (queue) {
            case WorkQueue.OFFERED    : _qOffered.remove(wir);
            case WorkQueue.ALLOCATED  : _qAllocated.remove(wir);
            case WorkQueue.STARTED    : _qStarted.remove(wir);
            case WorkQueue.SUSPENDED  : _qSuspended.remove(wir);
            case WorkQueue.WORKLISTED : _qWorklisted.remove(wir);
            case WorkQueue.UNOFFERED  : _qUnoffered.add(wir);
        }
    }


    public Set getWorkQueue(int queue) {
        Set result = null;
        switch (queue) {
            case WorkQueue.OFFERED    : result = _qOffered.getAll();
            case WorkQueue.ALLOCATED  : result = _qAllocated.getAll();
            case WorkQueue.STARTED    : result = _qStarted.getAll();
            case WorkQueue.SUSPENDED  : result =  _qSuspended.getAll();
            case WorkQueue.WORKLISTED : result = _qWorklisted.getAll();
            case WorkQueue.UNOFFERED  : result = _qUnoffered.getAll();
        }
        return result ;
    }

    public WorkQueue getCombinedQueues() {
        WorkQueue result = new WorkQueue() ;

        result.addQueue(_qOffered);
        result.addQueue(_qAllocated);
        result.addQueue(_qStarted);
        result.addQueue(_qSuspended);
        return result ;
    }

    public void removeFromAllQueues(WorkItemRecord wir) {
        removeFromQueue(wir, WorkQueue.ALLOCATED);
        removeFromQueue(wir, WorkQueue.OFFERED);
        removeFromQueue(wir, WorkQueue.STARTED);
        removeFromQueue(wir, WorkQueue.SUSPENDED);
        removeFromQueue(wir, WorkQueue.WORKLISTED);
        removeFromQueue(wir, WorkQueue.UNOFFERED);
    }


    // hibernate mappings
    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private String get_type() { return _type.name(); }

    private void set_type(String type) { _type = setType.valueOf(type); }
}
