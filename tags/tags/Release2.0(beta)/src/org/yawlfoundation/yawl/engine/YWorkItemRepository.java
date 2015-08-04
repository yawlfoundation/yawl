/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import static org.yawlfoundation.yawl.engine.YWorkItemStatus.*;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/05/2003
 * Time: 14:04:39
 * 
 */
public class YWorkItemRepository {
    private static Map _idStringToWorkItemsMap;//[case&taskIDStr=YWorkItem]
    protected static Map _caseToNetRunnerMap;
    private static YWorkItemRepository _myInstance;
    private static final Logger logger = Logger.getLogger(YWorkItemRepository.class);


    private YWorkItemRepository() {
        _idStringToWorkItemsMap = new HashMap();
        _caseToNetRunnerMap = new HashMap();
    }

    public void dump(Logger logger) {
        logger.debug("\n*** DUMPING " + _caseToNetRunnerMap.size() + " ENTRIES IN CASE_2_NETRUNNER MAP ***");
        {
            Iterator keys = _caseToNetRunnerMap.keySet().iterator();
            int sub = 0;
            while (keys.hasNext()) {
                sub++;
                Object objKey = keys.next();

                if (objKey == null) {
                    logger.debug("Key = NULL !!!");
                } else {
                    YIdentifier key = (YIdentifier) objKey;
                    YNetRunner runner = (YNetRunner) _caseToNetRunnerMap.get(key);

                    logger.debug("Entry " + sub + " Key=" + key.get_idString());
                    logger.debug(("    CaseID        " + runner.get_caseID()));
                    logger.debug("     YNetID        " + runner.getYNetID());
                }
            }
        }

        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");

        logger.debug("\n*** DUMPING " + _idStringToWorkItemsMap.size() + " ENTRIES IN ID_2_WORKITEMS_MAP ***");
        {
            Iterator keys = _idStringToWorkItemsMap.keySet().iterator();
            int sub = 0;
            while (keys.hasNext()) {
                sub++;
                String key = (String) keys.next();
                YWorkItem workitem = (YWorkItem) _idStringToWorkItemsMap.get(key);

                logger.debug("Entry " + sub + " Key=" + key);
                logger.debug(("    WorkitemID        " + workitem.getIDString()));
            }
        }

        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");

    }


    public static YWorkItemRepository getInstance() {
        if (_myInstance == null) {
            _myInstance = new YWorkItemRepository();
        }
        return _myInstance;
    }


    //mutators
    protected void addNewWorkItem(YWorkItem workItem) {
        logger.debug("--> addNewWorkItem: " + workItem.getIDString());
        _idStringToWorkItemsMap.put(workItem.getIDString(), workItem);
    }


    private void removeWorkItem(YWorkItem workItem) {
        logger.debug("--> cancelAllWorkItemsInGroupOf: " + workItem.getIDString());
        _idStringToWorkItemsMap.remove(workItem.getIDString());
    }


    public boolean removeWorkItemFamily(YWorkItem workItem) {
        logger.debug("--> removeWorkItemFamily: " + workItem.getIDString());
        Set children = workItem.getParent() == null ? workItem.getChildren() : workItem.getParent().getChildren();
        YWorkItem parent = workItem.getParent() == null ? workItem : workItem.getParent();
        if (parent != null) {
            if (children != null) {
                Iterator iter = children.iterator();
                while (iter.hasNext()) {
                    YWorkItem siblingItem = (YWorkItem) iter.next();
                    removeWorkItem(siblingItem);
                }
            }
            removeWorkItem(parent);
            return true;
        }
        return false;
    }


    //look up tables to find netrunners
    public void setNetRunnerToCaseIDBinding(YNetRunner netRunner, YIdentifier caseID) {
        _caseToNetRunnerMap.put(caseID, netRunner);
    }


    protected void clear() {
        _idStringToWorkItemsMap = new HashMap();
        _caseToNetRunnerMap = new HashMap();
    }


    /**
     * Cancels the net runner and removes any workitems that belong to it.
     * @param caseIDForNet
     */
    public void cancelNet(YIdentifier caseIDForNet) {
        _caseToNetRunnerMap.remove(caseIDForNet);
        Set itemsToRemove = new HashSet();
        List allWorkItems = new ArrayList(_idStringToWorkItemsMap.values());
        //go through all the work items and if there are any belonging to
        //the id for this net then remove them.
        for (int i = 0; i < allWorkItems.size(); i++) {
            YWorkItem item = (YWorkItem) allWorkItems.get(i);
            YIdentifier identifier = item.getWorkItemID().getCaseID();
            if (identifier.isImmediateChildOf(caseIDForNet) ||
                    identifier.toString().equals(caseIDForNet.toString())) {
                itemsToRemove.add(item.getIDString());
            }
        }
        removeItems(itemsToRemove);
    }


    //###################################################################################
    //                                  accessors
    //###################################################################################

    public YNetRunner getNetRunner(YIdentifier caseID) {
        return (YNetRunner)
                _caseToNetRunnerMap.get(caseID);
    }


    public YWorkItem getWorkItem(String caseIDStr, String taskID) {
        return (YWorkItem) _idStringToWorkItemsMap.get(caseIDStr + ":" + taskID);
    }


    public YWorkItem getWorkItem(String workItemID) {
        return (YWorkItem) _idStringToWorkItemsMap.get(workItemID);
    }


    public Set getEnabledWorkItems() {
        logger.debug("--> getEnabledWorkItems: _idStringToWorkItemsMap=" +
                     _idStringToWorkItemsMap.size() + " _caseToNetRunnerMap=" +
                     _caseToNetRunnerMap.size());
        Set aSet = new HashSet();
        Set itemsToRemove = new HashSet();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == statusEnabled) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = (YNetRunner) _caseToNetRunnerMap.get(
                        caseID);
                boolean addedOne = false;
                Set enabledTasks = runner.getEnabledTasks();
                for (Iterator iterator = enabledTasks.iterator(); iterator.hasNext();) {
                    YTask task = (YTask) iterator.next();
                    if (task.getID().equals(workitem.getTaskID())) {
                        aSet.add(workitem);
                        addedOne = true;
                    }
                }
                if (!addedOne) {
                    itemsToRemove.add(workitem.getIDString());
                }
            }
        }
        removeItems(itemsToRemove);

        logger.debug("<-- getEnabledWorkItems");
        return aSet;
    }


    private void removeItems(Set itemsToRemove) {
        for (Iterator iterator = itemsToRemove.iterator(); iterator.hasNext();) {
            String workItemID = (String) iterator.next();
            _idStringToWorkItemsMap.remove(workItemID);
        }
    }


    public Set getParentWorkItems() {
        Set aSet = new HashSet();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == statusIsParent) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }


    public Set getFiredWorkItems() {
        Set aSet = new HashSet();
        Set itemsToRemove = new HashSet();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == statusFired) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = (YNetRunner) _caseToNetRunnerMap.get(
                        caseID.getParent());
                boolean addedOne = false;
                if (null != runner) {
                    Set busyTasks = runner.getBusyTasks();
                    for (Iterator iterator = busyTasks.iterator(); iterator.hasNext();) {
                        YTask task = (YTask) iterator.next();
                        if (task.getID().equals(workitem.getTaskID())) {
                            aSet.add(workitem);
                            addedOne = true;
                        }
                    }
                }
                if (!addedOne) {
                    itemsToRemove.add(workitem.getIDString());
                }
            }
        }
        removeItems(itemsToRemove);
        return aSet;
    }


    public Set getExecutingWorkItems() {
        Set aSet = new HashSet();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == statusExecuting) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }


    public Set getExecutingWorkItems(String userName) {
        Set aSet = new HashSet();
        Set itemsToRemove = new HashSet();

        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == statusExecuting) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = (YNetRunner)
                        _caseToNetRunnerMap.get(caseID.getParent());
                boolean foundOne = false;
                Set busyTasks = runner.getBusyTasks();
                for (Iterator iterator = busyTasks.iterator(); iterator.hasNext();) {
                    YTask task = (YTask) iterator.next();
                    if (task.getID().equals(workitem.getTaskID())) {
                        foundOne = true;
                        if (workitem.getUserWhoIsExecutingThisItem().equals(userName)) {
                            aSet.add(workitem);
                        }
                    }
                }
                if (!foundOne) {
                    itemsToRemove.add(workitem.getIDString());
                }
            }
        }
        removeItems(itemsToRemove);
        return aSet;
    }


    public Set getCompletedWorkItems() {
        Set aSet = new HashSet();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == statusComplete) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }


    public Set getWorkItems() {
        Set aSet = new HashSet();
        Set itemsToRemove = new HashSet();
        //rather than just return the work items we have to chek that the items in the
        //repository are in synch with the engine
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();

            YIdentifier caseID = workitem.getWorkItemID().getCaseID();
            YNetRunner runner;
            if (workitem.getStatus().equals(statusEnabled) ||
                    workitem.getStatus().equals(statusIsParent) ||
                    workitem.isEnabledSuspended()) {
                runner = (YNetRunner) _caseToNetRunnerMap.get(caseID);
            } else if (workitem.getStatus().equals(statusComplete) ||
                    workitem.getStatus().equals(statusExecuting) ||
                    workitem.getStatus().equals(statusSuspended) ||
                    workitem.getStatus().equals(statusFired)) {
                runner = (YNetRunner) _caseToNetRunnerMap.get(caseID.getParent());
            } else {
                continue;
            }
            if (runner != null) {                                      //MLF can be null
                boolean foundOne = false;
                Set busyTasks = runner.getBusyTasks();
                Set enableTasks = runner.getEnabledTasks();
                Set workItemTasks = new HashSet();
                workItemTasks.addAll(busyTasks);
                workItemTasks.addAll(enableTasks);

                for (Iterator iterator = workItemTasks.iterator(); iterator.hasNext();) {
                    YTask task = (YTask) iterator.next();
                    if (task.getID().equals(workitem.getTaskID())) {
                        foundOne = true;
                        aSet.add(workitem);
                    }
                }
                //clean up all the work items that are out of synch with the engine.
                if (!foundOne) {
                    itemsToRemove.add(workitem.getIDString());
                }
            }
        }
        removeItems(itemsToRemove);
        return aSet;
    }


    public Map getNetRunners() {
        return _caseToNetRunnerMap;
    }


    public Set getChildrenOf(String workItemID) {
        YWorkItem item = (YWorkItem) _idStringToWorkItemsMap.get(workItemID);
        if (item != null) {
            return item.getChildren();
        } else
            return new HashSet();
    }

    /**
     * Removes all work items for a given case id.  Searches through the work items for
     * ones that are related to the case id and removes them.
     * @param caseID must be a case id, (not a child of a caseid).
     */
    public void removeWorkItemsForCase(YIdentifier caseID) {
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid.");
        }

        List workItems = getWorkItemsForCase(caseID);
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
                removeWorkItemFamily(item);
        }
    }

    public List getWorkItemsForCase(YIdentifier caseID) {
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid.");
        }
        
        Set workItems = getWorkItems();
        ArrayList caseItems = new ArrayList();

        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
            YWorkItemID wid = item.getWorkItemID();

            //get the root id for this work items case
            //and if it matches the cancellation case id then remove it.
            YIdentifier workItemsCaseID = wid.getCaseID();
            while (workItemsCaseID.getParent() != null) {
                workItemsCaseID = workItemsCaseID.getParent();
            }
            //if a work item's root case id matches case passed in save it
            if (workItemsCaseID.toString().equals(caseID.toString())) {
                caseItems.add(item);
            }
        }
        return caseItems;
    }

}
