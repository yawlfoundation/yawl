/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem.Status;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction.Operation;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/05/2003
 * Time: 14:04:39
 * 
 */
public class YWorkItemRepository {
//    private static Map<String,YWorkItem> _idStringToWorkItemsMap;//[case&taskIDStr=YWorkItem]
//    protected static Map<YIdentifier,YNetRunner> _caseToNetRunnerMap;
    private static YWorkItemRepository _myInstance;
//    private YEngineInterface _engine;

    private YWorkItemRepository() {
//        _idStringToWorkItemsMap = new HashMap<String,YWorkItem>();
//        _caseToNetRunnerMap = new HashMap<YIdentifier,YNetRunner>();
    }

	public void dump(Logger logger) {
		try {
			List<YWorkItem> items = getEngine().getDao().retrieveByRestriction(YWorkItem.class, new Unrestricted());
			
			logger.error("\n*** DUMPING " + items.size() + " YWORKITEMS ***");
	        {
	            Iterator<YWorkItem> iterator = items.iterator();
	            int sub = 0;
	            while (iterator.hasNext()) {
	                sub++;
	                YWorkItem workitem = iterator.next();

	                logger.error("Entry " + sub);
	                logger.error(("    WorkitemID        " + workitem.getIDString()));
	            }
	        }

	        logger.error("*** DUMP OF YWORKITEMS ENDS");
		}
		catch(Exception e) {
			logger.error("Error taking dump!", e);
		}
		
//        logger.debug("\n*** DUMPING " + _caseToNetRunnerMap.size() + " ENTRIES IN CASE_2_NETRUNNER MAP ***");
//        {
//            Iterator keys = _caseToNetRunnerMap.keySet().iterator();
//            int sub = 0;
//            while (keys.hasNext()) {
//                sub++;
//                Object objKey = keys.next();
//
//                if (objKey == null) {
//                    logger.error("Key = NULL !!!");
//                } else {
//                    YIdentifier key = (YIdentifier) objKey;
//                    YNetRunner runner = _caseToNetRunnerMap.get(key);
//
//                    logger.error("Entry " + sub + " Key=" + key.getId());
//                    logger.error(("    CaseID        " + runner.getCaseID().toString()));
//                    logger.error("     YNetID        " + runner.getYNetID());
//                }
//            }
//        }
//
//        logger.error("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");
//
//        logger.error("\n*** DUMPING " + _idStringToWorkItemsMap.size() + " ENTRIES IN ID_2_WORKITEMS_MAP ***");
//        {
//            Iterator keys = _idStringToWorkItemsMap.keySet().iterator();
//            int sub = 0;
//            while (keys.hasNext()) {
//                sub++;
//                String key = (String) keys.next();
//                YWorkItem workitem = _idStringToWorkItemsMap.get(key);
//
//                logger.error("Entry " + sub + " Key=" + key);
//                logger.error(("    WorkitemID        " + workitem.getIDString()));
//            }
//        }
//
//        logger.error("*** DUMP OF CASE_2_NETRUNNER_MAP ENDS");
    }


    public static YWorkItemRepository getInstance() {
        if (_myInstance == null) {
            _myInstance = new YWorkItemRepository();
//            _myInstance._engine = EngineFactory.getExistingEngine();
        }
        return _myInstance;
    }

    private YEngineInterface getEngine() {
    	return EngineFactory.getExistingEngine();
    }

    //mutators
//    protected void addNewWorkItem(YWorkItem workItem) {
//        Logger.getLogger(this.getClass()).debug("--> addNewWorkItem: " + workItem.getIDString());
//        _idStringToWorkItemsMap.put(workItem.getIDString(), workItem);
//    }


    public void removeWorkItem(YWorkItem workItem) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> cancelAllWorkItemsInGroupOf: " + workItem.getIDString());
//        _idStringToWorkItemsMap.remove(workItem.getIDString());
        getEngine().getDao().delete( workItem );
    }


    public boolean removeWorkItemFamily(YWorkItem workItem) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> removeWorkItemFamily: " + workItem.getIDString());
        Set<YWorkItem> children = workItem.getParent() == null ? workItem.getChildren() : workItem.getParent().getChildren();
        YWorkItem parent = workItem.getParent() == null ? workItem : workItem.getParent();
        if (parent != null) {
            if (children != null) {
                for (YWorkItem siblingItem : children) {
                	YWorkItem engineCopy = (YWorkItem) getEngine().getDao().retrieve(YWorkItem.class, siblingItem.getId());
                	if (engineCopy != null) {
                		removeWorkItem(siblingItem);
//                		getEngine().getDao().delete(engineCopy);
                	}
//                	_idStringToWorkItemsMap.remove(siblingItem.getIDString());
                }
            }
            YWorkItem engineCopy = (YWorkItem) getEngine().getDao().retrieve(YWorkItem.class, parent.getId());
            if (engineCopy != null) {
            	removeWorkItem(engineCopy);
//            	getEngine().getDao().delete(engineCopy);
            }
//            _idStringToWorkItemsMap.remove(parent.getIDString());
            return true;
        }
        return false;
    }


    //look up tables to find netrunners
//    public void setNetRunnerToCaseIDBinding(YNetRunner netRunner, YIdentifier caseID) {
//        _caseToNetRunnerMap.put(caseID, netRunner);
//    }


    // XXX Should this do anything with the database?
//    public void clear() {
//        _idStringToWorkItemsMap = new HashMap<String,YWorkItem>();
//        _caseToNetRunnerMap = new HashMap<YIdentifier,YNetRunner>();
//    }


    /**
     * Cancels the net runner and removes any workitems that belong to it.
     * @param caseIDForNet
     */
//
//	This is where we are now::::::::
//
    public void cancelNet(YIdentifier caseIDForNet) throws YPersistenceException {
//        _caseToNetRunnerMap.remove(caseIDForNet);
//        Set<String> itemsToRemove = new HashSet<String>();
        
        List<YWorkItem> itemsToRemove = getEngine().getDao().retrieveByRestriction(YWorkItem.class,
        		new PropertyRestriction("identifierString", Comparison.EQUAL, caseIDForNet.getId()));
        
        
//        List<YWorkItem> allWorkItems = new ArrayList<YWorkItem>(_idStringToWorkItemsMap.values());
//        //go through all the work items and if there are any belonging to
//        //the id for this net then remove them.
//        for (YWorkItem item : allWorkItems) {
//            YIdentifier identifier = item.getWorkItemID().getCaseID();
//            if (identifier.isImmediateChildOf(caseIDForNet) ||
//                    identifier.toString().equals(caseIDForNet.toString())) {
//                itemsToRemove.add(item.getIDString());
//            }
//        }
        removeItems(itemsToRemove);
    }


    //###################################################################################
    //                                  accessors
    //###################################################################################

    public YNetRunner getNetRunner(YIdentifier caseID) throws YPersistenceException {
//        return _caseToNetRunnerMap.get(caseID);
    	return getEngine().getNetRunner(caseID);
    }


    public YWorkItem getWorkItem(String caseIDStr, String taskID) throws YPersistenceException {
    	return getWorkItem(caseIDStr + ":" + taskID);
//        return _idStringToWorkItemsMap.get(caseIDStr + ":" + taskID);
    }


    public YWorkItem getWorkItem(String workItemID) throws YPersistenceException {
    	
		List<YWorkItem> testitems = getEngine().getDao().retrieveByRestriction(YWorkItem.class, new Unrestricted());
		
        {
            Iterator<YWorkItem> iterator = testitems.iterator();
            int sub = 0;
            while (iterator.hasNext()) {
                sub++;
                YWorkItem workitem = iterator.next();

            }
        }

    	List<YWorkItem> items = getEngine().getDao().retrieveByRestriction(YWorkItem.class,
    			new PropertyRestriction("thisId", Comparison.EQUAL, workItemID));
    		
    	if(items.size() == 1) {
    		return items.get(0);
    	}
    	else if(items.size() > 1) {
    		throw new YPersistenceException("error! " + items.size() + " work items had the same thisid!");
    	}
    	return null;
//        return _idStringToWorkItemsMap.get(workItemID);
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set <YWorkItem> getEnabledWorkItems() throws YPersistenceException {
//        Logger.getLogger(this.getClass()).debug("--> getEnabledWorkItems: _idStringToWorkItemsMap=" + _idStringToWorkItemsMap.size() + " _caseToNetRunnerMap=" + _caseToNetRunnerMap.size());
        List<YWorkItem> all = getWorkItemsWithStatus(YWorkItem.Status.Enabled);
    	List<YWorkItem> itemsToRemove = new ArrayList<YWorkItem>();
    	Iterator<YWorkItem> iterator = all.iterator();
    	while(iterator.hasNext()) {
    		YWorkItem item = iterator.next();
    		boolean addedOne = false;
    		YNetRunner runner = getNetRunner(item.getYIdentifier());
            Set<YExternalNetElement> enabledTasks = runner.getEnabledTasks();
            for (YExternalNetElement task : enabledTasks) {
                if (task.getID().equals(item.getTaskID())) {
                    addedOne = true;
                    break;
                }
            }
            if (!addedOne) {
                itemsToRemove.add(item);
                iterator.remove();
            }
    	}
        
//    	Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        Set<String> itemsToRemove = new HashSet<String>();
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            if (workitem.getStatus().equals(YWorkItem.Status.Enabled)) {
//                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
//                YNetRunner runner = _caseToNetRunnerMap.get(
//                        caseID);
//                boolean addedOne = false;
//                Set<YExternalNetElement> enabledTasks = runner.getEnabledTasks();
//                for (YExternalNetElement task : enabledTasks) {
//                    if (task.getID().equals(workitem.getTaskID())) {
//                        aSet.add(workitem);
//                        addedOne = true;
//                    }
//                }
//                if (!addedOne) {
//                    itemsToRemove.add(workitem.getIDString());
//                }
//            }
//        }
        removeItems(itemsToRemove);

        Logger.getLogger(this.getClass()).debug("<-- getEnabledWorkItems");
        return new HashSet<YWorkItem>(all);
    }


    private void removeItems(Collection<YWorkItem> itemsToRemove) throws YPersistenceException {
        for (YWorkItem item : itemsToRemove) {
//        	YWorkItem item = (YWorkItem)_idStringToWorkItemsMap.get(workItemID);
        	removeWorkItem( item );
//        	_idStringToWorkItemsMap.remove(workItemID); 
//
//        	getEngine().getDao().delete( item );
        }
    }
    
    private List<YWorkItem> getWorkItemsWithStatus(Status status) throws YPersistenceException {
    	return getEngine().getDao().retrieveByRestriction(YWorkItem.class,
        		new PropertyRestriction("status", Comparison.EQUAL, status));
    }


    public Set<YWorkItem> getParentWorkItems() throws YPersistenceException {
//        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            if (workitem.getStatus() == YWorkItem.Status.IsParent) {
//                aSet.add(workitem);
//            }
//        }
//        return aSet;
    	return new HashSet<YWorkItem>(getWorkItemsWithStatus(YWorkItem.Status.IsParent));
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set<YWorkItem> getFiredWorkItems() throws YPersistenceException {
    	List<YWorkItem> all = getWorkItemsWithStatus(YWorkItem.Status.Fired);
    	List<YWorkItem> itemsToRemove = new ArrayList<YWorkItem>();
    	Iterator<YWorkItem> iterator = all.iterator();
    	while(iterator.hasNext()) {
    		YWorkItem item = iterator.next();
    		boolean addedOne = false;
    		YNetRunner runner = getNetRunner(item.getYIdentifier());
    		if(runner != null) {
	            Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
	            for (YExternalNetElement task : busyTasks) {
	                if (task.getID().equals(item.getTaskID())) {
	                    addedOne = true;
	                    break;
	                }
	            }
	            if (!addedOne) {
	                itemsToRemove.add(item);
	                iterator.remove();
	            }
    		}
    	}
    	
//        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        Set<String> itemsToRemove = new HashSet<String>();
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            if (workitem.getStatus() == YWorkItem.Status.Fired) {
//                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
//                YNetRunner runner = _caseToNetRunnerMap.get(
//                        caseID.getParent());
//                boolean addedOne = false;
//                if (null != runner) {
//                    Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
//                    for (YExternalNetElement task : busyTasks) {
//                        if (task.getID().equals(workitem.getTaskID())) {
//                            aSet.add(workitem);
//                            addedOne = true;
//                        }
//                    }
//                }
//                if (!addedOne) {
//                    itemsToRemove.add(workitem.getIDString());
//                }
//            }
//        }
        removeItems(itemsToRemove);
        return new HashSet<YWorkItem>(all);
    }


    public Set<YWorkItem> getExecutingWorkItems() throws YPersistenceException {
//        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            if (workitem.getStatus() == YWorkItem.Status.Executing) {
//                aSet.add(workitem);
//            }
//        }
//        return aSet;
    	return new HashSet<YWorkItem>(getWorkItemsWithStatus(YWorkItem.Status.Executing));
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set<YWorkItem> getExecutingWorkItems(String userName) throws YPersistenceException {
    	List<YWorkItem> all = getEngine().getDao().retrieveByRestriction(YWorkItem.class,
        		new LogicalRestriction(
        				new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.Executing),
        				Operation.AND,
        				new PropertyRestriction("userWhoIsExecutingThisItem", Comparison.EQUAL, userName)
        				));
    	List<YWorkItem> itemsToRemove = new ArrayList<YWorkItem>();
    	Iterator<YWorkItem> iterator = all.iterator();
    	while(iterator.hasNext()) {
    		YWorkItem item = iterator.next();
    		boolean addedOne = false;
    		YNetRunner runner = getNetRunner(item.getYIdentifier());
    		if(runner != null) {
	            Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
	            for (YExternalNetElement task : busyTasks) {
	                if (task.getID().equals(item.getTaskID())) {
	                    addedOne = true;
	                    break;
	                }
	            }
	            if (!addedOne) {
	                itemsToRemove.add(item);
	                iterator.remove();
	            }
    		}
    	}
    	
//        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        Set<String> itemsToRemove = new HashSet<String>();
//
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            if (workitem.getStatus() == YWorkItem.Status.Executing) {
//                YIdentifier caseID = workitem.getWorkItemID().getCaseID();
//                YNetRunner runner =
//                        _caseToNetRunnerMap.get(caseID.getParent());
//                boolean foundOne = false;
//                Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
//                for (YExternalNetElement task : busyTasks) {
//                    if (task.getID().equals(workitem.getTaskID())) {
//                        foundOne = true;
//                        if (workitem.getUserWhoIsExecutingThisItem().equals(userName)) {
//                            aSet.add(workitem);
//                        }
//                    }
//                }
//                if (!foundOne) {
//                    itemsToRemove.add(workitem.getIDString());
//                }
//            }
//        }
        removeItems(itemsToRemove);
        return new HashSet<YWorkItem>(all);
    }
    
    public Set<YWorkItem> getSuspendedWorkItems() throws YPersistenceException {
//    	Set<YWorkItem> set = new HashSet<YWorkItem>();
//    	for( YWorkItem item : _idStringToWorkItemsMap.values() ) {
//    		if( item.getStatus() == YWorkItem.Status.Suspended ) {
//    			set.add( item );
//    		}
//    	}
//    	return set;
    	return new HashSet<YWorkItem>(getWorkItemsWithStatus(YWorkItem.Status.Suspended));
    }


    public Set<YWorkItem> getCompletedWorkItems() throws YPersistenceException {
//        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            if (workitem.getStatus() == YWorkItem.Status.Complete) {
//                aSet.add(workitem);
//            }
//        }
//        return aSet;
    	return new HashSet<YWorkItem>(getWorkItemsWithStatus(YWorkItem.Status.Complete));
    }

    public Set<YWorkItem> getAllWorkItems() throws YPersistenceException {
//    	return new HashSet<YWorkItem>( _idStringToWorkItemsMap.values() );
    	return new HashSet<YWorkItem>(getEngine().getDao().retrieveByRestriction(YWorkItem.class, new Unrestricted()));
    }

    /**
     * Side effect: deletes dead items from the repository.
     */
    public Set<YWorkItem> getWorkItems() throws YPersistenceException {
    	List<YWorkItem> all = getEngine().getDao().retrieveByRestriction(YWorkItem.class,
    			new LogicalRestriction(
        		new LogicalRestriction(
        		new LogicalRestriction(
        		new LogicalRestriction(
        		new LogicalRestriction(
        				new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.Enabled),
        				Operation.OR,
        				new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.IsParent)
        				),
        				Operation.OR,
        				new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.Complete)
        				),
        				Operation.OR,
        				new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.Executing)
        				),
        				Operation.OR,
        				new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.Fired)
        				),
        				Operation.OR,
        				new LogicalRestriction(
        						new PropertyRestriction("status", Comparison.EQUAL, YWorkItem.Status.Suspended),
        						Operation.AND,
        						new PropertyRestriction("prevStatus", Comparison.EQUAL, YWorkItem.Status.Enabled)
        				)));
    	List<YWorkItem> itemsToRemove = new ArrayList<YWorkItem>();
    	Iterator<YWorkItem> iterator = all.iterator();
    	while(iterator.hasNext()) {
    		YWorkItem item = iterator.next();
    		YNetRunner runner;
    		if (item.getStatus().equals(YWorkItem.Status.Enabled) ||
                    item.getStatus().equals(YWorkItem.Status.IsParent) ||
                    item.isEnabledSuspended()) {
                runner = getNetRunner(item.getYIdentifier());
            } else if (item.getStatus().equals(YWorkItem.Status.Complete) ||
                    item.getStatus().equals(YWorkItem.Status.Executing) ||
                    item.getStatus().equals(YWorkItem.Status.Fired)) {
                runner = getNetRunner(item.getYIdentifier().getParent());
            } else {
                throw new YPersistenceException("Status " + item.getStatus() + " not expected here!" + item.thisId);
            }
    		boolean addedOne = false;
    		
    		Set<YExternalNetElement> tasks = new HashSet<YExternalNetElement>();
    		tasks.addAll(runner.getBusyTasks());
    		tasks.addAll(runner.getEnabledTasks());
    		
            for (YExternalNetElement task : tasks) {
                if (task.getID().equals(item.getTaskID())) {
                    addedOne = true;
                    break;
                }
            }
            if (!addedOne) {
                itemsToRemove.add(item);
                iterator.remove();
            }
    	}
    	
//        Set<YWorkItem> aSet = new HashSet<YWorkItem>();
//        Set<String> itemsToRemove = new HashSet<String>();
//        //rather than just return the work items we have to chek that the items in the
//        //repository are in synch with the engine
//        for (YWorkItem workitem : _idStringToWorkItemsMap.values()) {
//            YIdentifier caseID = workitem.getWorkItemID().getCaseID();
//            YNetRunner runner;
//            if (workitem.getStatus().equals(YWorkItem.Status.Enabled) ||
//                    workitem.getStatus().equals(YWorkItem.Status.IsParent) ||
//                    workitem.isEnabledSuspended()) {
//                runner = _caseToNetRunnerMap.get(caseID);
//            } else if (workitem.getStatus().equals(YWorkItem.Status.Complete) ||
//                    workitem.getStatus().equals(YWorkItem.Status.Executing) ||
//                    workitem.getStatus().equals(YWorkItem.Status.Fired)) {
//                runner = _caseToNetRunnerMap.get(caseID.getParent());
//            } else {
//                continue;
//            }
//            boolean foundOne = false;
//            Set<YExternalNetElement> busyTasks = runner.getBusyTasks();
//            Set<YExternalNetElement> enableTasks = runner.getEnabledTasks();
//            Set<YExternalNetElement> workItemTasks = new HashSet<YExternalNetElement>();
//            workItemTasks.addAll(busyTasks);
//            workItemTasks.addAll(enableTasks);
//
//            for (YExternalNetElement task : workItemTasks) {
//                if (task.getID().equals(workitem.getTaskID())) {
//                    foundOne = true;
//                    aSet.add(workitem);
//                }
//            }
//            //clean up all the work items that are out of synch with the engine.
//            if (!foundOne) {
//                itemsToRemove.add(workitem.getIDString());
//            }
//        }
        removeItems(itemsToRemove);
        return new HashSet<YWorkItem>(all);
    }


//    public Map getNetRunners() {
//        return _caseToNetRunnerMap;
//    }

    public Set getChildrenOf(String workItemID) throws YPersistenceException {
//        YWorkItem item = _idStringToWorkItemsMap.get(workItemID);
        YWorkItem item = getWorkItem(workItemID);
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
    public void removeWorkItemsForCase(YIdentifier caseID) throws YPersistenceException {
        if (caseID == null || caseID.getParent() != null) {
            throw new IllegalArgumentException("the argument <caseID> is not valid." + caseID);
        }
        Set<YWorkItem> workItems = getWorkItems();
        for (YWorkItem item : workItems) {
//            YWorkItemID wid = item.getWorkItemID();
            //get the root id for this work items case
            //and if it matches the cancellation case id then remove it.
            YIdentifier workItemsCaseID = item.getCaseID();
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
