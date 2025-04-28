/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing;

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A repository of work queues belonging to a participant
 *
 *  @author Michael Adams
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
        String resStatus = parent.getResourceStatus();
        if (resStatus != null) {                      // will be null if resourcing is SSS
            if (resStatus.equals(WorkItemRecord.statusResourceAllocated))
                removeFromQueue(parent, WorkQueue.ALLOCATED);
            else
                removeFromQueue(parent, WorkQueue.OFFERED);
        }
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
        addToQueue(wir, WorkQueue.STARTED, false);              // don't log as a start
    }


    /*****************************************************************************/

    private boolean isNullQueue(int queue) {
        return getQueue(queue) == null ;
    }
    
    /** instantiates the queue if it is not yet instantiated */
    private void checkQueueExists(int queue) {
        if (isNullQueue(queue)) {
            setQueue(new WorkQueue(_ownerID, queue, _persisting));
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
        getQueue(queue).add(wir, true);
        notifyIfRequired(wir, queue);
    }


    public void addToQueue(WorkItemRecord wir, int queue, boolean log) {
        checkQueueExists(queue) ;
        getQueue(queue).add(wir, log);
    }


    public void addToQueue(int queue, WorkQueue queueToAdd) {
        checkQueueExists(queue) ;
        getQueue(queue).addQueue(queueToAdd);
    }


    public boolean removeFromQueue(WorkItemRecord wir, int queue) {
        return ! isNullQueue(queue) && getQueue(queue).remove(wir);
    }

    public void removeFromQueue(WorkQueue queueToRemove, int queue) {
        if (! isNullQueue(queue)) getQueue(queue).removeQueue(queueToRemove);
    }


    public void removeCaseFromQueue(String caseID, int queue) {
        if (! isNullQueue(queue)) getQueue(queue).removeCase(caseID);
    }


    public void cleanseQueue(WorkItemCache cache, int queue) {
        if (! isNullQueue(queue)) getQueue(queue).cleanse(cache);
    }

    public Set<WorkItemRecord> getQueuedWorkItems(int queue) {
        if (isNullQueue(queue)) return Collections.emptySet() ;
        else return getQueue(queue).getAll();
    }

    public WorkQueue getWorklistedQueues() {
        WorkQueue result = new WorkQueue() ;
        result.setQueueType(WorkQueue.WORKLISTED);
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            if (! isNullQueue(queue)) result.addQueue(getQueue(queue));
        return result ;
    }

    public Set<WorkQueue> getActiveQueues() {
        Set<WorkQueue> activeSet = new HashSet<WorkQueue>();
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            if (! isNullQueue(queue)) activeSet.add(getQueue(queue));
        return activeSet;
    }

    public void refresh(WorkItemRecord wir) {
        for (int queue = WorkQueue.OFFERED; queue <= WorkQueue.SUSPENDED; queue++)
            if (! isNullQueue(queue)) getQueue(queue).refresh(wir);
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
            for (Element qElem : element.getChildren()) {
                WorkQueue wq = new WorkQueue() ;
                wq.fromXML(qElem);
                setQueue(wq) ;
            }
        }
    }


    private void notifyIfRequired(WorkItemRecord wir, int queue) {
        if (queue == WorkQueue.OFFERED || queue == WorkQueue.ALLOCATED) {
            ResourceManager rm = ResourceManager.getInstance();
            Participant p = rm.getOrgDataSet().getParticipant(_ownerID);
            if (p != null && p.getEmail() != null) {
                if ((queue == WorkQueue.OFFERED && p.isEmailOnOffer()) ||
                        (queue == WorkQueue.ALLOCATED && p.isEmailOnAllocation())) {
                    rm.sendMailNotification(p, wir, queue);
                }
            }
        }
    }


    // hibernate mappings
    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private String get_type() { return _type.name(); }

    private void set_type(String type) { _type = setType.valueOf(type); }

}
