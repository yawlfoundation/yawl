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

package org.yawlfoundation.yawl.stateless.engine;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.engine.YNetData;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.exceptions.YDataStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YQueryException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.*;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.elements.marking.YInternalCondition;
import org.yawlfoundation.yawl.stateless.engine.time.YTimerVariable;
import org.yawlfoundation.yawl.stateless.engine.time.YWorkItemTimer.State;
import org.yawlfoundation.yawl.stateless.listener.event.YEventType;
import org.yawlfoundation.yawl.stateless.listener.event.YLogEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YWorkItemEvent;
import org.yawlfoundation.yawl.stateless.listener.predicate.YLogPredicate;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

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

    public enum ExecutionStatus { Normal, Suspending, Suspended, Resuming }

    private static final Logger _logger = LogManager.getLogger(YNetRunner.class);

    protected YNet _net;
    private Set<YTask> _netTasks;
    private Set<YTask> _enabledTasks = new HashSet<>();
    private Set<YTask> _busyTasks = new HashSet<>();
    private YIdentifier _caseIDForNet;
    private YSpecificationID _specID;
    private YCompositeTask _containingCompositeTask;
    private YAnnouncer _announcer;
    private boolean _cancelling;
    private String _caseID = null;
    private String _containingTaskID = null;
    private YNetData _netdata = null;
    private long _startTime;
    private Map<String, String> _timerStates;
    private ExecutionStatus _executionStatus;
    private Set<YWorkItemEvent> _announcements;
    private Set<YNetRunner> _children;
    private final YWorkItemRepository _workItemRepository;


    protected YNetRunner() {
        _logger.debug("YNetRunner: <init>");
        _workItemRepository = new YWorkItemRepository();
    }


    public YNetRunner(YNet netPrototype, Element paramsData, String caseID)
            throws YStateException, YDataStateException {
         this();

        // initialise case identifier - if caseID is null, a new one is supplied
        _caseIDForNet = new YIdentifier(caseID);   

        // get case data from external data gateway, if set for this specification
        Element externalData = netPrototype.getCaseDataFromExternal(_caseIDForNet.toString());
        if (externalData != null) paramsData = externalData;

        initialise(netPrototype, _caseIDForNet, paramsData) ;
    }

    /**
     * Constructor called by a composite task (creating a sub-net runner)
     * @param netPrototype
     * @param container
     * @param caseIDForNet
     * @param incomingData
     * @throws YDataStateException
     * @throws YPersistenceException
     */
    public YNetRunner(YNet netPrototype, YCompositeTask container, YIdentifier caseIDForNet,
                      Element incomingData)
            throws YDataStateException {

        this();
        initialise(netPrototype, caseIDForNet, incomingData) ;
        _containingCompositeTask = container;
        setContainingTaskID(container.getID());
    }


    public void setAnnouncer(YAnnouncer announcer) { _announcer = announcer; }

    public YAnnouncer getAnnouncer() { return _announcer; }

    public YWorkItemRepository getWorkItemRepository() { return _workItemRepository; }


    private void initialise(YNet netPrototype,
                      YIdentifier caseIDForNet, Element incomingData)
            throws YDataStateException {

        _caseIDForNet = caseIDForNet;
        _caseID = _caseIDForNet.toString();
        _netdata = new YNetData(_caseID);
        _net = (YNet) netPrototype.clone();
        _net.initializeDataStore(_netdata);
        _netTasks = new HashSet<>(_net.getNetTasks());
        _specID = _net.getSpecification().getSpecificationID();
        _startTime = System.currentTimeMillis();
        prepare();
        if (incomingData != null) _net.setIncomingData(incomingData);
        initTimerStates();
        refreshAnnouncements();
        _executionStatus = ExecutionStatus.Normal;
    }


    private void prepare() {
        YInputCondition inputCondition = _net.getInputCondition();
        inputCondition.add(_caseIDForNet);
        _net.initialise();
    }


    public Set<YWorkItemEvent> refreshAnnouncements() {
        Set<YWorkItemEvent> current = new HashSet<>();
        if (_announcements != null) {
            current.addAll(_announcements);
        }
        _announcements = new HashSet<>();
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


    public boolean addChildRunner(YNetRunner child) {
        if (_children == null) _children = new HashSet<>();
        return _children.add(child);
    }

    public boolean removeChildRunner(YNetRunner child) {
        return _children != null && _children.remove(child);
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
        _netTasks = new HashSet<>(_net.getNetTasks());
    }

    public YNet getNet() {
        return _net;
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

    public void removeActiveTask(YTask task) {
        _busyTasks.remove(task);
        _enabledTasks.remove(task);
    }


    public String get_caseID() {
        return this._caseID;
    }

    public void set_caseID(String ID) {
        this._caseID = ID;
    }


    public long getStartTime() { return _startTime; }

    public void setStartTime(long time) { _startTime = time ; }

    
    /************************************************/


    public void start() throws YDataStateException, YQueryException, YStateException {
        kick();
    }


    public boolean isAlive() {
        return ! _cancelling;
    }

    /**
     * Assumption: this will only get called AFTER a workitem has been progressed?
     * Because if it is called any other time then it will cause the case to stop.
     */
    public synchronized void kick() throws YDataStateException, YQueryException, YStateException {
        _logger.debug("--> YNetRunner.kick");

        if (! continueIfPossible()) {
            _logger.debug("YNetRunner not able to continue");

            // if root net can't continue it means a case completion
            if (isRootNet()) {
                announceCaseCompletion();
                if (endOfNetReached() && warnIfNetNotEmpty()) {
                    _cancelling = true;                       // flag its not a deadlock                                   
                }

                // call the external data source, if its set for this specification
                _net.postCaseDataToExternal(getCaseID().toString());

                _logger.debug("Asking engine to finish case");

            }
            if (! _cancelling && deadLocked()) notifyDeadLock();
            cancel();
        }

        _logger.debug("<-- YNetRunner.kick");
    }


    private YLogDataItemList getLogPredicate(YLogPredicate logPredicate, String trigger) {
        if (logPredicate != null) {
            String predicate = trigger.equals("OnStart") ?
                    logPredicate.getParsedStartPredicate(getNet()) :
                    logPredicate.getParsedCompletionPredicate(getNet());
            if (predicate != null) return new YLogDataItemList(
                    new YLogDataItem("Predicate", trigger, predicate, "string"));
        }
        return null;
    }

    private void announceCaseCompletion() {
        // announce case event
        _announcer.announceCaseCompletion(_specID, _caseIDForNet, _net.getOutputData());

        // announce exception checkpoint event (post's for case end)
        _announcer.announceCheckCaseConstraints(_specID, _caseIDForNet,
                _net.getInternalDataDocument(), false);

        // announce log event
        _announcer.logNetCompleted(_caseIDForNet, _specID,
                getLogPredicate(getNet().getLogPredicate(), "OnCompletion"));
    }


    private boolean isRootNet() {
        return _caseIDForNet.getParent() == null;
    }


    private void notifyDeadLock() {
        Set<YTask> deadlockedTasks = new HashSet<>();
        for (Object o : _caseIDForNet.getLocations()) {
            if (o instanceof YExternalNetElement) {
                YExternalNetElement element = (YExternalNetElement) o;
                if (_net.getNetElements().containsValue(element)) {
                    if (element instanceof YTask) {
                        deadlockedTasks.add((YTask) element);
                    }
                    Set<YExternalNetElement> postset = element.getPostsetElements();
                    for (YExternalNetElement postsetElement : postset) {
                        deadlockedTasks.add((YTask) postsetElement);
                    }
                }
            }
        }
        _announcer.announceDeadlock(_caseIDForNet, deadlockedTasks);
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


    private synchronized void processCompletedSubnet(YIdentifier caseIDForSubnet,
                                                     YCompositeTask busyCompositeTask,
                                                     Document rawSubnetData)
            throws YDataStateException, YStateException, YQueryException {

        _logger.debug("--> processCompletedSubnet");

        if (caseIDForSubnet == null) throw new RuntimeException();

        if (busyCompositeTask.t_complete(caseIDForSubnet, rawSubnetData)) {
            _busyTasks.remove(busyCompositeTask);
            logCompletingTask(caseIDForSubnet, busyCompositeTask);

            //check to see if completing this task resulted in completing the net.
            if (endOfNetReached()) {
                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _containingCompositeTask.getNetRunner();
                    if ((parentRunner != null) && _containingCompositeTask.t_isBusy()) {
                        Document dataDoc = _net.usesSimpleRootData() ?
                                    _net.getInternalDataDocument() :
                                    _net.getOutputData() ;
                        parentRunner.processCompletedSubnet(_caseIDForNet,
                                    _containingCompositeTask, dataDoc);
                    }
                }
            }
            kick();
        }
        _logger.debug("<-- processCompletedSubnet");
    }


    public List<YIdentifier> attemptToFireAtomicTask(String taskID)
            throws YDataStateException, YStateException, YQueryException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_enabled(_caseIDForNet)) {
            List<YIdentifier> newChildIdentifiers = task.t_fire();
            _enabledTasks.remove(task);
            _busyTasks.add(task);
            _logger.debug("NOTIFYING RUNNER");
            kick();
            return newChildIdentifiers;
        }
        throw new YStateException("Task is not (or no longer) enabled: " + taskID);
    }


    public synchronized YIdentifier addNewInstance(String taskID,
                                                   YIdentifier aSiblingInstance,
                                                   Element newInstanceData)
            throws YDataStateException, YStateException, YQueryException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_isBusy()) {
            return task.t_add(aSiblingInstance, newInstanceData);
        }
        return null;
    }


    public void startWorkItemInTask(YWorkItem workItem)
            throws YDataStateException, YQueryException, YStateException {
        startWorkItemInTask(workItem.getCaseID(), workItem.getTaskID());
    }


    public synchronized void startWorkItemInTask(YIdentifier caseID, String taskID)
            throws YDataStateException, YQueryException, YStateException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        task.t_start(this, caseID);
    }


    public boolean completeWorkItemInTask(YWorkItem workItem,
                                          Document outputData)
            throws YDataStateException, YStateException, YQueryException {
        return completeWorkItemInTask(workItem, workItem.getCaseID(),
                workItem.getTaskID(), outputData);
    }


    public synchronized boolean completeWorkItemInTask(YWorkItem workItem,
                                                       YIdentifier caseID,
                                                       String taskID,
                                                       Document outputData)
            throws YDataStateException, YStateException, YQueryException {
        _logger.debug("--> completeWorkItemInTask");
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        boolean success = completeTask(workItem, task, caseID, outputData);

        // notify exception checkpoint to service if available
        _announcer.announceCheckWorkItemConstraints(
                    workItem, _net.getInternalDataDocument(), false);
        _logger.debug("<-- completeWorkItemInTask");
        return success;
    }


    public synchronized boolean continueIfPossible()
           throws YDataStateException, YStateException, YQueryException {
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

                // if the task is not (or no longer) an enabled transition, and it
                // has been previously enabled by the engine, then it must be withdrawn
                if (_enabledTasks.contains(task)) {
                    withdrawEnabledTask(task);
                }
            }

            // wait if necessary for the runner busy tasks to synch (rarely required)
            int retries = 5;
            while (task.t_isBusy() && !_busyTasks.contains(task)) {
                System.out.println("****RETRYING: " + task.getID() + " " + retries);
                try {
                    Thread.sleep(5);
                }
                catch (InterruptedException e) {
                    //fall through;
                }
                if (--retries == 0) {
                    _logger.error("Throwing RTE for lists out of sync");
                    throw new RuntimeException("Busy task list out of synch with a busy task: "
                            + task.getID() + " busy tasks: " + _busyTasks);
                }
            }
        }

        // fire the set of enabled 'transitions' (if any)
        if (! enabledTransitions.isEmpty()) fireTasks(enabledTransitions);

  //      _busyTasks = _net.getBusyTasks();
        _logger.debug("<-- continueIfPossible");

        return hasActiveTasks();
    }


    private void fireTasks(YEnabledTransitionSet enabledSet)
            throws YDataStateException, YStateException, YQueryException {
        Set<YTask> enabledTasks = new HashSet<YTask>();

        // A TaskGroup is a group of tasks that are all enabled by a single condition.
        // If the group has more than one task, it's a deferred choice, in which case:
        // 1. If any are composite, fire one (chosen randomly) - rest are withdrawn
        // 2. Else, if any are empty, fire one (chosen randomly) - rest are withdrawn
        // 3. Else, fire and announce all enabled atomic tasks to the environment
        for (YEnabledTransitionSet.TaskGroup group : enabledSet.getAllTaskGroups()) {
            if (group.hasCompositeTasks()) {
                YCompositeTask composite = group.getRandomCompositeTaskFromGroup();
                if (! (enabledTasks.contains(composite) || endOfNetReached())) {
                    fireCompositeTask(composite);
                    enabledTasks.add(composite);
                }
            }
            else if (group.hasEmptyTasks()) {
                YAtomicTask atomic = group.getRandomEmptyTaskFromGroup();
                if (! (enabledTasks.contains(atomic) || endOfNetReached())) {
                    processEmptyTask(atomic);
                }
            }
            else {
                String groupID = group.getDeferredChoiceID();       // null if <2 tasks
                for (YAtomicTask atomic : group.getAtomicTasks()) {
                    if (! (enabledTasks.contains(atomic) || endOfNetReached())) {
                        atomic.setNetRunner(this);
                        YWorkItemEvent event = fireAtomicTask(atomic, groupID);
                        _announcements.add(event);
                        enabledTasks.add(atomic);
                    }
                }
            }
        }
    }


    private YWorkItemEvent fireAtomicTask(YAtomicTask task, String groupID)
            throws YDataStateException, YStateException, YQueryException {

        _enabledTasks.add(task);
        YWorkItem item = createEnabledWorkItem(_caseIDForNet, task);
        if (groupID != null) item.setDeferredChoiceGroupID(groupID);

        _announcer.announceCheckWorkItemConstraints(item, _net.getInternalDataDocument(), true);
        return new YWorkItemEvent(YEventType.ITEM_ENABLED, item);
    }


    private void fireCompositeTask(YCompositeTask task)
                      throws YDataStateException, YStateException, YQueryException {
        
        if (! _busyTasks.contains(task)) {     // don't proceed if task already started
            _busyTasks.add(task);

            List<YIdentifier> caseIDs = task.t_fire();
            for (YIdentifier id : caseIDs) {
                try {
                    task.t_start(this, id);
                }
                catch (YDataStateException ydse) {
                    task.rollbackFired(id);
                    throw ydse;
                }
            }
        }
    }


    // fire, start and complete a decomposition-less atomic task in situ
    protected void processEmptyTask(YAtomicTask task)
            throws YDataStateException, YStateException, YQueryException {
        try {
            if (task.t_enabled(_caseIDForNet)) {            // may be already processed
                YIdentifier id = task.t_fire().get(0);
                task.t_start(this, id);
                _busyTasks.add(task);                        // pre-req for completeTask
                completeTask(null, task, id, null);
            }
        }
        catch (YStateException yse) {
            // ignore - task already removed due to alternate path or case completion
        }
    }

    
    private void withdrawEnabledTask(YTask task) {
        _enabledTasks.remove(task);
        
        //  remove the withdrawn task from persistence
        YWorkItem wItem = _workItemRepository.get(_caseID, task.getID());
        if (wItem != null) {               //may already have been removed by task.cancel

            //announce all cancelled work items
            YWorkItemEvent event = new YWorkItemEvent(YEventType.ITEM_CANCEL, wItem);
            _announcements.add(event);

            // log it
            _announcer.announceLogEvent(new YLogEvent(YEventType.ITEM_CANCEL, wItem, null));

            // cancel any live timer
            if (wItem.hasTimerStarted()) {
                YTimer.getInstance().cancelTimerTask(wItem.getIDString());
            }
        }
    }


    /**
     * Creates an enabled work item.
     *
     * @param caseIDForNet the caseid for the net
     * @param atomicTask   the atomic task that contains it.
     */
    private YWorkItem createEnabledWorkItem(YIdentifier caseIDForNet,
                                            YAtomicTask atomicTask)
            throws YDataStateException, YQueryException {
        _logger.debug("--> createEnabledWorkItem: Case={}, Task={}",
                caseIDForNet.get_idString(), atomicTask.getID());

        boolean allowDynamicCreation = atomicTask.getMultiInstanceAttributes() != null &&
                    YMultiInstanceAttributes.CREATION_MODE_DYNAMIC.equals(
                            atomicTask.getMultiInstanceAttributes().getCreationMode());

        //creating a new work item puts it into the work item repository automatically.
        YWorkItem workItem = new YWorkItem(
                atomicTask.getNet().getSpecification().getSpecificationID(), atomicTask,
                new YWorkItemID(caseIDForNet, atomicTask.getID()),
                allowDynamicCreation, false);


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
            workItem.checkStartTimer(_netdata);
        }

        // set custom form for workitem if specified
        URL customFormURL = atomicTask.getCustomFormURL();
        if (customFormURL != null)
            workItem.setCustomFormURL(customFormURL) ;

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
    private synchronized boolean completeTask(YWorkItem workItem,
                                 YAtomicTask atomicTask, YIdentifier identifier,
                                 Document outputData)
            throws YDataStateException, YStateException, YQueryException {

        _logger.debug("--> completeTask: {}", atomicTask.getID());

        boolean taskExited = atomicTask.t_complete(identifier, outputData);

        if (taskExited) {
            if (workItem != null) {
                for (YWorkItem removed : _workItemRepository.removeWorkItemFamily(workItem)) {
                    if (! (removed.hasCompletedStatus() || removed.isParent())) {      // MI fired or incomplete
                        _announcer.announceCancelledWorkItem(removed);
                    }
                }

                updateTimerState(workItem.getTask(), State.closed);
            }

            // check if completing this task resulted in completing the net.
            if (endOfNetReached()) {

                // check if the completed net is a subnet.
                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _containingCompositeTask.getNetRunner();
                    if (parentRunner != null) {
                        synchronized (parentRunner) {
                            if (_containingCompositeTask.t_isBusy()) {

                                warnIfNetNotEmpty();

                                Document dataDoc = _net.usesSimpleRootData() ?
                                                   _net.getInternalDataDocument() :
                                                   _net.getOutputData() ;

                                parentRunner.processCompletedSubnet(_caseIDForNet,
                                            _containingCompositeTask,
                                            dataDoc);
                                parentRunner.removeChildRunner(this);
                                parentRunner.getAnnouncer().announceRunnerEvents(
                                        parentRunner.refreshAnnouncements());

                                _logger.debug("YNetRunner::completeTask() finished local task: {}," +
                                        " composite task: {}, caseid for decomposed net: {}",
                                        atomicTask, _containingCompositeTask, _caseIDForNet);
                            }
                        }
                    }
                }
            }

            continueIfPossible();
            _busyTasks.remove(atomicTask);

            _logger.debug("NOTIFYING RUNNER");
            kick();
        }
        _logger.debug("<-- completeTask: {}, Exited={}", atomicTask.getID(), taskExited);

        return taskExited;
    }


    public synchronized void cancel() {
        _logger.debug("--> NetRunner cancel {}", getCaseID().get_idString());

        _cancelling = true;
        for (YExternalNetElement netElement : _net.getNetElements().values()) {
            if (netElement instanceof YTask) {
                YTask task = ((YTask) netElement);
                if (task.t_isBusy()) {
                    task.cancel();
                }
            }
            else if (((YCondition) netElement).containsIdentifier()) {
                ((YCondition) netElement).removeAll();
            }
        }
        _enabledTasks = new HashSet<>();
        _busyTasks = new HashSet<>();

        if (_containingCompositeTask != null) {
            _announcer.announceLogEvent(new YLogEvent(YEventType.NET_CANCELLED,
                    getCaseID(), _specID, null));
        }
    }


    public synchronized boolean rollbackWorkItem(YIdentifier caseID, String taskID) {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_rollBackToFired(caseID);
    }


    private void logCompletingTask(YIdentifier caseIDForSubnet,
                                   YCompositeTask busyCompositeTask) {
        YLogPredicate logPredicate = busyCompositeTask.getDecompositionPrototype().getLogPredicate();
        _announcer.logNetCompleted(caseIDForSubnet, _specID,
                getLogPredicate(logPredicate, "OnCompletion"));
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



    private boolean warnIfNetNotEmpty() {
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
                        if (executingItem != null) executingItem.setStatusToDiscarded();
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
               .append("] has successfully completed, there were one or more ")
               .append("tokens remaining in the net, within these elements: [");

            msg.append(StringUtil.join(haveTokens, ','));
            msg.append("], which usually indicates that the net is unsound. Those ")
               .append("tokens were removed when the net completed.");
            _logger.warn(msg.toString());
        }
        return (! haveTokens.isEmpty());
    }


    public String toString() {

        return String.format("CaseID: %s; Enabled: %s; Busy: %s", _caseIDForNet.toString(),
                setToCSV(_enabledTasks), setToCSV(_busyTasks));
    }


    private String setToCSV(Set<YTask> tasks) {
        StringBuilder out = new StringBuilder();
        for (YTask task : tasks) {
            out.append(task.getID()).append(", ");
        }
        return out.toString();
    }


    public void dump() {
        dump(_enabledTasks, "ENABLED");
        dump(_busyTasks, "BUSY");
    }


    private void dump(Set<YTask> tasks, String label) {
        _logger.debug("*** DUMP OF NETRUNNER {} TASKS ***", label);
        for (YTask t : tasks) {
            _logger.debug("Type = {}", t.getClass().getName());
        }
        _logger.debug("*** END OF DUMP OF NETRUNNER {} TASKS ***", label);
    }

    /***************************************************************************/
    /** The following methods have been added to support the exception service */


    /** cancels the specified task */
    public synchronized void cancelTask(String taskID) {
        YAtomicTask task = (YAtomicTask) getNetElement(taskID);
        task.cancel(this.getCaseID());
        _busyTasks.remove(task);
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
        _timerStates = new HashMap<String, String>();
        for (YTask task : _netTasks) {
            if (task.getTimerVariable() != null) {
                updateTimerState(task, State.dormant);
            }
        }
    }


    public void updateTimerState(YTask task, State state) {
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

    public Set<YNetRunner> getAllRunnersInTree() {
        Set<YNetRunner> runners = new HashSet<>();
        runners.add(this);
        if (_children != null) {
            for (YNetRunner child : _children) {
                runners.addAll(child.getAllRunnersInTree());
            }
        }
        return runners;
    }

    
    public YNetRunner getRunnerWithID(YIdentifier id) throws YStateException {
        if (id.equals(getCaseID())) return this;          // short-circuit
        for (YNetRunner runner : getAllRunnersInTree()) {
            if (runner.getCaseID().equals(id)) {
                return runner;
            }
        }
        throw new YStateException("No runner found for work item");
    }

    public YNetRunner getTopRunner() {
        if (_containingCompositeTask != null) {
            YNetRunner parentRunner = _containingCompositeTask.getNetRunner();
            return parentRunner.getTopRunner();
        }
        return this;
    }

    public Set<YNetRunner> getAllRunnersForCase() {
        return getTopRunner().getAllRunnersInTree();
    }

    public YNetRunner getCaseRunner(YIdentifier id) {
        for (YNetRunner runner : getAllRunnersForCase()) {
            if (id.equals(runner.getCaseID())) {
                return runner;
            }
        }
        return null;
    }
}


