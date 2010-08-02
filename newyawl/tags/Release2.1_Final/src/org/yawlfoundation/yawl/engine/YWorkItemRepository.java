/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import static org.yawlfoundation.yawl.engine.YWorkItemStatus.*;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/05/2003
 * Time: 14:04:39
 *
 * @author Michael Adams (refactored for v2.1)
 * 
 */
public class YWorkItemRepository {
    private static Map<String, YWorkItem> _idStringToWorkItemsMap; //[case&taskIDStr=YWorkItem]
    protected static Map<YIdentifier, YNetRunner> _caseToNetRunnerMap;
    private static YWorkItemRepository _myInstance;
    private static final Logger logger = Logger.getLogger(YWorkItemRepository.class);


    private YWorkItemRepository() {
        _idStringToWorkItemsMap = new HashMap<String, YWorkItem>();
        _caseToNetRunnerMap = new HashMap<YIdentifier, YNetRunner>();
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
        YEngine.getInstance().getInstanceCache().addWorkItem(workItem);
    }


    private void removeWorkItem(YWorkItem workItem) {
        logger.debug("--> removeWorkItem: " + workItem.getIDString());
        _idStringToWorkItemsMap.remove(workItem.getIDString());
    }


    public Set<YWorkItem> removeWorkItemFamily(YWorkItem workItem) {
        logger.debug("--> removeWorkItemFamily: " + workItem.getIDString());
        Set<YWorkItem> removedSet = new HashSet<YWorkItem>();
        YWorkItem parent = workItem.getParent() != null ? workItem.getParent() : workItem;
        Set<YWorkItem> children = parent.getChildren();
        if (children != null) {
           for (YWorkItem siblingItem : children) {
               removeWorkItem(siblingItem);
               removedSet.add(siblingItem);
            }
        }
        removeWorkItem(parent);
        removedSet.add(parent);
        return removedSet;
    }


    public void addNetRunner(YNetRunner runner) {
        setNetRunnerToCaseIDBinding(runner, runner.getCaseID()) ;
    }

    // look up tables to find netrunners
    public void setNetRunnerToCaseIDBinding(YNetRunner netRunner, YIdentifier caseID) {
        _caseToNetRunnerMap.put(caseID, netRunner);
    }


    protected void clear() {
        _idStringToWorkItemsMap = new HashMap<String, YWorkItem>();
        _caseToNetRunnerMap = new HashMap<YIdentifier, YNetRunner>();
    }


    /**
     * Cancels the net runner and removes any workitems that belong to it.
     * @param caseIDForNet
     */
    public Set<YWorkItem> cancelNet(YIdentifier caseIDForNet) {
        _caseToNetRunnerMap.remove(caseIDForNet);

        Set<String> itemsToRemove = new HashSet<String>();
        for (YWorkItem item : _idStringToWorkItemsMap.values()) {
            YIdentifier identifier = item.getWorkItemID().getCaseID();
            if (identifier.isImmediateChildOf(caseIDForNet) ||
                    identifier.toString().equals(caseIDForNet.toString())) {
                itemsToRemove.add(item.getIDString());
            }
        }
        return removeItems(itemsToRemove);
    }


    //###################################################################################
    //                                  accessors
    //###################################################################################

    public YNetRunner getNetRunner(YIdentifier caseID) {
        return _caseToNetRunnerMap.get(caseID);
    }

    public YNetRunner getNetRunner(String caseID) {
        for (YIdentifier id : _caseToNetRunnerMap.keySet()) {
            if (id.get_idString().equals(caseID)) {
                return _caseToNetRunnerMap.get(id);
            }
        }
        return null;
    }


    public YWorkItem getWorkItem(String caseIDStr, String taskID) {
        return _idStringToWorkItemsMap.get(caseIDStr + ":" + taskID);
    }


    public YWorkItem getWorkItem(String workItemID) {
        return _idStringToWorkItemsMap.get(workItemID);
    }


    private Set<YWorkItem> removeItems(Set<String> itemsToRemove) {
        Set<YWorkItem> removedSet = new HashSet<YWorkItem>();
        for (String workItemID : itemsToRemove) {
            YWorkItem item = _idStringToWorkItemsMap.remove(workItemID);
            if (item != null) removedSet.add(item);
        }
        return removedSet;
    }


    public Set<YWorkItem> getParentWorkItems() {
        return getWorkItems(statusIsParent);
    }


    public Set<YWorkItem> getEnabledWorkItems() {
        logger.debug("--> getEnabledWorkItems: _idStringToWorkItemsMap=" +
                     _idStringToWorkItemsMap.size() + " _caseToNetRunnerMap=" +
                     _caseToNetRunnerMap.size());

        Set<YWorkItem> result = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();

        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == statusEnabled) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = _caseToNetRunnerMap.get(caseID);
                boolean addedOne = false;

                for (YTask task : runner.getEnabledTasks()) {
                    if (task.getID().equals(workitem.getTaskID())) {
                        result.add(workitem);
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
        return result;
    }


    public Set<YWorkItem> getFiredWorkItems() {
        Set<YWorkItem> result = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();

        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == statusFired) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = _caseToNetRunnerMap.get(caseID.getParent());
                boolean addedOne = false;
                if (null != runner) {
                    for (YTask task : runner.getBusyTasks()) {
                        if (task.getID().equals(workitem.getTaskID())) {
                            result.add(workitem);
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
        return result;
    }


    public Set<YWorkItem> getExecutingWorkItems() {
        return getWorkItems(statusExecuting);
    }


    public Set<YWorkItem> getExecutingWorkItems(String serviceName) {
        Set<YWorkItem> executingItems = new HashSet<YWorkItem>();
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == statusExecuting) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = _caseToNetRunnerMap.get(caseID.getParent());
                for (YTask task : runner.getBusyTasks()) {
                    if (task.getID().equals(workitem.getTaskID())) {
                        if (workitem.getExternalClient().equals(serviceName)) {
                            executingItems.add(workitem);
                        }
                    }
                }
            }
        }
        return executingItems;
    }


    public Set<YWorkItem> getCompletedWorkItems() {
        return getWorkItems(statusComplete);
    }


    public Set<YWorkItem> getWorkItems(YWorkItemStatus status) {
        Set<YWorkItem> itemSet = new HashSet<YWorkItem>();
        for (YWorkItem workitem :_idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == status) {
                itemSet.add(workitem);
            }
        }
        return itemSet;
    }


    public Set<YWorkItem> getWorkItems() {
        Set<YWorkItem> workItems = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();

        // rather than just return the work items we should check that  
        // the items in the repository are in synch with the engine
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            YIdentifier caseID = workitem.getWorkItemID().getCaseID();
            YNetRunner runner;
            if (workitem.getStatus().equals(statusEnabled) ||
                    workitem.getStatus().equals(statusIsParent) ||
                    workitem.isEnabledSuspended()) {
                runner = _caseToNetRunnerMap.get(caseID);
            }
            else if (workitem.getStatus().equals(statusComplete) ||
                    workitem.getStatus().equals(statusExecuting) ||
                    workitem.getStatus().equals(statusSuspended) ||
                    workitem.getStatus().equals(statusFired)) {
                runner = _caseToNetRunnerMap.get(caseID.getParent());
            }
            else continue;

            if (runner != null) {                                      //MLF can be null
                boolean foundOne = false;
                for (YTask task : runner.getActiveTasks()) {
                    if (task.getID().equals(workitem.getTaskID())) {
                        foundOne = true;
                        workItems.add(workitem);
                    }
                }
                //clean up all the work items that are out of synch with the engine.
                if (!foundOne) {
                    itemsToRemove.add(workitem.getIDString());
                }
            }
        }
        removeItems(itemsToRemove);
        return workItems;
    }


    public Map<YIdentifier, YNetRunner> getNetRunners() {
        return _caseToNetRunnerMap;
    }


    public Set<YWorkItem> getChildrenOf(String workItemID) {
        YWorkItem item = _idStringToWorkItemsMap.get(workItemID);
        return (item != null) ? item.getChildren() : new HashSet<YWorkItem>();
    }

    /**
     * Removes all work items for a given case id.  Searches through the work items for
     * ones that are related to the case id and removes them.
     * Called by YEngine.cancelCase()
     * @param caseID must be a case id (not a child of a caseid).
     */
    public Set<YWorkItem> removeWorkItemsForCase(YIdentifier caseID) {
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid.");
        }

        Set<YWorkItem> removedItems = new HashSet<YWorkItem>();
        for (YWorkItem item : getWorkItemsForCase(caseID)) {
            removedItems.addAll(removeWorkItemFamily(item));
        }
        return removedItems;
    }


    public List<YWorkItem> getWorkItemsForCase(YIdentifier caseID) {
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid.");
        }
        
        List<YWorkItem> caseItems = new ArrayList<YWorkItem>();
        for (YWorkItem item : getWorkItems()) {
            YWorkItemID wid = item.getWorkItemID();

            //get the root id for this work item's case
            YIdentifier rootCaseID = wid.getCaseID().getRootAncestor();

            //if a work item's root case id matches case passed in save it
            if (rootCaseID.toString().equals(caseID.toString())) {
                caseItems.add(item);
            }
        }
        return caseItems;
    }


    public Set<YWorkItem> getWorkItemsWithIdentifier(String idType, String id) {
        Set<YWorkItem> result = new HashSet<YWorkItem>() ;

        // find out which items belong to the specified case/spec/task
        for (YWorkItem item : getWorkItems()) {
            if ((idType.equalsIgnoreCase("spec") &&
                 item.getSpecificationID().getUri().equals(id)) ||
                (idType.equalsIgnoreCase("case") &&
                 (item.getCaseID().toString().equals(id) ||
                  item.getCaseID().toString().startsWith(id + "."))) ||
                (idType.equalsIgnoreCase("task") && item.getTaskID().equals(id)))

                result.add(item);
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }


    public void dump(Logger logger) {
        logger.debug("\n*** DUMPING " + _caseToNetRunnerMap.size() +
                     " ENTRIES IN CASE_2_NETRUNNER MAP ***");
        int sub = 1;
        for (YIdentifier key : _caseToNetRunnerMap.keySet()) {
             if (key == null) {
                 logger.debug("Key = NULL !!!");
             }
             else {
                 YNetRunner runner = _caseToNetRunnerMap.get(key);
                 logger.debug("Entry " + sub++ + " Key=" + key.get_idString());
                 logger.debug(("    CaseID        " + runner.get_caseID()));
                 logger.debug("     YNetID        " + runner.getSpecificationID().getUri());
             }
        }
        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");

        logger.debug("\n*** DUMPING " + _idStringToWorkItemsMap.size() +
                     " ENTRIES IN ID_2_WORKITEMS_MAP ***");
        sub = 1;
        for (String key : _idStringToWorkItemsMap.keySet()) {
            YWorkItem workitem = _idStringToWorkItemsMap.get(key);
            logger.debug("Entry " + sub++ + " Key=" + key);
            logger.debug(("    WorkitemID        " + workitem.getIDString()));
        }
        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");
    }

}
