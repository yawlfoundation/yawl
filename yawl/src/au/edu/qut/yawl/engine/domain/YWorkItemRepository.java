/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/05/2003
 * Time: 14:04:39
 * 
 */
public class YWorkItemRepository {
    private static Map<String,YWorkItem> _idStringToWorkItemsMap;//[case&taskIDStr=YWorkItem]
    protected static Map<YIdentifier,YNetRunner> _caseToNetRunnerMap;
    private static YWorkItemRepository _myInstance;


    private YWorkItemRepository() {
        _idStringToWorkItemsMap = new HashMap<String,YWorkItem>();
        _caseToNetRunnerMap = new HashMap<YIdentifier,YNetRunner>();
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
                    YNetRunner runner = _caseToNetRunnerMap.get(key);

                    logger.debug("Entry " + sub + " Key=" + key.getId());
                    logger.debug(("    CaseID        " + runner.getCaseID().toString()));
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
                YWorkItem workitem = _idStringToWorkItemsMap.get(key);

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
        Logger.getLogger(this.getClass()).debug("--> addNewWorkItem: " + workItem.getIDString());
        _idStringToWorkItemsMap.put(workItem.getIDString(), workItem);
    }


    private void removeWorkItem(YWorkItem workItem) {
        Logger.getLogger(this.getClass()).debug("--> cancelAllWorkItemsInGroupOf: " + workItem.getIDString());
        _idStringToWorkItemsMap.remove(workItem.getIDString());
        //todo Question by Lachlan: add code to remove from DB here?
    }


    public boolean removeWorkItemFamily(YWorkItem workItem) {
        Logger.getLogger(this.getClass()).debug("--> removeWorkItemFamily: " + workItem.getIDString());
        Set<YWorkItem> children = workItem.getParent() == null ? workItem.getChildren() : workItem.getParent().getChildren();
        YWorkItem parent = workItem.getParent() == null ? workItem : workItem.getParent();
        if (parent != null) {
            if (children != null) {
                for (YWorkItem siblingItem : children) {
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


    public void clear() {
        _idStringToWorkItemsMap = new HashMap<String,YWorkItem>();
        _caseToNetRunnerMap = new HashMap<YIdentifier,YNetRunner>();
    }


    /**
     * Cancels the net runner and removes any workitems that belong to it.
     * @param caseIDForNet
     */
    public void cancelNet(YIdentifier caseIDForNet) {
        _caseToNetRunnerMap.remove(caseIDForNet);
        Set<String> itemsToRemove = new HashSet<String>();
        List<YWorkItem> allWorkItems = new ArrayList<YWorkItem>(_idStringToWorkItemsMap.values());
        //go through all the work items and if there are any belonging to
        //the id for this net then remove them.
        for (YWorkItem item : allWorkItems) {
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
        return _caseToNetRunnerMap.get(caseID);
    }


    public YWorkItem getWorkItem(String caseIDStr, String taskID) {
        return _idStringToWorkItemsMap.get(caseIDStr + ":" + taskID);
    }


    public YWorkItem getWorkItem(String workItemID) {
        return _idStringToWorkItemsMap.get(workItemID);
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set <YWorkItem> getEnabledWorkItems() {
        Logger.getLogger(this.getClass()).debug("--> getEnabledWorkItems: _idStringToWorkItemsMap=" + _idStringToWorkItemsMap.size() + " _caseToNetRunnerMap=" + _caseToNetRunnerMap.size());
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus().equals(YWorkItem.Status.Enabled)) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = _caseToNetRunnerMap.get(
                        caseID);
                boolean addedOne = false;
                Set<YExternalNetElement> enabledTasks = runner.getEnabledTasks();
                for (YExternalNetElement task : enabledTasks) {
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

        Logger.getLogger(this.getClass()).debug("<-- getEnabledWorkItems");
        return aSet;
    }


    private void removeItems(Set<String> itemsToRemove) {
        for (String workItemID : itemsToRemove) {
            _idStringToWorkItemsMap.remove(workItemID);
        }
    }


    public Set<YWorkItem> getParentWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == YWorkItem.Status.IsParent) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set<YWorkItem> getFiredWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == YWorkItem.Status.Fired) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner = _caseToNetRunnerMap.get(
                        caseID.getParent());
                boolean addedOne = false;
                if (null != runner) {
                    Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
                    for (YExternalNetElement task : busyTasks) {
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


    public Set<YWorkItem> getExecutingWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == YWorkItem.Status.Executing) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set<YWorkItem> getExecutingWorkItems(String userName) {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();

        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == YWorkItem.Status.Executing) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                YNetRunner runner =
                        _caseToNetRunnerMap.get(caseID.getParent());
                boolean foundOne = false;
                Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
                for (YExternalNetElement task : busyTasks) {
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
    
    public Set<YWorkItem> getSuspendedWorkItems() {
    	Set<YWorkItem> set = new HashSet<YWorkItem>();
    	for( YWorkItem item : _idStringToWorkItemsMap.values() ) {
    		if( item.getStatus() == YWorkItem.Status.Suspended ) {
    			set.add( item );
    		}
    	}
    	return set;
    }


    public Set<YWorkItem> getCompletedWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            if (workitem.getStatus() == YWorkItem.Status.Complete) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }

    public Set<YWorkItem> getAllWorkItems() {
    	return new HashSet<YWorkItem>( _idStringToWorkItemsMap.values() );
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set<YWorkItem> getWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();
        //rather than just return the work items we have to chek that the items in the
        //repository are in synch with the engine
        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
            YIdentifier caseID = workitem.getWorkItemID().getCaseID();
            YNetRunner runner;
            if (workitem.getStatus().equals(YWorkItem.Status.Enabled) ||
                    workitem.getStatus().equals(YWorkItem.Status.IsParent) ||
                    workitem.isEnabledSuspended()) {
                runner = _caseToNetRunnerMap.get(caseID);
            } else if (workitem.getStatus().equals(YWorkItem.Status.Complete) ||
                    workitem.getStatus().equals(YWorkItem.Status.Executing) ||
                    workitem.getStatus().equals(YWorkItem.Status.Fired)) {
                runner = _caseToNetRunnerMap.get(caseID.getParent());
            } else {
                continue;
            }
            boolean foundOne = false;
            Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
            Set<YExternalNetElement> enableTasks = runner.getEnabledTasks();
            Set<YExternalNetElement> workItemTasks = new HashSet<YExternalNetElement>();
            workItemTasks.addAll(busyTasks);
            workItemTasks.addAll(enableTasks);

            for (YExternalNetElement task : workItemTasks) {
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
        removeItems(itemsToRemove);
        return aSet;
    }


    public Map getNetRunners() {
        return _caseToNetRunnerMap;
    }

    public Set getChildrenOf(String workItemID) {
        YWorkItem item = _idStringToWorkItemsMap.get(workItemID);
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
    //todo LA Ques: Link to persistance??
    public void removeWorkItemsForCase(YIdentifier caseID) {
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid.");
        }
        Set<YWorkItem> workItems = getWorkItems();
        for (YWorkItem item : workItems) {
            YWorkItemID wid = item.getWorkItemID();
            //get the root id for this work items case
            //and if it matches the cancellation case id then remove it.
            YIdentifier workItemsCaseID = wid.getCaseID();
            while (workItemsCaseID.getParent() != null) {
                workItemsCaseID = workItemsCaseID.getParent();
            }
            //if a work item's root case id matches case to be cancelled
            //then remove it.
            if (workItemsCaseID.toString().equals(caseID.toString())) {
                removeWorkItemFamily(item);
            }
        }
    }
}
