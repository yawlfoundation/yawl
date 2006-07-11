/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */
package au.edu.qut.yawl.worklet.exception;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.util.JDOMConversionTools;
import au.edu.qut.yawl.engine.interfce.interfaceX.*;

import au.edu.qut.yawl.worklet.rdr.*;
import au.edu.qut.yawl.worklet.WorkletService;
import au.edu.qut.yawl.worklet.selection.CheckedOutItem;
import au.edu.qut.yawl.worklet.support.EventLogger;
import au.edu.qut.yawl.worklet.support.DBManager;

import java.io.IOException;
import java.util.*;

import org.jdom.*;
import org.apache.log4j.Logger;

/**
 *  The ExceptionService class manages the handling of exceptions that may occur
 *  during the life of a case instance. It receives events from the engine via
 *  InterfaceX at various milestones for constraint checking, and when certain
 *  exceptional events occur. It runs exception handlers when required, which may
 *  involve the running of compensatory worklets. It is derived from the
 *  WorkletService class and uses many of the same methods as those used in the
 *  worklet selection process.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 04/07/2006
 */


public class ExceptionService extends WorkletService implements InterfaceX_Service {

    private Map<String,HandlerRunner> _handlersStarted =
            new HashMap<String, HandlerRunner>() ;      // running exception worklets
    private Map<String,CaseMonitor> _monitoredCases =
            new HashMap<String, CaseMonitor>() ;       // cases monitored for exceptions
    private static Logger _log ;                        // debug log4j file
    private InterfaceX_ServiceSideClient _ixClient ;    // interface client to engine
    private static ExceptionService _me ;               // reference to self
    private WorkItemConstraintData _pushedItemData;     // see 'pushWIConstraintEvent'
    private String _exceptionURI = "http://localhost:8080/workletService/ix" ;
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
        _ixClient = new InterfaceX_ServiceSideClient(_engineURI + "/ix");
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.exception.ExceptionService");
        setUpInterfaceBClient(_engineURI + "/ib");
        _me = this ;
        registerExceptionService(this);                 // register service with parent
        if (_persisting) restoreDataSets();             // reload running cases data
    }

    public static ExceptionService getInst() {
        return _me ;
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
                else _log.info("No current exception handlers for case " + caseID);

                completeCaseMonitoring(monitor, caseID);
            }
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
             _log.info("HANDLE CHECK WORKITEM CONSTRAINT EVENT");

             String caseID = getIntegralID(wir.getCaseID());

             CaseMonitor monitor = _monitoredCases.get(caseID);
             if (connected()) {
                 if (monitor != null) {

                    monitor.updateData(data);               // update case monitor's data

                    String itemID = wir.getID();
                    String sType = preCheck ? "pre" : "post";
                    _log.info("Checking " + sType + "-constraints for workitem: " + itemID);

                    checkConstraints(monitor, wir, preCheck);
                    if (preCheck)
                        monitor.addLiveItem(getDecompID(wir));  // flag this item as active
                    else
                        monitor.removeLiveItem(getDecompID(wir));  // remove item flag
                    destroyMonitorIfDone(monitor, caseID);
                }
                else pushCheckWorkItemConstraintEvent(wir, data, preCheck);
             }
             else _log.error("Unable to connect the Exception Service to the Engine");
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
    public void handleCheckCaseConstraintEvent(String specID, String caseID, String data,
                                               boolean preCheck) {
        synchronized (mutex) {

            _log.info("HANDLE CHECK CASE CONSTRAINT EVENT");        // note to log

            CaseMonitor monitor;     // object that maintains info of each monitored case

            if (connected()) {
               if (preCheck) {
                    _log.info("Checking constraints for start of case " + caseID +
                            " (of specification: " + specID + ")");

                    // create new monitor for case - will live until case completes
                    monitor = new CaseMonitor(specID, caseID, data);
                    _monitoredCases.put(caseID, monitor);
                    if (_persisting) _dbMgr.persist(monitor, DBManager.DB_INSERT);
                    checkConstraints(monitor, preCheck);
                    if (_pushedItemData != null) popCheckWorkItemConstraintEvent(monitor) ;
               }
               else {                                                      // end of case
                   _log.debug("Checking constraints for end of case " + caseID);

                   // get the monitor for this case
                   monitor = _monitoredCases.get(caseID);
                   checkConstraints(monitor, preCheck);
                   monitor.setCaseCompleted();

                   // treat this as a case complete event for exception worklets also
                   if (_handlersStarted.containsKey(caseID))
                       handleCompletingExceptionWorklet(caseID,
                                                     JDOMConversionTools.stringToElement(data));

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
                String caseID = getIntegralID(wir.getCaseID());
                String specID = wir.getSpecificationID();
                CaseMonitor monitor = _monitoredCases.get(caseID);

                // split task list into individual taskids
                taskList = taskList.substring(1, taskList.lastIndexOf(']')); // remove [ ]
                String[] tList = taskList.split(", ");

                // for each parallel task in the time out set
                for (String taskName : tList) {

                    // get workitem record for this task & add it to the monitor's data
                    List<WorkItemRecord> wirs = getWorkItemRecordsForTaskInstance(specID, taskName);
                    if (wirs != null) {
                        WorkItemRecord wirt = wirs.get(0);
                        monitor.addProcessInfo(wirt);

                        // get the exception handler for this time out for this task (if any)
                        String taskID = getDecompID(specID, taskName);
                        RdrConclusion conc = getExceptionHandler(monitor, taskID, XTYPE_TIMEOUT);

                        // if conc is null there's no rules defined for this type of constraint
                        if (conc == null)
                            _log.info("No time-out exception handler defined for task: " + taskID);
                        else {
                            if (! conc.nullConclusion())                 // we have a handler
                                raiseException(monitor, conc, wirt, XTYPE_TIMEOUT);
                            else                       // there are rules but the item passes
                                _log.info("Nothing to do for this TimeOut Event");
                        }
                        monitor.removeProcessInfo();
                    } else _log.info("No live work item found for task: " + taskName);
                }
            }
            else _log.error("Unable to connect the Exception Service to the Engine");
        }
    }


    /** Interface Event handlers yet to be implemented */

    public void handleWorkItemAbortException(WorkItemRecord wir) {}

    public void handleResourceUnavailableException(WorkItemRecord wir) {}

    public void handleConstraintViolationException(WorkItemRecord wir) {}

    //***************************************************************************//
    //***************************************************************************//

    /*********************************************/
    /* RULE CHECKING & EXCEPTION RAISING METHODS */
    /*********************************************/

    /**
     * Checks for case-level constraint violations
     *
     * @param monitor the CaseMonitor for this case
     * @param pre true for pre-constraints, false for post-constraints
     */
    private void checkConstraints(CaseMonitor monitor, boolean pre ) {
        RdrConclusion conc ;       // the conclusion from a set of rules, if any
        String sType = pre ? "pre" : "post";
        int xType = pre ? XTYPE_CASE_PRE_CONSTRAINTS : XTYPE_CASE_POST_CONSTRAINTS;

        // get the exception handler that would result from a constraint violation
        conc = getExceptionHandler(monitor, null, xType) ;

        // if conc is null there's no rules defined for this type of constraint
        if (conc == null)
            _log.info("No " + sType + "-case constraints defined for spec: " +
                          monitor.getSpecID());
        else {
           if (! conc.nullConclusion()) {                // there's been a violation
              _log.info("Case " + monitor.getCaseID() + " failed " + sType +
                        "-case constraints");
              raiseException(monitor, conc, sType, xType) ;
           }
            else                                 // there are rules but the case passes
               _log.info("Case " + monitor.getCaseID() + " passed " + sType +
                        "-case constraints");
        }
    }

    //***************************************************************************//

    /**
     * Checks for item-level constraint violations
     *
     * @param monitor the CaseMonitor for the case this item is a member of
     * @param wir the WorkItemRecord for the workitem
     * @param pre true for pre-constraints, false for post-constraints
     */
    private void checkConstraints(CaseMonitor monitor, WorkItemRecord wir, boolean pre ) {
        RdrConclusion conc ;       // the conclusion from a set of rules, if any
        String itemID = wir.getID();
        String taskID = getDecompID(wir) ;
        String sType = pre? "pre" : "post";
        int xType = pre? XTYPE_ITEM_PRE_CONSTRAINTS : XTYPE_ITEM_POST_CONSTRAINTS;

        // get the exception handler that would result from a constraint violation
        conc = getExceptionHandler(monitor, taskID, xType) ;

        // if conc is null there's no rules defined for this type of constraint
        if (conc == null)
            _log.info("No " + sType + "-task constraints defined for task: " + taskID);
        else {
           if (! conc.nullConclusion()) {                    // there's been a violation
              _log.info("Workitem " + itemID + " failed " + sType +  "-task constraints");
              raiseException(monitor, conc, wir, xType) ;
           }
            else                                   // there are rules but the case passes
               _log.info("Workitem " + itemID + " passed " + sType + "-task constraints");
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
     private RdrConclusion getExceptionHandler(CaseMonitor monitor, String taskID, int xType){
         RdrTree tree = getTree(monitor.getSpecID(), taskID, xType);
         if (tree != null) {
            RdrConclusion conc = new RdrConclusion(tree.search(monitor.getCaseData()));
            conc.setLastPair(tree.getLastPair());
            return conc ;
         }
         else return null ;
     }

    //***************************************************************************//

    /**
     * Raises a case-level exception by creating a HandlerRunner for the exception
     * process, then starting the processing of it
     *
     * @param cmon the CaseMonitor for the case that 'owns' the exception
     * @param conc represents the exception handling process
     * @param sType the type of exception triggered (as a string)
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
     private void raiseException(CaseMonitor cmon, RdrConclusion conc, String sType,
                                  int xType){
         if (connected()) {
             _log.debug("Invoking exception handling process for Case: " + cmon.getCaseID());
             HandlerRunner hr = new HandlerRunner(cmon, conc, xType) ;
             cmon.addHandlerRunner(hr, sType);
             if (_persisting) _dbMgr.persist(hr, DBManager.DB_INSERT);
             processException(hr) ;
         }
         else _log.error("Could not connect to YAWL Engine to handle Exception") ;
    }

    //***************************************************************************//

    /**
     * Raises an item-level exception - see above for more info
     * @param cmon the CaseMonitor for the case that 'owns' the exception
     * @param conc represents the exception handling process
     * @param wir the WorkItemRecord of the item that triggered the event
     * @param xType the int descriptor of the exception type (WorkletService xType)
     */
    private void raiseException(CaseMonitor cmon, RdrConclusion conc,
                                WorkItemRecord wir, int xType){
        if (connected()) {
            _log.debug("Invoking exception handling process for item: " + wir.getID());
            HandlerRunner hr = new HandlerRunner(cmon, wir, conc, xType) ;
            cmon.addHandlerRunner(hr, wir.getID());
            if (_persisting) _dbMgr.persist(hr, DBManager.DB_INSERT);
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
    private void processException(HandlerRunner hr) {
        boolean doNextStep = true;
        while (hr.hasNextAction() && (doNextStep)) {
            doNextStep = doAction(hr);               // becomes false if worklet started
            hr.incActionIndex();                     // move to next primitive
        }

        CaseMonitor mon = hr.getOwnerCaseMonitor();

        // if no more actions to do in runner, remove it from the monitor (& the db)
        if (! hr.hasNextAction()) {
            mon.removeHandlerRunner(hr);
            if (_persisting) _dbMgr.persist(hr, DBManager.DB_DELETE);
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
     private boolean doAction(HandlerRunner runner){
         String action = runner.getNextAction();
         String target = runner.getNextTarget();

         _log.debug("Exception process step " + runner.getActionIndex() + ". Action = " +
                    action + ", Target = " + target);

         if (action.equalsIgnoreCase("continue"))
             doContinue(runner) ;                                           // un-suspend
         else if (action.equalsIgnoreCase("suspend"))
             doSuspend(runner) ;
         else if (action.equalsIgnoreCase("remove"))
             doRemove(runner) ;                                             // cancel
         else if (action.equalsIgnoreCase("restart")) {
             if (target.equalsIgnoreCase("workitem"))
                 restartWorkItem(runner.getItem());
             else {
                 _log.error("Unexpected target type '" + target +
                            "' for exception handling primitive '" + action + "'") ;
                 return false ;
             }
         }
         else if (action.equalsIgnoreCase("complete")) {
             if (target.equalsIgnoreCase("workitem"))
                 forceCompleteWorkItem(runner.getItem(), runner.getItem().getWorkItemData());
             else {
                 _log.error("Unexpected target type '" + target +
                            "' for exception handling primitive '" + action + "'") ;
                 return false ;
             }
         }
         else if (action.equalsIgnoreCase("fail")) {
             if (target.equalsIgnoreCase("workitem"))
                 failWorkItem(runner.getItem());
             else {
                 _log.error("Unexpected target type '" + target +
                            "' for exception handling primitive '" + action + "'") ;
                 return false ;
             }
         }
         else if (action.equalsIgnoreCase("compensate")) {

             // launch & run compensatory worklet
             String workletName = runner.getNextTarget();
             runner.setWorkletName(workletName);
             if (uploadWorklet(workletName)) {
                 String caseID = launchWorklet(runner, false) ;
                 if (caseID != null) {
                     _handlersStarted.put(caseID, runner) ;            // exception case
                     runner.saveSearchResults();
                     return false ;            // to break out of loop while worklet runs
                 }
                 else _log.error("Unable to load compensatory worklet, will ignore: " +
                                  workletName);
             }
         }
         else if (action.equalsIgnoreCase("rollback")) {
            // todo
            _log.warn("Rollback is not yet implemented - will ignore this step.");
         }
         else  {
            _log.error("Unknown action type in exception handling primitive: " + action);
            return false ;
         }
         return true ;                    // successful processing of exception primitive
     }

    //***************************************************************************//

    /**
     * Calls the appropriate continue method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doContinue(HandlerRunner runner) {
        String target = runner.getNextTarget();
        if (target.equalsIgnoreCase("workitem")) {
            unsuspendList(runner);
            runner.unsetItemSuspended();
        }
        else if ((target.equalsIgnoreCase("case")) ||
                 (target.equalsIgnoreCase("allcases"))) {
            unsuspendList(runner);
            runner.unsetCaseSuspended();
        }
        else _log.error("Unexpected target type '" + target +
                   "' for exception handling primitive 'continue'") ;
    }

    //***************************************************************************//

    /**
     * Calls the appropriate suspend method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doSuspend(HandlerRunner runner) {
        String target = runner.getNextTarget();
        if (target.equalsIgnoreCase("workitem"))
            suspendWorkItem(runner);
        else if (target.equalsIgnoreCase("case"))
           suspendCase(runner);
        else if (target.equalsIgnoreCase("allcases"))
            suspendAllCases(runner);
        else _log.error("Unexpected target type '" + target +
                   "' for exception handling primitive 'suspend'") ;
    }

    //***************************************************************************//

    /**
     * Calls the appropriate remove method for the exception target scope
     *
     * @param runner the HandlerRunner stepping through this exception process
     */
    private void doRemove(HandlerRunner runner) {
        String target = runner.getNextTarget();
        if (target.equalsIgnoreCase("workitem"))
            removeWorkItem(runner.getItem());
        else if (target.equalsIgnoreCase("case"))
            removeCase(runner.getCaseID());
        else if (target.equalsIgnoreCase("ancestorCases"))
            removeAncestorCases(runner);
        else if (target.equalsIgnoreCase("allcases"))
            removeAllCases(runner.getSpecID());
        else _log.error("Unexpected target type '" + target +
                   "' for exception handling primitive 'remove'") ;
    }

    //***************************************************************************//

     /**
      * Deals with the end of an exception worklet case.
      *  @param caseId - the id of the completing case
      *  @param wlCasedata - the completing case's datalist Element
      */
      private void handleCompletingExceptionWorklet(String caseId, Element wlCasedata) {

          // get the HandlerRunner that launched this worklet
          HandlerRunner runner = _handlersStarted.get(caseId);
         _log.debug("Worklet ran as exception handler for case: " + runner.getCaseID());

         // remove it from the running Handlers
         _handlersStarted.remove(caseId);

         /** Update data of parent workitem/case if allowed and required
          * ASSUMPTION: the output data of the worklet will be used to update the
          * case/item only if:
          *    - it is a pre-case exception and the case is suspended, in which case
          *      the net-level data is updated; or
          *    - it is a pre-item exception and the item is suspended, in which case
          *      the item-level data is updated; or
          *    - it is an item exception and the case is suspended, in which case the
          *       net-level *and* item-level data is updated
          */
         CaseMonitor monitor = runner.getOwnerCaseMonitor();

         // commented out while suspending enabled tasks is investigated
//         if (monitor.isCaseRunner(runner)) {
//             if (runner.isCaseSuspended()) updateCaseData(runner, wlCasedata);
//         }
//         else if (monitor.isItemRunner(runner)) {
//             runner.setItem(updateWIR(runner.getItem())) ;  // update the wir contents
//             if (runner.isItemSuspended()) updateItemData(runner, wlCasedata) ;
//             if (runner.isCaseSuspended()) updateCaseData(runner, wlCasedata);
//         }

         // this is the cut down version of the above commented code
         if (monitor.isCaseRunner(runner)) {
             updateCaseData(runner, wlCasedata);
         }
         else if (monitor.isItemRunner(runner)) {
             _log.info("$$$$$ item before update: " + runner.getItem().toXML());
             runner.setItem(updateWIR(runner.getItem())) ;  // update the wir contents
             _log.info("$$$$$ item after update: " + runner.getItem().toXML());
             updateItemData(runner, wlCasedata) ;
             updateCaseData(runner, wlCasedata);
         }

         runner.removeRunningCaseId();            // worklet's case id no longer needed

         // log the worklet's case completion event
         EventLogger.log(_dbMgr, EventLogger.eComplete, caseId, runner.getWorkletName(),
                                          "", runner.getCaseID(), -1) ;
         // process the next exception primitive
         processException(runner);
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
    private boolean suspendWorkItem(HandlerRunner hr) {
        WorkItemRecord wir = hr.getItem();
        ArrayList<WorkItemRecord> children = new ArrayList<WorkItemRecord>();

        // ASSUMPTION: Only an 'executing' workitem may be suspended
        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            children.add(wir);                          // put item in list for next call
            if (suspendWorkItemList(children)) {        // suspend the item (list)
                hr.setItemSuspended();                  // record the action
                hr.setItem(updateWIR(wir));             // refresh the stored wir
                hr.setSuspendedList(children);          // record the suspended item
                return true ;
            }
        }
        else
            _log.error("Can't suspend a workitem with a status of " + wir.getStatus());

        return false ;
    }

    //***************************************************************************//

    /**
     * Suspends each workitem in the list of items passed
     * @param items - items a list of workitems to suspend
     * @return true if all were successfully suspended
     */
    private boolean suspendWorkItemList(List<WorkItemRecord> items) {
        String itemID = "";

        try {
            for (WorkItemRecord item : items) {
                itemID = item.getID();
                if (! successful(_interfaceBClient.suspendWorkItem(itemID, _sessionHandle)))
                    throw new IOException();
                _log.debug("Successful work item suspend: " + itemID);
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
        List<WorkItemRecord> suspendedItems = getListOfExecutingWorkItems("case", caseID) ;
        if (suspendWorkItemList(suspendedItems)) {
            _log.debug("Completed suspend for case: " + caseID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend case failed for case: " + caseID);
            return false ;
        }
     }

    //***************************************************************************//

    /**
     * Suspends all live workitems in the specified case
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    private boolean suspendCase(HandlerRunner hr) {
        String caseID = hr.getCaseID();
        List<WorkItemRecord> suspendedItems = getListOfExecutingWorkItems("case", caseID) ;

        if (suspendWorkItemList(suspendedItems)) {
            hr.setSuspendedList(suspendedItems);
            hr.setCaseSuspended();
            _log.debug("Completed suspend for all work items in case: " + caseID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend case failed for case: " + caseID);
            return false ;
        }
    }

    //***************************************************************************//

    /**
     * Retreives a list of live workitems for a specified scope
     * @param scope - either case (all items in a case) or spec (all items in all case
     *                instances of that specification) or task (all workitem instances
     *                of that task)
     * @param id - the id of the case/spec/task
     * @return a list of the requested workitems
     */
    private List<WorkItemRecord> getListOfExecutingWorkItems(String scope, String id) {
        List<WorkItemRecord> liveItems = getLiveWorkItemsForIdentifier(scope, id) ;
        ArrayList<WorkItemRecord> result = new ArrayList<WorkItemRecord>();

        for (WorkItemRecord wir : liveItems) {
            if (wir.getStatus().equals(WorkItemRecord.statusExecuting))
                result.add(wir);
        }
        return result ;
    }


    //***************************************************************************//

    /**
     * Suspends all live workitems in all live cases for the specification passed
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    private boolean suspendAllCases(HandlerRunner hr) {
        String specID = hr.getSpecID();
        List<WorkItemRecord> suspendedItems = getListOfExecutingWorkItems("spec", specID) ;

        if (suspendWorkItemList(suspendedItems)) {
            hr.setSuspendedList(suspendedItems);
            hr.setCaseSuspended();
            _log.info("Completed suspend for all work items in spec: " + specID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend all cases failed for spec: " + specID);
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
            // only executing items can be removed, so if its only fired or enabled,
            // move it to executing first
            if (wir.getStatus().equals(WorkItemRecord.statusFired) ||
                wir.getStatus().equals(WorkItemRecord.statusEnabled)) {
                CheckedOutItem coi = executeWorkItem(wir);
                wir = coi.getChildWorkItem(0);
            }

            _ixClient.cancelWorkItem(wir.getID(), false, _sessionHandle);
            _log.info("WorkItem successfully removed from Engine: " + wir.getID());

        }
        catch (IOException ioe) {
            _log.error("Exception attempting to remove workitem: " + wir.getID(), ioe);
        }
    }

    //***************************************************************************//

    /**
     * Cancels the specified case
     * @param caseID the case to cancel
     */
    private void removeCase(String caseID) {
        try {
            _interfaceBClient.cancelCase(caseID, _sessionHandle);
            if (_monitoredCases.containsKey(caseID))
               (_monitoredCases.get(caseID)).setCaseCompleted();
            _log.info("Case successfully removed from Engine: " + caseID);
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
    private void removeAllCases(String specID) {
        try {
            String casesForSpec =  _interfaceBClient.getCases(specID, _sessionHandle);
            Element eCases = JDOMConversionTools.stringToElement(casesForSpec);
            List<Element> caseList = eCases.getChildren();

            for (Element aCase : caseList) {
                removeCase(aCase.getText());
            }
         }
        catch (IOException ioe) {
            _log.error("Exception attempting to all cases for specification: " + specID, ioe);
        }
    }

    //***************************************************************************//

    /**
     * Cancels all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    private void removeAncestorCases(HandlerRunner runner){
        String caseID = runner.getCaseID();                // i.e. id of parent case

        removeCase(caseID);                                // cancel parent case

        // if parent is also a worklet, remove it too
        if (_handlersStarted.containsKey(caseID)) {
             _log.debug("recursing removeAncestorCases with caseId = " + caseID);
             removeAncestorCases(_handlersStarted.get(caseID)) ;
             _handlersStarted.remove(caseID);
        }
    }

    //***************************************************************************//

    /**
     * ForceCompletes the specified workitem
     * @param wir the item to ForceComplete
     * @param data the final data params for the workitem
     */
    private void forceCompleteWorkItem(WorkItemRecord wir, Element data) {

        // only executing items can complete, so if its only fired or enabled,
        // move it to executing first
        if (wir.getStatus().equals(WorkItemRecord.statusFired) ||
            wir.getStatus().equals(WorkItemRecord.statusEnabled)) {
            CheckedOutItem coi = executeWorkItem(wir);
            wir = coi.getChildWorkItem(0);
            data = wir.getWorkItemData();
        }

        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            try {
                _ixClient.forceCompleteWorkItem(wir, data, _sessionHandle);
                _log.info("Item successfully force completed: " + wir.getID());
            }
            catch (IOException ioe) {
               _log.error("Exception attempting complete workitem: " + wir.getID(), ioe);
            }
        }
        else _log.error("Can't force complete a workitem with a status of " + wir.getStatus());
    }

    //***************************************************************************//

    /** restarts the specified workitem */
    private void restartWorkItem(WorkItemRecord wir) {

        // ASSUMPTION: Only an 'executing' workitem may be restarted
        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
           try {
               _ixClient.restartWorkItem(wir.getID(), _sessionHandle);
           }
           catch (IOException ioe) {
               _log.error("Exception attempting restart workitem: " + wir.getID(), ioe);
           }
       }
    }

    //***************************************************************************//

    /** Cancels a workitem and marks it as failed */
    private void failWorkItem(WorkItemRecord wir) {
        _log.info("failWorkItem, wir 1 = " + wir.toXML());
        try {
            // only executing items can be removed, so if its only fired or enabled,
            // move it to executing first
            if (wir.getStatus().equals(WorkItemRecord.statusFired) ||
                wir.getStatus().equals(WorkItemRecord.statusEnabled)) {
                CheckedOutItem coi = executeWorkItem(wir);
                wir = coi.getChildWorkItem(0);
            }

            _ixClient.cancelWorkItem(wir.getID(), true, _sessionHandle);
            _log.debug("WorkItem successfully failed: " + wir.getID());

        }
        catch (IOException ioe) {
            _log.error("Exception attempting to fail workitem: " + wir.getID(), ioe);
        }
    }

    //***************************************************************************//

    /**
     * Moves a workitem from 'fired' to executing
     * @param wir - the workitem to unsuspend
     * @return the unsuspended workitem (record)
     */
    private WorkItemRecord unsuspendWorkItem(WorkItemRecord wir) {
        WorkItemRecord result = null ;

        // ASSUMPTION: Only an 'fired' workitem may be unsuspended
        if (wir.getStatus().equals(WorkItemRecord.statusFired)) {
            try {
                result = _ixClient.continueWorkItem(wir.getID(), _sessionHandle);
                _log.debug("Successful work item unsuspend: " + wir.getID());
            }
            catch (IOException ioe) {
                _log.error("Exception attempting to unsuspend workitem: " + wir.getID(),
                                                                                    ioe);
            }
        }
        else _log.error("Can't unsuspend a workitem with a status of " + wir.getStatus());

        return result ;
    }

    //***************************************************************************//

    /** unsuspends all previously suspended workitems in this case and/or spec */
    private void unsuspendList(HandlerRunner runner) {

        List<WorkItemRecord> suspendedItems = runner.getSuspendedList();

        if (suspendedItems != null) {
            for (WorkItemRecord wir : suspendedItems) {
                runner.setItem(unsuspendWorkItem(wir));
            }
           _log.debug("Completed unsuspend for all suspended work items");
        }
        else _log.info("No suspended workitems to unsuspend") ;
    }


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
        catch (JDOMException jde){
            _log.error("JDOM Exception attempting complete workitem: " + wir.getID(), jde);

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
    private void updateItemData(HandlerRunner runner, Element wlData) {

        // update the items datalist with corresponding values from the worklet
        Element out = updateDataList(runner.getDatalist(), wlData) ;

        // copy the updated list to the (locally cached) wir
        runner.getItem().setDataList(out);

        // and copy that back to the engine
        try {
            _ixClient.updateWorkItemData(runner.getItem(), wlData, _sessionHandle);
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
    private void updateCaseData(HandlerRunner runner, Element wlData) {

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
            ArrayList<HandlerRunner> list = monitor.getHandlerRunners();
            for (HandlerRunner hr : list) {

                // if this runner has a live worklet, cancel it
                if (hr.hasRunningWorklet()) {
                    _log.info("Removing exception handling worklet case: " +
                            hr.getRunningCaseId() + " for cancelled case: " + caseID);

                    // cancel the worklet (will call this method recursively)
                    removeCase(hr.getRunningCaseId());
                    _handlersStarted.remove(hr.getRunningCaseId());

                    EventLogger.log(_dbMgr, EventLogger.eCancel, hr.getRunningCaseId(),
                            hr.getWorkletName(), "", caseID, -1);

                    runnerFound = true;
                }
                // unpersist the runner
                if (_persisting) _dbMgr.persist(hr, DBManager.DB_DELETE);
            }
           if (! runnerFound) _log.info("No worklets running for cancelled case: "
                                        + caseID);
        }
        else _log.error("Unable to connect the Exception Service to the Engine");
   }

    //***************************************************************************//

    /**
     * Moves a fired or enabled item to executing
     */
    private CheckedOutItem executeWorkItem(WorkItemRecord wir) {
        CheckedOutItem coItem = checkOutParentItem(wir) ;
        checkOutChildren(coItem) ;	          // always at least one child
        return coItem ;
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

         ArrayList<WorkItemRecord> result = new ArrayList<WorkItemRecord>() ;

         try {
             List wirs = _interfaceBClient.getCompleteListOfLiveWorkItems(_sessionHandle) ;
             if (wirs != null){

                 // find out which wirs belong to the specified case/spec/task
                 for (Object obj : wirs) {
                     WorkItemRecord wir = (WorkItemRecord) obj;
                     if ((idType.equalsIgnoreCase("spec") &&
                             wir.getSpecificationID().equals(id)) ||
                             (idType.equalsIgnoreCase("case") &&
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
         catch (JDOMException jde) {
             _log.error("JDOM Exception attempting to get work items for: " + id, jde);
         }
         if (result.isEmpty()) result = null ;
         return result ;
     }

    //***************************************************************************//

    /**
     * Retrieves a list of all workitems that are instances of the specified task
     * within the specified spec
     * @param specID
     * @param taskID
     * @return the list of workitems
     */
    private List<WorkItemRecord> getWorkItemRecordsForTaskInstance(String specID, String taskID) {

        // get all the live work items that are instances of this task
        List<WorkItemRecord> items = getLiveWorkItemsForIdentifier("task", taskID) ;

        if (items != null) {

            // filter those for this spec
            for (WorkItemRecord wir : items) {
                if (! wir.getSpecificationID().equals(specID))
                    items.remove(wir);
            }
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
     private void registerThisAsExceptionObserver() {
         try {
            _ixClient.setExceptionObserver(_exceptionURI);
         }
         catch (IOException ioe) {
             _log.error("Error attempting to register worklet service as " +
                        " an Exception Observer with the engine", ioe);
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
         if ((! _handlersStarted.containsKey(caseID)) && monitor.isDone()) {
             completeCaseMonitoring(monitor, caseID);
         }
     }

    /** completes case monitoring by performing housekeeping for the (completed) case */
     private void completeCaseMonitoring(CaseMonitor monitor, String caseID) {
         _monitoredCases.remove(caseID);
         if (_persisting) _dbMgr.persist(monitor, DBManager.DB_DELETE);
         _log.info("Exception monitoring complete for case " + caseID);
     }

    //***************************************************************************//

    /**
     * Starts the specified workitem in the engine
     * @param wir - the item to start
     * @return a list of the satarted item's child items
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
        catch (JDOMException jde) {
            _log.error("Exception getting WIR: " + itemID, jde);
           return null ;
        }
    }

    //***************************************************************************//

    /** returns the spec id for the specified case id */
    public String getSpecIDForCaseID(String caseID) {
        CaseMonitor mon = _monitoredCases.get(getIntegralID(caseID));
        return mon.getSpecID();
    }

    //***************************************************************************//

    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified case
      * @param caseID - the id of the case to get the triggers for
     * @return the (String) list of triggers
     */
    public List<String> getExternalTriggersForCase(String caseID) {
        CaseMonitor mon = _monitoredCases.get(getIntegralID(caseID));
        RdrTree tree = getTree(mon.getSpecID(), null, XTYPE_CASE_EXTERNAL_TRIGGER);

        return getExternalTriggers(tree) ;
    }

    //***************************************************************************//

    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified workitem
      * @param itemID - the id of the item to get the triggers for
     * @return the (String) list of triggers
     */
    public List<String> getExternalTriggersForItem(String itemID) {
        WorkItemRecord wir = getWorkItemRecord(itemID);
        RdrTree tree = getTree(wir.getSpecificationID(), getDecompID(wir),
                               XTYPE_ITEM_EXTERNAL_TRIGGER);
        return getExternalTriggers(tree) ;
    }

    //***************************************************************************//

    /** Traverse the extracted conditions from all nodes of the passed RdrTree
     *  and return the external exception triggers found within them
      * @param tree - the (external exception) RdrTree containing the triggers
     *  @return the (String) list of triggers
     */
    private List<String> getExternalTriggers(RdrTree tree) {
        ArrayList<String> list = new ArrayList<String>();
        String trigger ;

        if (tree != null) {
            for (String cond : tree.getAllConditions()) {
                trigger = getConditionValue(cond, "trigger");
                if (trigger != null) {
                    trigger = trigger.replaceAll("\"", "");         // de-quote
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

            if (level.equalsIgnoreCase("case")) {                // if case level
                caseID = id;
                taskID = null;
            }
            else {                                               // else item level
                wir = getWorkItemRecord(id);
                caseID = wir.getCaseID();
                taskID = wir.getTaskID();
            }

            // get case monitor for this case
            CaseMonitor monitor = _monitoredCases.get(caseID);

            monitor.addTrigger(trigger);                 // add trigger value to case data

            // get the exception handler for this trigger
            RdrConclusion conc = getExceptionHandler(monitor, taskID, XTYPE_CASE_EXTERNAL_TRIGGER);

            // if conc is null there's no rules defined for this type of constraint
            if (conc == null)
                _log.error("No external rules defined for spec: " + monitor.getSpecID() +
                        ". Unable to raise exception for '" + trigger + "'");
            else if (wir == null)
                raiseException(monitor, conc, "external", XTYPE_CASE_EXTERNAL_TRIGGER);
            else
                raiseException(monitor, conc, wir, XTYPE_ITEM_EXTERNAL_TRIGGER);

            monitor.removeTrigger();
        }
    }

    //***************************************************************************//

    /** returns true if case specified is a worklet instance */
    public boolean isWorkletCase(String caseID) {
       return (_handlersStarted.containsKey(caseID) || super.isWorkletCase(caseID)) ;
    }

    //***************************************************************************//

    /** stub method called from RDRConditionFunction class */
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
        _handlersStarted = new HashMap<String, HandlerRunner>();
        HashMap<String,HandlerRunner> runners = restoreRunners() ;
        _monitoredCases = restoreMonitoredCases(runners) ;
    }

    //***************************************************************************//

    /** restores active HandlerRunner instances */
    private HashMap<String,HandlerRunner> restoreRunners() {
        HashMap<String,HandlerRunner> result = new HashMap<String, HandlerRunner>();

        // retrieve persisted runner objects from database
        List<HandlerRunner> items = _dbMgr.getObjectsForClass(HandlerRunner.class.getName());

        if (items != null) {
            for (HandlerRunner runner : items) {
                runner.initNonPersistedItems();            // finish the reconstitution
                String id = String.valueOf(runner.get_id());
                result.put(id, runner);
            }
       }
       return result ;
   }

    //***************************************************************************//

    /** Restores active CaseMonitor instances
     * @param runnerMap - the set of restored HandlerRunner instances
     * @return the set of restored CaseMonitor instances
     */
    private HashMap<String,CaseMonitor> restoreMonitoredCases(HashMap<String,HandlerRunner> runnerMap) {
        HashMap<String,CaseMonitor> result = new HashMap<String, CaseMonitor>();

        // retrieve persisted monitor objects from database
        List<CaseMonitor> items = _dbMgr.getObjectsForClass(CaseMonitor.class.getName());

        if (items != null) {
            for (CaseMonitor monitor : items) {

                // 'reattach' relevant runners to this case monitor
                if (runnerMap != null) {
                    List<HandlerRunner> restoredRunners = monitor.restoreRunners(runnerMap);
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
    private void rebuildHandlersStarted(List<HandlerRunner> runners) {
        for (HandlerRunner runner : runners) {
            if (runner.hasRunningWorklet())
                _handlersStarted.put(runner.getRunningCaseId(), runner);
        }
    }


    /********************************************************************************/
    /********************************************************************************/

    // PUSHING & POPPING INITIAL WORKITEM CONSTRAINT EVENT //

    /**
     * There is no defined ordering of constraint events - a workitem check constraint
     * event is sometimes received before a start-of-case check constraint event. Since
     * a CaseMonitor object is required by a workitem check, and created by a start-of-
     * case event, we must ensure that the start-of-case chewck is always done first. Also,
     * pre-case constraint violations may cause a case cancellation, so that a check
     * item pre-constraint event may come after a case has been cancelled.
     * In the situation where the woritem event is received first, it is stored until
     * the case event has been processed (via the push method below), then called (via
     *  the pop method) immediately after the case check has completed.
     */

    /** stores the data passed to the event for later processing */
    private void pushCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                  boolean preCheck) {
        _pushedItemData = new WorkItemConstraintData(wir, data, preCheck);
    }

    /** retreives the data passed to the initial event and recalls it */
    private void popCheckWorkItemConstraintEvent(CaseMonitor mon) {
        if (mon != null)
           handleCheckWorkItemConstraintEvent(_pushedItemData.getWIR(),
                                              _pushedItemData.getData(),
                                              _pushedItemData.getPreCheck());
        else _log.info("Case cancelled: check workitem constraint event ignored.");
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
