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

package org.yawlfoundation.yawl.engine;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.YEngineEvent;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YTimerVariable;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.net.URL;
import java.util.*;

/**
 *
 *
 * @author Lachlan Aldred
 *         Date: 16/04/2003
 *         Time: 16:08:01
 *
 * @author Michael Adams (for v2)
 */
public class YNetRunner {

    public static enum ExecutionStatus { Normal, Suspending, Suspended, Resuming }

    private static final Logger _logger = Logger.getLogger(YNetRunner.class);

    protected YNet _net;
    private YWorkItemRepository _workItemRepository;
    private Set<YTask> _netTasks;
    private Set<YTask> _enabledTasks = new HashSet<YTask>();
    private Set<YTask> _busyTasks = new HashSet<YTask>();
    private final Set<YTask> _deadlockedTasks = new HashSet<YTask>();
    private YIdentifier _caseIDForNet;
    private YSpecificationID _specID;
    private YCompositeTask _containingCompositeTask;
    private YEngine _engine;
    private YAnnouncer _announcer;
    private boolean _cancelling;
    private final Set<String> _enabledTaskNames = new HashSet<String>();
    private final Set<String> _busyTaskNames = new HashSet<String>();
    private String _caseID = null;
    private String _containingTaskID = null;
    private YNetData _netdata = null;
    private YAWLServiceReference _caseObserver;
    private long _startTime;
    private Map<String, String> _timerStates;
    private ExecutionStatus _executionStatus;
    private Set<YAnnouncement> _announcements;

    // used to persist observers
    private String _caseObserverStr = null ;


    // Constructors //

    protected YNetRunner() {
        _logger.debug("YNetRunner: <init>");
        _engine = YEngine.getInstance();
        init();
    }


    public YNetRunner(YPersistenceManager pmgr, YNet netPrototype, Element paramsData,
                      String caseID) throws YDataStateException, YPersistenceException {
         this();

        // initialise and persist case identifier - if caseID is null, a new one is supplied
        _caseIDForNet = new YIdentifier(caseID);   
        if (pmgr != null) pmgr.storeObject(_caseIDForNet);

        // get case data from external data gateway, if set for this specification
        Element externalData = netPrototype.getCaseDataFromExternal(_caseIDForNet.toString());
        if (externalData != null) paramsData = externalData;

        initialise(pmgr, netPrototype, _caseIDForNet, paramsData) ;
    }

    /**
     * Constructor called by a composite task (creating a sub-net runner)
     * @param pmgr
     * @param netPrototype
     * @param container
     * @param caseIDForNet
     * @param incomingData
     * @throws YDataStateException
     * @throws YPersistenceException
     */
    public YNetRunner(YPersistenceManager pmgr, YNet netPrototype,
                      YCompositeTask container, YIdentifier caseIDForNet,
                      Element incomingData)
            throws YDataStateException, YPersistenceException {

        this();
        initialise(pmgr, netPrototype, caseIDForNet, incomingData) ;
        _containingCompositeTask = container;
        setContainingTaskID(container.getID());
        if (pmgr != null) pmgr.storeObject(this);
    }


    private void init() {
        _workItemRepository = _engine.getWorkItemRepository();
        _announcer = _engine.getAnnouncer();
    }

    private void initialise(YPersistenceManager pmgr, YNet netPrototype,
                      YIdentifier caseIDForNet, Element incomingData)
            throws YDataStateException, YPersistenceException {

        _caseIDForNet = caseIDForNet;
        _caseID = _caseIDForNet.toString();
        _netdata = new YNetData(_caseID);
        _net = (YNet) netPrototype.clone();
        _net.initializeDataStore(pmgr, _netdata);
        _netTasks = new HashSet<YTask>(_net.getNetTasks());
        _specID = _net.getSpecification().getSpecificationID();
        _startTime = System.currentTimeMillis();
        prepare(pmgr);
        if (incomingData != null) _net.setIncomingData(pmgr, incomingData);
        initTimerStates();
        refreshAnnouncements();
        _executionStatus = ExecutionStatus.Normal;
    }


    private void prepare(YPersistenceManager pmgr) throws YPersistenceException {
        YInputCondition inputCondition = _net.getInputCondition();
        inputCondition.add(pmgr, _caseIDForNet);
        _net.initialise(pmgr);
    }


    public Set<YAnnouncement> refreshAnnouncements() {
         Set<YAnnouncement> current = _announcements;
        _announcements = new HashSet<YAnnouncement>();
        return current;
    }


    public boolean equals(Object other) {
        return (other instanceof YNetRunner) &&   // instanceof = false if other is null
                ((getCaseID() != null) ? getCaseID().equals(((YNetRunner) other).getCaseID())
                : super.equals(other));
    }

    public int hashCode() {
        return (getCaseID() != null) ? getCaseID().hashCode() : super.hashCode();
    }


    /******************************************************************************/

    public void setContainingTask(YCompositeTask task) {
        _containingCompositeTask = task;
    }

    public String getContainingTaskID() {
        return _containingTaskID;
    }

    public void setContainingTaskID(String taskid) {
        _containingTaskID = taskid;
    }

    public void setNet(YNet net) {
        _net = net;
        _specID = net.getSpecification().getSpecificationID();
        _net.restoreData(_netdata);
        _netTasks = new HashSet<YTask>(_net.getNetTasks());
    }

    public YNet getNet() {
        return _net;
    }

    public void setEngine(YEngine engine) {
        _engine = engine;
        init();
    }

    public YSpecificationID getSpecificationID() {
        return _specID;
    }

    public void setSpecificationID(YSpecificationID id) {
        _specID = id;
    }

    public YNetData getNetData() {
        return _netdata;
    }

    public void setNetData(YNetData data) {
        _netdata = data;
    }

    public YIdentifier get_caseIDForNet() {
        return _caseIDForNet;
    }

    public void set_caseIDForNet(YIdentifier id) {
        this._caseIDForNet = id;
        _caseID = _caseIDForNet.toString();
    }


    public void addBusyTask(YTask ext) {
        _busyTasks.add(ext);
    }

    public void addEnabledTask(YTask ext) {
        _enabledTasks.add(ext);
    }

    public void removeActiveTask(YPersistenceManager pmgr, YTask task) throws YPersistenceException {
        _busyTasks.remove(task);
        _busyTaskNames.remove(task.getID());
        _enabledTasks.remove(task);
        _enabledTaskNames.remove(task.getID());
        if (pmgr != null) pmgr.updateObject(this);
    }


    public String get_caseID() {
        return this._caseID;
    }

    public void set_caseID(String ID) {
        this._caseID = ID;
    }

    public Set<String> getEnabledTaskNames() {
        return _enabledTaskNames;
    }

    public Set<String> getBusyTaskNames() {
        return _busyTaskNames;
    }


    public long getStartTime() { return _startTime; }

    public void setStartTime(long time) { _startTime = time ; }

    
    /************************************************/


    public void start(YPersistenceManager pmgr)
            throws YPersistenceException, YDataStateException,
                   YQueryException, YStateException {
        kick(pmgr);
    }


    public boolean isAlive() {
        return ! _cancelling;
    }

    /**
     * Assumption: this will only get called AFTER a workitem has been progressed?
     * Because if it is called any other time then it will cause the case to stop.
     * @param pmgr
     * @throws YPersistenceException
     */
    public synchronized void kick(YPersistenceManager pmgr)
            throws YPersistenceException, YDataStateException,
                   YQueryException, YStateException {
        _logger.debug("--> YNetRunner.kick");

        if (! continueIfPossible(pmgr)) {
            _logger.debug("YNetRunner not able to continue");

            // if root net can't continue it means a case completion
            if ((_engine != null) && isRootNet()) {
                announceCaseCompletion();
                if (endOfNetReached() && warnIfNetNotEmpty(pmgr)) {
                    _cancelling = true;                       // flag its not a deadlock                                   
                }

                // call the external data source, if its set for this specification
                _net.postCaseDataToExternal(getCaseID().toString());

                _logger.debug("Asking engine to finish case");
                _engine.removeCaseFromCaches(_caseIDForNet);

                // log it
                YLogPredicate logPredicate = getNet().getLogPredicate();
                YLogDataItemList logData = null;
                if (logPredicate != null) {
                    String predicate = logPredicate.getParsedCompletionPredicate(getNet());
                    if (predicate != null) {
                        logData = new YLogDataItemList(new YLogDataItem("Predicate",
                                     "OnCompletion", predicate, "string"));
                    }
                }                
                YEventLogger.getInstance().logNetCompleted(pmgr, _caseIDForNet, logData);
            }
            if (! _cancelling && deadLocked()) notifyDeadLock(pmgr);
            cancel(pmgr);
            if ((_engine != null) && isRootNet()) _engine.clearCaseFromPersistence(_caseIDForNet);
        }

        _logger.debug("<-- YNetRunner.kick");
    }


    private void announceCaseCompletion() {
        _announcer.announceCaseCompletion(_caseObserver, _caseIDForNet, _net.getOutputData());

        // notify exception checkpoint to listeners if any (post's for case end)
        if (_announcer.hasInterfaceXListeners()) {
            Document data = _net.getInternalDataDocument();
            _announcer.announceCheckCaseConstraints(_specID, _caseID,
                    JDOMUtil.documentToString(data), false);
        }
    }


    private boolean isRootNet() {
        return _caseIDForNet.getParent() == null;
    }


    private void notifyDeadLock(YPersistenceManager pmgr)
            throws YPersistenceException {
        for (Object o : _caseIDForNet.getLocations()) {
            if (o instanceof YExternalNetElement) {
                YExternalNetElement element = (YExternalNetElement) o;
                if (_net.getNetElements().values().contains(element)) {
                    if (element instanceof YTask) {
                        createDeadlockItem(pmgr, (YTask) element);
                    }
                    Set<YExternalNetElement> postset = element.getPostsetElements();
                    for (YExternalNetElement postsetElement : postset) {
                        createDeadlockItem(pmgr, (YTask) postsetElement);
                    }
                }
            }
        }
        _announcer.announceDeadlock(_caseIDForNet, _deadlockedTasks);
    }


    private void createDeadlockItem(YPersistenceManager pmgr, YTask task)
            throws YPersistenceException {
        if (! _deadlockedTasks.contains(task)) {
            _deadlockedTasks.add(task);

            // create a new deadlocked workitem so that the deadlock can be logged
            new YWorkItem(pmgr, _net.getSpecification().getSpecificationID(), task,
                    new YWorkItemID(_caseIDForNet, task.getID()), false, true);

            YProblemEvent event  = new YProblemEvent(_net, "Deadlocked",
                                                     YProblemEvent.RuntimeError);
            event.logProblem(pmgr);
        }
    }


    /**
     * Assumption: there are no enabled tasks
     * @return if deadlocked
     */
    private boolean deadLocked() {
        for (YNetElement location : _caseIDForNet.getLocations()) {
            if (location instanceof YExternalNetElement) {
                if (((YExternalNetElement) location).getPostsetElements().size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private synchronized void processCompletedSubnet(YPersistenceManager pmgr,
                                                     YIdentifier caseIDForSubnet,
                                                     YCompositeTask busyCompositeTask,
                                                     Document rawSubnetData)
            throws YDataStateException, YStateException, YQueryException,
            YPersistenceException {

        _logger.debug("--> processCompletedSubnet");

        if (caseIDForSubnet == null) throw new RuntimeException();

        if (busyCompositeTask.t_complete(pmgr, caseIDForSubnet, rawSubnetData)) {
            _busyTasks.remove(busyCompositeTask);
            if (pmgr != null) pmgr.updateObject(this);
            logCompletingTask(pmgr, caseIDForSubnet, busyCompositeTask);

            //check to see if completing this task resulted in completing the net.
            if (endOfNetReached()) {
                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _engine.getNetRunner(_caseIDForNet.getParent());
                    if ((parentRunner != null) && _containingCompositeTask.t_isBusy()) {
                        parentRunner.setEngine(_engine);           // added to avoid NPE
                        Document dataDoc = _net.usesSimpleRootData() ?
                                    _net.getInternalDataDocument() :
                                    _net.getOutputData() ;
                        parentRunner.processCompletedSubnet(pmgr, _caseIDForNet,
                                    _containingCompositeTask, dataDoc);
                    }
                }
            }
            kick(pmgr);
        }
        _logger.debug("<-- processCompletedSubnet");
    }


    public List<YIdentifier> attemptToFireAtomicTask(YPersistenceManager pmgr, String taskID)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_enabled(_caseIDForNet)) {
            List<YIdentifier> newChildIdentifiers = task.t_fire(pmgr);
            _enabledTasks.remove(task);
            _enabledTaskNames.remove(task.getID());
            _busyTasks.add(task);
            _busyTaskNames.add(task.getID());
            if (pmgr != null) pmgr.updateObject(this);
            _logger.debug("NOTIFYING RUNNER");
            kick(pmgr);
            return newChildIdentifiers;
        }
        return null;
    }


    public synchronized YIdentifier addNewInstance(YPersistenceManager pmgr,
                                                   String taskID,
                                                   YIdentifier aSiblingInstance,
                                                   Element newInstanceData)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_isBusy()) {
            return task.t_add(pmgr, aSiblingInstance, newInstanceData);
        }
        return null;
    }


    public void startWorkItemInTask(YPersistenceManager pmgr, YWorkItem workItem)
            throws YDataStateException, YPersistenceException,
                   YQueryException, YStateException {
        startWorkItemInTask (pmgr, workItem.getCaseID(), workItem.getTaskID());
    }


    public synchronized void startWorkItemInTask(YPersistenceManager pmgr,
                                                 YIdentifier caseID, String taskID)
            throws YDataStateException, YPersistenceException,
                   YQueryException, YStateException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        task.t_start(pmgr, caseID);
    }


    public boolean completeWorkItemInTask(YPersistenceManager pmgr, YWorkItem workItem,
                                          Document outputData)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {
        return completeWorkItemInTask(pmgr, workItem, workItem.getCaseID(),
                workItem.getTaskID(), outputData);
    }


    public synchronized boolean completeWorkItemInTask(YPersistenceManager pmgr,
                                                       YWorkItem workItem,
                                                       YIdentifier caseID,
                                                       String taskID,
                                                       Document outputData)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {
        _logger.debug("--> completeWorkItemInTask");
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        boolean success = completeTask(pmgr, workItem, task, caseID, outputData);

        // notify exception checkpoint to service if available
        if (_announcer.hasInterfaceXListeners()) {
            _announcer.announceCheckWorkItemConstraints(workItem, outputData, false);
        }
        _logger.debug("<-- completeWorkItemInTask");
        return success;
    }


    public synchronized boolean continueIfPossible(YPersistenceManager pmgr)
           throws YDataStateException, YStateException, YQueryException,
                  YPersistenceException {
        _logger.debug("--> continueIfPossible");

        // Check if we are suspending (or suspended?) and if so exit out as we
        // shouldn't post new workitems
        if (isInSuspense()) {
            _logger.debug("Aborting runner continuation as case is currently suspending/suspended");
            return true;
        }

        // don't continue if the net has already finished
        if (isCompleted()) return false;

        // storage for the running set of enabled tasks
        YEnabledTransitionSet enabledTransitions = new YEnabledTransitionSet();

        // iterate through the full set of tasks for the net
        for (YTask task : _netTasks) {

            // if this task is an enabled 'transition'
            if (task.t_enabled(_caseIDForNet)) {
                if (! (_enabledTasks.contains(task) || _busyTasks.contains(task)))
                    enabledTransitions.add(task) ;
            }
            else {

                // if the task is not an enabled transition, and it has been previously
                // enabled by the engine, then it must be cancelled
                if (_enabledTasks.contains(task)) {
                    cancelEnabledTask(task, pmgr);
                }
            }

            if (task.t_isBusy() && !_busyTasks.contains(task)) {
                _logger.error("Throwing RTE for lists out of sync");
                throw new RuntimeException("Busy task list out of synch with a busy task: "
                        + task.getID() + " busy tasks: " + _busyTasks);
            }
        }

        // fire the set of enabled 'transitions' (if any)
        if (! enabledTransitions.isEmpty()) fireTasks(enabledTransitions, pmgr);

        _busyTasks = _net.getBusyTasks();
        _logger.debug("<-- continueIfPossible");

        return hasActiveTasks();
    }


    private void fireTasks(YEnabledTransitionSet enabledSet, YPersistenceManager pmgr)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {

        Set<YTask> enabledTasks = new HashSet<YTask>() ;
        List<YEnabledTransitionSet.TaskGroup> taskGroups = enabledSet.getEnabledTaskGroups();
        for (YEnabledTransitionSet.TaskGroup group : taskGroups) {
            if (group.hasEnabledCompositeTasks()) {
                YCompositeTask composite = group.getRandomCompositeTaskFromTaskGroup();
                if (! (enabledTasks.contains(composite) || endOfNetReached())) {
                    fireCompositeTask(composite, pmgr) ;
                    enabledTasks.add(composite) ;
                }
            }
            else {
                List<YAtomicTask> taskList = group.getEnabledAtomicTasks();
                String groupID = taskList.size() > 1 ? group.getID() : null;
                boolean groupHasEmptyTask = group.hasEmptyAtomicTask(); 
                for (YAtomicTask atomic : taskList) {
                    if (! (enabledTasks.contains(atomic) || endOfNetReached())) {
                        YAnnouncement announcement = fireAtomicTask(atomic, groupID, pmgr) ;
                        if ((announcement != null) && (! groupHasEmptyTask)) {
                            _announcements.add(announcement);
                        }    
                        enabledTasks.add(atomic) ;
                    }
                }
            }
        }
    }


    private YAnnouncement fireAtomicTask(YAtomicTask task, String groupID,
                                                   YPersistenceManager pmgr)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {

        YAnnouncement announcement = null ;
        YAWLServiceGateway wsgw = (YAWLServiceGateway) task.getDecompositionPrototype();

        // if its not an empty task
        if (wsgw != null) {
            YWorkItem item = createEnabledWorkItem(pmgr, _caseIDForNet, task);
            if (groupID != null) item.setDeferredChoiceGroupID(groupID);

            announcement = _announcer.createAnnouncement(wsgw.getYawlService(), item,
                    YEngineEvent.ITEM_ADD);

            if (_announcer.hasInterfaceXListeners()) {
                _announcer.announceCheckWorkItemConstraints(item,
                                                 _net.getInternalDataDocument(), true);
            }
            _enabledTasks.add(task);
            _enabledTaskNames.add(task.getID());
            if (pmgr != null) pmgr.updateObject(this);
        }
        else processEmptyTask(task, pmgr);

        return announcement;
    }


    private void fireCompositeTask(YCompositeTask task, YPersistenceManager pmgr)
                      throws YDataStateException, YStateException, YQueryException,
                             YPersistenceException {
        
        if (! _busyTasks.contains(task)) {     // don't proceed if task already started
            _busyTasks.add(task);
            _busyTaskNames.add(task.getID());
            if (pmgr != null) pmgr.updateObject(this);

            List<YIdentifier> caseIDs = task.t_fire(pmgr);
            for (YIdentifier id : caseIDs) {
                task.t_start(pmgr, id);
            }
        }
    }


    protected void processEmptyTask(YAtomicTask task,YPersistenceManager pmgr)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {

        // fire, start and complete the decomposition-less atomic task in situ
        YIdentifier id = task.t_fire(pmgr).get(0);
        task.t_start(pmgr, id);
        completeTask(pmgr, null, task, id, null);
    }

    
    private void cancelEnabledTask(YTask task, YPersistenceManager pmgr)
                      throws YPersistenceException {

        _enabledTasks.remove(task);
        _enabledTaskNames.remove(task.getID());

        //  remove the cancelled task from persistence
        YWorkItem wItem = _workItemRepository.get(_caseID, task.getID());
        if (wItem != null) {               //may already have been removed by task.cancel

            //announce all cancelled work items
            YAnnouncement announcement = _announcer.createAnnouncement(wItem,
                    YEngineEvent.ITEM_CANCEL);
            if (announcement != null) _announcements.add(announcement);

            // log it
            YEventLogger.getInstance().logWorkItemEvent(pmgr, wItem,
                    YWorkItemStatus.statusDeleted, null);

            // cancel any live timer
            if (wItem.hasTimerStarted()) {
                YTimer.getInstance().cancelTimerTask(wItem.getIDString());
            }

            if (pmgr != null) {
                pmgr.deleteObject(wItem);
                pmgr.updateObject(this);
            }
        }
    }


    /**
     * Creates an enabled work item.
     *
     * @param caseIDForNet the caseid for the net
     * @param atomicTask   the atomic task that contains it.
     */
    private YWorkItem createEnabledWorkItem(YPersistenceManager pmgr,
                                            YIdentifier caseIDForNet,
                                            YAtomicTask atomicTask)
            throws YPersistenceException, YDataStateException, YQueryException {
        _logger.debug("--> createEnabledWorkItem: Case=" + caseIDForNet.get_idString() +
                      " Task=" + atomicTask.getID());

        boolean allowDynamicCreation = atomicTask.getMultiInstanceAttributes() != null &&
                    YMultiInstanceAttributes.CREATION_MODE_DYNAMIC.equals(
                            atomicTask.getMultiInstanceAttributes().getCreationMode());

        //creating a new work item puts it into the work item repository automatically.
        YWorkItem workItem = new YWorkItem(pmgr,
                atomicTask.getNet().getSpecification().getSpecificationID(), atomicTask,
                new YWorkItemID(caseIDForNet, atomicTask.getID()),
                allowDynamicCreation, false);

        if (atomicTask.getDataMappingsForEnablement().size() > 0) {
            Element data = atomicTask.prepareEnablementData();            
			      workItem.setData(pmgr, data);
        }

        // copy in relevant data from the task's decomposition
        YDecomposition decomp = atomicTask.getDecompositionPrototype();
        if (decomp != null) {
            workItem.setRequiresManualResourcing(decomp.requiresResourcingDecisions());
            workItem.setCodelet(decomp.getCodelet());
            workItem.setAttributes(decomp.getAttributes());
        }

        // set timer params and start timer if required
        YTimerParameters timerParams = atomicTask.getTimerParameters();
        if (timerParams != null) {
            workItem.setTimerParameters(timerParams);
            workItem.checkStartTimer(pmgr, _netdata);
        }

        // set custom form for workitem if specified
        URL customFormURL = atomicTask.getCustomFormURL();
        if (customFormURL != null)
            workItem.setCustomFormURL(customFormURL) ;

        // persist the changes
        if (pmgr != null) pmgr.updateObject(workItem);
        
        return workItem;
    }


    /**
     * Completes a work item inside an atomic task.
     *
     * @param workItem The work item. If null is supplied, this work item cannot be
     * removed from the work items repository (hack)
     * @param atomicTask the atomic task
     * @param identifier the identifier of the work item
     * @param outputData the document containing output data
     * @return whether or not the task exited
     * @throws YDataStateException
     */
    private synchronized boolean completeTask(YPersistenceManager pmgr, YWorkItem workItem,
                                 YAtomicTask atomicTask, YIdentifier identifier,
                                 Document outputData)
            throws YDataStateException, YStateException, YQueryException,
                   YPersistenceException {

        _logger.debug("--> completeTask: " + atomicTask.getID());

        boolean taskExited = atomicTask.t_complete(pmgr, identifier, outputData);

        if (taskExited) {
            if (workItem != null) {
                for (YWorkItem removed : _workItemRepository.removeWorkItemFamily(workItem)) {
                    if (! removed.hasCompletedStatus()) {      // MI fired or incomplete
                        _announcer.announceCancelledWorkItem(removed);
                    }
                }

                updateTimerState(workItem.getTask(), YWorkItemTimer.State.closed);
            }

            // check if completing this task resulted in completing the net.
            if (endOfNetReached()) {

                // check if the completed net is a subnet.
                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _engine.getNetRunner(_caseIDForNet.getParent());
                    if (parentRunner != null) {
                        synchronized (parentRunner) {
                            if (_containingCompositeTask.t_isBusy()) {

                                warnIfNetNotEmpty(pmgr);

                                Document dataDoc = _net.usesSimpleRootData() ?
                                                   _net.getInternalDataDocument() :
                                                   _net.getOutputData() ;

                                parentRunner.processCompletedSubnet(pmgr,
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            dataDoc);

                                if (_caseIDForNet == null) {
                                    _logger.debug("YNetRunner::completeTask() finished local task: " +
                                            atomicTask + " composite task: " +
                                            _containingCompositeTask +
                                            " caseid for decomposed net: " +
                                            _caseIDForNet);
                                }
                            }
                        }
                    }
                }
            }

            continueIfPossible(pmgr);
            _busyTasks.remove(atomicTask);
            _busyTaskNames.remove(atomicTask.getID());

            if ((pmgr != null) && _engine.getRunningCaseIDs().contains(_caseIDForNet)) {
                    pmgr.updateObject(this);
                }
            _logger.debug("NOTIFYING RUNNER");
            kick(pmgr);
        }
        _logger.debug("<-- completeTask: " + atomicTask.getID()
                + ", Exited=" + taskExited);

        return taskExited;
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        _logger.debug("--> NetRunner cancel " + getCaseID().get_idString());

        _cancelling = true;
        for (YExternalNetElement netElement : _net.getNetElements().values()) {
            if (netElement instanceof YTask) {
                YTask task = ((YTask) netElement);
                if (task.t_isBusy()) {
                    task.cancel(pmgr);
                }
            }
            else if (((YCondition) netElement).containsIdentifier()) {
                ((YCondition) netElement).removeAll(pmgr);
            }
        }
        _enabledTasks = new HashSet<YTask>();
        _busyTasks = new HashSet<YTask>();

        if (_containingCompositeTask != null) {
            YEventLogger.getInstance().logNetCancelled(pmgr,
                    getSpecificationID(), this, _containingCompositeTask.getID(), null);
        }
        if (isRootNet()) _workItemRepository.removeWorkItemsForCase(_caseIDForNet);
        _engine.getNetRunnerRepository().remove(_caseIDForNet);
    }


    public void removeFromPersistence(YPersistenceManager pmgr) throws YPersistenceException {
        if (pmgr != null) {
            pmgr.deleteObject(this);
        }
    }


    public synchronized boolean rollbackWorkItem(YPersistenceManager pmgr,
                                                 YIdentifier caseID, String taskID)
            throws YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_rollBackToFired(pmgr, caseID);
    }


    private void logCompletingTask(YPersistenceManager pmgr, YIdentifier caseIDForSubnet,
                                  YCompositeTask busyCompositeTask) {
        YLogPredicate logPredicate = busyCompositeTask.getDecompositionPrototype().getLogPredicate();
        YLogDataItemList logData = null;
        if (logPredicate != null) {
            String predicate = logPredicate.getParsedCompletionPredicate(
                    busyCompositeTask.getDecompositionPrototype());
            if (predicate != null) {
                logData = new YLogDataItemList(new YLogDataItem("Predicate",
                        "OnCompletion", predicate, "string"));
            }
        }
        YEventLogger.getInstance().logNetCompleted(pmgr, caseIDForSubnet, logData);
    }


    //###############################################################################
    //                              accessors
    //###############################################################################
    public YExternalNetElement getNetElement(String id) {
        return _net.getNetElement(id);
    }


    public YIdentifier getCaseID() {
        return _caseIDForNet;
    }


    public boolean isCompleted() {
        return endOfNetReached() || isEmpty();
    }

    public boolean endOfNetReached() {
        return _net.getOutputCondition().containsIdentifier();
    }


    public boolean isEmpty() {
        for (YExternalNetElement element : _net.getNetElements().values()) {
            if (element instanceof YCondition) {
                if (((YCondition) element).containsIdentifier()) return false;
            }
            else {
                if (((YTask) element).t_isBusy()) return false;                
            }
        }
        return true;
    }


    protected Set<YTask> getBusyTasks() {
        return _busyTasks;
    }


    protected Set<YTask> getEnabledTasks() {
        return _enabledTasks;
    }

    protected Set<YTask> getActiveTasks() {
        Set<YTask> activeTasks = new HashSet<YTask>();
        activeTasks.addAll(_busyTasks);
        activeTasks.addAll(_enabledTasks);
        return activeTasks;
    }

    protected boolean hasActiveTasks() {
        return _enabledTasks.size() > 0 || _busyTasks.size() > 0;
    }


    public boolean isAddEnabled(String taskID, YIdentifier childID) {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_addEnabled(childID);
    }

    public void setObserver(YAWLServiceReference observer) {
        _caseObserver = observer;
        _caseObserverStr = observer.getURI();                       // for persistence
    }


    private boolean warnIfNetNotEmpty(YPersistenceManager pmgr) {
        List<YExternalNetElement> haveTokens = new ArrayList<YExternalNetElement>();
        for (YExternalNetElement element : _net.getNetElements().values()) {
            if (! (element instanceof YOutputCondition)) {  // ignore end condition tokens
                if ((element instanceof YCondition) && ((YCondition) element).containsIdentifier()) {
                    haveTokens.add(element);
                }
                else if ((element instanceof YTask) && ((YTask) element).t_isBusy()) {
                    haveTokens.add(element);

                    // flag and announce any executing workitems
                    YInternalCondition exeCondition = ((YTask) element).getMIExecuting();
                    for (YIdentifier id : exeCondition.getIdentifiers()) {
                        YWorkItem executingItem = _workItemRepository.get(
                                id.toString(), element.getID());
                        if (executingItem != null) executingItem.setStatusToDiscarded(pmgr);
                    }
                }
            }
        }
        if (! haveTokens.isEmpty()) {
            StringBuilder msg = new StringBuilder(100);
            msg.append("Although Net [")
               .append(_net.getID())
               .append("] of case [")
               .append(_caseIDForNet.toString())
               .append("] has successfully completed, there are one or more ")
               .append("tokens remaining in the net, within these elements: [");

            msg.append(StringUtils.join(haveTokens, ", "));
            msg.append("], which usually indicates that the net is unsound. Those ")
               .append("tokens were removed when the net completed.");
            _logger.warn(msg.toString());
        }
        return (! haveTokens.isEmpty());
    }


    public String toString() {
        return String.format("CaseID: %s; Enabled: %s; Busy: %s", _caseIDForNet.toString(),
                _enabledTaskNames.toString(), _busyTaskNames.toString());
    }


    public void dump() {
        _logger.debug("*** DUMP OF NETRUNNER ENABLED TASKS ***");

        Iterator iter = _enabledTasks.iterator();
        while(iter.hasNext())
        {
            Object obj = iter.next();
            _logger.debug("Type = " + obj.getClass().getName());
        }

       _logger.debug("*** END OF DUMP OF NETRUNNER ENABLED TASKS ***");

        _logger.debug("*** DUMP OF NETRUNNER BUSY TASKS ***");

        iter = _busyTasks.iterator();
        while(iter.hasNext())
        {
            Object obj = iter.next();
            _logger.debug("Type = " + obj.getClass().getName());
        }

        _logger.debug("*** END OF DUMP OF NETRUNNER BUSY TASKS ***");
    }

    /***************************************************************************/
    /** The following methods have been added to support the exception service */


    /** restores the IB and IX observers on session startup (via persistence) */
    public void restoreObservers() {
        if(_caseObserverStr != null) {
            YAWLServiceReference caseObserver =
                                    _engine.getRegisteredYawlService(_caseObserverStr);
            if (caseObserver != null) setObserver(caseObserver);
        }
    }


   /** these two methods are here to support persistence of the IB Observer */
    private String get_caseObserverStr() { return _caseObserverStr ; }

    private void set_caseObserverStr(String obStr) { _caseObserverStr = obStr ; }


    /** cancels the specified task */
    public synchronized void cancelTask(YPersistenceManager pmgr, String taskID) {
        YAtomicTask task = (YAtomicTask) getNetElement(taskID);

        try {
            task.cancel(pmgr, this.getCaseID());
            _busyTasks.remove(task);
            _busyTaskNames.remove(task.getID());
        }
        catch (YPersistenceException ype) {
            _logger.fatal("Failure whilst cancelling task: " + taskID, ype);

        }
    }


    /** returns true if the specified workitem is registered with the Time Service */
    public boolean isTimeServiceTask(YWorkItem item) {
        YTask task = (YTask) getNetElement(item.getTaskID());
        if ((task != null) && (task instanceof YAtomicTask)) {
            YAWLServiceGateway wsgw = (YAWLServiceGateway) task.getDecompositionPrototype();
            if (wsgw != null) {
                YAWLServiceReference ys = wsgw.getYawlService();
                if (ys != null) {
                    return ys.getServiceID().indexOf("timeService") > -1 ;
                }
            }
        }
        return false ;
    }


    /** returns a list of all workitems executing in parallel to the time-out
        workitem passed (the list includes the time-out task) */
    public List<String> getTimeOutTaskSet(YWorkItem item) {
        YTask timeOutTask = (YTask) getNetElement(item.getTaskID());
        String nextTaskID = getFlowsIntoTaskID(timeOutTask);
        ArrayList<String> result = new ArrayList<String>() ;

        if (nextTaskID != null) {
            for (YTask task : _netTasks) {
               String nextTask = getFlowsIntoTaskID(task);
               if (nextTask != null) {
                   if (nextTask.equals(nextTaskID))
                      result.add(task.getID());
               }
            }
        }
        if (result.isEmpty()) result = null ;
        return result;
    }


    /** returns the task id of the task that the specified task flows into
        In other words, gets the id of the next task in the process flow */
    private String getFlowsIntoTaskID(YTask task) {
        if ((task != null) && (task instanceof YAtomicTask)) {
            Element eTask = JDOMUtil.stringToElement(task.toXML());
            return eTask.getChild("flowsInto").getChild("nextElementRef").getAttributeValue("id");
        }
        return null ;
    }


    // **** TIMER STATE VARIABLES **********//
    
    // returns all the tasks in this runner's net that have timers
    public void initTimerStates() {
        _timerStates = new Hashtable<String, String>();
        for (YTask task : _netTasks) {
            if (task.getTimerVariable() != null) {
                updateTimerState(task, YWorkItemTimer.State.dormant);
            }
        }
    }


    public void restoreTimerStates() {
        if (! _timerStates.isEmpty()) {
            for (String taskName : _timerStates.keySet()) {
                for (YTask task : _netTasks) {
                    if (task.getName().equals(taskName)) {
                        String stateStr = _timerStates.get(taskName);
                        YTimerVariable timerVar = task.getTimerVariable();
                        timerVar.setState(YWorkItemTimer.State.valueOf(stateStr), true);
                        break;
                    }
                }
            }
        }
    }


    public void updateTimerState(YTask task, YWorkItemTimer.State state) {
        YTimerVariable timerVar = task.getTimerVariable();
        if (timerVar != null) {
            timerVar.setState(state);
            _timerStates.put(task.getName(), timerVar.getStateString());
        }
    }

    
    public Map<String, String> get_timerStates() {
        return _timerStates;
    }

    public void set_timerStates(Map<String, String> states) {
        _timerStates = states;
    }

    public boolean evaluateTimerPredicate(String predicate) throws YQueryException {
        predicate = predicate.trim();
        int pos = predicate.indexOf(')');
        if (pos > -1) {
            String taskName = predicate.substring(6, pos);     // 6 = 'timer('
            YTimerVariable timerVar = getTimerVariable(taskName);
            if (timerVar != null) {
                return timerVar.evaluatePredicate(predicate);
            }
            else throw new YQueryException("Unable to find timer state for task named " +
                        "in predicate: " + predicate);
        }
        else throw new YQueryException("Malformed timer predicate: " + predicate);
    }


    private YTimerVariable getTimerVariable(String taskName) {
        for (YTask task : _netTasks) {
            if (task.getName().equals(taskName)) {
                return task.getTimerVariable();
            }
        }
        return null;
    }

    public boolean isSuspending() { return _executionStatus == ExecutionStatus.Suspending; }

    public boolean isSuspended() { return _executionStatus == ExecutionStatus.Suspended; }

    public boolean isResuming() { return _executionStatus == ExecutionStatus.Resuming; }

    public boolean isInSuspense() { return isSuspending() || isSuspended(); }

    public boolean hasNormalState() { return _executionStatus == ExecutionStatus.Normal; }

    public void setStateSuspending() { _executionStatus = ExecutionStatus.Suspending; }

    public void setStateSuspended() { _executionStatus = ExecutionStatus.Suspended; }

    public void setStateResuming() { _executionStatus = ExecutionStatus.Resuming; }

    public void setStateNormal() { _executionStatus = ExecutionStatus.Normal; }

    public void setExecutionStatus(String status) {
        _executionStatus = (status != null) ? ExecutionStatus.valueOf(status) :
                ExecutionStatus.Normal;
    }

    public String getExecutionStatus() {
        return _executionStatus.name();
    }

}


