/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.worklet.exception;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.*;

import org.yawlfoundation.yawl.worklet.rdr.*;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.selection.CheckedOutItem;
import org.yawlfoundation.yawl.worklet.support.*;

import java.io.IOException;
import java.util.*;

import org.jdom.*;
import org.apache.log4j.Logger;

/**
 *  The ExceptionService class manages the handling of exceptions that may occur
 *  during the life of a case instance. It receives events from the engine via
 *  InterfaceX at various milestones for constraint checking, and when certain
 *  exceptional events occur. It runs exception handlers when required, which may
 *  involve the running of compensatory worklets. It derives from the
 *  WorkletService class and uses many of the same methods as those used in the
 *  worklet selection process.
 *
 *  Here's the class hierarchy for the ExceptionService:
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
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */


public class ExceptionService extends WorkletService implements InterfaceX_Service {

    private Map _handlersStarted = new HashMap() ;      // running exception worklets
    private Map _monitoredCases = new HashMap() ;       // cases monitored for exceptions
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
        _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.exception.ExceptionService");
        setUpInterfaceBClient(_engineURI + "/ib");
        _me = this ;
        registerExceptionService(this);                 // register service with parent
    }


    public static ExceptionService getInst() {
        return _me ;
    }


    // called from servlet WorkletGateway after contexts are loaded
    public void completeInitialisation() {
        super.completeInitialisation();
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
                CaseMonitor monitor = (CaseMonitor) _monitoredCases.get(caseID);

                // cancel any/all running worklets for this case
                if (monitor.hasLiveHandlerRunners())
                    cancelLiveWorkletsForCase(monitor);
                else _log.info("No current exception handlers for case " + caseID);

                completeCaseMonitoring(monitor, caseID);
            }
            else _log.info("Case monitoring complete for case " + caseID +
                           " - cancellation event ignored.");
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
             CaseMonitor monitor = (CaseMonitor) _monitoredCases.get(caseID);

             if (connected()) {
                 if (monitor != null) {
                     _log.info("HANDLE CHECK WORKITEM CONSTRAINT EVENT");
                     if (! monitor.isPreCaseCancelled()) {
                         monitor.updateData(data);         // update case monitor's data

                         String sType = preCheck ? "pre" : "post";
                         _log.info("Checking " + sType + "-constraints for workitem: " +
                                    wir.getID());

                         checkConstraints(monitor, wir, preCheck);

                         if (preCheck)
                             monitor.addLiveItem(getDecompID(wir));  // flag item as active
                         else
                             monitor.removeLiveItem(getDecompID(wir));  // remove item flag

                         destroyMonitorIfDone(monitor, caseID);
                     }
                     else {
                         _log.info("Case cancelled: check workitem constraint event ignored.");
                         completeCaseMonitoring(monitor, monitor.getCaseID());
                     }
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

                   // if check item event received before case initialised, call it now
                    if (_pushedItemData != null)
                        popCheckWorkItemConstraintEvent(monitor) ;
               }
               else {                                                      // end of case
                   _log.debug("Checking constraints for end of case " + caseID);

                   // get the monitor for this case
                   monitor = (CaseMonitor) _monitoredCases.get(caseID);
                   checkConstraints(monitor, preCheck);
                   monitor.setCaseCompleted();

                   // treat this as a case complete event for exception worklets also
                   if (_handlersStarted.containsKey(caseID))
                       handleCompletingExceptionWorklet(caseID,
                                              JDOMUtil.stringToElement(data));

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
                CaseMonitor monitor = (CaseMonitor) _monitoredCases.get(caseID);

                // split task list into individual taskids
                taskList = taskList.substring(1, taskList.lastIndexOf(']')); // remove [ ]
                String[] tList = taskList.split(", ");

                // for each parallel task in the time out set
                for (int i = 0; i < tList.length; i++) {

                    // get workitem record for this task & add it to the monitor's data
                    List wirs = getWorkItemRecordsForTaskInstance(specID, tList[i]);
                    if (wirs != null) {
                        WorkItemRecord wirt = (WorkItemRecord) wirs.get(0);
                        monitor.addProcessInfo(wirt);

                        // get the exception handler for this time out for this task (if any)
                        String taskID = getDecompID(specID, tList[i]);
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
                    }
                    else _log.info("No live work item found for task: " + tList[i]);
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
             if (_persisting) {
                 _dbMgr.persist(hr, DBManager.DB_INSERT);
                 hr.ObjectPersisted();
             }
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
            if (_persisting) {
                _dbMgr.persist(hr, DBManager.DB_INSERT);
                hr.ObjectPersisted();
            }
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

        while (hr.hasNextAction() && doNextStep) {
            doNextStep = doAction(hr);               // becomes false if worklets started
            hr.incActionIndex();                     // move to next primitive
        }

        CaseMonitor mon = hr.getOwnerCaseMonitor();

        // if no more actions to do in runner, remove it from the monitor (& the db)
        // except if the runner has worklets running (only necessary if the last prim.
        // in the sequence is a compensatory task)
        if (! hr.hasNextAction() && ! hr.hasRunningWorklet()) {
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
             String workletList = runner.getNextTarget();
             if (launchWorkletList(runner, workletList)) {
                 runner.saveSearchResults();
                 return false ;            // to break out of loop while worklet runs
             }
             else _log.error("Unable to load compensatory worklet(s), will ignore: " +
                                  workletList);
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
     * Launches each of the worklets listed in the hr for starting
     * @param hr - the worklet record containing the list of worklets to launch
     * @return true if *any* of the worklets are successfully launched
     */
     protected boolean launchWorkletList(HandlerRunner hr, String list) {
        String[] wNames = list.split(",");
        boolean launchSuccess = false;

        // for each worklet listed in the conclusion (in case of multiple worklets)
        for (int i=0; i < wNames.length; i++) {

            // load spec & launch case as substitute for checked out workitem
   		    if (uploadWorklet(wNames[i])) {
	           String caseID = launchWorklet(hr, wNames[i], false) ;
               if (caseID != null) {
                   _handlersStarted.put(caseID, hr) ;
                   launchSuccess = true ;
               }
            }
        }
        return launchSuccess ;
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
            runner.setItem(unsuspendWorkItem(runner.getItem()));     // refresh item            
            runner.unsetItemSuspended();
        }
        else if ((target.equalsIgnoreCase("case")) ||
                 (target.equalsIgnoreCase("allcases")) ||
                 (target.equalsIgnoreCase("ancestorCases"))) {
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
        else if (target.equalsIgnoreCase("ancestorCases"))
            suspendAncestorCases(runner);
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
            removeCase(runner);
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
          HandlerRunner runner = (HandlerRunner) _handlersStarted.get(caseId);
         _log.debug("Worklet ran as exception handler for case: " + runner.getCaseID());

         // remove it from the running Handlers
         _handlersStarted.remove(caseId);

         /** Update data of parent workitem/case if allowed and required
          * ASSUMPTION: the output data of the worklet will be used to update the
          * case/item only if:
          *   1. it is a case level exception and the case has been suspended, in
          *      which case the case-level data is updated; or
          *   2. it is an pre-executing item level exception and the item is suspended,
          *      in which case the case-level data is updated (because the item has not
          *      yet received data values from the case before starting); or
          *   3. it is an exectuing item exception and the item is suspended, in which
          *      case the item-level data is updated
          */

         if (runner.isCaseSuspended() || runner.isItemSuspended())
                updateCaseData(runner, wlCasedata);

         if (runner.isItemSuspended() && isExecutingItemException(runner.getReasonType()))
                updateItemData(runner, wlCasedata);

         // log the worklet's case completion event
         EventLogger.log(_dbMgr, EventLogger.eComplete, caseId, runner.getWorkletName(caseId),
                                          "", runner.getCaseID(), -1) ;

         runner.removeRunnerByCaseID(caseId);       // worklet's case id no longer needed

         // if all worklets have completed, process the next exception primitive
         if (! runner.hasRunningWorklet()) {
             _log.info("All compensatory worklets have completed - " +
                       "continuing exception processing");
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
    private boolean suspendWorkItem(HandlerRunner hr) {
        WorkItemRecord wir = hr.getItem();
        ArrayList children = new ArrayList();

        if (wir.hasLiveStatus()) {
            children.add(wir);                          // put item in list for next call
            if (suspendWorkItemList(children)) {        // suspend the item (list)
                hr.setItemSuspended();                  // record the action
                hr.setItem(updateWIR(wir));             // refresh the stored wir
                children.set(0, hr.getItem());          // ... and the list
                hr.setSuspendedList(children);          // record the suspended item
                return true ;
            }
        }
        else
            _log.error("Can't suspend a workitem with a status of " + wir.getStatus());

        return false ;
    }

    public boolean suspendWorkItem(String itemID) {
        WorkItemRecord wir = getWorkItemRecord(itemID);
        ArrayList children = new ArrayList();

        if (wir.hasLiveStatus()) {
            children.add(wir);                          // put item in list for next call
            if (suspendWorkItemList(children)) {        // suspend the item (list)
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
    private boolean suspendWorkItemList(List items) {
        WorkItemRecord item ;
        String itemID = "";

        try {
            Iterator itr = items.iterator();
            while (itr.hasNext()) {
                item = (WorkItemRecord) itr.next();
                itemID = item.getID();
                if (! successful(_interfaceBClient.suspendWorkItem(itemID, _sessionHandle)))
                    throw new IOException() ;
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
        List suspendItems = getListOfSuspendableWorkItems("case", caseID) ;
        if (suspendWorkItemList(suspendItems)) {
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
        List suspendedItems = getListOfSuspendableWorkItems("case", caseID) ;

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
     * Retrieves a list of live workitems for a specified scope
     * @param scope - either case (all items in a case) or spec (all items in all case
     *                instances of that specification) or task (all workitem instances
     *                of that task)
     * @param id - the id of the case/spec/task
     * @return a list of the requested workitems
     */
    private List getListOfExecutingWorkItems(String scope, String id) {
        WorkItemRecord wir;
        List liveItems = getLiveWorkItemsForIdentifier(scope, id) ;
        ArrayList result = new ArrayList();

        Iterator itr = liveItems.iterator();
        while (itr.hasNext()) {
            wir = (WorkItemRecord) itr.next() ;
            if (wir.getStatus().equals(WorkItemRecord.statusExecuting))
                result.add(wir);
        }
        return result ;
    }


    //***************************************************************************//

    /**
     * Retrieves a list of suspendable workitems (ie. enabled, fired or executing)
     * for a specified scope.
     * @param scope - either case (all items in a case) or spec (all items in all case
     *                instances of that specification) or task (all workitem instances
     *                of that task)
     * @param id - the id of the case/spec/task
     * @return a list of the requested workitems
     */
    private List getListOfSuspendableWorkItems(String scope, String id) {
        WorkItemRecord wir;
        List liveItems = getLiveWorkItemsForIdentifier(scope, id) ;
        ArrayList result = new ArrayList();

        Iterator itr = liveItems.iterator();
        while (itr.hasNext()) {
            wir = (WorkItemRecord) itr.next() ;
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
    private List getListOfSuspendableWorkItemsInChain(String caseID) {
        List result = getListOfSuspendableWorkItems("case", caseID);

        // if parent is also a worklet, get it's list too
        while (_handlersStarted.containsKey(caseID)) {

            // get parent's caseID
            caseID = ((HandlerRunner) _handlersStarted.get(caseID)).getCaseID() ;
            result.addAll(getListOfSuspendableWorkItems("case", caseID));
        }
        return result;
    }

    //***************************************************************************//


    /**
     * Suspends all live workitems in all live cases for the specification passed
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    private boolean suspendAllCases(HandlerRunner hr) {
        String specID = hr.getSpecID();
        List suspendedItems = getListOfSuspendableWorkItems("spec", specID) ;

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
     * Suspends all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    private boolean suspendAncestorCases(HandlerRunner runner){
        String caseID = runner.getCaseID();                    // i.e. id of parent case
        List items = getListOfSuspendableWorkItemsInChain(caseID);

        if (suspendWorkItemList(items)) {
            runner.setSuspendedList(items);
            runner.setCaseSuspended();
            _log.info("Completed suspend for all work items in ancestor cases: " + caseID);
            return true ;
        }
        else {
            _log.error("Attempt to suspend all ancestor cases failed for case: " + caseID);
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
            if (wir.getStatus().equals(WorkItemRecord.statusSuspended))
               unsuspendWorkItem(wir);
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
     * @param hr the HandlerRunner instance with the case to cancel
     */
    private void removeCase(HandlerRunner hr) {
        String caseID =  hr.getCaseID();
        try {
            if (successful( _interfaceBClient.cancelCase(caseID, _sessionHandle))) {
                if (_monitoredCases.containsKey(caseID)) {
                     if (hr.getReasonType() == XTYPE_CASE_PRE_CONSTRAINTS) {
                        CaseMonitor mon = (CaseMonitor) _monitoredCases.get(caseID);
                        mon.setPreCaseCancellationFlag();
                     }
                }
                _log.info("Case successfully removed from Engine: " + caseID);
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
                _log.info("Case successfully removed from Engine: " + caseID);
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
    private void removeAllCases(String specID) {
        try {
            String casesForSpec =  _interfaceBClient.getCases(specID, _sessionHandle);
            Element eCases = JDOMUtil.stringToElement(casesForSpec);
            List caseList = eCases.getChildren();

            Iterator itr = caseList.iterator();
            while (itr.hasNext()){
                Element aCase = (Element) itr.next() ;
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
        String caseID = getFirstAncestorCase(runner);
        CaseMonitor mon = (CaseMonitor) _monitoredCases.get(caseID);

        _log.info("The ultimate parent case of this worklet has an id of: " + caseID);
        _log.info("Removing all child worklets of case: " + caseID);

        cancelLiveWorkletsForCase(mon);

        removeCase(caseID);                                // remove ultimate parent
    }

    //***************************************************************************//

    /** returns the ultimate ancestor case of the runner passed */
    private String getFirstAncestorCase(HandlerRunner runner) {
        String parentCaseID = runner.getCaseID();         // i.e. id of parent case

        // if the caseid is a started worklet handler, get it's runner
        while (_handlersStarted.containsKey(parentCaseID)) {
            runner = (HandlerRunner) _handlersStarted.get(parentCaseID);
            parentCaseID = runner.getCaseID();
        }
        return parentCaseID ;
    }

    //***************************************************************************//

    /**
     * ForceCompletes the specified workitem
     * @param wir the item to ForceComplete
     * @param data the final data params for the workitem
     */
    private void forceCompleteWorkItem(WorkItemRecord wir, Element data) {

        // only executing items can complete, so if its only fired or enabled, or
        // if its suspended, move it to executing first
        if (wir.getStatus().equals(WorkItemRecord.statusSuspended))
           unsuspendWorkItem(wir);
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
       else _log.error("Can't restart a workitem with a status of " + wir.getStatus());
    }

    //***************************************************************************//

    /** Cancels a workitem and marks it as failed */
    private void failWorkItem(WorkItemRecord wir) {
        try {
            // only executing items can be failed, so if its only fired or enabled, or
            // if its suspended, move it to executing first
            if (wir.getStatus().equals(WorkItemRecord.statusSuspended))
               unsuspendWorkItem(wir);
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
        List suspendedItems = runner.getSuspendedList();

        if (suspendedItems != null) {
           Iterator itr = suspendedItems.iterator();
           while (itr.hasNext()) {
               WorkItemRecord wir = (WorkItemRecord) itr.next() ;
//               wir = updateWIR(wir);                    // refresh the stored wir
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
            _ixClient.updateWorkItemData(runner.getItem(), out, _sessionHandle);
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
        HandlerRunner hr ;
        boolean runnerFound = false;         // flag for msg if no runners
        String caseID = monitor.getCaseID();
        boolean isWorklet = isWorkletCase(caseID);

        if (connected()) {

            // iterate through all the live runners for this case
            Iterator itr = monitor.getHandlerRunners().iterator();
            while (itr.hasNext()) {
                hr = (HandlerRunner) itr.next();

                // if this runner has live worklet(s), cancel it/them
                if (hr.hasRunningWorklet()) {
                    _log.info("Removing exception handling worklet case(s) " +
                              hr.getCaseMapAsCSVList().get("caseIDs") +
                              " for cancelled parent case: " + caseID);

                    // cancel each worklet case of this runner
                   String caseIdToCancel ;
                   Iterator ritr = hr.getRunningCaseIds().iterator();
                   while (ritr.hasNext()) {
                       caseIdToCancel = (String) ritr.next() ;   // a worklet case id

                       // recursively call this method for each child worklet (in case
                       // they also have worklets running)
                       CaseMonitor mon = (CaseMonitor) _monitoredCases.get(caseIdToCancel);
                       if ((mon != null) && mon.hasLiveHandlerRunners())
                           cancelLiveWorkletsForCase(mon);

                       _log.info("Worklet case running for the cancelled parent case " +
                                 "has id of: " + caseIdToCancel) ;

                       EventLogger.log(_dbMgr, EventLogger.eCancel, caseIdToCancel,
                                        hr.getWorkletName(caseIdToCancel), "", caseID, -1) ;

                       removeCase(caseIdToCancel);
                       _handlersStarted.remove(caseIdToCancel);
                       runnerFound = true;
                   }
               }

                // unpersist the runner if its a 'top-level' parent
                monitor.removeHandlerRunner(hr);
                if (_persisting && ! isWorklet) _dbMgr.persist(hr, DBManager.DB_DELETE);
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


    /** returns true if the exception type passed occurs for an executing workitem */
    private boolean isExecutingItemException(int xType) {
        return (xType == XTYPE_WORKITEM_ABORT) || (xType == XTYPE_TIMEOUT) ||
               (xType == XTYPE_CONSTRAINT_VIOLATION) ||
               (xType == XTYPE_ITEM_EXTERNAL_TRIGGER);
    }

    public boolean isCaseLevelException(int xType) {
        return (xType == XTYPE_CASE_PRE_CONSTRAINTS) ||
               (xType == XTYPE_CASE_POST_CONSTRAINTS) ||
               (xType == XTYPE_CASE_EXTERNAL_TRIGGER) ;
    }

    //***************************************************************************//


    /**
     * Retrieves a List of live workitems for the case or spec id passed
     * @param idType "case" for a case's workitems, "spec" for a specification's,
     *        "task" for a specific taskID
     * @param id the identifier for the case/spec/task
     * @return the List of live workitems
     */
    private List getLiveWorkItemsForIdentifier(String idType, String id) {

         ArrayList result = new ArrayList() ;

         try {
             List wirs = _interfaceBClient.getCompleteListOfLiveWorkItems(_sessionHandle) ;
             if (wirs != null){

                 // find out which wirs belong to the specified case/spec/task
                 Iterator itr = wirs.iterator();
                 while (itr.hasNext()) {
                     WorkItemRecord wir = (WorkItemRecord) itr.next() ;
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
    private List getWorkItemRecordsForTaskInstance(String specID, String taskID) {

        // get all the live work items that are instances of this task
        List items = getLiveWorkItemsForIdentifier("task", taskID) ;

        if (items != null) {

            // filter those for this spec
            Iterator itr = items.iterator();
            while (itr.hasNext()) {
                WorkItemRecord wir = (WorkItemRecord) itr.next() ;
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
         if ((! _handlersStarted.containsKey(caseID)) && monitor.isDone() &&
             (! monitor.isPreCaseCancelled()))
                 completeCaseMonitoring(monitor, caseID);
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
        catch (JDOMException jde) {
            _log.error("Exception getting WIR: " + itemID, jde);
           return null ;
        }
    }

    //***************************************************************************//

    /** returns the spec id for the specified case id */
    public String getSpecIDForCaseID(String caseID) {
        CaseMonitor mon = (CaseMonitor) _monitoredCases.get(getIntegralID(caseID));
        return mon.getSpecID();
    }

    //***************************************************************************//

    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified case
      * @param caseID - the id of the case to get the triggers for
     * @return the (String) list of triggers
     */
    public List getExternalTriggersForCase(String caseID) {
        CaseMonitor mon = (CaseMonitor) _monitoredCases.get(getIntegralID(caseID));
        RdrTree tree = getTree(mon.getSpecID(), null, XTYPE_CASE_EXTERNAL_TRIGGER);

        return getExternalTriggers(tree) ;
    }

    //***************************************************************************//

    /** retrieves a complete list of external exception triggers from the ruleset
     *  for the specified workitem
      * @param itemID - the id of the item to get the triggers for
     * @return the (String) list of triggers
     */
    public List getExternalTriggersForItem(String itemID) {
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
    private List getExternalTriggers(RdrTree tree) {
        ArrayList list = new ArrayList();
        String cond, trigger ;

        if (tree != null) {
            Iterator itr = tree.getAllConditions().iterator();
            while (itr.hasNext()) {
                cond = (String) itr.next() ;
                trigger = getConditionValue(cond, "trigger");
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
            int xLevel ;

            if (level.equalsIgnoreCase("case")) {                // if case level
                caseID = id;
                taskID = null;
                xLevel = XTYPE_CASE_EXTERNAL_TRIGGER ;
            }
            else {                                               // else item level
                wir = getWorkItemRecord(id);
                caseID = wir.getCaseID();
                taskID = wir.getTaskID();
                xLevel = XTYPE_ITEM_EXTERNAL_TRIGGER ;
            }

            // get case monitor for this case
            CaseMonitor monitor = (CaseMonitor) _monitoredCases.get(caseID);

            monitor.addTrigger(trigger);               // add trigger value to case data

            // get the exception handler for this trigger
            RdrConclusion conc = getExceptionHandler(monitor, taskID, xLevel);

            // if conc is null there's no rules defined for this type of constraint
            if (conc == null)
                _log.error("No external exception rules defined for spec: " +
                        monitor.getSpecID() +
                        ". Unable to raise exception for '" + trigger + "'");
            else if (wir == null)
                raiseException(monitor, conc, "external", xLevel);
            else
                raiseException(monitor, conc, wir, xLevel);

            monitor.removeTrigger();
        }
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
   	public String replaceWorklet(int xType, String caseid, String itemid, String trigger) {
        String result, workletCaseID ;
        WorkItemRecord wir = null;
        boolean caseLevel = isCaseLevelException(xType);
        CaseMonitor mon = (CaseMonitor) _monitoredCases.get(caseid);

        _log.info("REPLACE EXECUTING WORKLET REQUEST");

        result = "Locating " +
                 (caseLevel? "case '" + caseid : "workitem '" + itemid) +
   		         "' in the set of currently handled cases...";

        caseid = getIntegralID(caseid);

        // if case is currently being handled
   		if (mon != null) {
   			result += "found." + Library.newline ;
   			_log.debug("Caseid received found in monitoredCases: " + caseid);

            // get the HandlerRunner for the Exception
            HandlerRunner hr = mon.getRunnerForType(xType, itemid) ;

            if (! caseLevel) wir = hr.getItem();

            // get the case ids of the running worklets for this case/workitem
            Iterator witr = hr.getRunningCaseIds().iterator() ;

            while (witr.hasNext()) {
                 workletCaseID = (String) witr.next() ;

                // cancel the worklet running for the case/workitem
    	        result += "Cancelling running worklet case with case id " + workletCaseID + "...";
    	        _log.debug("Running worklet case id for this case/item is: " + workletCaseID);
                removeCase(workletCaseID);

                _log.debug("Removing worklet from handlers started: " + workletCaseID);
		        _handlersStarted.remove(workletCaseID) ;

                result += "done." + Library.newline ;
            }

            // go through the selection process again
	        result += "Launching new replacement worklet case(s) based on revised ruleset...";
	        _log.debug("Launching new replacement worklet case(s) based on revised ruleset");

            // refresh ruleset to pickup newly added rule
            RefreshRuleSet(mon.getSpecID());

            // remove monitor's runner for cancelled worklet
            mon.removeHandlerRunner(hr);
            if (_persisting) _dbMgr.persist(hr, DBManager.DB_DELETE);

            // go through the process again, depending on the exception type
            switch (xType) {
               case XTYPE_CASE_PRE_CONSTRAINTS : checkConstraints(mon, true); break;
               case XTYPE_CASE_POST_CONSTRAINTS : checkConstraints(mon, false); break;
               case XTYPE_ITEM_PRE_CONSTRAINTS : checkConstraints(mon, wir, true); break;
               case XTYPE_ITEM_POST_CONSTRAINTS : checkConstraints(mon, wir, false); break;
               case XTYPE_WORKITEM_ABORT : break;   // not yet implemented
               case XTYPE_TIMEOUT :
                       if (wir != null) handleTimeoutEvent(wir, wir.getTaskID()); break ;
               case XTYPE_RESOURCE_UNAVAILABLE : break;   // not yet implemented
               case XTYPE_CONSTRAINT_VIOLATION : break;   // not yet implemented
               case XTYPE_CASE_EXTERNAL_TRIGGER :
                       raiseExternalException("case", caseid, trigger); break;
               case XTYPE_ITEM_EXTERNAL_TRIGGER :
                       raiseExternalException("item", caseid, trigger); break;
    	   }


           // get the new HandlerRunner for the new Exception
           hr = mon.getRunnerForType(xType, itemid) ;

           HashMap cases = hr.getCaseMapAsCSVList();
           result += "done. " + Library.newline +
                   "The worklet(s) '" + cases.get("workletNames") +
                   "' have been launched for case '" + caseid + Library.newline +
                   "' and have case id(s): " + cases.get("caseIDs") +
                   Library.newline ;
          }
   		else {
   		   _log.warn("Case monitor not found for case: " + caseid) ;
   		   result += "not found." + Library.newline +
   		             "It appears that it has already completed." ;
   		}
   	    return result ;
   	}

    //***************************************************************************//

    /** returns true if case specified is a worklet instance */
    public boolean isWorkletCase(String caseID) {
       return (_handlersStarted.containsKey(caseID) ||
               WorkletService.getInstance().isWorkletCase(caseID)) ;
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
        _handlersStarted = new HashMap();
        HashMap runners = restoreRunners() ;
        _monitoredCases = restoreMonitoredCases(runners) ;
    }

    //***************************************************************************//

    /** restores active HandlerRunner instances */
    private HashMap restoreRunners() {
        HashMap result = new HashMap();
        HandlerRunner runner ;

        // retrieve persisted runner objects from database
        List items = _dbMgr.getObjectsForClass(HandlerRunner.class.getName());

        if (items != null) {
           Iterator itr = items.iterator();
           while (itr.hasNext()) {
               runner = (HandlerRunner) itr.next();
               runner.initNonPersistedItems();            // finish the reconstitution
               runner.restoreCaseMap();
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
    private HashMap restoreMonitoredCases(HashMap runnerMap) {
        HashMap result = new HashMap();
        CaseMonitor monitor ;

        // retrieve persisted monitor objects from database
        List items = _dbMgr.getObjectsForClass(CaseMonitor.class.getName());

        if (items != null) {
           Iterator itr = items.iterator();
           while (itr.hasNext()) {
               monitor = (CaseMonitor) itr.next();

               // 'reattach' relevant runners to this case monitor
               if (runnerMap != null) {
                   List restoredRunners = monitor.restoreRunners(runnerMap);
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
    private void rebuildHandlersStarted(List runners) {
        HandlerRunner runner ;
        Iterator itr = runners.iterator();

        while (itr.hasNext()) {
            runner = (HandlerRunner) itr.next();
            if (runner.hasRunningWorklet()) {
                Iterator runningIDs = runner.getRunningCaseIds().iterator();
                while (runningIDs.hasNext()) {
                    _handlersStarted.put(runningIDs.next(), runner);
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
     * In the situation where the woritem event is received first, it is stored until
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
