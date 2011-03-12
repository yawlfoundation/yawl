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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache of active workitems.
 * 
 * @author Lachlan Aldred
 * Date: 30/05/2003
 * Time: 14:04:39
 *
 * @author Michael Adams (refactored for v2.1, 2.2)
 * 
 */
public class YWorkItemRepository extends ConcurrentHashMap<String, YWorkItem> { //[case&taskIDStr=YWorkItem]
    private final Logger _logger;

    public YWorkItemRepository() {
        super();
        _logger = Logger.getLogger(YWorkItemRepository.class);
    }


    protected void add(YWorkItem workItem) {
        _logger.debug("--> YWorkItemRepository#add: " + workItem.getIDString());
        this.putIfAbsent(workItem.getIDString(), workItem);
    }


    public YWorkItem get(String caseIDStr, String taskID) {
        return get(caseIDStr + ":" + taskID);
    }


    public void remove(YWorkItem workItem) {
        _logger.debug("--> YWorkItemRepository#remove: " + workItem.getIDString());
        this.remove(workItem.getIDString());
    }


    public Set<YWorkItem> removeWorkItemFamily(YWorkItem workItem) {
        _logger.debug("--> removeWorkItemFamily: " + workItem.getIDString());
        Set<YWorkItem> removedSet = new HashSet<YWorkItem>();
        YWorkItem parent = workItem.getParent() != null ? workItem.getParent() : workItem;
        Set<YWorkItem> children = parent.getChildren();
        if (children != null) {
           for (YWorkItem siblingItem : children) {
               remove(siblingItem);
               removedSet.add(siblingItem);
            }
        }
        remove(parent);
        removedSet.add(parent);
        return removedSet;
    }


    /**
     * Removes all workitems that belong to a net.
     * @param caseIDForNet
     */
    public Set<YWorkItem> cancelNet(YIdentifier caseIDForNet) {
        Set<String> itemsToRemove = new HashSet<String>();
        for (YWorkItem item : this.values()) {
            YIdentifier identifier = item.getWorkItemID().getCaseID();
            if (identifier.isImmediateChildOf(caseIDForNet) ||
                    identifier.toString().equals(caseIDForNet.toString())) {
                itemsToRemove.add(item.getIDString());
            }
        }
        return removeItems(itemsToRemove);
    }



    private Set<YWorkItem> removeItems(Set<String> itemsToRemove) {
        Set<YWorkItem> removedSet = new HashSet<YWorkItem>();
        for (String workItemID : itemsToRemove) {
            YWorkItem item = this.remove(workItemID);
            if (item != null) removedSet.add(item);
        }
        return removedSet;
    }


    public Set<YWorkItem> getParentWorkItems() {
        return getWorkItems(statusIsParent);
    }


    public Set<YWorkItem> getEnabledWorkItems() {
        return getWorkItems(statusEnabled);
    }


    public Set<YWorkItem> getFiredWorkItems() {
        return getWorkItems(statusFired);
    }


    public Set<YWorkItem> getExecutingWorkItems() {
        return getWorkItems(statusExecuting);
    }


    public Set<YWorkItem> getExecutingWorkItems(String serviceName) {
        Set<YWorkItem> executingItems = new HashSet<YWorkItem>();
        for (YWorkItem workitem : getWorkItems(statusExecuting)) {
            if (workitem.getExternalClient().getUserName().equals(serviceName)) {
                executingItems.add(workitem);
            }
        }
        return executingItems;
    }


    public Set<YWorkItem> getCompletedWorkItems() {
        return getWorkItems(statusComplete);
    }


    public Set<YWorkItem> getWorkItems(YWorkItemStatus status) {
        Set<YWorkItem> itemSet = new HashSet<YWorkItem>();
        for (YWorkItem workitem : this.values()) {
            if (workitem.getStatus() == status) {
                itemSet.add(workitem);
            }
        }
        return itemSet;
    }


    public Set<YWorkItem> getWorkItems() {
        cleanseRepository();
        return new HashSet<YWorkItem>(this.values());
    }


    // check that the items in the repository are in synch with the engine
    public void cleanseRepository() {
        Set<String> itemsToRemove = new HashSet<String>();
        YNetRunnerRepository netRunnerRepository =
                YEngine.getInstance().getNetRunnerRepository();
        YNetRunner runner = null;
        for (YWorkItem workitem : this.values()) {
            YWorkItemStatus status = workitem.getStatus();
            YIdentifier caseID = workitem.getWorkItemID().getCaseID();
            if (status.equals(statusEnabled) || status.equals(statusIsParent) ||
                    workitem.isEnabledSuspended()) {
                runner = netRunnerRepository.get(caseID);
            }
            else if (status.equals(statusComplete) || status.equals(statusExecuting) ||
                    status.equals(statusSuspended) || status.equals(statusFired)) {
                runner = netRunnerRepository.get(caseID.getParent());
            }

            if (runner != null) {                                      //MLF can be null
                boolean foundOne = false;
                for (YTask task : runner.getActiveTasks()) {
                    if (task.getID().equals(workitem.getTaskID())) {
                        foundOne = true;
                        break;
                    }
                }

                //clean up all the work items that are out of synch with the engine.
                if (! foundOne) itemsToRemove.add(workitem.getIDString());
            }
        }
        removeItems(itemsToRemove);
    }


    public Set<YWorkItem> getChildrenOf(String workItemID) {
        YWorkItem item = this.get(workItemID);
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
        Set<YWorkItem> matches = new HashSet<YWorkItem>() ;

        // find out which items belong to the specified case/spec/task
        for (YWorkItem item : getWorkItems()) {
            if ((idType.equalsIgnoreCase("spec") &&
                 item.getSpecificationID().getUri().equals(id)) ||
                (idType.equalsIgnoreCase("case") &&
                 (item.getCaseID().toString().equals(id) ||
                  item.getCaseID().toString().startsWith(id + "."))) ||
                (idType.equalsIgnoreCase("task") && item.getTaskID().equals(id)))

                matches.add(item);
        }
        if (matches.isEmpty()) matches = null ;
        return matches ;
    }


    public void dump(Logger logger) {
        logger.debug("\n*** DUMPING " + this.size() +
                     " ENTRIES IN ID_2_WORKITEMS_MAP ***");
        int sub = 1;
        for (String key : this.keySet()) {
            YWorkItem workitem = this.get(key);
            if (workitem != null) {
                logger.debug("Entry " + sub++ + " Key=" + key);
                logger.debug(("    WorkitemID        " + workitem.getIDString()));
            }    
        }
        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");
    }

}
