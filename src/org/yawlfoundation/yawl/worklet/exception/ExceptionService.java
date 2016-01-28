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

package org.yawlfoundation.yawl.worklet.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_Service;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.rdr.*;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.*;

import java.io.IOException;
import java.util.*;

/**
 *  The ExceptionService class manages the handling of exceptions that may occur
 *  during the life of a case instance. It receives events from the engine via
 *  InterfaceX at various milestones for constraint checking, and when certain
 *  exceptional events occur. It runs exception handlers when required, which may
 *  involve the running of compensatory worklets. It derives from the
 *  WorkletService class and uses many of the same methods as those used in the
 *  worklet selection process.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */

public class ExceptionService extends WorkletService implements InterfaceX_Service {

    private Map<String, ExletRunner> _handlersStarted =
            new HashMap<String, ExletRunner>() ;    // running exception worklets
    private Map<String, CaseMonitor> _monitoredCases =
            new HashMap<String, CaseMonitor>() ;      // cases monitored for exceptions
    private static Logger _log ;                        // debug log4j file
    private static ExceptionService _me ;               // reference to self
    private WorkItemConstraintData _pushedItemData;     // see 'pushWIConstraintEvent'
    private final ExceptionActions _actions;
    private final Object _mutex = new Object();

    /**
     * HashMap mappings:
     *   _monitoredCases:
     *      - KEY: [String] case id of case being monitored
     *      - VALUE: [CaseMonitor] obj describing the monitored case
     *   _handlersStarted:
     *      - KEY: [String] case id of a launched worklet compensatory case
     *      - VALUE: [HandlerRunner] obj managing the exception process of which
     *               the launched worklet is part
     */

    // the constructor
    public ExceptionService() {
        super();
        _log = LogManager.getLogger(ExceptionService.class);
        _actions = new ExceptionActions(_engineClient);
        _handlersStarted = new HashMap<String, ExletRunner>();
        _me = this ;
        registerExceptionService(this);                 // register service with parent
    }


    public static ExceptionService getInst() {
        return _me ;
    }


    // called from servlet WorkletGateway after contexts are loaded
    public void completeInitialisation() {
        super.completeInitialisation();
        _engineClient.setupIXClient();
        if (_persisting) restoreDataSets();             // reload running cases data
    }


    /*******************************/
    // INTERFACE X IMPLEMENTATIONS //
    /*******************************/

    /**
     * Handles a notification from the Engine that a case has been cancelled.
     * If the case passed has any exception handling worklets running for it,
     * they are also cancelled.
     *
     * @param caseID the case cancelled by the Engine
     */
    public void handleCaseCancellationEvent(String caseID) {

        synchronized (_mutex) {
            _log.info("HANDLE CASE CANCELLATION EVENT");

            caseID = getIntegralID(caseID);      // strip back to basic caseID

            if (_monitoredCases.containsKey(caseID)) {

                // retrieve the CaseMonitor for this case
                CaseMonitor monitor = _monitoredCases.get(caseID);

                // cancel any/all running worklets for this case
                if (monitor.hasLiveHandlerRunners())
                    cancelLiveWorkletsForCase(monitor);
                else _log.info("No current exception handlers for case {}", caseID);

                completeCaseMonitoring(monitor, caseID);

                // if this case was a worklet running for another case, process
                // the worklet cancellation
                if (_handlersStarted.containsKey(caseID)) {
                    handleCompletingExceptionWorklet(caseID, null, true);
                }
            }
            else _log.info("Case monitoring complete for case {}" +
                    " - cancellation event ignored.", caseID);
        }
    }


    /**
     * Handles a notification from the Engine that a workitem is either starting
     * or has completed.
     * Checks the rules for the workitem, and evaluates any pre-constraints or
     * post-constraints (if any), and if a constraint has been violated, raises
     * the appropriate exception.
     *
     * @param wir the workitem that triggered the event
     * @param data the workitem's data params
     * @param preCheck true for pre-constraints, false for post-constraints
     */
    public void handleCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                   boolean preCheck){
        synchronized (_mutex) {

            String caseID = getIntegralID(wir.getCaseID());
            CaseMonitor monitor = _monitoredCases.get(caseID);

            if (monitor != null) {
                _log.info("HANDLE CHECK WORKITEM CONSTRAINT EVENT");
                if (! monitor.isPreCaseCancelled()) {
                    monitor.updateData(data);         // update case monitor's data

                    String sType = preCheck ? "pre" : "post";
                    _log.info("Checking {}-constraints for workitem: {}", sType,
                            wir.getID());

                    checkConstraints(monitor, wir, preCheck);

                    if (preCheck)
                        monitor.addLiveItem(wir.getTaskID());  // flag item as active
                    else
                        monitor.removeLiveItem(wir.getTaskID());  // remove item flag

                    destroyMonitorIfDone(monitor, caseID);
                }
                else {
                    _log.info("Case cancelled: check workitem constraint event ignored.");
                    completeCaseMonitoring(monitor, monitor.getCaseID());
                }
            }
            else pushCheckWorkItemConstraintEvent(wir, data, preCheck);
        }
    }


    /**
     * Handles a notification from the Engine that a case is either starting
     * or has completed.
     * Checks the rules for the case and evaluates any pre-constraints or
     * post-constraints (if any), and if a constraint has been violated, raises
     * the appropriate exception.
     *
     * @param specID specification id of the case
     * @param caseID the id for the case
     * @param data the case-level data params
     * @param preCheck true for pre-constraints, false for post-constraints
     */
    public void handleCheckCaseConstraintEvent(YSpecificationID specID, String caseID,
                                               String data, boolean preCheck) {
        synchronized (_mutex) {

            _log.info("HANDLE CHECK CASE CONSTRAINT EVENT");        // note to log

            CaseMonitor monitor;     // object that maintains info of each monitored case

            if (preCheck) {
                _log.info("Checking constraints for start of case {} " +
                        "(of specification: {})", caseID, specID);

                // create new monitor for case - will live until case completes
                monitor = new CaseMonitor(specID, caseID, data);
                _monitoredCases.put(caseID, monitor);
                Persister.insert(monitor);
                checkConstraints(monitor, preCheck);

                // if check item event received before case initialised, call it now
                if (_pushedItemData != null)
                    popCheckWorkItemConstraintEvent(monitor) ;
            }
            else {                                                      // end of case

                // get the monitor for this case
                monitor = _monitoredCases.get(caseID);
                if (monitor != null) {
                    _log.info("Checking constraints for end of case {}", caseID);
                    checkConstraints(monitor, preCheck);
                    monitor.setCaseCompleted();

                    // treat this as a case complete event for exception worklets also
                    if (_handlersStarted.containsKey(caseID))
                        handleCompletingExceptionWorklet(caseID,
                                JDOMUtil.stringToElement(data), false);

                    destroyMonitorIfDone(monitor, caseID);
                }
                else {
                    _log.info("Case {} already completed", caseID);
                }
            }
        }
    }


    /**
     *  Handles a notification from the Engine that a workitem associated with the
     *  timeService has timed out.
     *  Checks the rules for timeout for the other items associated with this timeout item
     *  and raises thr appropriate exception.
     *
     * @param wir - the item that caused the timeout event
     * @param taskList - a list of taskids of those tasks that were running in
     *        parallel with the timeout task
     */
    public void handleTimeoutEvent(WorkItemRecord wir, String taskList) {

        synchronized (_mutex) {
            _log.info("HANDLE TIMEOUT EVENT");
            if (taskList != null) {

                // split task list into individual taskids
                taskList = taskList.substring(1, taskList.lastIndexOf(']')); // remove [ ]

                // for each parallel task in the time out set
                for (String taskID : taskList.split(", ")) {

                    // get workitem record for this task & add it to the monitor's data
                    YSpecificationID specID = new YSpecificationID(wir);
                    List<WorkItemRecord> wirs =
                            getWorkItemRecordsForTaskInstance(specID, taskID);
                    if (wirs != null) {
                        handleTimeout(wirs.get(0));
                    }
                    else _log.info("No live work item found for task: {}", taskID);
                }
            }
            else handleTimeout(wir);
        }
    }


    private void handleTimeout(WorkItemRecord wir) {
        handleItemException(wir, wir.getDataListString(), RuleType.ItemTimeout);
    }

    public void handleResourceUnavailableException(String resourceID, WorkItemRecord wir, 
                                                   String caseData, boolean primary) {
        handleItemException(wir, caseData, RuleType.ItemResourceUnavailable);
    }


    public String handleWorkItemAbortException(WorkItemRecord wir, String data) {
        return handleItemException(wir, data, RuleType.ItemAbort);
    }


    public String handleConstraintViolationException(WorkItemRecord wir, String data) {
        return handleItemException(wir, data, RuleType.ItemConstraintViolation);
    }


    private String handleItemException(WorkItemRecord wir, String data, RuleType ruleType) {
        String msg;
        String caseID = wir.getRootCaseID();
        CaseMonitor monitor = _monitoredCases.get(caseID);
        if (monitor == null) monitor = new CaseMonitor(new YSpecificationID(wir), caseID, data);

        if (data != null) monitor.updateData(data);
        monitor.addProcessInfo(wir);

        // get the exception handler for this task (if any)
        RdrPair pair = getExceptionHandler(monitor, wir.getTaskID(), ruleType);

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null) {
            msg = "No " + ruleType.toLongString() + " rules defined for workitem: " +
                    wir.getTaskID();
        }
        else {
            if (! pair.hasNullConclusion()) {                // we have a handler
                msg = ruleType.toLongString() + " exception raised for work item: " +
                        wir.getID();
                raiseException(monitor, pair, wir, ruleType);
            }
            else {                      // there are rules but the item passes
                msg = "Workitem '" + wir.getID() + "' has passed " +
                        ruleType.toLongString() + " rules.";
            }
        }
        monitor.removeProcessInfo();
        _log.info(msg);
        return StringUtil.wrap(msg, "result");
    }

    //***************************************************************************//

    /**
     * Checks for case-level constraint violations.
     *
     * @param monitor the CaseMonitor for this case
     * @param pre true for pre-constraints, false for post-constraints
     */
    private void checkConstraints(CaseMonitor monitor, boolean pre) {
        RdrPair pair;       // the conclusion from a set of rules, if any
        String sType = pre ? "pre" : "post";
        RuleType xType = pre ? RuleType.CasePreconstraint : RuleType.CasePostconstraint;

        // get the exception handler that would result from a constraint violation
        pair = getExceptionHandler(monitor, null, xType) ;

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null)
            _log.info("No {}-case constraints defined for spec: {}", sType,
                    monitor.getSpecID());
        else {
            if (! pair.hasNullConclusion()) {                // there's been a violation
                _log.info("Case {} failed {}-case constraints", monitor.getCaseID(),
                        sType);
                raiseException(monitor, pair, sType, xType) ;
            }
            else {                                // there are rules but the case passes
                getServer().announceConstraintPass(monitor.getCaseID(),
                        monitor.getCaseData(), xType);
                _log.info("Case {} passed {}-case constraints", monitor.getCaseID(),
                        sType);
            }
        }
    }


    /**
     * Checks for item-level constraint violations.
     *
     * @param monitor the CaseMonitor for the case this item is a member of
     * @param wir the WorkItemRecord for the workitem
     * @param pre true for pre-constraints, false for post-constraints
     */
    private void checkConstraints(CaseMonitor monitor, WorkItemRecord wir, boolean pre) {
        RdrPair pair;       // the conclusion from a set of rules, if any
        String itemID = wir.getID();
        String taskID = wir.getTaskID() ;
        String sType = pre? "pre" : "post";
        RuleType xType = pre? RuleType.ItemPreconstraint : RuleType.ItemPostconstraint;

        // get the exception handler that would result from a constraint violation
        pair = getExceptionHandler(monitor, taskID, xType) ;

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null)
            _log.info("No {}-task constraints defined for task: {}", sType, taskID);
        else {
            if (! pair.hasNullConclusion()) {                    // there's been a violation
                _log.info("Workitem {} failed {}-task constraints", itemID, sType);
                raiseException(monitor, pair, wir, xType) ;
            }
            else {                                  // there are rules but the case passes
                getServer().announceConstraintPass(wir, monitor.getCaseData(), xType);
                _log.info("Workitem {} passed {}-task constraints", itemID, sType);
            }
        }
    }


    /**
     * Discovers whether this case or item has rules for this exception type, and if so,
     * returns the result of the rule evaluation. Note that if the conclusion
     * returned from the search is empty, no exception has occurred.
     *
     * @param monitor the CaseMonitor for this case (or item is a member of)
     * @param taskID item's task id, or null for case-level exception
     * @param xType the type of exception triggered
     * @return an RdrConclusion representing an exception handling process,
     *         or null if no rules are defined for these criteria
     */
    private RdrPair getExceptionHandler(CaseMonitor monitor, String taskID,
                                              RuleType xType) {
        if (monitor != null) {
            return _rdr.evaluate(monitor.getSpecID(), taskID, monitor.getCaseData(), xType);
        }
        return null ;
    }


    /**
     * Raises a case-level exception by creating a HandlerRunner for the exception
     * process, then starting the processing of it
     *
     * @param cmon the CaseMonitor for the case that 'owns' the exception
     * @param pair represents the exception handling process
     * @param sType the type of exception triggered (as a string)
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
    private void raiseException(CaseMonitor cmon, RdrPair pair, String sType,
                                RuleType xType){
        _log.debug("Invoking exception handling process for Case: {}", cmon.getCaseID());
        ExletRunner hr = new ExletRunner(cmon, pair.getConclusion(), xType) ;
        cmon.addHandlerRunner(hr, sType);
        if (_persisting) {
            Persister.insert(hr);
        }
        getServer().announceException(cmon.getCaseID(), cmon.getCaseData(),
                pair.getLastTrueNode(), xType);
        processException(hr) ;
    }


    /**
     * Raises an item-level exception - see above for more info
     * @param cmon the CaseMonitor for the case that 'owns' the exception
     * @param pair represents the exception handling process
     * @param wir the WorkItemRecord of the item that triggered the event
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
    private void raiseException(CaseMonitor cmon, RdrPair pair,
                                WorkItemRecord wir, RuleType xType){
        _log.debug("Invoking exception handling process for item: {}", wir.getID());
        ExletRunner hr = new ExletRunner(cmon, wir, pair.getConclusion(), xType) ;
        cmon.addHandlerRunner(hr, wir.getID());
        if (_persisting) {
            Persister.insert(hr);
        }
        getServer().announceException(wir, cmon.getCaseData(),
                pair.getLastTrueNode(), xType);
        processException(hr) ;
    }


    /**
     * Begin (or continue after a worklet completes) the exception handling process
     *
     * @param hr the HandlerRunner for this handler process
     */
    private void processException(ExletRunner hr) {
        boolean doNextStep = true;

        while (hr.hasNextAction() && doNextStep) {
            doNextStep = doAction(hr);               // becomes false if worklets started
            hr.incActionIndex();                     // move to next primitive
        }

        CaseMonitor mon = hr.getOwnerCaseMonitor();

        // if no more actions to do in runner, remove it from the monitor (& the db)
        // except if the runner has worklets running (only necessary if the last prim.
        // in the sequence is a compensatory task)
        if (! (hr.hasNextAction() || hr.hasRunningWorklet())) {
            mon.removeHandlerRunner(hr);
            Persister.delete(hr);
        }

        // if case is completed and all exception handling is done, remove the record
        destroyMonitorIfDone(mon, mon.getCaseID());
    }


    /**
     * Perform a single step in an exception handing process
     * @param runner the HandlerRunner for this exception handling process
     * @return true if ok for processing to continue
     */
    private boolean doAction(ExletRunner runner){
        String action = runner.getNextAction();
        String target = runner.getNextTarget();
        boolean isItemTarget = target.equals("workitem");
        boolean success = true;

        _log.debug("Exception process step {}. Action = {}, Target = {}",
                runner.getActionIndex(), action, target);

        switch (ExletAction.fromString(action)) {
            case Continue : doContinue(runner); break;                    // un-suspend
            case Suspend  : doSuspend(runner); break;
            case Remove   : doRemove(runner); break;                      // cancel
            case Restart  : {
                if (isItemTarget) {
                    restartWorkItem(runner.getWir());
                }
                else success = invalidTarget(action, target);
                break;
            }
            case Complete: {
                if (isItemTarget) {
                    forceCompleteWorkItem(runner.getWir(), runner.getUpdatedData());
                }
                else success = invalidTarget(action, target);
                break;
            }
            case Fail: {
                if (isItemTarget) {
                    failWorkItem(runner.getWir());
                }
                else success = invalidTarget(action, target);
                break;
            }
            case Compensate: {

                // launch & run compensatory worklet(s)
                if (doCompensate(runner, target)) {
                    success = false;         // to break out of loop while worklet runs
                }
                break;
            }
            case Rollback: {
                _log.warn("Rollback is not yet implemented - will ignore this step.");
                break;
            }
            default:  {
                _log.warn("Unrecognised action type '{}' in exception handling primitive" +
                          " - will ignore this primitive.", action);
            }
        }
        return success;                   // successful processing of exception primitive
    }



    private boolean invalidTarget(String action, String target) {
        _log.error("Unexpected target type '{}' for exception handling primitive '{}'",
                target, action) ;
        return false ;
    }


    /**
     * Calls the appropriate continue method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doContinue(ExletRunner runner) {
        String target = runner.getNextTarget();
        switch (ExletTarget.fromString(target)) {
            case Workitem : {
                WorkItemRecord wir = _actions.unsuspendWorkItem(runner.getWir());
                runner.setItem(wir);     // refresh item
                runner.unsetItemSuspended();
                break;
            }
            case Case:
            case AllCases:
            case AncestorCases: {
                _actions.unsuspendList(runner);
                runner.clearCaseSuspended();
                break;
            }
            default: _log.error("Unexpected target type '{}' " +
                    "for exception handling primitive 'continue'", target) ;
        }
    }


    /**
     * Calls the appropriate suspend method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doSuspend(ExletRunner runner) {
        String target = runner.getNextTarget();
        switch (ExletTarget.fromString(target)) {
            case Workitem      : suspendWorkItem(runner); break;
            case Case          : _actions.suspendCase(runner); break;
            case AncestorCases : _actions.suspendAncestorCases(_handlersStarted, runner);
                                 break;
            case AllCases      : _actions.suspendAllCases(runner); break;
            default: _log.error("Unexpected target type '{}' " +
                          "for exception handling primitive 'suspend'", target) ;
        }
    }


    /**
     * Calls the appropriate remove method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doRemove(ExletRunner runner) {
        String target = runner.getNextTarget();
        switch (ExletTarget.fromString(target)) {
            case Workitem      : removeWorkItem(runner.getWir()); break;
            case Case          : removeCase(runner); break;
            case AncestorCases : removeAncestorCases(runner); break;
            case AllCases      : removeAllCases(runner.getSpecID()); break;
            default: _log.error("Unexpected target type '{}' " +
                          "for exception handling primitive 'remove'", target) ;
        }
    }


    private boolean doCompensate(ExletRunner exletRunner, String target) {
        Set<WorkletSpecification> workletList = _loader.parseTarget(target);
        Set<WorkletRunner> runners = _engineClient.launchWorkletList(exletRunner.getWir(),
                workletList, exletRunner.getRuleType());
        if (!runners.isEmpty()) {
            exletRunner.addWorkletRunners(runners);
            for (WorkletRunner runner : runners) {
                runner.logLaunchEvent();
            }
        }
        else _log.error("Unable to load compensatory worklet(s), will ignore: {}",
            target);
        return !runners.isEmpty();
    }


    /**
     * Deals with the end of an exception worklet case.
     *  @param caseId - the id of the completing case
     *  @param wlCasedata - the completing case's datalist Element
     *  @param cancelled - true if the worklet has been cancelled, false for a normal
     *  completion
     */
    private void handleCompletingExceptionWorklet(String caseId, Element wlCasedata,
                                                  boolean cancelled) {

        // get and remove the HandlerRunner that launched this worklet
        ExletRunner runner = _handlersStarted.remove(caseId);
        _log.debug("Worklet ran as exception handler for case: {}", runner.getCaseID());

        /* Update data of parent workitem/case if allowed and required and not cancelled
         * ASSUMPTION: the output data of the worklet will be used to update the
         * case/item only if:
         *   1. it is a case level exception and the case has been suspended, in
         *      which case the case-level data is updated; or
         *   2. it is an pre-executing item level exception and the item is suspended,
         *      in which case the case-level data is updated (because the item has not
         *      yet received data values from the case before starting); or
         *   3. it is an executing item exception and the item is suspended, in which
         *      case the item-level data is updated
         */

        if (! cancelled) {
            if (runner.isCaseSuspended() || runner.isItemSuspended())
                updateCaseData(runner, wlCasedata);

            if (runner.isItemSuspended() && runner.getRuleType().isExecutingItemType())
                updateItemData(runner, wlCasedata);
        }

        // log the worklet's case completion event
        String event = cancelled ? EventLogger.eCancel : EventLogger.eComplete;
        YSpecificationID specID = runner.getWir() != null ?
                new YSpecificationID(runner.getWir()) : getSpecIDForCaseID(caseId);
        EventLogger.log(event, caseId, specID, "", runner.getCaseID(), -1) ;

        runner.removeWorklet(caseId);       // worklet's case id no longer needed

         //if all worklets have completed, process the next exception primitive
         if (! runner.hasRunningWorklet()) {
            _log.info("All compensatory worklets have finished execution - " +
                    "continuing exception processing.");
            processException(runner);
        }
    }


     /**
     * Suspends the specified workitem
     * @param hr the HandlerRunner instance with the workitem to suspend
     */
    private boolean suspendWorkItem(ExletRunner hr) {
        WorkItemRecord wir = hr.getWir();
        if (_actions.suspendWorkItem(wir)) {
            hr.setItemSuspended();                  // record the action
            hr.setItem(updateWIR(wir));             // refresh the stored wir
            Set<WorkItemRecord> wirSet = new HashSet<WorkItemRecord>();
            wirSet.add(hr.getWir());          // ... and the list
            hr.setSuspendedItems(wirSet);          // record the suspended item
            return true ;
        }
        return false ;
    }

    public boolean suspendWorkItem(String itemID) {
        return _actions.suspendWorkItem(getWorkItemRecord(itemID));
    }

    public boolean suspendCase(String caseID) {
        return _actions.suspendCase(caseID);
    }


    /**
     * Cancels the workitem specified
     * @param wir the workitem (record) to cancel
     */
    private void removeWorkItem(WorkItemRecord wir) {
        try {
            // only executing items can be removed, so if its only fired or enabled, or
            // if its suspended, move it to executing first
            wir = moveToExecuting(wir);

            if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
                String msg = _engineClient.cancelWorkItem(wir.getID());
                if (successful(msg)) {
                    _log.info("WorkItem successfully removed from Engine: {}", wir.getID());
                }
                else _log.error("Failed to remove work item: {}", msg);
            }
            else _log.error("Can't remove a workitem with a status of {}", wir.getStatus());

        }
        catch (IOException ioe) {
            _log.error("Exception attempting to remove workitem: " + wir.getID(), ioe);
        }
    }


    /**
     * Cancels the specified case
     * @param hr the HandlerRunner instance with the case to cancel
     */
    private void removeCase(ExletRunner hr) {
        String caseID =  hr.getCaseID();
        try {
            if (successful( _engineClient.cancelCase(caseID))) {
                if (_monitoredCases.containsKey(caseID)) {
                    if (hr.getRuleType() == RuleType.CasePreconstraint) {
                        CaseMonitor mon = _monitoredCases.get(caseID);
                        mon.setPreCaseCancellationFlag();
                    }
                }
                _log.info("Case successfully removed from Engine: {}", caseID);
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to remove case: " + caseID, ioe);
        }
    }


    /**
     * Cancels the specified case
     * @param caseID the id of the case to cancel
     */
    private void removeCase(String caseID) {
        try {
            if (successful(_engineClient.cancelCase(caseID))) {
                _log.info("Case successfully removed from Engine: {}", caseID);
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to remove case: " + caseID, ioe);
        }
    }


    /**
     * Cancels all running instances of the specification passed
     * @param specID the id of the specification to cancel
     */
    private void removeAllCases(YSpecificationID specID) {
        try {
            String casesForSpec = _engineClient.getCases(specID);
            Element eCases = JDOMUtil.stringToElement(casesForSpec);

            for (Element eCase : eCases.getChildren()) {
                removeCase(eCase.getText());
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to all cases for specification: " +
                    specID.toString(), ioe);
        }
    }


    /**
     * Cancels all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    private void removeAncestorCases(ExletRunner runner){
        String caseID = getFirstAncestorCase(runner);
        CaseMonitor mon = _monitoredCases.get(caseID);

        _log.info("The ultimate parent case of this worklet has an id of: {}", caseID);
        _log.info("Removing all child worklets of case: {}", caseID);

        cancelLiveWorkletsForCase(mon);
        removeCase(caseID);                                // remove ultimate parent
    }


    /** returns the ultimate ancestor case of the runner passed */
    private String getFirstAncestorCase(ExletRunner runner) {
        String parentCaseID = runner.getCaseID();         // i.e. id of parent case

        // if the caseid is a started worklet handler, get it's runner
        while (_handlersStarted.containsKey(parentCaseID)) {
            runner = _handlersStarted.get(parentCaseID);
            parentCaseID = runner.getCaseID();
        }
        return parentCaseID ;
    }


    /**
     * ForceCompletes the specified workitem
     * @param wir the item to ForceComplete
     * @param out the final data params for the workitem
     */
    private void forceCompleteWorkItem(WorkItemRecord wir, Element out) {

        // only executing items can complete, so if its only fired or enabled, or
        // if its suspended, move it to executing first
        wir = moveToExecuting(wir);

        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            try {
                Element data = mergeCompletionData(wir, wir.getDataList(), out);
                String msg = _engineClient.forceCompleteWorkItem(wir, data);
                if (successful(msg)) {
                    _log.info("Item successfully force completed: {}", wir.getID());
                }
                else _log.error("Failed to force complete workitem: {}", msg);
            }
            catch (IOException ioe) {
                _log.error("Failed to force complete workitem: " + wir.getID(), ioe);
            }
        }
        else _log.error("Can't force complete a workitem with a status of {}", wir.getStatus());
    }


    /** restarts the specified workitem */
    private void restartWorkItem(WorkItemRecord wir) {

        // ASSUMPTION: Only an 'executing' workitem may be restarted
        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            try {
                String msg = _engineClient.restartWorkItem(wir.getID());
                if (successful(msg)) {
                    _log.info("Item successfully restarted: {}", wir.getID());
                }
                else _log.error("Failed to restart workitem: {}", msg);
            }
            catch (IOException ioe) {
                _log.error("Exception attempting restart workitem: " + wir.getID(), ioe);
            }
        }
        else _log.error("Can't restart a workitem with a status of {}", wir.getStatus());
    }


    /** Cancels a workitem and marks it as failed */
    private void failWorkItem(WorkItemRecord wir) {
        try {
            // only executing items can be failed, so if its only fired or enabled, or
            // if its suspended, move it to executing first
            wir = moveToExecuting(wir);

            // ASSUMPTION: Only an 'executing' workitem may be failed
            if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
                String result = _engineClient.failWorkItem(wir);
                if (successful(result)) {
                    _log.info("WorkItem successfully failed: {}", wir.getID());
                    if (result.contains("cancelled")) {
                        _log.info("Case {} was unable to continue as a consequence of the " +
                                "workItem force fail, and was also cancelled.", wir.getRootCaseID());
                    }
                }
                else _log.error(StringUtil.unwrap(result));
            }
            else _log.error("Can't fail a workitem with a status of {}", wir.getStatus());
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to fail workitem: " + wir.getID(), ioe);
        }
    }


    /**
     * Refreshes a locally cached WorkItemRecord with the Engine stored one
     * @param wir the item to refresh
     * @return the refreshed workitem, or the unchanged workitem on exception
     */
    private WorkItemRecord updateWIR(WorkItemRecord wir) {
        try {
            wir = getEngineStoredWorkItem(wir.getID(), _engineClient.getSessionHandle());
        }
        catch (IOException ioe){
            _log.error("IO Exception attempting to update WIR: " + wir.getID(), ioe);
        }
        return wir ;
    }


    /**
     * Updates a workitem's data param values with the output data of a
     * completing worklet, then copies the updates to the engine stored workitem
     * @param runner the HandlerRunner containing the exception handling process
     * @param wlData the worklet's output data params
     */
    private void updateItemData(ExletRunner runner, Element wlData) {

        // update the items datalist with corresponding values from the worklet
        Element out = updateDataList(runner.getDatalist(), wlData) ;

        // copy the updated list to the (locally cached) wir
        runner.getWir().setDataList(out);

        // and copy that back to the engine
        try {
            _engineClient.updateWorkItemData(runner.getWir(), out);
        }
        catch (IOException ioe) {
            _log.error("IO Exception calling interface X");
        }
    }


    /**
     * Updates the case-level data params with the output data of a
     * completing worklet, then copies the updates to the engine stored caseData
     * @param runner the HandlerRunner containing the exception handling process
     * @param wlData the worklet's output data params
     */
    private void updateCaseData(ExletRunner runner, Element wlData) {

        // get local copy of case data
        Element in = runner.getOwnerCaseMonitor().getCaseData();

        // update data values as required
        Element updated = updateDataList(in, wlData) ;

        // copy the updated list back to the case monitor
        runner.getOwnerCaseMonitor().setCaseData(updated);

        // and copy that back to the engine
        try {
            _engineClient.updateCaseData(runner.getCaseID(), updated);
        }
        catch (IOException ioe) {
            _log.error("IO Exception calling interface X");
        }
    }


    // merge the input and output data together
    private Element mergeCompletionData(WorkItemRecord wir, Element in, Element out)
            throws IOException {
        String mergedOutputData = Marshaller.getMergedOutputData(in, out);
        if (StringUtil.isNullOrEmpty(mergedOutputData)) {
            if (_log.isWarnEnabled()) {
                _log.warn("Problem merging workitem data: In [{}] Out [{}]",
                        JDOMUtil.elementToStringDump(in),
                        JDOMUtil.elementToStringDump(out));
            }
            return (in != null) ? in : out;
        }
        YSpecificationID specID = new YSpecificationID(wir);
        TaskInformation taskInfo = getTaskInformation(specID, wir.getTaskID(),
                _engineClient.getSessionHandle());
        List<YParameter> outputParams = taskInfo.getParamSchema().getOutputParams();
        try {
            return JDOMUtil.stringToElement(
                    Marshaller.filterDataAgainstOutputParams(mergedOutputData, outputParams));
        }
        catch (JDOMException jde) {
            return (in != null) ? in : out;
        }
    }


    /** cancels all worklets running as exception handlers for a case when that
     *  parent case is cancelled
     */
    private void cancelLiveWorkletsForCase(CaseMonitor monitor) {
        boolean runnerFound = false;         // flag for msg if no runners
        String caseID = monitor.getCaseID();

        // iterate through all the live runners for this case
        for (ExletRunner hr : monitor.getHandlerRunners()) {

            // if this runner has live worklet(s), cancel it/them
            if (hr.hasRunningWorklet()) {
                _log.info("Removing exception compensation worklet(s) " +
                        " for cancelled parent case: {}", caseID);
            }

            // cancel each worklet case of this runner
            for (WorkletRunner runner : hr.getWorkletRunners()) {

                // recursively call this method for each child worklet (in case
                // they also have worklets running)
                String caseIdToCancel = runner.getCaseID();
                CaseMonitor mon = _monitoredCases.get(caseIdToCancel);
                if ((mon != null) && mon.hasLiveHandlerRunners())
                    cancelLiveWorkletsForCase(mon);

                _log.info("Worklet case running for the cancelled parent case " +
                        "has id of: {}", caseIdToCancel) ;

                EventLogger.log(EventLogger.eCancel, caseIdToCancel,
                        new YSpecificationID(hr.getWir()), "", caseID, -1) ;

                removeCase(caseIdToCancel);
                _handlersStarted.remove(caseIdToCancel);
                runnerFound = true;
            }

            // unpersist the runner if its a 'top-level' parent
            monitor.removeHandlerRunner(hr);
            if (_persisting && ! isWorkletCase(caseID))
                Persister.delete(hr);
        }
        if (! runnerFound) _log.info("No worklets running for cancelled case: {}",
                caseID);
    }


    private WorkItemRecord moveToExecuting(WorkItemRecord wir) {
        if (wir.getStatus().equals(WorkItemRecord.statusSuspended))
            _actions.unsuspendWorkItem(wir);
        if (wir.getStatus().equals(WorkItemRecord.statusFired) ||
                wir.getStatus().equals(WorkItemRecord.statusEnabled)) {
            Set<WorkItemRecord> cos = _engineClient.checkOutItem(wir);
            if (! cos.isEmpty()) {
                wir = cos.iterator().next();
            }
        }
        return wir;
    }


    public Set<WorkletRunner> getRunningWorklets() {
        Set<WorkletRunner> workletRunners = new HashSet<WorkletRunner>();
        for (ExletRunner exletRunner : _handlersStarted.values()) {
            workletRunners.addAll(exletRunner.getWorkletRunners());
        }
        return workletRunners;
    }


    public List<String> getExternalTriggersForItem(String itemID) {
        return _rdr.getExternalTriggersForItem(itemID);
    }


    /**
     * Retrieves a list of all workitems that are instances of the specified task
     * within the specified spec
     * @param specID
     * @param taskID
     * @return the list of workitems
     */
    private List<WorkItemRecord> getWorkItemRecordsForTaskInstance(
            YSpecificationID specID, String taskID) {

        // get all the live work items that are instances of this task
        List<WorkItemRecord> items = _engineClient.getLiveWorkItemsForIdentifier("task", taskID) ;
        List<WorkItemRecord> toRemove = new ArrayList<WorkItemRecord>();

        if (items != null) {

            // filter those for this spec
            for (WorkItemRecord wir : items) {
                YSpecificationID wirSpecID = new YSpecificationID(wir);
                if (! wirSpecID.equals(specID)) toRemove.add(wir);
            }
            items.removeAll(toRemove);
        }
        return items;
    }


    /**
     * Strips off the non-integral part of a case id
     * @param id the case id to fix
     * @return the integral part of the caseid passed
     */
    private String getIntegralID(String id) {
        int end = id.indexOf('.') ;         // where's the decimal point
        if (end == -1) return id ;          // no dec point, no change required
        return id.substring(0, end);        // else return its substring
    }


    /**
     * Removes the CaseMonitor instance from the list of monitored cases iff all
     * exception handling and constraint checking is complete for the case
     * @param monitor the CaseMonitor to remove if finished
     * @param caseID the caseID of a worklet run as a compensation process
     */
    private void destroyMonitorIfDone(CaseMonitor monitor, String caseID) {
        if ((! _handlersStarted.containsKey(caseID)) && monitor.isDone() &&
                (! monitor.isPreCaseCancelled()))
            completeCaseMonitoring(monitor, caseID);
    }


    /** completes case monitoring by performing housekeeping for the (completed) case */
    private void completeCaseMonitoring(CaseMonitor monitor, String caseID) {
        _monitoredCases.remove(caseID);
        Persister.delete(monitor);
        _log.info("Exception monitoring complete for case {}", caseID);
    }


    /** returns the specified wir for the id passed */
    public WorkItemRecord getWorkItemRecord(String itemID) {
        try {
            return _engineClient.getEngineStoredWorkItem(itemID);
        }
        catch (IOException ioe) {
            _log.error("Exception getting WIR: " + itemID, ioe);
            return null ;
        }
    }


    /** returns the spec id for the specified case id */
    public YSpecificationID getSpecIDForCaseID(String caseID) {
        CaseMonitor mon = _monitoredCases.get(getIntegralID(caseID));
        return mon.getSpecID();
    }


    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified case
     * @param caseID - the id of the case to get the triggers for
     * @return the (String) list of triggers
     */
    public List getExternalTriggersForCase(String caseID) {
        if (caseID != null) {
            CaseMonitor mon = _monitoredCases.get(getIntegralID(caseID));
            if (mon != null) {
                RdrTree tree = _rdr.getTree(mon.getSpecID(), null, RuleType.CaseExternalTrigger);
                return _rdr.getExternalTriggers(tree) ;
            }
        }
        return null;
    }


    /**
     * Raise an externally triggered exception
     * @param level - the level of the exception (case/item)
     * @param id - the id of the case or item on which the exception is being raised
     * @param trigger - the identifier of (or reason for) the external exception
     */
    public void raiseExternalException(String level, String id, String trigger) {

        synchronized (_mutex) {
            _log.info("HANDLE EXTERNAL EXCEPTION EVENT");        // note to log

            String caseID, taskID;
            WorkItemRecord wir = null;
            RuleType xLevel ;

            if (level.equalsIgnoreCase("case")) {                // if case level
                caseID = id;
                taskID = null;
                xLevel = RuleType.CaseExternalTrigger;
            }
            else {                                               // else item level
                wir = getWorkItemRecord(id);
                caseID = wir.getRootCaseID();
                taskID = wir.getTaskID();
                xLevel = RuleType.ItemExternalTrigger;
            }

            // get case monitor for this case
            CaseMonitor monitor = _monitoredCases.get(caseID);

            monitor.addTrigger(trigger);               // add trigger value to case data

            // get the exception handler for this trigger
            RdrPair pair = getExceptionHandler(monitor, taskID, xLevel);

            // if pair is null there's no rules defined for this type of constraint
            if (pair == null)
                _log.error("No external exception rules defined for spec: {}" +
                        ". Unable to raise exception for '{}'",
                        monitor.getSpecID(), trigger);
            else if (wir == null)
                raiseException(monitor, pair, "external", xLevel);
            else
                raiseException(monitor, pair, wir, xLevel);

            monitor.removeTrigger();
        }
    }


    /**
     * Raises an exception (triggered via the API) using the conclusion passed in
     * @param wir the wir to raise the exception for
     * @param ruleType the exception rule type
     * @param conclusion a populated conclusion object
     */
    public String raiseException(WorkItemRecord wir, RuleType ruleType,
                               RdrConclusion conclusion) {
        String caseID = wir.getRootCaseID();
        CaseMonitor monitor = _monitoredCases.get(caseID);
        if (monitor == null) {
            monitor = new CaseMonitor(new YSpecificationID(wir), caseID, null);
        }
        RdrNode dummyNode = new RdrNode(-1);
        dummyNode.setConclusion(conclusion);
        RdrPair dummyPair = new RdrPair(dummyNode, dummyNode);
        raiseException(monitor, dummyPair, wir, ruleType);
        return StringUtil.wrap(ruleType.toLongString() +
                " exception raised for work item: " + wir.getID(), "result");
    }


    /**
     *  Replaces a running worklet case with another worklet case after an
     *  amendment to the ruleset for this exception.
     *  Called by WorkletGateway after a call from the Editor that the ruleset
     *  has been updated.
     *  Overrides the WorkletService equivalent - This one looks after exceptions,
     *  that one looks after selections
     *
     *  @param xType - the type of exception that launched the worklet
     *  @param caseid - the id of the original checked out case
     *  @param itemid - the id of the original checked out workitem
     *  @return a string of messages describing the success or otherwise of
     *          the process
     */
    public String replaceWorklet(RuleType xType, String caseid, String itemid, String trigger)
            throws IOException {
        WorkItemRecord wir = null;
        boolean caseLevel = xType.isCaseLevelType();
        CaseMonitor mon = _monitoredCases.get(caseid);

        _log.info("REPLACE EXECUTING WORKLET REQUEST");

        caseid = getIntegralID(caseid);

        // check if case is currently being handled
        if (mon == null) {
            raise("Unable to locate running case with id: " + caseid);
        }

        // get the HandlerRunner for the Exception
        ExletRunner hr = mon.getRunnerForType(xType, itemid);
        if (hr == null) {
            raise("No compensatory worklets running for case: " + caseid);
        }

        // get the case ids of the running worklets for this case/workitem
        for (WorkletRunner runner : hr.getWorkletRunners()) {

            // cancel the worklet running for the case/workitem
            String workletCaseID = runner.getCaseID();
            removeCase(workletCaseID);
            _log.debug("Removed worklet case: {}", workletCaseID);
            _handlersStarted.remove(workletCaseID) ;
            _log.debug("Removing worklet from handlers started: {}", workletCaseID);
        }

        // go through the selection process again
        _log.debug("Launching new replacement worklet case(s) based on revised ruleset");

        // remove monitor's runner for cancelled worklet
        mon.removeHandlerRunner(hr);
        Persister.delete(hr);

        // go through the process again, depending on the exception type
        if (! caseLevel) wir = hr.getWir();
        switch (xType) {
            case CasePreconstraint : checkConstraints(mon, true); break;
            case CasePostconstraint: checkConstraints(mon, false); break;
            case ItemPreconstraint : checkConstraints(mon, wir, true); break;
            case ItemPostconstraint: checkConstraints(mon, wir, false); break;
            case ItemAbort         :
                if (wir != null) handleWorkItemAbortException(wir,
                                    wir.getDataListString()); break ;
            case ItemTimeout :
                if (wir != null) handleTimeoutEvent(wir, wir.getTaskID()); break ;
            case ItemResourceUnavailable : break;   // todo
            case ItemConstraintViolation :
                if (wir != null) handleConstraintViolationException(wir,
                    wir.getDataListString()); break;
            case CaseExternalTrigger :
                raiseExternalException("case", caseid, trigger); break;
            case ItemExternalTrigger :
                raiseExternalException("item", caseid, trigger); break;
        }

        // get the new HandlerRunner for the new Exception
        hr = mon.getRunnerForType(xType, itemid) ;
        return getRunnerCaseIdList(hr.getWorkletRunners());
    }


    /** returns true if case specified is a worklet instance */
    public boolean isWorkletCase(String caseID) {
        return (_handlersStarted.containsKey(caseID) || super.isWorkletCase(caseID));
    }


    /** stub method called from RdrConditionFunctions class */
    public String getStatus(String taskID) {
        return null;
    }


    /** restores the contents of the running datasets after a web server restart */
    private void restoreDataSets() {
        StateRestorer restorer = new StateRestorer();
        Map<String, ExletRunner> runners = restorer.restoreRunners();
        _monitoredCases = restorer.restoreMonitoredCases(runners, _handlersStarted);
    }


    /********************************************************************************/

    // PUSHING & POPPING INITIAL WORKITEM CONSTRAINT EVENT //

    /**
     * There is no pre-defined ordering of constraint events - a workitem check constraint
     * event is sometimes received before a start-of-case check constraint event. Since
     * a CaseMonitor object is required by a workitem check, and created by a start-of-
     * case event, we must ensure that the start-of-case check is always done first. Also,
     * pre-case constraint violations may cause a case cancellation, so that a check
     * item pre-constraint event may come after a case has been cancelled.
     * In the situation where the workitem event is received first, it is stored until
     * the case event has been processed (via the push method below), then called (via
     *  the pop method) immediately after the case check has completed.
     */

    /** stores the data passed to the event for later processing */
    private void pushCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                  boolean preCheck) {
        _pushedItemData = new WorkItemConstraintData(wir, data, preCheck);
    }

    /** retrieves the data passed to the initial event and recalls it */
    private void popCheckWorkItemConstraintEvent(CaseMonitor mon) {
        if (! mon.isPreCaseCancelled())
            handleCheckWorkItemConstraintEvent(_pushedItemData.getWIR(),
                    _pushedItemData.getData(),
                    _pushedItemData.getPreCheck());
        _pushedItemData = null ;
    }

    /** class/structure used to store and retrieve event data items */
    class WorkItemConstraintData {
        private WorkItemRecord _wir ;
        private String _data ;
        private boolean _preCheck ;

        public WorkItemConstraintData(WorkItemRecord wir, String data, boolean preCheck) {
            _wir = wir ;
            _data = data ;
            _preCheck = preCheck ;
        }

        public WorkItemRecord getWIR() { return _wir; }
        public String getData() { return _data; }
        public boolean getPreCheck() { return _preCheck; }
    }

    /********************************************************************************/
    /********************************************************************************/


} // end of ExceptionService class
