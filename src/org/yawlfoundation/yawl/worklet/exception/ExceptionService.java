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
import org.yawlfoundation.yawl.worklet.support.EventLogger;
import org.yawlfoundation.yawl.worklet.support.Persister;
import org.yawlfoundation.yawl.worklet.support.StateRestorer;
import org.yawlfoundation.yawl.worklet.support.WorkletSpecification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

//    private Map<String, CaseMonitor> _monitoredCases =
//            new HashMap<String, CaseMonitor>() ;      // cases monitored for exceptions
    private static Logger _log ;                        // debug log4j file
    private static ExceptionService _me ;               // reference to self
    private final ExceptionActions _actions;
    private final ExletRunnerCache _runners;
//    private final CaseStartEventMap _caseStartEvents;
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
        _runners = new ExletRunnerCache();
//        _caseStartEvents = new CaseStartEventMap();
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

            // cancel any/all running worklets for this case
            cancelWorkletsForCase(caseID);

            // if this case was a worklet running for another case, process
            // the worklet cancellation
            if (_runners.isCompensationWorklet(caseID)) {
                handleCompletingExceptionWorklet(caseID, null, true);
            }
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
     * @param data the current case data (i.e. immediately prior to item start or
     *             after item completion)
     * @param preCheck true for pre-constraints, false for post-constraints
     */
    public void handleCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                   boolean preCheck){
        synchronized (_mutex) {

            String caseID = getIntegralID(wir.getCaseID());
//            if (_caseStartEvents.isCasePreCheckComplete(caseID)) {

                _log.info("HANDLE CHECK WORKITEM CONSTRAINT EVENT");
                _log.info("Checking {}-constraints for workitem: {}",
                        preCheck ? "pre" : "post", wir.getID());
                checkConstraints(wir, augmentItemData(wir, data), preCheck);
//            }
//            else _caseStartEvents.addCheckWorkItemConstraintEvent(wir, data, preCheck);
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

            Element eData = JDOMUtil.stringToElement(data);
            if (preCheck) {
                _log.info("Checking constraints for start of case {} " +
                        "(of specification: {})", caseID, specID);

                checkConstraints(specID, caseID, eData, true);
//                _caseStartEvents.addCasePreCheckComplete(caseID);
//
//                // if check item event received before case initialised, call it now
//                WorkItemConstraintData constraintData =
//                        _caseStartEvents.getCheckWorkItemConstraintEvent(caseID);
//                if (constraintData != null) {
//                    handleCheckWorkItemConstraintEvent(constraintData.getWIR(),
//                            constraintData.getData(), constraintData.getPreCheck());
//                }
            }
            else {                                                      // end of case

                _log.info("Checking constraints for end of case {}", caseID);
                checkConstraints(specID, caseID, eData, false);

                // treat this as a case complete event for exception worklets also
                if (_runners.isCompensationWorklet(caseID)) {
                    handleCompletingExceptionWorklet(caseID, eData, false);
                }
//                _caseStartEvents.caseCompleted(caseID);
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


    private String handleItemException(WorkItemRecord wir, String dataStr,
                                       RuleType ruleType) {
        String msg;
        YSpecificationID specID = new YSpecificationID(wir);
        Element data = augmentItemData(wir, dataStr);

        // get the exception handler for this task (if any)
        RdrPair pair = getExceptionHandler(specID, wir.getTaskID(), data, ruleType);

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null) {
            msg = "No " + ruleType.toLongString() + " rules defined for workitem: " +
                    wir.getTaskID();
        }
        else {
            if (! pair.hasNullConclusion()) {                // we have a handler
                msg = ruleType.toLongString() + " exception raised for work item: " +
                        wir.getID();
                raiseException(wir, pair, data, ruleType);
            }
            else {                      // there are rules but the item passes
                msg = "Workitem '" + wir.getID() + "' has passed " +
                        ruleType.toLongString() + " rules.";
            }
        }
        _log.info(msg);
        return StringUtil.wrap(msg, "result");
    }

    //***************************************************************************//

    /**
     * Checks for case-level constraint violations.
     *
     * @param pre true for pre-constraints, false for post-constraints
     */
    private void checkConstraints(YSpecificationID specID, String caseID,
                                  Element data, boolean pre) {
        String sType = pre ? "pre" : "post";
        RuleType xType = pre ? RuleType.CasePreconstraint : RuleType.CasePostconstraint;

        // get the exception handler that would result from a constraint violation
        RdrPair pair = getExceptionHandler(specID, null, data, xType) ;

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null) {
            _log.info("No {}-case constraints defined for spec: {}", sType, specID);
        }
        else {
            if (! pair.hasNullConclusion()) {                // there's been a violation
                _log.info("Case {} failed {}-case constraints", caseID, sType);
                raiseException(specID, caseID, pair, data, xType) ;
            }
            else {                                // there are rules but the case passes
                getServer().announceConstraintPass(caseID, data, xType);
                _log.info("Case {} passed {}-case constraints", caseID, sType);
            }
        }
    }


    /**
     * Checks for item-level constraint violations.
     *
     * @param wir the WorkItemRecord for the workitem
     * @param pre true for pre-constraints, false for post-constraints
     */
    private void checkConstraints(WorkItemRecord wir, Element data, boolean pre) {
        String itemID = wir.getID();
        String taskID = wir.getTaskID();
        YSpecificationID specID = new YSpecificationID(wir);
        String sType = pre? "pre" : "post";
        RuleType xType = pre? RuleType.ItemPreconstraint : RuleType.ItemPostconstraint;

        // get the exception handler that would result from a constraint violation
        RdrPair pair = getExceptionHandler(specID, taskID, data, xType) ;

        // if pair is null there's no rules defined for this type of constraint
        if (pair == null) {
            _log.info("No {}-task constraints defined for task: {}", sType, taskID);
        }
        else {
            if (! pair.hasNullConclusion()) {                // there's been a violation
                _log.info("Workitem {} failed {}-task constraints", itemID, sType);
                raiseException(wir, pair, data, xType) ;
            }
            else {                                // there are rules but the case passes
                getServer().announceConstraintPass(wir, data, xType);
                _log.info("Workitem {} passed {}-task constraints", itemID, sType);
            }
        }
    }


    /**
     * Discovers whether this case or item has rules for this exception type, and if so,
     * returns the result of the rule evaluation. Note that if the conclusion
     * returned from the search is empty, no exception has occurred.
     *
     * @param taskID item's task id, or null for case-level exception
     * @param xType the type of exception triggered
     * @return an RdrConclusion representing an exception handling process,
     *         or null if no rules are defined for these criteria
     */
    private RdrPair getExceptionHandler(YSpecificationID specID, String taskID,
                                        Element data, RuleType xType) {
        return _rdr.evaluate(specID, taskID, data, xType);
    }


    /**
     * Raises a case-level exception by creating a HandlerRunner for the exception
     * process, then starting the processing of it
     *
     * @param pair represents the exception handling process
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
    private void raiseException(YSpecificationID specID, String caseID, RdrPair pair,
                                Element data, RuleType xType) {
        _log.debug("Invoking exception handling process for Case: {}", caseID);
        getServer().announceException(caseID, data, pair.getLastTrueNode(), xType);
        raiseException(new ExletRunner(specID, caseID, pair.getConclusion(), xType));
    }


    /**
     * Raises an item-level exception - see above for more info
     * @param pair represents the exception handling process
     * @param wir the WorkItemRecord of the item that triggered the event
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
    private void raiseException(WorkItemRecord wir, RdrPair pair, Element data,
                                RuleType xType){
        _log.debug("Invoking exception handling process for item: {}", wir.getID());
        getServer().announceException(wir, data, pair.getLastTrueNode(), xType);
        raiseException(new ExletRunner(wir, pair.getConclusion(), xType));
    }


    private void raiseException(ExletRunner runner) {
        _runners.add(runner);
        if (_persisting) {
            Persister.insert(runner);
        }
        processException(runner) ;
    }


    /**
     * Begin (or continue after a worklet completes) the exception handling process
     *
     * @param runner the HandlerRunner for this handler process
     */
    private void processException(ExletRunner runner) {
        boolean doNextStep = true;

        while (runner.hasNextAction() && doNextStep) {
            doNextStep = doAction(runner);           // becomes false if worklets started
            runner.incActionIndex();                 // move to next primitive
        }

        // if no more actions to do (or worklets) in runner, remove it
        if (! (runner.hasNextAction() || runner.hasRunningWorklet())) {
            _runners.remove(runner);
            Persister.delete(runner);
        }
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
                    forceCompleteWorkItem(runner.getWir(), runner.getWorkItemUpdatedData());
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
            case AncestorCases : _actions.suspendAncestorCases(_runners, runner);
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
        ExletRunner runner = _runners.getRunnerForWorklet(caseId);
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
            _log.error("Failed to remove workitem '{}': {}", wir.getID(), ioe.getMessage());
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
//                if (hr.getRuleType() == RuleType.CasePreconstraint) {
//                    _caseStartEvents.caseCancelled(caseID);
//                }

                _log.info("Case successfully removed from Engine: {}", caseID);
            }
        }
        catch (IOException ioe) {
            _log.error("Failed to remove case '{}': {}", caseID, ioe.getMessage());
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
            _log.error("Failed to remove case '{}': {}", caseID, ioe.getMessage());
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
            _log.error("Failed to remove all cases for specification '{}': {}",
                    specID.toString(), ioe.getMessage());
        }
    }


    /**
     * Cancels all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    private void removeAncestorCases(ExletRunner runner){
        String caseID = getFirstAncestorCase(runner);

        _log.info("The ultimate parent case of this worklet has an id of: {}", caseID);
        _log.info("Removing all child compensatory worklets of case: {}", caseID);

        cancelWorkletsForCase(caseID);
        removeCase(caseID);                                // remove ultimate parent
    }


    /** returns the ultimate ancestor case of the runner passed */
    private String getFirstAncestorCase(ExletRunner runner) {
        String parentCaseID = null;         // i.e. id of parent case

        // if the caseid is a started worklet handler, get it's runner
        while (runner != null) {
            parentCaseID = runner.getCaseID();
            runner = _runners.getRunnerForWorklet(parentCaseID);
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
        Element out = updateDataList(runner.getWorkItemDatalist(), wlData) ;

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
        try {

            // get engine copy of case data
            Element in = getCaseData(runner.getCaseID());

            // update data values as required
            Element updated = updateDataList(in, wlData);

            // and copy that back to the engine
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
    private void cancelWorkletsForCase(String caseID) {
        boolean runnerFound = false;         // flag for msg if no runners

        // iterate through all the live runners for this case
        for (ExletRunner runner : _runners.getRunnersForCase(caseID)) {

            // if this runner has live worklet(s), cancel it/them
            if (runner.hasRunningWorklet()) {
                _log.info("Removing compensatory worklet(s) " +
                        " for cancelled parent case: {}", caseID);
            }

            // cancel each worklet case of this runner
            for (WorkletRunner worklet : runner.getWorkletRunners()) {

                // recursively call this method for each child worklet (in case
                // they also have worklets running)
                String workletID = runner.getCaseID();
                cancelWorkletsForCase(workletID);

                _log.info("Worklet case running for the cancelled parent case " +
                        "has id of: {}", workletID) ;

                EventLogger.log(EventLogger.eCancel, workletID,
                        worklet.getWorkletSpecID(), "", caseID, -1) ;

                removeCase(workletID);
                runner.removeWorklet(worklet);
                runnerFound = true;
            }

            // unpersist the runner if its a 'top-level' parent
            _runners.remove(runner);
            if (_persisting && ! isWorkletCase(caseID))
                Persister.delete(runner);
        }
        if (! runnerFound) {
            _log.info("No compensatory worklets running for cancelled case: {}", caseID);
        }
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
        return _runners.getAllWorklets();
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
        return _engineClient.getSpecificationIDForCase(caseID);
    }


    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified case
     * @param caseID - the id of the case to get the triggers for
     * @return the (String) list of triggers
     */
    public List getExternalTriggersForCase(String caseID) {
        YSpecificationID specID = getSpecIDForCaseID(caseID);
        if (specID != null) {
            RdrTree tree = _rdr.getTree(specID, null, RuleType.CaseExternalTrigger);
            return _rdr.getExternalTriggers(tree) ;
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
            YSpecificationID specID;
            WorkItemRecord wir = null;
            RuleType xLevel ;

            if (level.equalsIgnoreCase("case")) {                // if case level
                caseID = id;
                specID = getSpecIDForCaseID(caseID);
                taskID = null;
                xLevel = RuleType.CaseExternalTrigger;
            }
            else {                                               // else item level
                wir = getWorkItemRecord(id);
                specID = new YSpecificationID(wir);
                caseID = wir.getRootCaseID();
                taskID = wir.getTaskID();
                xLevel = RuleType.ItemExternalTrigger;
            }

            // add trigger value to case data
            Element eData = augmentExternalData(getCaseData(caseID), trigger);

            // get the exception handler for this trigger
            RdrPair pair = getExceptionHandler(specID, taskID, eData, xLevel);

            // if pair is null there's no rules defined for this type of constraint
            if (pair == null) {
                _log.error("No external exception rules defined for spec: {}" +
                        ". Unable to raise exception for '{}'", specID, trigger);
            }
            else if (wir == null) {
                raiseException(specID, caseID, pair, eData, xLevel);
            }
            else {
                raiseException(wir, pair, eData, xLevel);
            }
        }
    }


    private Element getCaseData(String caseID) {
        String data;
        try {
             data = _engineClient.getCaseData(caseID);
        }
        catch (IOException ioe) {
             data = "<caseData/>";
        }
        return JDOMUtil.stringToElement(data);
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
        RdrNode dummyNode = new RdrNode(-1);
        dummyNode.setConclusion(conclusion);
        RdrPair dummyPair = new RdrPair(dummyNode, dummyNode);
        raiseException(wir, dummyPair, null, ruleType);
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
    public String replaceWorklet(RuleType xType, String caseid, String itemid,
                                 String trigger)
            throws IOException {
        WorkItemRecord wir = null;
        boolean caseLevel = xType.isCaseLevelType();

        _log.info("REPLACE EXECUTING WORKLET REQUEST");

        caseid = getIntegralID(caseid);

        // get the HandlerRunner for the Exception
        ExletRunner runner = _runners.getRunner(xType, caseid, itemid);
        if (runner == null) {
            raise("No compensatory worklets running for case: " + caseid);
        }

        // get the case ids of the running worklets for this case/workitem
        for (WorkletRunner worklet : runner.getWorkletRunners()) {

            // cancel the worklet running for the case/workitem
            String workletCaseID = worklet.getCaseID();
            removeCase(workletCaseID);
            _log.debug("Removed worklet case: {}", workletCaseID);
            _log.debug("Removing worklet from handlers started: {}", workletCaseID);
        }

        // go through the selection process again
        _log.debug("Launching new replacement worklet case(s) based on revised ruleset");

        // remove monitor's runner for cancelled worklet
        _runners.remove(runner);
        Persister.delete(runner);

        YSpecificationID specID = getSpecIDForCaseID(caseid);
        Element data = getCaseData(caseid);

        // go through the process again, depending on the exception type
        if (! caseLevel) wir = runner.getWir();
        switch (xType) {
            case CasePreconstraint : checkConstraints(specID, caseid, data, true); break;
            case CasePostconstraint: checkConstraints(specID, caseid, data, false); break;
            case ItemPreconstraint : checkConstraints(wir, data, true); break;
            case ItemPostconstraint: checkConstraints(wir, data, false); break;
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
        runner = _runners.getRunner(xType, caseid, itemid) ;
        return getRunnerCaseIdList(runner.getWorkletRunners());
    }


    /** returns true if case specified is a worklet instance */
    public boolean isWorkletCase(String caseID) {
        return (_runners.isCompensationWorklet(caseID) || super.isWorkletCase(caseID));
    }


    /** stub method called from RdrConditionFunctions class */
    public String getStatus(String taskID) {
        return null;
    }


    /** restores the contents of the running datasets after a web server restart */
    private void restoreDataSets() {
        StateRestorer restorer = new StateRestorer();
        _runners.restore();
 //       _monitoredCases = restorer.restoreMonitoredCases(_handlersStarted);
    }


    /**
     * Adds the contents of the workitem record to the case data for this case - thus
     * providing information about the workitem to the ruleset
     * @param wir - the wir being tested for an exception
     */
    private Element augmentItemData(WorkItemRecord wir, String dataStr) {
        Element data = JDOMUtil.stringToElement(dataStr);

        //convert the wir contents to an Element
        Element eWir = JDOMUtil.stringToElement(wir.toXML()).detach();

        Element eInfo = new Element("process_info");     // new Element for info
        eInfo.addContent(eWir);                          // add the wir
        data.addContent(eInfo);                          // add element to case data
        return data;
    }


    /**
     *  Adds an external exception trigger to the case data so that the correct
     *  RDR can be found for it
     * @param triggerValue - the string value of the external trigger
     */
    private Element augmentExternalData(Element data, String triggerValue) {

        // all external triggers must be delimited with "'s
        if (! triggerValue.startsWith("\""))
            triggerValue = "\"" + triggerValue + "\"" ;

        Element eTrigger = new Element("trigger");        // new Element for trigger
        eTrigger.addContent(triggerValue);                // add the text
        data.addContent(eTrigger);                   // add element to case data
        return data;
    }

} // end of ExceptionService class
