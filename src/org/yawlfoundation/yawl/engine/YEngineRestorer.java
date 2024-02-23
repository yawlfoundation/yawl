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

package org.yawlfoundation.yawl.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.engine.time.YLaunchDelayer;
import org.yawlfoundation.yawl.engine.time.YTimedObject;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.*;

/**
 * Handles the restoration of persisted objects and data pertaining to the Engine.
 *
 * @author Michael Adams
 *         Creation Date: 25/06/2008
 */

public class YEngineRestorer {

    private YEngine _engine;
    private YPersistenceManager _pmgr;
    private Map<String, YIdentifier> _idLookupTable;
    private List<YNetRunner> _runners;
    private Map<String, YTask> _taskLookupTable;
    private boolean _hasServices;
    private boolean _importingCases = false;
    private Set<YClient> _addedDefaultClients;
    private Logger _log;


    // CONSTRUCTORS //

    protected YEngineRestorer() {}

    protected YEngineRestorer(YEngine engine, YPersistenceManager pmgr) {
        _engine = engine;
        _pmgr = pmgr;
        _idLookupTable = new HashMap<String, YIdentifier>();
        _taskLookupTable = new HashMap<String, YTask>();
        _log = LogManager.getLogger(this.getClass());
    }


    protected void restoreServicesAndClients() throws YPersistenceException {
        restoreYAWLServices();
        restoreExternalClients();
    }


    protected void restoreInstances() throws YPersistenceException {
        restoreProcessInstances();
        restoreWorkItems();
    }


    /**
     * Restores YAWL Services from persistence
     *
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    protected void restoreYAWLServices() throws YPersistenceException {
        _log.debug("Restoring Services - Starts");
        List<YAWLServiceReference> services = restoreObjects(YAWLServiceReference.class);
        for (YAWLServiceReference service : services) {
            _engine.addYawlService(service);
        }
        _hasServices = ! services.isEmpty();
        _log.debug("Restoring Services - Ends");
    }


    /**
     * Restores registered external client credentials (eg the editor logon)
     *
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    protected void restoreExternalClients() throws YPersistenceException {
        _log.debug("Restoring External Clients - Starts");
        List<YExternalClient> clients = restoreObjects(YExternalClient.class);
        if (! clients.isEmpty()) {
            for (YExternalClient client : clients) {
                _engine.addExternalClient(client);
            }
        } else {
            if (!_hasServices) {

                // no services and no clientapps indicates a fresh db (there should be at
                // least a row for the editor user) - so needs default accounts to be added
                _addedDefaultClients = _engine.loadDefaultClients();
            }
        }
        _log.debug("Restoring External Clients - Ends");
    }


    /**
     * Restores Specifications from persistence
     *
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    protected void restoreSpecifications() throws YPersistenceException {
        _log.debug("Restoring Specifications - Starts");
        for (YSpecification s : restoreObjects(YSpecification.class)) {
            loadSpecification(s);
        }
        _log.debug("Restoring Specifications - Ends");
    }


    /**
     * Restores the next available case number from persistence
     *
     * @return a YCaseNbrStore object initialised to the next available case number
     * @throws YPersistenceException if there's a problem reading from the tables
     */
    protected YCaseNbrStore restoreNextAvailableCaseNumber() throws YPersistenceException {
        YCaseNbrStore caseNbrStore = YCaseNbrStore.getInstance();
        Query query = _pmgr.createQuery("from YCaseNbrStore");
        if ((query != null) && (!query.list().isEmpty())) {
            caseNbrStore = (YCaseNbrStore) query.iterate().next();
            caseNbrStore.setPersisted(true);               // flag to update only
        } else {

            // secondary attempt: eg. if there's no case number stored (as will be
            // the case if this is the first restart after a database rebuild)
            caseNbrStore.setCaseNbr(YEventLogger.getInstance().getMaxCaseNbr());
        }

        // persisting flag must be reset as it is not itself persisted
        caseNbrStore.setPersisting(true);

        return caseNbrStore;
    }


    protected void restoreProcessInstances() throws YPersistenceException {
        _log.debug("Restoring process instances - Starts");
        List<YNetRunner> runners = restoreObjects(YNetRunner.class,
                "from YNetRunner order by case_id");
        runners = removeDeadRunners(runners);
        restoreProcessInstances(runners);
    }


    // called directly by CaseImporter
    protected void restoreProcessInstances(List<YNetRunner> runners) throws YPersistenceException {
        _log.debug("Restoring {} net runners", runners.size());
        _runners = runners;
        restoreRunners(_runners);
        _log.debug("Restoring process instances - Ends");
    }


    protected void restoreWorkItems() throws YPersistenceException {
        _log.debug("Restoring work items - Starts");
        restoreWorkItems(restoreObjects(YWorkItem.class));
    }


    protected void restoreWorkItems(List<YWorkItem> workItems) throws YPersistenceException {
        _log.debug("Restoring {} work items", workItems.size());
        List<YWorkItem> toBeRestored = new ArrayList<YWorkItem>();
        List<YWorkItem> toBeRemoved = new ArrayList<YWorkItem>();

        for (YWorkItem witem : workItems) {
            if (hasRestoredIdentifier(witem))
                toBeRestored.add(witem);
            else
                toBeRemoved.add(witem);
        }

        List<YWorkItem> orphans = checkWorkItemFamiliesIntact(toBeRestored);
        toBeRestored.removeAll(orphans);
        toBeRemoved.addAll(orphans);

        for (YWorkItem witem : toBeRestored) {

            // persisted data stored as string - restore to Element
            String data = witem.get_dataString();
            if (data != null) witem.setInitData(JDOMUtil.stringToElement(data));

            // reconstruct the caseID-YIdentifier for this item
            String id = witem.get_thisID();
            int delim1 = id.indexOf(':');
            int delim2 = id.indexOf('!');
            String caseID = id.substring(0, delim1);
            String taskID;
            String uniqueID = null;
            if (delim2 > -1) {
                taskID = id.substring(delim1 + 1, delim2);
                uniqueID = id.substring(delim2 + 1);
            }
            else {
                taskID = id.substring(delim1 + 1);
            }

            YIdentifier yCaseID = _idLookupTable.get(caseID);

            // MJF: use the unique id if we have one - stays in synch
            if (uniqueID != null) {
                witem.setWorkItemID(new YWorkItemID(yCaseID, taskID, uniqueID));
            } else {
                witem.setWorkItemID(new YWorkItemID(yCaseID, taskID));
            }

            witem.setTask(getTaskReference(witem.getSpecificationID(), taskID));
            
            witem.setEngine(_engine);
            witem.addToRepository();

            // MJF: for any work items with data, restore to netrunner instance
            witem.restoreDataToNet(_engine.getYAWLServices());
        }

        removeWorkItems(toBeRemoved);

        _log.debug("Restoring work items - Ends");
    }


    protected Set<YTimedObject> restoreTimedObjects() throws YPersistenceException {
        Set<YTimedObject> expiredObjects = restoreWorkItemTimers();
        expiredObjects.addAll(restoreDelayedLaunches());
        return expiredObjects;
    }


    protected Set<YTimedObject> restoreWorkItemTimers() throws YPersistenceException {
        _log.debug("Restoring work item timers - Starts");
        Set<YTimedObject> expiredTimers = new HashSet<YTimedObject>();
        Set<YWorkItemTimer> orphanedTimers = new HashSet<YWorkItemTimer>();

        for (YWorkItemTimer witemTimer : restoreObjects(YWorkItemTimer.class)) {
            witemTimer.setPersisting(true);

            // check to see if workitem still exists
            YWorkItem witem = _engine.getWorkItem(witemTimer.getOwnerID());
            if (witem == null)
                orphanedTimers.add(witemTimer);
            else {
                long endTime = witemTimer.getEndTime();

                // if the deadline has passed, time the workitem out
                if (endTime < System.currentTimeMillis())
                    expiredTimers.add(witemTimer);
                else {
                    // reschedule the workitem's timer
                    YTimer.getInstance().schedule(witemTimer, new Date(endTime));
                    witem.setTimerStarted(true);
                }
            }
        }

        unpersistObjects(orphanedTimers);

        _log.debug("Restoring work item timers - Ends");
        return expiredTimers;
    }


    protected Set<YTimedObject> restoreDelayedLaunches() throws YPersistenceException {
        _log.debug("Restoring delayed launch timers - Starts");
        Set<YTimedObject> expiredTimers = new HashSet<YTimedObject>();
        for (YLaunchDelayer delayer : restoreObjects(YLaunchDelayer.class)) {
            delayer.setPersisting(true);

            long endTime = delayer.getEndTime();

            // if the deadline has passed, launch the instance when engine is ready
            if (endTime < System.currentTimeMillis()) {
                expiredTimers.add(delayer);
            } else {
                // reschedule the launches timer
                YTimer.getInstance().schedule(delayer, new Date(endTime));
            }
        }

        _log.debug("Restoring delayed launch timers - Ends");
        return expiredTimers;
    }


    protected void restartRestoredProcessInstances() throws YPersistenceException {
        /*
          Start net runners. This is a restart of a NetRunner not a clean start,
          therefore the net runner should not create any new work items, if they
          have already been created.
         */
        _log.debug("Restarting restored process instances - Starts");

        for (YNetRunner runner : _runners) {
            _log.debug("Restarting {}", runner.get_caseID());
            try {
                if (! runner.isCompleted()) runner.start(_pmgr);
            } catch (Exception e) {
                throw new YPersistenceException(e.getMessage());
            }
        }
        _log.debug("Restarting restored process instances - Ends");
    }


    protected void persistDefaultClients() {
        if (_addedDefaultClients != null) {
            try {
                for (YClient client : _addedDefaultClients) {
                    _engine.storeObject(client);
                }
            } catch (YPersistenceException ype) {
                _log.warn("Unable to persist added default clients.", ype);
            }
        }
    }

    /**
     * *************************************************************************
     */

    private YSpecification getSpecification(YNetRunner runner) {
        return _engine.getSpecification(runner.getSpecificationID());
    }


    private void loadSpecification(YSpecification spec) throws YPersistenceException {
        try {
            long key = spec.getRowKey();

            // false == don't validate, since it has already been done when first loaded
            spec = YMarshal.unmarshalSpecifications(spec.getRestoredXML(), false).get(0);
            spec.setRowKey(key);
            _engine.loadSpecification(spec);
        } catch (Exception e) {
            throw new YPersistenceException("Failure whilst restoring specification", e);
        }
    }


    private List<YNetRunner> removeDeadRunners(List<YNetRunner> runners)
            throws YPersistenceException {
        List<YNetRunner> result = new ArrayList<YNetRunner>();

        for (YNetRunner runner : runners) {
            if (getSpecification(runner) != null) {
                result.add(runner);
            } else {
                /* This occurs when a specification has been unloaded, but the case is
                   still there. This case is removed, since we must have the
                   specification stored as well. */
                String msg = String.format("YEngineRestorer: The specification '%s' for" +
                        " active case '%s' is not loaded; the active case cannot" +
                        " continue and so has been removed.",
                        runner.getSpecificationID().getUri(),
                        runner.getCaseID().toString());
                _log.warn(msg);
                _pmgr.deleteObject(runner);
            }
        }

        return result;
    }


    private Map<String, YNetRunner> restoreNets(List<YNetRunner> runners)
            throws YPersistenceException {
        Map<String, YNetRunner> result = new HashMap<String, YNetRunner>();

        // restore all root nets first
        for (YNetRunner runner : runners) {
            runner.setEngine(_engine);       // Set engine for parent and composite nets
            if (runner.getContainingTaskID() == null) { // this is a root net runner
                YNet net = (YNet) getSpecification(runner).getRootNet().clone();
                runner.setNet(net);
                result.put(runner.getCaseID().toString(), runner);
            }
        }

        // now the sub nets
        for (YNetRunner runner : runners) {
            if (runner.getContainingTaskID() != null) {

                // Find the parent runner
                String runnerID = runner.getCaseID().toString();
                String parentID = runnerID.substring(0, runnerID.lastIndexOf("."));
                YNetRunner parentrunner = result.get(parentID);
                if (parentrunner != null) {
                    _log.debug("Restoring composite YNetRunner: {}", parentID);
                    YNet parentnet = parentrunner.getNet();
                    YCompositeTask task = (YCompositeTask) parentnet.getNetElement(
                            runner.getContainingTaskID());
                    runner.setContainingTask(task);
                    try {
                        YNet net = (YNet) task.getDecompositionPrototype().clone();
                        runner.setNet(net);
                    } catch (CloneNotSupportedException cnse) {
                        String msg = String.format("YEngineRestorer: The decomposition" +
                                "'%s' for  active case '%s' could not be set." +
                                task.getDecompositionPrototype().getID(),
                                runner.getCaseID().toString());
                        throw new YPersistenceException(msg);
                    }
                    result.put(runner.getCaseID().toString(), runner);
                }
            }
        }
        return result;
    }


    private void restoreRunners(List<YNetRunner> runners) throws YPersistenceException {
        Map<String, YNetRunner> runnerMap = restoreNets(runners);
        for (YNetRunner runner : runners) {
            YNet net = runner.getNet();
            if (runner.getContainingTaskID() == null) {

                // This is a root net runner
                restoreYIdentifiers(runnerMap, runner.getCaseID(), null, net);
                _engine.addRunner(runner);
            } else {
                _engine.getNetRunnerRepository().add(runner);         // a subnet
            }

            // restore enabled and busy tasks
            for (String busytask : runner.getBusyTaskNames()) {
                runner.addBusyTask((YTask) net.getNetElement(busytask));
            }
            for (String enabledtask : runner.getEnabledTaskNames()) {
                runner.addEnabledTask((YTask) net.getNetElement(enabledtask));
            }

            // restore any timer variables
            runner.restoreTimerStates();

            // restore case & exception observers (where they exist)
            runner.restoreObservers();

            // create a clean announcement transport
            runner.refreshAnnouncements();
        }
        if (! _importingCases) removeOrphanedIdentifiers();
    }


    protected YIdentifier restoreYIdentifiers(Map<String, YNetRunner> runnermap,
                                              YIdentifier id, YIdentifier parent, YNet net)
            throws YPersistenceException {

        id.set_parent(parent);

        for (YIdentifier child : id.getChildren()) {
            if (child != null) {
                YNetRunner netRunner = runnermap.get(child.toString());
                YNet runnerNet = netRunner != null ? netRunner.getNet() : net;
                YIdentifier caseid = restoreYIdentifiers(runnermap, child, id, runnerNet);

                if (netRunner != null) {
                    netRunner.set_caseIDForNet(caseid);
                }
            }
        }
        return restoreLocations(runnermap, id, parent, net);
    }


    protected YIdentifier restoreLocations(Map<String, YNetRunner> runnermap,
                                           YIdentifier id, YIdentifier parent, YNet net)
            throws YPersistenceException {

        YTask task;
        YNetRunner runner = null;

        // make external list of locations to avoid concurrency exceptions
        List<String> locationNames = new ArrayList<String>(id.getLocationNames());
        id.clearLocations(null);                         // locations are readded below

        for (String name : locationNames) {
            YExternalNetElement element = net.getNetElement(name);

            if (element == null) {
                name = name.substring(0, name.length() - 1);     // remove trailling ']'
                String[] splitname = name.split(":");

                if (parent != null) {
                    runner = runnermap.get(parent.toString());
                }

                // Get the task associated with this condition
                if (name.contains("CompositeTask")) {
                    task = (YTask) runner.getNet().getNetElement(splitname[1]);
                } else {
                    task = (YTask) net.getNetElement(splitname[1]);
                }

                postTaskCondition(task, net, splitname[0], id);
            } else {
                if (element instanceof YTask) {
                    task = (YTask) element;
                    task.setI(id);
                    task.prepareDataDocsForTaskOutput();
                    id.addLocation(null, task);
                } else if (element instanceof YCondition) {
                    ((YConditionInterface) element).add(_pmgr, id);
                }
            }
        }

        _idLookupTable.put(id.toString(), id);
        return id;
    }


    /**
     * ***************************************************************************
     */

    private void postTaskCondition(YTask task, YNet net, String condName, YIdentifier id)
            throws YPersistenceException {
        if (task != null) {
            _log.debug("Posting conditions on task {}", task);
            YInternalCondition condition = null;
            if (condName.startsWith(YInternalCondition._mi_active)) {
                condition = task.getMIActive();
            } else if (condName.startsWith(YInternalCondition._mi_complete)) {
                condition = task.getMIComplete();
            } else if (condName.startsWith(YInternalCondition._mi_entered)) {
                condition = task.getMIEntered();
            } else if (condName.startsWith(YInternalCondition._mi_executing)) {
                condition = task.getMIExecuting();
            } else {
                _log.error("Unknown YInternalCondition state");
            }
            if (condition != null) condition.add(null, id);
        } else {
            if (condName.startsWith("InputCondition")) {
                net.getInputCondition().add(null, id);
            } else if (condName.startsWith("OutputCondition")) {
                net.getOutputCondition().add(null, id);
            }
        }
    }


    /**
     * Checks if a workitem restored from persistence has had its YIdentifier
     * previously restored.
     *
     * @param item the workitem to check
     * @return true if there has been a YIdentifier restored for the workitem
     */
    private boolean hasRestoredIdentifier(YWorkItem item) {
        String[] caseTaskSplit = item.get_thisID().split(":");
        return _idLookupTable.get(caseTaskSplit[0]) != null;
    }


    /**
     * When restoring workitems, this method checks (1) if a parent workitem is in the
     * list of items to restore, all of its children are in the list also; and (2) each
     * child workitem in the list has a parent. If either is false, the workitem is
     * put in a list of items to not be restored and to be removed from persistence
     *
     * @param itemList the list of workitems to potentially restore
     * @return the sublist of items not to restore (if any)
     */
    private List<YWorkItem> checkWorkItemFamiliesIntact(List<YWorkItem> itemList) {
        List<YWorkItem> orphans = new ArrayList<YWorkItem>();
        for (YWorkItem witem : itemList) {
            if (witem.getStatus().equals(YWorkItemStatus.statusIsParent)) {
                Set<YWorkItem> children = witem.getChildren();
                if ((children != null) && (!itemList.containsAll(children))) {
                    orphans.add(witem);
                }
            } else {
                YWorkItem parent = witem.getParent();
                if ((parent != null) && (!itemList.contains(parent))) {
                    orphans.add(witem);
                }

                // if not a parent and not an orphan, must init (possibly future)
                // children collection to avoid potential future LazyInitializationException
                else Hibernate.initialize(witem.getChildren());
            }
        }
        return orphans;
    }


    /**
     * Removes the workitems in the list from persistence
     *
     * @param items the workitems to remove
     */
    private void removeWorkItems(List<YWorkItem> items) {
        try {

            // clear child items first (to avoid foreign key constraint exceptions)
            for (YWorkItem item : items) {
                if (!item.getStatus().equals(YWorkItemStatus.statusIsParent))
                    _pmgr.deleteObject(item);
            }

            // now clear any parents
            for (YWorkItem item : items) {
                if (item.getStatus().equals(YWorkItemStatus.statusIsParent))
                    _pmgr.deleteObject(item);
            }
        } catch (YPersistenceException ype) {
            _log.error("Exception removing orphaned workitems from persistence.", ype);
        }
    }


    /**
     * Finds and removes any persisted YIdentifiers that refer to
     * cases that are no longer executing
     */
    private void removeOrphanedIdentifiers() {
        Set<YIdentifier> orphaned = new HashSet<YIdentifier>();
        List<String> caseIDs = new ArrayList<String>();
        for (YIdentifier id : _engine.getRunningCaseIDs()) {
            caseIDs.add(id.toString());
        }

        try {
            for (YIdentifier id : restoreObjects(YIdentifier.class)) {
                String idString = id.toString();
                if (idString.contains(".")) {
                    idString = idString.substring(0, idString.indexOf('.'));
                }
                if (!caseIDs.contains(idString)) {
                    orphaned.add(id);
                }
            }
            unpersistObjects(orphaned);
        } catch (YPersistenceException ype) {
            _log.error("Exception removing orphaned identifiers from persistence.", ype);
        }
    }


    /**
     * Gets the YTask for the specification and task id passed. Any new requests are
     * stored in a lookup table to minimise engine calls for subsequent identical
     * requests (where many workitems are active for a spec/task combination)
     *
     * @param specID the spec ID
     * @param taskID the task ID
     * @return the task reference
     */
    private YTask getTaskReference(YSpecificationID specID, String taskID) {
        String key = specID.toString() + ":" + taskID;
        YTask task = _taskLookupTable.get(key);
        if (task == null) {
            task = _engine.getTaskDefinition(specID, taskID);
            _taskLookupTable.put(key, task);
        }
        return task;
    }


    protected void setImportingCases(boolean b) {_importingCases = b; }

    
    private <T> List<T> restoreObjects(Class<T> clazz) throws YPersistenceException {
        return restoreObjects(clazz, "from " + clazz.getSimpleName());
    }
    

    private <T> List<T> restoreObjects(Class<T> clazz, String queryString) throws YPersistenceException {
        List<T> list = new ArrayList<T>();
        Query query = _pmgr.createQuery(queryString);
        for (Iterator it = query.iterate(); it.hasNext(); ) {
            try {
                T item = clazz.cast(it.next());
                if (item != null) list.add(item);
            }
            catch (ClassCastException cce) {
                // ignore this object
                _log.warn("Ignored object while restoring: " + cce.getMessage());
            }
        }
        return list;
    }


    private <T> void unpersistObjects(Set<T> objects) throws YPersistenceException  {
        for (Object o : objects) {
            _pmgr.deleteObject(o);                   // remove from persistence
        }
    }

}
