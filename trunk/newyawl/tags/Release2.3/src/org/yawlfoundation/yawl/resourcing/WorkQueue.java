/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Convenience class that encapsulates the various work queues for a Participant and/or
 * Administrator - each instance representing a single queue
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class WorkQueue {

    // different queue types
    public final static int UNDEFINED = -1 ;
    public final static int OFFERED = 0 ;
    public final static int ALLOCATED = 1 ;
    public final static int STARTED = 2 ;
    public final static int SUSPENDED = 3 ;
    public final static int UNOFFERED = 4 ;                  // administrator only
    public final static int WORKLISTED = 5 ;                 // administrator only

    
    // the workitems assigned to this queue: <item's id, item>
    private Map<String, WorkItemRecord> _workitems =
            new ConcurrentHashMap<String, WorkItemRecord>();

    private long _id ;                                       // hibernate primary key
    private String _ownerID ;                                // who owns this queue?
    private int _queueType ;
    private boolean _persisting ;


    /******************************************************************************/

    // CONSTRUCTORS //

    public WorkQueue() {}

    public WorkQueue(String ownerID, int qType, boolean persisting) {
        _ownerID = ownerID != null ? ownerID : "admin";
        _queueType = qType ;
        _persisting = persisting ;
        if (_persisting) Persister.getInstance().insert(this);
    }

    public WorkQueue(String ownerID, WorkItemRecord item, int qType, boolean persisting) {
        this(ownerID, qType, persisting) ;
        add(item, true) ;
    }

    public WorkQueue(String ownerID, HashMap<String, WorkItemRecord> items,
                     int qType, boolean persisting) {
        this(ownerID, qType, persisting) ;
        addQueue(items);
    }

    public WorkQueue(String ownerID, WorkQueue queue, int qType, boolean persisting) {
        this(ownerID, qType, persisting) ;
        addQueue(queue) ;
    }


    /******************************************************************************/

    // PRIVATE METHODS //

    /** Called when this workqueue's contents have changed.
     *  Note that the admin worklisted queue is dynamically constructed from the union
     *  of all participant queues and thus does not need to be persisted
     */
    private void persistThis() {
        if (_persisting && _queueType < WORKLISTED) {
            Persister.getInstance().update(this);
        }    
    }


    /**
     * adds an entry in the process log when a workitem is added to a queue
     * (since that signifies a resourcing status change).
     * Additions to the admin worklisted queue is ignored.
     *
     * @param wir the workitem effecting the change
     */
    private void logEvent(WorkItemRecord wir) {
        if (_queueType < WORKLISTED)
            EventLogger.log(wir, _ownerID, _queueType) ;
    }


    private void logEvent(Map<String, WorkItemRecord> map) {
        if (_queueType < WORKLISTED)
            for (WorkItemRecord wir : map.values()) logEvent(wir) ;        
    }

    /******************************************************************************/

    // ACCESSIBLE METHODS //

    public void setOwnerID(String id) { _ownerID = id ; }

    public String getOwnerID() { return _ownerID ; }

    public String getID() { return _ownerID; }


    public int getQueueType() { return _queueType ; }

    public void setQueueType(int qType) { _queueType = qType ; }


    public boolean isPersisting() { return _persisting; }

    public void setPersisting(boolean persist) { _persisting = persist; }


    /**
     * Adds a workitem to the queue
     * @param item the workitem to add
     */
    public void add(WorkItemRecord item, boolean log) {
        _workitems.put(item.getID(), item) ;
        persistThis() ;
        if (log) logEvent(item);
    }

    /**
     * Adds all members of the Map passed to the queue
     * @param queueMap the Map of [item id, YWorkItem] to add
     */
    public void addQueue(Map<String, WorkItemRecord> queueMap) {
        _workitems.putAll(queueMap);
        persistThis() ;
        logEvent(queueMap) ;
    }

    /**
     * Adds all the items in the queue passed to this work queue
     *  (i.e. does not replace the queue)
     * @param queue the queue of items to add
     */
    public void addQueue(WorkQueue queue) {
        addQueue(queue.getQueueAsMap());
    }


    /**
     * Sets (replaces) this work queue's members with the members of the queue passed
     * @param queue the new queue
     */
    public void setQueue(WorkQueue queue) {
        _workitems = queue.getQueueAsMap() ;
        persistThis() ;
        logEvent(_workitems) ;
    }

    /**
     * Retrieves a workitem from the queue (but does not remove it)
     * @param itemID the ID of the workitem to retrieve
     * @return the retrieved workitem
     */
    public WorkItemRecord get(String itemID) {
        return _workitems.get(itemID);
    }

    /**
     * Retrieves a Set of all workitems in the queue
     * @return a Set of all WorkItemRecord objects in the work queue
     */
    public Set<WorkItemRecord> getAll() {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();
        for (WorkItemRecord wir : _workitems.values()) {
            if (wir != null) result.add(wir);
        }
        return result  ;
    }

    /**
     * Retrieves a HashMap of all workitems in the queue
     * @return all members of the queue as a HashMap of [item id, YWorkItem]
     */
    public Map<String, WorkItemRecord> getQueueAsMap() { return _workitems; }


    /**
     * Removes a workitem from the queue
     * @param item the workitem to remove
     */
    public void remove(WorkItemRecord item) {
        if (_workitems.containsKey(item.getID())) {
            _workitems.remove(item.getID());
            persistThis();
        }    
    }


    /**
     * Removes all the items in the queue passed from this work queue
     *  (i.e. does not replace the queue)
     * @param queue the queue of items to remove
     */
    public void removeQueue(WorkQueue queue) {
        for (WorkItemRecord wir : queue.getAll()) {
            remove(wir);
        }
    }

    

    /* Removes all workitems from the queue */
    public void clear() {
        if (! _workitems.isEmpty()) {
            _workitems.clear();
            persistThis() ;
        }    
    }


    public void cleanse(WorkItemCache cache) {
        Set<String> clonedQueue = new HashSet<String>(_workitems.keySet());
        for (String itemID : clonedQueue) {
            if (cache.get(itemID) == null) _workitems.remove(itemID);
        }
        if (_workitems.size() != clonedQueue.size()) persistThis();
    }

    
    public void removeCase(String caseID) {
        Set<WorkItemRecord> clonedQueue = new HashSet<WorkItemRecord>(_workitems.values());
        for (WorkItemRecord wir : clonedQueue) {
            if (wir.getRootCaseID().equals(caseID)) _workitems.remove(wir.getID());
        }
        if (_workitems.size() != clonedQueue.size()) persistThis();
    }


    /** @return true if the work queue contains no work items */
    public boolean isEmpty() { return _workitems.isEmpty(); }


    /** returns the number of workitems in this queue */
    public int getQueueSize() { return _workitems.size() ; }


    /** returns the apropriate String identifier for the queue type passed */
    public static String getQueueName(int qType) {
        String result ;
        switch (qType) {
            case OFFERED    : result = "Offered" ; break ;
            case ALLOCATED  : result = "Allocated" ; break ;
            case STARTED    : result = "Started" ; break ;
            case SUSPENDED  : result = "Suspended" ; break ;
            case UNOFFERED  : result = "Unoffered" ; break ;
            case WORKLISTED : result = "Worklisted" ; break ;
            default : result = "Invalid Queue Type" ;
        }
        return result ;
    }


    public static boolean isValidQueueType(int queueType) {
        return (queueType >= OFFERED) && (queueType <= WORKLISTED);
    }

    /** returns the name of this queue */
    public String getQueueName() {
        return getQueueName(_queueType) ;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<WorkQueue>");
        xml.append(StringUtil.wrap(String.valueOf(_queueType), "queuetype"));
        xml.append(StringUtil.wrap(_ownerID, "ownerid")) ;
        xml.append("<workitems>");
        for (WorkItemRecord wir : _workitems.values()) {
            xml.append(wir.toXML());
        }
        xml.append("</workitems>");
        xml.append("</WorkQueue>");
        return xml.toString();
    }


    public void fromXML(String xml) {
        fromXML(JDOMUtil.stringToElement(xml));
    }

    public void fromXML(Element element) {
        if (element != null) {
            _queueType = new Integer(element.getChildText("queuetype"));
            _ownerID = element.getChildText("ownerid");
            Element items = element.getChild("workitems");
            if (items != null) {
                for (Object o : items.getChildren()) {
                    WorkItemRecord wir = Marshaller.unmarshalWorkItem((Element) o) ;
                    _workitems.put(wir.getID(), wir);
                }
            }
        }
    }

    // hibernate mappings

    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private int get_queueType() { return _queueType; }

    private void set_queueType(int queueType) { _queueType = queueType; }

    private long get_id() { return _id; }

    private void set_id(long id) {_id = id; }

}
