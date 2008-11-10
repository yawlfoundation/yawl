/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * A repository of work queues belonging to a participant
 *
 *  @author Michael Adams
 *  v0.1, 23/08/2007
 */

public class QueueSet implements Serializable {

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
        removeFromQueue(wir, WorkQueue.STARTED);
        addToQueue(wir, WorkQueue.SUSPENDED) ;
    }

    
    // moving from offered/allocated to start occurs when workitem moves from enabled to
    // executing. Thus, the offered/allocated queue contains the parent; the started
    // queue must receive the child
    public void movetoStarted(WorkItemRecord parent, WorkItemRecord child) {
        if (parent.getResourceStatus().equals(WorkItemRecord.statusResourceAllocated))
            removeFromQueue(parent, WorkQueue.ALLOCATED);
        else
            removeFromQueue(parent, WorkQueue.OFFERED);
        addToQueue(child, WorkQueue.STARTED);
    }


    // this variation is called when a workitem that is already started gets
    // reallocated or reoffered, then eventually moves back to a started queue.
    // Since it was previously started, it's already the child item
    public void movetoStarted(WorkItemRecord wir) {
        removeFromQueue(wir, WorkQueue.ALLOCATED);
        addToQueue(wir, WorkQueue.STARTED);
    }


    public void movetoUnsuspend(WorkItemRecord wir) {

        // explicitly log the resume event
        EventLogger.log(wir, _ownerID, EventLogger.event.resume);
        removeFromQueue(wir, WorkQueue.SUSPENDED);
        addToQueue(wir, WorkQueue.STARTED);
    }


    /*****************************************************************************/

    private boolean isNullQueue(int queue) {
        return getQueue(queue) == null ;
    }
    
    /** instantiates the queue if it is not yet instantiated */
    private void checkQueueExists(int queue) {
        if (isNullQueue(queue)) {
            WorkQueue newQueue = new WorkQueue(_ownerID, queue, _persisting) ;
            switch (queue) {
                case WorkQueue.OFFERED    : _qOffered  = newQueue; break;
                case WorkQueue.ALLOCATED  : _qAllocated = newQueue; break;
                case WorkQueue.STARTED    : _qStarted = newQueue; break;
                case WorkQueue.SUSPENDED  : _qSuspended = newQueue; break;
                case WorkQueue.WORKLISTED : _qWorklisted = newQueue; break;
                case WorkQueue.UNOFFERED  : _qUnoffered = newQueue;
            }
        }
    }


    public WorkQueue getQueue(int queue) {
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


    public void setQueue(WorkQueue queue) {
        switch(queue.getQueueType()) {
            case WorkQueue.OFFERED    : _qOffered = queue; break;
            case WorkQueue.ALLOCATED  : _qAllocated = queue; break;
            case WorkQueue.STARTED    : _qStarted = queue; break;
            case WorkQueue.SUSPENDED  : _qSuspended = queue; break;
            case WorkQueue.WORKLISTED : _qWorklisted = queue; break;
            case WorkQueue.UNOFFERED  : _qUnoffered = queue;
        }
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


    public void removeCaseFromQueue(String caseID, int queue) {
        if (! isNullQueue(queue)) getQueue(queue).removeCase(caseID);
    }


    public void cleanseQueue(WorkItemCache cache, int queue) {
        if (! isNullQueue(queue)) getQueue(queue).cleanse(cache);
    }

    public Set<WorkItemRecord> getQueuedWorkItems(int queue) {
        if (isNullQueue(queue)) return null ;
        else return getQueue(queue).getAll();
    }

    public WorkQueue getWorklistedQueues() {
        WorkQueue result = new WorkQueue() ;
        result.setQueueType(WorkQueue.WORKLISTED);
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            if (! isNullQueue(queue)) result.addQueue(getQueue(queue));
        return result ;
    }

    public boolean hasWorkItemInQueue(String itemID, int queue) {
        return !isNullQueue(queue) && (getQueue(queue).get(itemID) != null);
    }

    public boolean hasWorkItemInAnyQueue(WorkItemRecord wir) {
        if (wir == null) return false;
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++) {
            if (hasWorkItemInQueue(wir.getID(), queue)) return true ;
        }
        return false ;
    }

    public void removeFromAllQueues(WorkItemRecord wir) {
        for (int queue = getStartQueue(); queue <= getEndQueue(); queue++) 
            removeFromQueue(wir, queue);
    }


    public void removeCaseFromAllQueues(String caseID) {
        for (int queue = getStartQueue(); queue <= getEndQueue(); queue++)
            removeCaseFromQueue(caseID, queue);
    }


    public void cleanseAllQueues(WorkItemCache cache) {
        for (int queue = getStartQueue(); queue <= getEndQueue(); queue++)
            cleanseQueue(cache, queue);
    }

    
    public void purgeQueue(int queue) {
        if (! isNullQueue(queue)) getQueue(queue).clear();        
    }

    
    public void purgeAllQueues() {
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            purgeQueue(queue);
    }

    
    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache) {
        q.restore(cache) ;
        setQueue(q);
    }


    private int getStartQueue() {
        return (_type == setType.adminSet) ? WorkQueue.UNOFFERED : WorkQueue.OFFERED;
    }


    private int getEndQueue() {
        return (_type == setType.adminSet) ? WorkQueue.WORKLISTED : WorkQueue.SUSPENDED;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<QueueSet>") ;
        for (int queue = getStartQueue(); queue <= getEndQueue(); queue++) {
             if (!isNullQueue(queue)) xml.append(getQueue(queue).toXML()) ;
        }
        xml.append("</QueueSet>");
        return xml.toString();
    }


    public void fromXML(String xml) {
        fromXML(JDOMUtil.stringToElement(xml));
    }


    public void fromXML(Element element) {
        if (element != null) {
            Iterator itr = element.getChildren().iterator();
            while (itr.hasNext()) {
                WorkQueue wq = new WorkQueue() ;
                wq.fromXML((Element) itr.next());
                setQueue(wq) ;
            }
        }
    }


    // hibernate mappings
    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private String get_type() { return _type.name(); }

    private void set_type(String type) { _type = setType.valueOf(type); }

}
