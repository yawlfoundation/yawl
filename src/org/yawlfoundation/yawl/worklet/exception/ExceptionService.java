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
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_ServiceSideClient;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.rdr.*;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.EventLogger;
import org.yawlfoundation.yawl.worklet.support.Library;
import org.yawlfoundation.yawl.worklet.support.Persister;
import org.yawlfoundation.yawl.worklet.support.WorkletSpecification;

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
/*  Here's the class hierarchy for the ExceptionService:
*
*   +================+            +====================+
*   | WorkletService |            | InterfaceX_Service |
*   +================+            +====================+
*          ^                             O
*          |                             |
* ---------+-----------------------------+----------------------------- *
*          |        +--------------------+                              *
*          |        |                                                   *
*  +===================+        +=============+        +==============+ *
*  | ExceptionService  | 1----M | CaseMonitor | 1----M | HandlerRunner| *
*  +===================+        +=============+        +==============+ *
*         ^                                                    O        *
*         |                                                    |        *
*         V                                                    |        *
*     +======+                      +=========+       +===============+ *
*     | JSPs |                      | CaseMap | 1---1 | WorkletRecord | *
*     +======+                      +=========+       +===============+ *
*                                                                       *
* --------------------------------------------------------------------- *
*
*/

public class ExceptionService extends WorkletService implements InterfaceX_Service {

    private Map<String, ExletRunner> _handlersStarted =
            new Hashtable<String, ExletRunner>() ;    // running exception worklets
    private Map<String, CaseMonitor> _monitoredCases =
            new Hashtable<String, CaseMonitor>() ;      // cases monitored for exceptions
    private static Logger _log ;                        // debug log4j file
    private InterfaceX_ServiceSideClient _ixClient ;    // interface client to engine
    private static ExceptionService _me ;               // reference to self
    private WorkItemConstraintData _pushedItemData;     // see 'pushWIConstraintEvent'
    private final Object mutex = new Object();

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
    public ExceptionService(){
        super();
        _log = LogManager.getLogger(ExceptionService.class);
        setUpInterfaceBClient(_engineURI);
        _me = this ;
        registerExceptionService(this);                 // register service with parent
    }


    public static ExceptionService getInst() {
        return _me ;
    }


    // called from servlet WorkletGateway after contexts are loaded
    public void completeInitialisation() {
        super.completeInitialisation();
        _ixClient = new InterfaceX_ServiceSideClient(_engineURI.replaceFirst("/ib", "/ix"));
        if (_persisting) restoreDataSets();             // reload running cases data
    }

    //***************************************************************************//

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

        synchronized (mutex) {
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

    //***************************************************************************//

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
        synchronized (mutex) {

            String caseID = getIntegralID(wir.getCaseID());
            CaseMonitor monitor = _monitoredCases.get(caseID);

            if (monitor != null) {
                if (connected()) {
                    _log.info("HANDLE CHECK WORKITEM CONSTRAINT EVENT");
                    if (! monitor.isPreCaseCancelled()) {
                        monitor.updateData(data);         // update case monitor's data

                        String sType = preCheck ? "pre" : "post";
                        _log.info("Checking {}-constraints for workitem: {}", sType,
                                wir.getID());

                        checkConstraints(monitor, wir, preCheck);

                        if (preCheck)
                            monitor.addLiveItem(wir.getTaskName());  // flag item as active
                        else
                            monitor.removeLiveItem(wir.getTaskName());  // remove item flag

                        destroyMonitorIfDone(monitor, caseID);
                    }
                    else {
                        _log.info("Case cancelled: check workitem constraint event ignored.");
                        completeCaseMonitoring(monitor, monitor.getCaseID());
                    }
                }
                else _log.error("Unable to connect the Exception Service to the Engine");
            }
            else pushCheckWorkItemConstraintEvent(wir, data, preCheck);
        }
    }

    //***************************************************************************//

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
        synchronized (mutex) {

            _log.info("HANDLE CHECK CASE CONSTRAINT EVENT");        // note to log

            CaseMonitor monitor;     // object that maintains info of each monitored case

            if (connected()) {
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
                    _log.info("Checking constraints for end of case {}", caseID);

                    // get the monitor for this case
                    monitor = _monitoredCases.get(caseID);
                    checkConstraints(monitor, preCheck);
                    monitor.setCaseCompleted();

                    // treat this as a case complete event for exception worklets also
                    if (_handlersStarted.containsKey(caseID))
                        handleCompletingExceptionWorklet(caseID,
                                JDOMUtil.stringToElement(data), false);

                    destroyMonitorIfDone(monitor, caseID);
                }
            }
            else _log.error("Unable to connect the Exception Service to the Engine");
        }
    }

    //***************************************************************************//

    /**
     *  Handles a notification from the Engine that a workitem associated with the
     *  timeService has timed out.
     *  Checks the rules for timeout for the other items associated withthis timeout item
     *  and raises thr appropriate exception.
     *
     * @param wir - the item that caused thetimeout event
     * @param taskList - a list of taskids of those tasks that were running in
     *        parallel with the timeout task
     */
    public void handleTimeoutEvent(WorkItemRecord wir, String taskList) {

        synchronized (mutex) {

            _log.info("HANDLE TIMEOUT EVENT");

            if (connected()) {
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
            else _log.error("Unable to connect the Exception Service to the Engine");
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
        RdrPair pair = getExceptionHandler(monitor, wir.getTaskName(), ruleType);

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null) {
            msg = "No " + ruleType.toLongString() + " rules defined for workitem: " +
                    wir.getTaskName();
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
    //***************************************************************************//

    /*********************************************/
    /* RULE CHECKING & EXCEPTION RAISING METHODS */
    /*********************************************/

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
                _server.announceConstraintPass(monitor.getCaseID(),
                        monitor.getCaseData(), xType);
                _log.info("Case {} passed {}-case constraints", monitor.getCaseID(),
                        sType);
            }
        }
    }

    //***************************************************************************//

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
        String taskID = wir.getTaskName() ;
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
                _server.announceConstraintPass(wir, monitor.getCaseData(), xType);
                _log.info("Workitem {} passed {}-task constraints", itemID, sType);
            }
        }
    }

    //***************************************************************************//

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

    //***************************************************************************//

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
        if (connected()) {
            _log.debug("Invoking exception handling process for Case: {}", cmon.getCaseID());
            ExletRunner hr = new ExletRunner(cmon, pair.getConclusion(), xType) ;
            cmon.addHandlerRunner(hr, sType);
            if (_persisting) {
                Persister.insert(hr);
            }
            _server.announceException(cmon.getCaseID(), cmon.getCaseData(),
                    pair.getLastTrueNode(), xType);
            processException(hr) ;
        }
        else _log.error("Could not connect to YAWL Engine to handle Exception") ;
    }

    //***************************************************************************//

    /**
     * Raises an item-level exception - see above for more info
     * @param cmon the CaseMonitor for the case that 'owns' the exception
     * @param pair represents the exception handling process
     * @param wir the WorkItemRecord of the item that triggered the event
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
    private void raiseException(CaseMonitor cmon, RdrPair pair,
                                WorkItemRecord wir, RuleType xType){
        if (connected()) {
            _log.debug("Invoking exception handling process for item: {}", wir.getID());
            ExletRunner hr = new ExletRunner(cmon, wir, pair.getConclusion(), xType) ;
            cmon.addHandlerRunner(hr, wir.getID());
            if (_persisting) {
                Persister.insert(hr);
            }
            _server.announceException(wir, cmon.getCaseData(),
                    pair.getLastTrueNode(), xType);
            processException(hr) ;
        }
        else  _log.error("Could not connect to YAWL Engine to handle Exception") ;
    }

    //***************************************************************************//
    //***************************************************************************//

    /********************************/
    /* EXCEPTION PROCESSING METHODS */
    /********************************/


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

    //***************************************************************************//

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


    //***************************************************************************//

    /**
     * Calls the appropriate continue method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doContinue(ExletRunner runner) {
        String target = runner.getNextTarget();
        switch (ExletTarget.fromString(target)) {
            case Workitem : {
                runner.setItem(unsuspendWorkItem(runner.getWir()));     // refresh item
                runner.unsetItemSuspended();
                break;
            }
            case Case:
            case AllCases:
            case AncestorCases: {
                unsuspendList(runner);
                runner.clearCaseSuspended();
                break;
            }
            default: _log.error("Unexpected target type '{}' " +
                    "for exception handling primitive 'continue'", target) ;
        }
    }

    //***************************************************************************//

    /**
     * Calls the appropriate suspend method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doSuspend(ExletRunner runner) {
        String target = runner.getNextTarget();
        switch (ExletTarget.fromString(target)) {
            case Workitem      : suspendWorkItem(runner); break;
            case Case          : suspendCase(runner); break;
            case AncestorCases : suspendAncestorCases(runner); break;
            case AllCases      : suspendAllCases(runner); break;
            default: _log.error("Unexpected target type '{}' " +
                          "for exception handling primitive 'suspend'", target) ;
        }
    }

    //***************************************************************************//

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

    //***************************************************************************//

    private boolean doCompensate(ExletRunner exletRunner, String target) {
        Set<WorkletSpecification> workletList = _loader.parseTarget(target);
        Set<WorkletRunner> runners = launchWorkletList(exletRunner.getWir(),
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

    //***************************************************************************//
    //***************************************************************************//

    /******************************/
    /* PROCESS PRIMITIVE ENACTIONS */
    /******************************/

    /**
     * Suspends the specified workitem
     * @param hr the HandlerRunner instance with the workitem to suspend
     */
    private boolean suspendWorkItem(ExletRunner hr) {
        WorkItemRecord wir = hr.getWir();

        if (wir.hasLiveStatus()) {
            Set<WorkItemRecord> children = new HashSet<WorkItemRecord>();
            children.add(wir);                          // put item in list for next call
            if (suspendWorkItemList(children)) {        // suspend the item (list)
                hr.setItemSuspended();                  // record the action
                hr.setItem(updateWIR(wir));             // refresh the stored wir
                children.clear();
                children.add(hr.getWir());          // ... and the list
                hr.setSuspendedItems(children);          // record the suspended item
                return true ;
            }
        }
        else
            _log.error("Can't suspend a workitem with a status of {}", wir.getStatus());

        return false ;
    }

    public boolean suspendWorkItem(String itemID) {
        WorkItemRecord wir = getWorkItemRecord(itemID);
        Set<WorkItemRecord> children = new HashSet<WorkItemRecord>();

        if (wir.hasLiveStatus()) {
            children.add(wir);                          // put item in list for next call
            if (suspendWorkItemList(children)) {        // suspend the item (list)
                return true ;
            }
        }
        else
            _log.error("Can't suspend a workitem with a status of {}", wir.getStatus());

        return false ;
    }

    //***************************************************************************//

    /**
     * Suspends each workitem in the list of items passed
     * @param items - items a list of workitems to suspend
     * @return true if all were successfully suspended
     */
    private boolean suspendWorkItemList(Set<WorkItemRecord> items) {
        String itemID = "";

        try {
            for (WorkItemRecord item : items) {
                itemID = item.getID();
                if (! successful(_interfaceBClient.suspendWorkItem(itemID, _sessionHandle)))
                    throw new IOException() ;
                _log.debug("Successful work item suspend: {}", itemID);
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to suspend workitem: " + itemID, ioe);
            return false;
        }
        return true ;
    }


    //***************************************************************************//

    /**
     * Suspends all live workitems in the specified case
     * @param caseID - the id of the case to suspend
     * @return true on successful suspend
     */
    public boolean suspendCase(String caseID) {
        Set<WorkItemRecord> suspendItems = getSuspendableWorkItems("case", caseID) ;
        if (suspendWorkItemList(suspendItems)) {
            _log.debug("Completed suspend for case: {}", caseID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend case failed for case: {}", caseID);
            return false ;
        }
    }

    //***************************************************************************//

    /**
     * Suspends all live workitems in the specified case
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    private boolean suspendCase(ExletRunner hr) {
        String caseID = hr.getCaseID();
        Set<WorkItemRecord> suspendedItems = getSuspendableWorkItems("case", caseID) ;

        if (suspendWorkItemList(suspendedItems)) {
            hr.setSuspendedItems(suspendedItems);
            hr.setCaseSuspended();
            _log.debug("Completed suspend for all work items in case: {}", caseID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend case failed for case: {}", caseID);
            return false ;
        }
    }

    //***************************************************************************//

    /**
     * Retrieves a list of live workitems for a specified scope
     * @param scope - either case (all items in a case) or spec (all items in all case
     *                instances of that specification) or task (all workitem instances
     *                of that task)
     * @param id - the id of the case/spec/task
     * @return a list of the requested workitems
     */
    private List<WorkItemRecord> getListOfExecutingWorkItems(String scope, String id) {
        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>();

        for (WorkItemRecord wir : getLiveWorkItemsForIdentifier(scope, id)) {
            if (wir.getStatus().equals(WorkItemRecord.statusExecuting))
                result.add(wir);
        }
        return result ;
    }


    //***************************************************************************//

    /**
     * Retrieves a list of 'suspendable' workitems (ie. enabled, fired or executing)
     * for a specified scope.
     * @param scope - either case (all items in a case) or spec (all items in all case
     *                instances of that specification) or task (all workitem instances
     *                of that task)
     * @param id - the id of the case/spec/task
     * @return a list of the requested workitems
     */
    private Set<WorkItemRecord> getSuspendableWorkItems(String scope, String id) {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();
        for (WorkItemRecord wir : getLiveWorkItemsForIdentifier(scope, id)) {
            if (wir.hasLiveStatus()) result.add(wir);
        }
        return result ;
    }

    private Set<WorkItemRecord> getSuspendableWorkItems(YSpecificationID id) {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();
        for (WorkItemRecord wir : getLiveWorkItemsForSpec(id)) {
            if (wir.hasLiveStatus()) result.add(wir);
        }
        return result ;
    }

    //***************************************************************************//

    /**
     * Returns all suspendable workitems in the hierarchy of ancestor cases
     * @param caseID
     * @return the list of suspendable workitems
     */
    private Set<WorkItemRecord> getSuspendableWorkItemsInChain(String caseID) {
        Set<WorkItemRecord> result = getSuspendableWorkItems("case", caseID);

        // if parent is also a worklet, get it's list too
        while (_handlersStarted.containsKey(caseID)) {

            // get parent's caseID
            caseID = _handlersStarted.get(caseID).getCaseID() ;
            result.addAll(getSuspendableWorkItems("case", caseID));
        }
        return result;
    }

    //***************************************************************************//


    /**
     * Suspends all live workitems in all live cases for the specification passed
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    private boolean suspendAllCases(ExletRunner hr) {
        YSpecificationID specID = hr.getSpecID();
        Set<WorkItemRecord> suspendedItems = getSuspendableWorkItems(specID) ;

        if (suspendWorkItemList(suspendedItems)) {
            hr.setSuspendedItems(suspendedItems);
            hr.setCaseSuspended();
            _log.info("Completed suspend for all work items in spec: {}", specID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend all cases failed for spec: {}", specID);
            return false ;
        }
    }

    //***************************************************************************//

    /**
     * Suspends all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    private boolean suspendAncestorCases(ExletRunner runner){
        String caseID = runner.getCaseID();                    // i.e. id of parent case
        Set<WorkItemRecord> items = getSuspendableWorkItemsInChain(caseID);

        if (suspendWorkItemList(items)) {
            runner.setSuspendedItems(items);
            runner.setCaseSuspended();
            _log.info("Completed suspend for all work items in ancestor cases: {}", caseID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend all ancestor cases failed for case: {}", caseID);
            return false ;
        }
    }

    //***************************************************************************//

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
                String msg = _ixClient.cancelWorkItem(wir.getID(), null, false, _sessionHandle);
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

    //***************************************************************************//

    /**
     * Cancels the specified case
     * @param hr the HandlerRunner instance with the case to cancel
     */
    private void removeCase(ExletRunner hr) {
        String caseID =  hr.getCaseID();
        try {
            if (successful( _interfaceBClient.cancelCase(caseID, _sessionHandle))) {
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

    //***************************************************************************//

    /**
     * Cancels the specified case
     * @param caseID the id of the case to cancel
     */
    private void removeCase(String caseID) {
        try {
            if (successful(_interfaceBClient.cancelCase(caseID, _sessionHandle))) {
                _log.info("Case successfully removed from Engine: {}", caseID);
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to remove case: " + caseID, ioe);
        }
    }


    //***************************************************************************//

    /**
     * Cancels all running instances of the specification passed
     * @param specID the id of the specification to cancel
     */
    private void removeAllCases(YSpecificationID specID) {
        try {
            String casesForSpec =  _interfaceBClient.getCases(specID, _sessionHandle);
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

    //***************************************************************************//

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

    //***************************************************************************//

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

    //***************************************************************************//

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
                String msg = _ixClient.forceCompleteWorkItem(wir, data, _sessionHandle);
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

    //***************************************************************************//

    /** restarts the specified workitem */
    private void restartWorkItem(WorkItemRecord wir) {

        // ASSUMPTION: Only an 'executing' workitem may be restarted
        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            try {
                String msg = _ixClient.restartWorkItem(wir.getID(), _sessionHandle);
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

    //***************************************************************************//

    /** Cancels a workitem and marks it as failed */
    private void failWorkItem(WorkItemRecord wir) {
        try {
            // only executing items can be failed, so if its only fired or enabled, or
            // if its suspended, move it to executing first
            wir = moveToExecuting(wir);

            // ASSUMPTION: Only an 'executing' workitem may be failed
            if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
                String result = _ixClient.cancelWorkItem(wir.getID(), wir.getDataListString(),
                        true, _sessionHandle);
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

    //***************************************************************************//

    /**
     * Moves a workitem from suspended to its previous status
     * @param wir - the workitem to unsuspend
     * @return the unsuspended workitem (record)
     */
    private WorkItemRecord unsuspendWorkItem(WorkItemRecord wir) {
        WorkItemRecord result = null ;

        wir = updateWIR(wir);                    // refresh the locally cached wir

        if (wir.getStatus().equals(WorkItemRecord.statusSuspended)) {
            try {
                result = _ixClient.unsuspendWorkItem(wir.getID(), _sessionHandle);
                _log.debug("Successful work item unsuspend: {}", wir.getID());
            }
            catch (IOException ioe) {
                _log.error("Exception attempting to unsuspend workitem: " + wir.getID(),
                        ioe);
            }
        }
        else _log.error("Can't unsuspend a workitem with a status of {}", wir.getStatus());

        return result ;
    }

    //***************************************************************************//

    /** unsuspends all previously suspended workitems in this case and/or spec */
    private void unsuspendList(ExletRunner runner) {
        Set<WorkItemRecord> suspendedItems = runner.getSuspendedItems();
        if (suspendedItems != null) {
            for (WorkItemRecord wir : suspendedItems) {
                unsuspendWorkItem(wir);
            }
            _log.debug("Completed unsuspend for all suspended work items");
        }
        else _log.info("No suspended workitems to unsuspend") ;
    }


    //***************************************************************************//
    //***************************************************************************//

    /*************************/
    /* DATA UPDATING METHODS */
    /*************************/

    /**
     * Refreshes a locally cached WorkItemRecord with the Engine stored one
     * @param wir the item to refresh
     * @return the refreshed workitem, or the unchanged workitem on exception
     */
    private WorkItemRecord updateWIR(WorkItemRecord wir) {
        try {
            wir = getEngineStoredWorkItem(wir.getID(), _sessionHandle);
        }
        catch (IOException ioe){
            _log.error("IO Exception attempting to update WIR: " + wir.getID(), ioe);
        }
        return wir ;
    }

    //***************************************************************************//

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
            _ixClient.updateWorkItemData(runner.getWir(), out, _sessionHandle);
        }
        catch (IOException ioe) {
            _log.error("IO Exception calling interface X");
        }
    }

    //***************************************************************************//

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
            _ixClient.updateCaseData(runner.getCaseID(), updated, _sessionHandle);
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
                                                          _sessionHandle);
        List<YParameter> outputParams = taskInfo.getParamSchema().getOutputParams();
        try {
            return JDOMUtil.stringToElement(
                    Marshaller.filterDataAgainstOutputParams(mergedOutputData, outputParams));
        }
        catch (JDOMException jde) {
            return (in != null) ? in : out;
        }
    }
    //***************************************************************************//
    //***************************************************************************//

    /*************************/
    /* MISC. SUPPORT METHODS */
    /*************************/


    /** cancels all worklets running as exception handlers for a case when that
     *  parent case is cancelled
     */
    private void cancelLiveWorkletsForCase(CaseMonitor monitor) {
        boolean runnerFound = false;         // flag for msg if no runners
        String caseID = monitor.getCaseID();

        if (connected()) {

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
        else _log.error("Unable to connect the Exception Service to the Engine");
    }

    //***************************************************************************//

    /**
     * Moves a fired or enabled item to executing
     */
    private Set<WorkItemRecord> executeWorkItem(WorkItemRecord wir) {
        return checkOutItem(wir);
    }


    private WorkItemRecord moveToExecuting(WorkItemRecord wir) {
        if (wir.getStatus().equals(WorkItemRecord.statusSuspended))
            unsuspendWorkItem(wir);
        if (wir.getStatus().equals(WorkItemRecord.statusFired) ||
                wir.getStatus().equals(WorkItemRecord.statusEnabled)) {
            Set<WorkItemRecord> cos = executeWorkItem(wir);
            if (! cos.isEmpty()) {
                wir = cos.iterator().next();
            }
        }
        return wir;
    }


    //***************************************************************************//


    /**
     * Retrieves a List of live workitems for the case or spec id passed
     * @param idType "case" for a case's workitems, "spec" for a specification's,
     *        "task" for a specific taskID
     * @param id the identifier for the case/spec/task
     * @return the List of live workitems
     */
    private List<WorkItemRecord> getLiveWorkItemsForIdentifier(String idType, String id) {

        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>() ;

        try {
            List<WorkItemRecord> wirs =
                    _interfaceBClient.getCompleteListOfLiveWorkItems(_sessionHandle) ;

            if (wirs != null) {

                // find out which wirs belong to the specified case/spec/task
                for (WorkItemRecord wir : wirs) {
                    if ((idType.equalsIgnoreCase("case") &&
                            wir.getCaseID().equals(id)) ||
                            (idType.equalsIgnoreCase("task") &&
                                    wir.getTaskID().equals(id)))
                        result.add(wir);
                }
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to get work items for: " + id, ioe);
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }


    private List<WorkItemRecord> getLiveWorkItemsForSpec(YSpecificationID specID) {
        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>() ;
        try {
            List<WorkItemRecord> wirs =
                    _interfaceBClient.getCompleteListOfLiveWorkItems(_sessionHandle) ;

            if (wirs != null) {

                // find out which wirs belong to the specified case/spec/task
                for (WorkItemRecord wir : wirs) {
                    YSpecificationID wirSpecID = new YSpecificationID(wir);
                    if (wirSpecID.equals(specID))
                        result.add(wir);
                }
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to get work items for: " +
                    specID.toString(), ioe);
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }


    public Set<WorkletRunner> getRunningWorklets() {
        Set<WorkletRunner> workletRunners = new HashSet<WorkletRunner>();
        for (ExletRunner exletRunner : _handlersStarted.values()) {
            workletRunners.addAll(exletRunner.getWorkletRunners());
        }
        return workletRunners;
    }

    //***************************************************************************//

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
        List<WorkItemRecord> items = getLiveWorkItemsForIdentifier("task", taskID) ;
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

    //***************************************************************************//

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

    //***************************************************************************//

    /** registers this ExceptionService instance with the Engine */
    public void setupInterfaceXListener(String workletURI) {
        try {
            String uri = workletURI.replaceFirst("/ib", "/ix");
            _ixClient.addInterfaceXListener(uri);
        }
        catch (IOException ioe) {
            _log.error("Error attempting to register worklet service as " +
                    " an Interface X Listener with the engine", ioe);
        }
    }

    //***************************************************************************//

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

    //***************************************************************************//

    /**
     * Starts the specified workitem in the engine
     * @param wir - the item to start
     * @return a list of the started item's child items
     */
    private List startItem(WorkItemRecord wir) {
        String itemID = wir.getID() ;

        try {
            _ixClient.startWorkItem(itemID, _sessionHandle);

            // get all the child instances of this workitem
            return getChildren(itemID, _sessionHandle);
        }
        catch (IOException ioe) {
            _log.error("Exception starting item: " + itemID, ioe);
            return null ;
        }
    }


    //***************************************************************************//
    //***************************************************************************//

    /***************************/
    /* JSP INTERACTION METHODS */
    /***************************/

    /** returns the specified wir for the id passed */
    public WorkItemRecord getWorkItemRecord(String itemID) {
        try {
            return getEngineStoredWorkItem(itemID, _sessionHandle);
        }
        catch (IOException ioe) {
            _log.error("Exception getting WIR: " + itemID, ioe);
            return null ;
        }
    }

    //***************************************************************************//

    /** returns the spec id for the specified case id */
    public YSpecificationID getSpecIDForCaseID(String caseID) {
        CaseMonitor mon = _monitoredCases.get(getIntegralID(caseID));
        return mon.getSpecID();
    }

    //***************************************************************************//

    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified case
     * @param caseID - the id of the case to get the triggers for
     * @return the (String) list of triggers
     */
    public List getExternalTriggersForCase(String caseID) {
        if (caseID != null) {
            CaseMonitor mon = _monitoredCases.get(getIntegralID(caseID));
            if (mon != null) {
                RdrTree tree = getTree(mon.getSpecID(), null, RuleType.CaseExternalTrigger);
                return getExternalTriggers(tree) ;
            }
        }
        return null;
    }

    //***************************************************************************//

    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified workitem
     * @param itemID - the id of the item to get the triggers for
     * @return the (String) list of triggers
     */
    public List getExternalTriggersForItem(String itemID) {
        if (itemID != null) {
            WorkItemRecord wir = getWorkItemRecord(itemID);
            if (wir != null) {
                RdrTree tree = getTree(new YSpecificationID(wir), wir.getTaskName(),
                        RuleType.ItemExternalTrigger);
                return getExternalTriggers(tree) ;
            }
        }
        return null;
    }

    //***************************************************************************//

    /** Traverse the extracted conditions from all nodes of the passed RdrTree
     *  and return the external exception triggers found within them
     * @param tree - the (external exception) RdrTree containing the triggers
     *  @return the (String) list of triggers
     */
    private List<String> getExternalTriggers(RdrTree tree) {
        List<String> list = new ArrayList<String>();

        if (tree != null) {
            for (String cond : tree.getAllConditions()) {
                String trigger = getConditionValue(cond, "trigger");
                if (trigger != null) {
                    trigger = trigger.replaceAll("\"","");         // de-quote
                    list.add(trigger);
                }
            }
        }
        if (list.isEmpty()) list = null ;
        return list ;
    }

    //***************************************************************************//

    /**
     * Gets the value for the specified variable in the condition string
     * @param cond - the codition containing the value
     * @param var - the variable to get the value of
     * @return the value of the variable passed
     */
    private String getConditionValue(String cond, String var){
        String[] parts = cond.split("=");

        for (int i = 0; i < parts.length; i+=2) {
            if (parts[i].trim().equalsIgnoreCase(var))
                return parts[i+1].trim() ;
        }
        return null ;
    }

    //***************************************************************************//

    /**
     * Raise an externally triggered exception
     * @param level - the level of the exception (case/item)
     * @param id - the id of the case or item on which the exception is being raised
     * @param trigger - the identifier of (or reason for) the external exception
     */
    public void raiseExternalException(String level, String id, String trigger) {

        synchronized (mutex) {
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
                taskID = wir.getTaskName();
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


//***************************************************************************//

    /**
     *  Replaces a running worklet case with another worklet case after an
     *  amendment to the ruleset for this exception.
     *  Called by WorkletGateway after a call from the RdrEditor that the ruleset
     *  has been updated.
     *  Overrides the WorkletService equivalent - This one looks after exceptions,
     *  that one looks after selections
     *
     *  @param xType - the type of exception that launched the worklet
     *  @param caseid - the id of the orginal checked out case
     *  @param itemid - the id of the orginal checked out workitem
     *  @return a string of messages decribing the success or otherwise of
     *          the process
     */
    public String replaceWorklet(RuleType xType, String caseid, String itemid, String trigger) {
        String result ;
        WorkItemRecord wir = null;
        boolean caseLevel = xType.isCaseLevelType();
        CaseMonitor mon = _monitoredCases.get(caseid);

        _log.info("REPLACE EXECUTING WORKLET REQUEST");

        result = "Locating " +
                (caseLevel? "case '" + caseid : "workitem '" + itemid) +
                "' in the set of currently handled cases...";

        caseid = getIntegralID(caseid);

        // if case is currently being handled
        if (mon != null) {
            result += "found." + Library.newline ;
            _log.debug("Caseid received found in monitoredCases: {}", caseid);

            // get the HandlerRunner for the Exception
            ExletRunner hr = mon.getRunnerForType(xType, itemid) ;

            if (! caseLevel) wir = hr.getWir();

            // get the case ids of the running worklets for this case/workitem
            for (WorkletRunner runner : hr.getWorkletRunners()) {

                // cancel the worklet running for the case/workitem
                String workletCaseID = runner.getCaseID();
                result += "Cancelling running worklet case with case id " + workletCaseID + "...";
                _log.debug("Running worklet case id for this case/item is: {}", workletCaseID);
                removeCase(workletCaseID);

                _log.debug("Removing worklet from handlers started: {}", workletCaseID);
                _handlersStarted.remove(workletCaseID) ;

                result += "done." + Library.newline ;
            }

            // go through the selection process again
            result += "Launching new replacement worklet case(s) based on revised ruleset...";
            _log.debug("Launching new replacement worklet case(s) based on revised ruleset");

            // refresh ruleset to pickup newly added rule
            refreshRuleSet(mon.getSpecID());

            // remove monitor's runner for cancelled worklet
            mon.removeHandlerRunner(hr);
            Persister.delete(hr);

            // go through the process again, depending on the exception type
            switch (xType) {
                case CasePreconstraint : checkConstraints(mon, true); break;
                case CasePostconstraint: checkConstraints(mon, false); break;
                case ItemPreconstraint : checkConstraints(mon, wir, true); break;
                case ItemPostconstraint: checkConstraints(mon, wir, false); break;
                case ItemAbort         : break ;   // not yet implemented
                case ItemTimeout :
                    if (wir != null) handleTimeoutEvent(wir, wir.getTaskID()); break ;
                case ItemResourceUnavailable : break;   // todo
                case ItemConstraintViolation : break;   // not yet implemented
                case CaseExternalTrigger :
                    raiseExternalException("case", caseid, trigger); break;
                case ItemExternalTrigger :
                    raiseExternalException("item", caseid, trigger); break;
            }


            // get the new HandlerRunner for the new Exception
            hr = mon.getRunnerForType(xType, itemid) ;

            result += result = hr.getWorkletRunners().size() + " worklet(s) launched";
        }
        else {
            _log.warn("Case monitor not found for case: {}", caseid) ;
            result += "not found." + Library.newline +
                    "It appears that it has already completed." ;
        }
        return result ;
    }

    //***************************************************************************//

    /** returns true if case specified is a worklet instance */
    public boolean isWorkletCase(String caseID) {
        return (_handlersStarted.containsKey(caseID) || super.isWorkletCase(caseID));
    }

    //***************************************************************************//

    /** stub method called from RdrConditionFunctions class */
    public String getStatus(String taskName) {
        return null;
    }

    //***************************************************************************//
    //***************************************************************************//

    /*******************************/
    // PERSISTENCE RESTORE METHODS //
    /*******************************/

    /** restores the contents of the running datasets after a web server restart */
    private void restoreDataSets() {
        _handlersStarted = new HashMap<String, ExletRunner>();
        Map<String, ExletRunner> runners = restoreRunners() ;
        _monitoredCases = restoreMonitoredCases(runners) ;
    }

    //***************************************************************************//

    /** restores active HandlerRunner instances */
    private Map<String, ExletRunner> restoreRunners() {
        Map<String, ExletRunner> result = new HashMap<String, ExletRunner>();

        // retrieve persisted runner objects from database
        List items = _db.getObjectsForClass(ExletRunner.class.getName());

        if (items != null) {
            for (Object o : items) {
                ExletRunner runner = (ExletRunner) o;
                result.put(String.valueOf(runner.getID()), runner);
            }
        }
        return result ;
    }

    //***************************************************************************//

    /** Restores active CaseMonitor instances
     * @param runnerMap - the set of restored HandlerRunner instances
     * @return the set of restored CaseMonitor instances
     */
    private Map<String, CaseMonitor> restoreMonitoredCases(Map<String, ExletRunner> runnerMap) {
        Map<String, CaseMonitor> result = new Hashtable<String, CaseMonitor>();

        // retrieve persisted monitor objects from database
        List items = _db.getObjectsForClass(CaseMonitor.class.getName());

        if (items != null) {
            for (Object o : items) {
                CaseMonitor monitor = (CaseMonitor) o;

                // 'reattach' relevant runners to this case monitor
                if (runnerMap != null) {
                    List<ExletRunner> restoredRunners = monitor.restoreRunners(runnerMap);
                    if (restoredRunners != null) rebuildHandlersStarted(restoredRunners);
                }
                monitor.initNonPersistedItems();            // finish the reconstitution
                result.put(monitor.getCaseID(), monitor);
            }
        }
        return result ;
    }

    //***************************************************************************//

    /** add the runners with active worklet instances to handlersStarted */
    private void rebuildHandlersStarted(List<ExletRunner> runners) {
        for (ExletRunner runner : runners) {
            if (runner.hasRunningWorklet()) {
                for (WorkletRunner wRunner : runner.getWorkletRunners()) {
                    _handlersStarted.put(wRunner.getCaseID(), runner);
                }
            }
        }
    }


    /********************************************************************************/
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
