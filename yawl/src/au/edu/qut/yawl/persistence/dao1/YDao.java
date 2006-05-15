/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;


/**
 * The YDao class is simply a skeleton implemention of all DAO types.
 * 
 * @author Dean Mao
 * @created Oct 27, 2005
 */
public abstract class YDao implements Dao, RunnerDao, WorkItemDao, SpecificationDao {
	
	
	// RunnerDao implementations
//    protected static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private Map<YIdentifier, String> _runningCaseIDToSpecIDMap = new HashMap<YIdentifier, String>();
    protected Map<YIdentifier, YNetRunner> _caseIDToNetRunnerMap = new HashMap<YIdentifier, YNetRunner>();
	public YNetRunner loadNetRunner( YIdentifier caseID ) {
		return _caseIDToNetRunnerMap.get(caseID);
	}
	public YNetRunner loadNetRunner(String caseID) {
		throw new RuntimeException("This method has not been implemented yet!");
	}
	public void storeNetRunner(YNetRunner netRunner, String specID) {
        _caseIDToNetRunnerMap.put(netRunner.getCaseID(), netRunner);
        _runningCaseIDToSpecIDMap.put(netRunner.getCaseID(), specID);
	}
	public void removeNetRunner(YIdentifier caseIDForNet) {
		_runningCaseIDToSpecIDMap.remove(caseIDForNet);
	}
	public Set getNetRunnerCaseIDs() {
		throw new RuntimeException("This method has not been implemented yet!");
	}
	public Set getRunningCaseIDs() {
		return _caseIDToNetRunnerMap.keySet();
	}
	public String getRunningSpecID( YIdentifier runningCaseID ) {
		return _runningCaseIDToSpecIDMap.get(runningCaseID);
	}
	public Set<String> getRunningSpecIDs() {
		return new HashSet<String>(_runningCaseIDToSpecIDMap.values());
	}
	
	// WorkItemDao implementations
    protected static Map<String, YWorkItem> _idStringToWorkItemsMap = new HashMap<String, YWorkItem>();//[case&taskIDStr=YWorkItem]
    protected static Map<YIdentifier, YNetRunner> _caseToNetRunnerMap = new HashMap<YIdentifier, YNetRunner>();

    public void dump(Logger logger) {
        logger.debug("\n*** DUMPING " + _caseToNetRunnerMap.size() + " ENTRIES IN CASE_2_NETRUNNER MAP ***");
        {
            Iterator keys = _caseToNetRunnerMap.keySet().iterator();
            int sub = 0;
            while (keys.hasNext()) {
                sub++;
                Object objKey = keys.next();

                if (objKey == null)
                {
                    logger.debug("Key = NULL !!!");
                }
                else
                {
                    YIdentifier key = (YIdentifier) objKey;
                    YNetRunner runner = _caseToNetRunnerMap.get(key);

                    logger.debug("Entry " + sub + " Key=" + key.getId());
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
                YWorkItem workitem = _idStringToWorkItemsMap.get(key);

                logger.debug("Entry " + sub + " Key=" + key);
                logger.debug(("    WorkitemID        " + workitem.getIDString()));
            }
        }

        logger.debug("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");

    }


    //mutators
    public void addNewWorkItem(YWorkItem workItem) {
        Logger.getLogger(this.getClass()).debug("--> addNewWorkItem: " + workItem.getIDString());
        _idStringToWorkItemsMap.put(workItem.getIDString(), workItem);
    }


    private void removeWorkItem(YWorkItem workItem) {
        Logger.getLogger(this.getClass()).debug("--> cancelAllWorkItemsInGroupOf: " + workItem.getIDString());
        _idStringToWorkItemsMap.remove(workItem.getIDString());
    }


    public boolean removeWorkItemFamily(YWorkItem workItem) {
        Logger.getLogger(this.getClass()).debug("--> removeWorkItemFamily: " + workItem.getIDString());
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
        _idStringToWorkItemsMap = new HashMap<String, YWorkItem>();
        _caseToNetRunnerMap = new HashMap<YIdentifier, YNetRunner>();
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
        for (int i = 0; i < allWorkItems.size(); i++) {
            YWorkItem item = allWorkItems.get(i);
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
/*        return (YNetRunner)
                (_caseToNetRunnerMap.containsKey(caseID) ?
                _caseToNetRunnerMap.get(caseID) : _caseToNetRunnerMap.get(caseID.getParent()));*/
    }


    public YWorkItem getWorkItem(String caseIDStr, String taskID) {
        return _idStringToWorkItemsMap.get(caseIDStr + ":" + taskID);
    }


    public YWorkItem getWorkItem(String workItemID) {
        return _idStringToWorkItemsMap.get(workItemID);
    }


    public Set getEnabledWorkItems() {
        Logger.getLogger(this.getClass()).debug("--> getEnabledWorkItems: _idStringToWorkItemsMap=" + _idStringToWorkItemsMap.size() + " _caseToNetRunnerMap=" + _caseToNetRunnerMap.size());

        Set<YWorkItem> aSet = new HashSet<YWorkItem>(); // TODO This variable needs a better name!
        Set<String> itemsToRemove = new HashSet<String>();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == YWorkItem.statusEnabled) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                // TODO: Could we create a Engine.getEnabledTask(caseID) instead?
                YNetRunner runner = _caseToNetRunnerMap.get(caseID);
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

        Logger.getLogger(this.getClass()).debug("<-- getEnabledWorkItems");
        return aSet;

    }

    private void removeItems(Set itemsToRemove) {
        for (Iterator iterator = itemsToRemove.iterator(); iterator.hasNext();) {
            String workItemID = (String) iterator.next();
            _idStringToWorkItemsMap.remove(workItemID);
        }
    }


    public Set getParentWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == YWorkItem.statusIsParent) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }


    public Set getFiredWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == YWorkItem.statusFired) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                // TODO Could we create an Engine.getBusyTasks(YIdentifier)?
                YNetRunner runner = _caseToNetRunnerMap.get(caseID.getParent());
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
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == YWorkItem.statusExecuting) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }


    protected Set getExecutingWorkItems(String userName) {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();

        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == YWorkItem.statusExecuting) {
                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
                // TODO Also Engine.getBusyTasks(YIdentifier)
                YNetRunner runner =  _caseToNetRunnerMap.get(caseID.getParent());
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
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();
            if (workitem.getStatus() == YWorkItem.statusComplete) {
                aSet.add(workitem);
            }
        }
        return aSet;
    }


    public Set getWorkItems() {
        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
        Set<String> itemsToRemove = new HashSet<String>();

        Iterator iter = _idStringToWorkItemsMap.values().iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();

            YIdentifier caseID = workitem.getWorkItemID().getCaseID();
            // TODO Also a case of Engine.getBusyTasks()/Engine.getEnabledTasks()
            YNetRunner runner;
            if (workitem.getStatus().equals(YWorkItem.statusEnabled) ||
                    workitem.getStatus().equals(YWorkItem.statusIsParent)) {
                runner = _caseToNetRunnerMap.get(caseID);
            } else if (workitem.getStatus().equals(YWorkItem.statusComplete) ||
                    workitem.getStatus().equals(YWorkItem.statusExecuting) ||
                    workitem.getStatus().equals(YWorkItem.statusFired)) {
                runner = _caseToNetRunnerMap.get(caseID.getParent());
            } else {
                continue;
            }
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
            if (!foundOne) {
                itemsToRemove.add(workitem.getIDString());
            }
        }
        removeItems(itemsToRemove);
        return aSet;
    }


    protected Map getNetRunners() {
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
    public void removeWorkItemsForCase(YIdentifier caseID) {
//    	_workItemRepository.removeWorkItemsForCase(caseID);
    	
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid.");
        }
        Set workItems = getWorkItems();
        for (Iterator iterator = workItems.iterator(); iterator.hasNext();) {
            YWorkItem item = (YWorkItem) iterator.next();
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
