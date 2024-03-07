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
import org.yawlfoundation.yawl.engine.WorkItemCompletion;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.stateless.elements.YAtomicTask;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.YTask;
import org.yawlfoundation.yawl.stateless.elements.data.YParameter;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.listener.event.YCaseEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YEventType;
import org.yawlfoundation.yawl.stateless.listener.event.YWorkItemEvent;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YBuildProperties;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A stateless version of the YAWL engine
 * @author Michael Adams
 */

public class YEngine {

    // Engine execution statuses
    public enum Status { Dormant, Initialising, Running, Terminating }

    private Logger _logger;
    private Status _engineStatus;
    private final YAnnouncer _announcer;
    private YBuildProperties _buildProps;

    private static final AtomicInteger ENGINE_COUNTER = new AtomicInteger();
    private final int _engineNbr;

    /**
     * Constructor called from YStatelessEngine
     */
    public YEngine() {
        setEngineStatus(Status.Initialising);
        _engineNbr = ENGINE_COUNTER.incrementAndGet();
        _announcer = new YAnnouncer(this);
        _logger = LogManager.getLogger(YEngine.class);
        setEngineStatus(Status.Running);
    }


    /**
     * Checks if the engine is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return getEngineStatus() == Status.Running;
    }

    
    public int getEngineNbr() { return _engineNbr; }


    public void shutdown() {
        YTimer.getInstance().shutdown();              // stop timer threads
        YTimer.getInstance().cancel();                // stop the timer
    }


     public void initBuildProperties(InputStream stream) {
         _buildProps = new YBuildProperties();
         _buildProps.load(stream);
     }


     public YBuildProperties getBuildProperties() {
         return _buildProps;
     }



    public void checkEngineRunning() throws YEngineStateException {
        if (getEngineStatus() != Status.Running) {
            throw new YEngineStateException("Unable to accept request as engine" +
                    " not in running state: Current state = " + getEngineStatus().name());
        }
    }

    public YAnnouncer getAnnouncer() {
        return _announcer;
    }

    public void setEngineStatus(Status status) {
        _engineStatus = status;
    }

    public Status getEngineStatus() {
        return _engineStatus;
    }

    /********************************************************************************/

    protected Element formatCaseParams(String paramStr, YSpecification spec) throws YStateException {
        if (StringUtil.isNullOrEmpty(paramStr)) {
            return null;
        }
        Element data = JDOMUtil.stringToElement(paramStr);
        if (data == null) {
            throw new YStateException("Invalid or malformed caseParams.");
        }
        if (! (spec.getRootNet().getID().equals(data.getName()) ||
                       (spec.getURI().equals(data.getName())))) {
            throw new YStateException("Invalid caseParams: outermost element name " +
                    "must match specification URI or root net name.");
        }
        return data;
    }


    private void logCaseStarted(YSpecification spec, YNetRunner runner,
                                String caseParams, YLogDataItemList logData) {
        YIdentifier caseID = runner.getCaseID();
        Document paramDoc = caseParams != null ? JDOMUtil.stringToDocument(caseParams) : null;
        _announcer.announceCheckCaseConstraints(spec.getSpecificationID(), caseID,
                paramDoc, true);
        _announcer.announceCaseStart(spec, runner, logData);
    }

    
    /**
     * Cancels a running case.

     * @throws YEngineStateException if there's some persistence problem
     */
    public void cancelCase(YNetRunner runner) throws YEngineStateException {
        _logger.debug("--> cancelCase");
        checkEngineRunning();
        if (runner == null) {
            throw new IllegalArgumentException(
                    "Attempt to cancel a case using a null case runner");
        }

        YTimer.getInstance().cancelTimersForCase(runner.get_caseID());
        runner.cancel();
        _announcer.announceCaseCancellation(runner);
    }
    
    
    public YNetRunner launchCase(YSpecification spec, String caseID, String caseParams,
                             YLogDataItemList logData)
            throws YStateException, YDataStateException, YEngineStateException,
            YQueryException {

        checkEngineRunning();

         // initialise case identifier - if caseID is null, a new one is supplied
        YIdentifier yCaseID = new YIdentifier(caseID);

        // init case monitoring for this case
        _announcer.announceCaseEvent(new YCaseEvent(YEventType.CASE_STARTING, yCaseID));

        try {
            // check & format case data params (if any)
            Element data = formatCaseParams(caseParams, spec);

            YNetRunner runner = new YNetRunner(spec.getRootNet(), data, yCaseID);
            runner.setAnnouncer(_announcer);
            runner.continueIfPossible();
            runner.start();
            announceEvents(runner);
            logCaseStarted(spec, runner, caseParams, logData);
            return runner;
        }
        catch (YStateException | YDataStateException | YQueryException ex) {
            _announcer.announceCaseEvent(new YCaseEvent(YEventType.CASE_START_FAILED, yCaseID));
            throw ex;
        }
    }


    /**
     * Suspends the execution of a case.


     * @throws YStateException if case cannot be suspended given the current engine
     * operating state
     */
    public void suspendCase(YNetRunner runner) throws YStateException {

        _logger.debug("--> suspendCase: CaseID = {}", runner.getCaseID());

        // Reject call if this case not currently in a normal state
        if (! runner.hasNormalState()) {
            throw new YStateException("Case " + runner.getCaseID() +
                    " cannot be suspended as currently not executing normally (SuspendStatus="
                    + runner.getExecutionStatus() + ")");
        }

            // Go thru all runners and set status to suspending
            for (YNetRunner ynr : runner.getAllRunnersInTree()) {
                _logger.debug("Current status of runner {} = {}", ynr.get_caseID(),
                        ynr.getExecutionStatus());
                ynr.setStateSuspending();
            }
            _announcer.announceCaseSuspending(runner);
            _logger.info("Case {} is attempting to suspend", runner.getCaseID());

            // See if we can progress this case into a fully suspended state.
            progressCaseSuspension(runner);

        _logger.debug("<-- suspendCase");
    }


    /**
     * Resumes execution of a case.

     * @throws YStateException if case cannot be resumed
     * @throws YDataStateException
     * @throws YQueryException
     */
    public void resumeCase(YNetRunner runner)
            throws YStateException, YQueryException, YDataStateException {
        _logger.debug("--> resumeCase: CaseID = {}", runner.getCaseID());

        // reject call if this case not currently suspended or suspending
        if (runner.isInSuspense()) {
           for (YNetRunner ynr : runner.getAllRunnersInTree()) {

               _logger.debug("Current status of runner {} = {}", ynr.get_caseID(),
                       ynr.getExecutionStatus());

               ynr.setStateNormal();
               ynr.kick();
           }
            announceEvents(runner);
           _announcer.announceCaseResumption(runner);

           _logger.info("Case {} has resumed execution", runner.getCaseID());
       }
       else {
           throw new YStateException("Case " + runner.getCaseID() +
                   " cannot be suspended as currently not executing normally (SuspendStatus="
                   + runner.getExecutionStatus() + ")");
       }
        _logger.debug("<-- resumeCase");
   }

    /**
     * Attempts to progress the suspension status of a case.
     *
     * Where a Case is "suspending", we scan the workitems associated with all the nets
     * associated with the case, and where no workitems are enabled, executing or fired,
     * we progress the suspension state from "suspending" to "suspended".

     * @throws YStateException if case cannot be progressed
     */
    private void progressCaseSuspension(YNetRunner runner) throws YStateException {

        _logger.debug("--> progressCaseSuspension: CaseID={}", runner.getCaseID());
        if (! runner.isSuspending()) {
            throw new YStateException("Case " + runner.getCaseID() +
                    " cannot be suspended as case not currently attempting to suspend.");
        }

        boolean executingTasks = false;
        Set<YNetRunner> runners = runner.getAllRunnersInTree();
        for (YNetRunner ynr : runners) {

            // Go thru busy and executing tasks and see if we have any atomic tasks
            for (YTask task : ynr.getActiveTasks()) {
                if (task instanceof YAtomicTask) {
                    _logger.debug("One or more executing atomic tasks found for case - " +
                            " Cannot fully suspend at this time");
                    executingTasks = true;
                    break;
                }
            }
        }

        // If no executing tasks found go thru nets and set state to suspended
        if (! executingTasks) {
            for (YNetRunner ynr : runners) {
                ynr.setStateSuspended();
            }

            _announcer.announceCaseSuspended(runner);
        }
        _logger.debug("<-- progressCaseSuspension");
    }


    // announces deferred events from all this case's net runners //
    private void announceEvents(YNetRunner parent) {
        for (YNetRunner runner : parent.getAllRunnersInTree()) {
            _announcer.announceRunnerEvents(runner.refreshAnnouncements());
        }
    }


    /**
     * Starts a work item.  If the workitem param is enabled this method fires the task
     * and returns the first of its child instances in the executing state.
     * Else if the workitem is fired then it moves the state from fired to executing.
     * Either way the method returns the resultant work item.
     *
     * @param workItem the enabled, or fired workitem.
     * @return the resultant work item in the executing state.
     * @throws YStateException     if the workitem is not in either of these
     *                             states.
     * @throws YDataStateException
     */
    public YWorkItem startWorkItem(YWorkItem workItem)
            throws YStateException, YDataStateException, YQueryException,
             YEngineStateException {

        _logger.debug("--> startWorkItem");
        checkEngineRunning();

        if (workItem == null) {
            throw new YStateException("Cannot start null work item.");
        }

        YNetRunner caseRunner = workItem.getTask().getNetRunner();
        YWorkItem startedItem = null;
        YNetRunner netRunner = null;

        try {
            switch (workItem.getStatus()) {
                case statusEnabled:
                    netRunner = caseRunner.getRunnerWithID(workItem.getCaseID());
                    startedItem = startEnabledWorkItem(netRunner, workItem);
                    break;

                case statusFired:
                    netRunner = caseRunner.getRunnerWithID(workItem.getCaseID().getParent());
                    startedItem = startFiredWorkItem(netRunner, workItem);
                    break;

                case statusDeadlocked:
                    startedItem = workItem;
                    break;

                default: // this work item is likely already executing.
                    throw new YStateException(String.format(
                            "Item [%s]: status [%s] does not permit starting.",
                            workItem.getIDString(), workItem.getStatus()));
            }
            announceItemStarted(startedItem);
            if (netRunner != null) announceEvents(netRunner);

            _logger.debug("<-- startWorkItem");
        }
        catch (Exception e) {
            throw new YStateException(e.getMessage());
        }

        return startedItem;
    }


    private void announceItemStarted(YWorkItem item) {
        YWorkItemEvent event = new YWorkItemEvent(YEventType.ITEM_STARTED, item);
        _announcer.announceWorkItemEvent(event);
    }


    private YWorkItem startEnabledWorkItem(YNetRunner netRunner, YWorkItem workItem)
            throws YStateException, YDataStateException, YQueryException,
                   YEngineStateException {
        YWorkItem startedItem = null;
        List<YIdentifier> childCaseIDs =
                netRunner.attemptToFireAtomicTask(workItem.getTaskID());

        if (childCaseIDs != null) {
            boolean oneStarted = false;
            for (YIdentifier childID : childCaseIDs) {
                YWorkItem childItem = workItem.createChild(childID);
                if (! oneStarted) {
                    netRunner.startWorkItemInTask(childItem);
                    childItem.setStatusToStarted();
                    startedItem = childItem;
                    oneStarted = true;
                }
                childItem.logCompletionData();
            }
        }
        return startedItem;
    }


    private YWorkItem startFiredWorkItem(YNetRunner netRunner, YWorkItem workItem)
            throws YStateException, YDataStateException, YQueryException,
            YEngineStateException {

        netRunner.startWorkItemInTask(workItem);
        workItem.logCompletionData();
        workItem.setStatusToStarted();
        return workItem;
    }


    /**
     * Completes the work item.
     *
     * @param workItem
     * @param data
     * @param logPredicate - a pre-parse of the completion log predicate for this item
     * @param completionType - one of the completion types 'normal' (ordinary completion)
     * 'force' (forced completion) or 'fail' (forced fail) completion
     * @throws YStateException
     */
    public YWorkItem completeWorkItem(YWorkItem workItem, String data, String logPredicate,
                                 WorkItemCompletion completionType)
            throws YStateException, YDataStateException, YQueryException, YEngineStateException{
        if (_logger.isDebugEnabled()) {
            _logger.debug("--> completeWorkItem\nWorkItem = {}\nXML = {}",
                    workItem != null ? workItem.get_thisID() : "null", data);
        }
        checkEngineRunning();

        if (workItem == null) {
            throw new YStateException("Cannot complete null work item.");
        }
        if (! workItem.getCaseID().hasParent()) {
            throw new YStateException("WorkItem with ID [" + workItem.getIDString() +
                    "] is a 'parent' and so may not be completed.");
        }
        if (! workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
            throw new YStateException("WorkItem with ID [" + workItem.getIDString() +
                    "] not in executing state.");
        }
        YNetRunner runner = workItem.getTask().getNetRunner();
        if (runner == null) {
            throw new YStateException("Cannot complete work item with null runner.");
        }

 //       try {
            completeExecutingWorkitem(workItem, runner, data, logPredicate, completionType);
            announceEvents(runner);
//        }
//        catch (Exception e) {
//            throw new YStateException(e.getMessage());
//        }

        _logger.debug("<-- completeWorkItem");

        return workItem;
    }


    private void completeExecutingWorkitem(YWorkItem workItem, YNetRunner netRunner,
                                           String data, String logPredicate,
                                           WorkItemCompletion completionType)
            throws YStateException, YDataStateException, YQueryException,
                   YEngineStateException {
        Document doc = getDataDocForWorkItemCompletion(workItem, data, completionType);
        if (netRunner.completeWorkItemInTask(workItem, doc, completionType)) {

            completeWorkItemLogging(workItem, logPredicate, completionType, doc);

            // If case is suspending, see if we can progress into a fully suspended state
            if (netRunner.isSuspending()) {
                progressCaseSuspension(netRunner);
            }

            /* When a Task is enabled twice by virtue of having two enabling sets of
             * tokens in the current marking the work items are not created twice.
             * Instead an Enabled work item is created for one of the enabling sets.
             * Once that task has well and truly finished it is then an appropriate
             * time to notify the worklists that it is enabled again.*/
            netRunner.continueIfPossible();
        }
        else {  // a MI item and other children items are still outstanding
            workItem.setStatusToComplete(completionType);
            completeWorkItemLogging(workItem, logPredicate, completionType, doc);
        }
    }


    private void completeWorkItemLogging(YWorkItem workItem, String logPredicate,
                                         WorkItemCompletion completionType, Document doc) {
        workItem.setExternalLogPredicate(logPredicate);
        workItem.cancelTimer();                              // if any
        workItem.logCompletionData(doc);
    }


    public YWorkItem skipWorkItem(YWorkItem workItem)
            throws YStateException, YDataStateException,
            YQueryException, YEngineStateException {

        // start item, get output data, get children, complete each child
        YWorkItem startedItem = startWorkItem(workItem) ;
        if (startedItem != null) {
            String data = mapOutputDataForSkippedWorkItem(startedItem, startedItem.getDataString()) ;
            Set<YWorkItem> children = workItem.getChildren() ;
            for (YWorkItem child : children)
                completeWorkItem(child, data, null, WorkItemCompletion.Normal) ;
        }
        else {
            throw new YStateException("Could not skip workitem: " + workItem.getIDString()) ;
        }
        return startedItem ;
    }


    private Document getDataDocForWorkItemCompletion(YWorkItem workItem, String data,
                                                     WorkItemCompletion completionType)
            throws YStateException {
        if (completionType != WorkItemCompletion.Normal) {
            data = mapOutputDataForSkippedWorkItem(workItem, data);
        }
        Document doc = JDOMUtil.stringToDocument(data);
        JDOMUtil.stripAttributes(doc.getRootElement());
        return doc;
    }


    private String mapOutputDataForSkippedWorkItem(YWorkItem workItem, String data)
            throws YStateException {

        // get input and output params for tas
        YTask task = workItem.getTask() ;

        Map<String, YParameter> inputs =
                                 task.getDecompositionPrototype().getInputParameters();
        Map<String, YParameter> outputs =
                                 task.getDecompositionPrototype().getOutputParameters();

        if (outputs.isEmpty()) {                   // no output data to map
            return StringUtil.wrap("", task.getID());
        }

        // map data values to params
        Element itemData = JDOMUtil.stringToElement(data);
        Element outputData = itemData != null ? itemData.clone() : new Element(task.getID());

        // remove the input-only params from output data
        for (String name : inputs.keySet())
            if (outputs.get(name) == null) outputData.removeChild(name);

        // for each output param:
        //   1. if matching output Element, do nothing
        //   2. else if matching input param, use its value
        //   3. else if default value specified, use its value
        //   4. else use default value for the param's data type
        List<YParameter> outParamList = new ArrayList<YParameter>(outputs.values());
        Collections.sort(outParamList);                        // get in right order
        for (YParameter outParam : outParamList) {
            String name = outParam.getName();
            if (outputData.getChild(name) != null) continue;   // matching I/O element

            // the output param has no corresponding input param, so add an element
            String defaultValue = outParam.getDefaultValue();
            if (defaultValue == null) {
                String typeName = outParam.getDataTypeName();
                if (!XSDType.isBuiltInType(typeName)) {
                    throw new YStateException(String.format(
                            "Could not skip work item [%s]: Output-Only parameter [%s]" +
                                    " requires a default value.", workItem.getIDString(), name));
                }
                defaultValue = JDOMUtil.getDefaultValueForType(typeName);
            }
            Element outData = new Element(name) ;
            outData.setText(defaultValue);
            outputData.addContent(outData);
        }

        return JDOMUtil.elementToStringDump(outputData);
    }

    /**
     * Determines whether or not a task will allow a dynamically
     * created new instance to be created.  MultiInstance Task with
     * dynamic instance creation.
     *
     * @throws YStateException if task is not MultiInstance, or
     *                         if task does not allow dynamic instance creation,
     *                         or if current number of instances is not less than the maxInstances
     *                         for the task.
     */
    public void checkEligibilityToAddInstances(YWorkItem item)
            throws YStateException {

        if (item != null) {
            if (item.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                if (item.allowsDynamicCreation()) {
                    YIdentifier identifier = item.getCaseID().getParent();
                    YNetRunner netRunner = item.getTask().getNetRunner().getRunnerWithID(identifier);
                    if (! netRunner.isAddEnabled(item.getTaskID(), item.getCaseID())) {
                        throw new YStateException("Adding instances is not possible in " +
                                "current state.");
                    }
                }
                else {
                    throw new YStateException("WorkItem[" + item.getIDString() +
                            "] does not allow new instance creation.");
                }
            }
            else {
                throw new YStateException("WorkItem[" + item.getIDString() +
                        "] is not in appropriate (executing) " +
                        "state for instance adding.");
            }
        }
        else {
            throw new YStateException("Could not check null work item");
        }
    }


    /**
     * Creates a new work item instance when possible.
     *
     * @param workItem the id of a work item inside the task to have a new instance.
     * @param paramValueForMICreation format "<data>[InputParam]*</data>
     *                                InputParam == <varName>varValue</varName>
     * @return the work item of the new instance.
     * @throws YStateException if the task is not able to create a new instance, due to
     *                         its state or its design.
     */
    public YWorkItem createNewInstance(YWorkItem workItem,
                                       String paramValueForMICreation)
            throws YStateException {

        if (workItem == null) throw new YStateException("No work item found.");

        // will throw a YStateException if not eligible
        checkEligibilityToAddInstances(workItem);

        YNetRunner runner = workItem.getTask().getNetRunner();
        String taskID = workItem.getTaskID();
        YIdentifier siblingID = workItem.getCaseID();
        YNetRunner netRunner = runner.getRunnerWithID(siblingID.getParent());

        try {
            Element paramValue = JDOMUtil.stringToElement(paramValueForMICreation);
            YIdentifier id = netRunner.addNewInstance(taskID,
                    workItem.getCaseID(), paramValue);
            return workItem.getParent().createChild(id);                   //success!!!!
        }
        catch (Exception e) {
            throw new YStateException(e.getMessage());
        }
    }


    public YWorkItem suspendWorkItem(YWorkItem workItem) throws YStateException {
        if ((workItem != null) && (workItem.hasLiveStatus())) {
            workItem.setStatusToSuspended();
        }
        return workItem ;
    }


    public YWorkItem unsuspendWorkItem(YWorkItem workItem) throws YStateException {
        if ((workItem != null) &&
                (workItem.getStatus().equals(YWorkItemStatus.statusSuspended))) {
            workItem.setStatusToUnsuspended();
        }
        return workItem ;
    }

    
    // rolls back a workitem from executing to fired
    public YWorkItem rollbackWorkItem(YWorkItem workItem) throws YStateException {
        if ((workItem != null) && workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
            workItem.rollBackStatus();
            YNetRunner netRunner = workItem.getTask().getNetRunner().getRunnerWithID(
                    workItem.getCaseID().getParent());
            if (! netRunner.rollbackWorkItem(workItem.getCaseID(), workItem.getTaskID())) {
                throw new YStateException("Unable to rollback: work Item[" + workItem.getIDString() +
                        "] is not in executing state.");

            }
        }
        else throw new YStateException("Work Item[" + workItem.getIDString() + "] not found.");

        return workItem;
    }


    public YWorkItem cancelWorkItem(YNetRunner caseRunner, YWorkItem workItem) throws YStateException {
        try {
            if ((workItem != null) && workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                YNetRunner runner = caseRunner.getRunnerWithID(workItem.getCaseID().getParent());

                workItem.setStatusToDeleted();
                YWorkItem parent = workItem.getParent();
                if ((parent != null) && (parent.getChildren().size() == 1)) {
                    runner.cancelTask(workItem.getTaskID());
                }
                else ((YAtomicTask) workItem.getTask()).cancel(workItem.getCaseID());

                runner.kick();
                announceEvents(runner);
            }
        }
        catch (Exception e) {
            throw new YStateException("Failure whilst cancelling workitem: " + e.getMessage());
        }

        return workItem;
    }


    private void cancelTimer(YWorkItem workItem) {
        if (workItem != null) {
            if (workItem.hasTimerStarted()) {
                YTimer.getInstance().cancelTimerTask(workItem.getIDString());
            }
            YWorkItem parent = workItem.getParent();
            if (parent != null && parent.hasTimerStarted()) {
                Set<YWorkItem> children = parent.getChildren();

                if (children == null || children.size() == 1) {
                    YTimer.getInstance().cancelTimerTask(parent.getIDString());
                }
            }
        }
    }

}
