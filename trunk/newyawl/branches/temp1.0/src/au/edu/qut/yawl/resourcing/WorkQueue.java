/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing;

import au.edu.qut.yawl.engine.YWorkItem;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.util.*;

/**
 * Convenience class that encapsulates the various work queues for a Participant and/or
 * Administrator - each instance representing a single queue
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public class WorkQueue {

    // the workitems assigned to this queue: <item's id, item>
    private HashMap<String, WorkItemRecord> _workitems =
            new HashMap<String, WorkItemRecord>();

    private String _id ;                                     // hibernate primary key
    private String _ownerID ;                                // who owns this queue?
    private int _queueType ;
    private Set _itemIDs ;

    // different queue types
    public final static int OFFERED = 0 ;
    public final static int ALLOCATED = 1 ;
    public final static int STARTED = 2 ;
    public final static int SUSPENDED = 3 ;
    public final static int UNOFFERED = 4 ;                  // administrator only
    public final static int WORKLISTED = 5 ;                 // administrator only

    /******************************************************************************/

    // CONSTRUCTORS //

    public WorkQueue() {}

    public WorkQueue(String ownerID) { _ownerID = ownerID ; }

    public WorkQueue(WorkItemRecord item) { add(item) ; }

    public WorkQueue(HashMap<String, WorkItemRecord> items) { addQueue(items); }

    public WorkQueue(WorkQueue queue) { addQueue(queue) ; }


    /******************************************************************************/

    // ACCESSIBLE METHODS //


    public void setOwnerID(String id) { _ownerID = id ; }

    public String getOwnerID() { return _ownerID ; }

    public String getID() { return _ownerID; }

    /**
     * Adds a workitem to the queue
     * @param item the workitem to add
     */
    public void add(WorkItemRecord item) {
        _workitems.put(item.getID(), item) ;
    }

    /**
     * Adds all members of the Map passed to the queue
     * @param queueMap the Map of [item id, YWorkItem] to add
     */
    public void addQueue(HashMap<String, WorkItemRecord> queueMap) {
        _workitems.putAll(queueMap);
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
     * @return a Set of all YWorkItem objects in the work queue
     */
    public Set getAll() {
        return (Set) _workitems.values() ;
    }

    /**
     * Retrieves a HashMap of all workitems in the queue
     * @return all members of the queue as a HashMap of [item id, YWorkItem]
     */
    public HashMap<String, WorkItemRecord> getQueueAsMap() { return _workitems; }


    /**
     * Removes a workitem from the queue
     * @param item the workitem to remove
     * @return the removed workitem
     */
    public WorkItemRecord remove(WorkItemRecord item) {
        return _workitems.remove(item.getID());
    }


    /** @return true if the work queue contains no work items */
    public boolean isEmpty() { return _workitems.isEmpty(); }


    /* Removes all workitems from the queue */
    public void clear() { _workitems.clear(); }

    public int getQueueSize() { return _workitems.size() ; }

    public void restoreQueue() {
        
    }

    // hibernate mappings

    private String get_ownerID() { return _ownerID; }

    private void set_ownerID(String ownerID) { _ownerID = ownerID; }

    private int get_queueType() { return _queueType; }

    private void set_queueType(int queueType) { _queueType = queueType; }

    private String get_id() { return _id; }

    private void set_id(String id) {_id = id; }

    private Set get_itemIDs() {
        _itemIDs = _workitems.keySet();
        return _itemIDs ;
    }

    private void set_itemIDs(Set itemSet) { _itemIDs = itemSet ; }

}
