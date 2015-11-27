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

package org.yawlfoundation.yawl.worklet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.admin.AdminTasksManager;
import org.yawlfoundation.yawl.worklet.admin.AdministrationTask;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;
import org.yawlfoundation.yawl.worklet.rdr.Rdr;
import org.yawlfoundation.yawl.worklet.rdr.RdrPair;
import org.yawlfoundation.yawl.worklet.rdr.RdrTree;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.selection.RunnerMap;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.*;


/**
 * This class and its support classes represent an implementation for YAWL
 * of the Worklet paradigm.
 * <p/>
 * The WorkletService class is the main class for the selection and exception
 * handling processes. For selection, it receives an enabled workitem from the
 * engine and attempts to substitute it with a worklet.
 */
 /*  Here's the class hierarchy for the selection service (see the ExceptionService
 *  class for how its hierarchy extends from this service):
 *
 *
 *    Interfaces A & B                     Rules Editor
 *          ^                                   ^
 *          |                                   |
 * ---------+-----------------------------------+---------------------------- *
 *          |                                   V                             *
 *          |                             +================+                  *
 *          |   +-----------------------> | WorkletGateway |                  *
 *          |   |                         +================+                  *
 *          |   |                                                             *
 *          |   |                                                             *
 *          |   |         +==================+        +====================+  *
 *          |   |   +---1 |AdminTasksManager | 1---M  | AdministrationTask |  *
 *          |   |   |     +==================+        +====================+  *
 *          |   |   |                                                         *
 *          V   V   1                                                         *
 *  ##################       +========+       +=========+       +=========+   *
 *  # WorkletService # 1----M | RdrSet | 1---M | RdrTree | 1---M | RdrNode |  *
 *  ##################       +========+       +=========+       +=========+   *
 *         1     ^                                                   1        *
 *         |     |                                                   |        *
 *         |     +--------------------+                              |        *
 *         |                    +=============+                      |        *
 *         M                    | EventLogger |                      1        *
 *  +================+          +=============+       +====================+  *
 *  | CheckedOutItem |                ^               | ConditionEvaluator |  *
 *  +================+                |               +====================+  *
 *         1                    +==============+                     ^        *
 *         |                    | WorkletEvent |                     |        *
 *         |                    +==============+                     |        *
 *         M                                                         V        *   
 *  +=====================+                        +=======================+  *               
 *  | CheckedOutChildItem |                        | RdrConditionException |  *
 *  +=====================+                        +=======================+  * 
 *         O                                                                  *
 *         |                                                                  *
 *         |                                                                  *
 *  +===============+       +=========+                 +=========+           *
 *  | WorkletRecord | 1---1 | CaseMap |                 | Library |           *
 *  +===============+       +=========+                 +=========+           *
 *                                                                            *
 * -------------------------------------------------------------------------- *
 *
 *  @author Michael Adams
 *  @version 0.8, 09/10/2006
 */

public class WorkletService extends InterfaceBWebsideController {

    // required data for interfacing with the engine
    protected String _sessionHandle = null;
    protected String _engineURI;
    protected String _workletURI = null;
    private InterfaceA_EnvironmentBasedClient _interfaceAClient;

    protected List<SpecificationData> _loadedSpecs =
            new ArrayList<SpecificationData>();         // all specs loaded in engine
    private AdminTasksManager _adminTasksMgr = new AdminTasksManager();   // admin tasks
    protected RunnerMap _runners = new RunnerMap();

    protected boolean _persisting;                      // is persistence enabled?
    protected HibernateEngine _db;                      // manages persistence
    private static Logger _log;                         // debug log4j file
    private static WorkletService INSTANCE;             // reference to self
    private static ExceptionService _exService;         // reference to ExceptionService
    protected WorkletEventServer _server;               // announces events
    protected Rdr _rdr;                                 // rule set interface
    protected WorkletLoader _loader;                    // manages worklet persistence

    private boolean _initCompleted = false;             // has engine initialised?
    private boolean restored = false;
    private boolean _exceptionServiceEnabled = false;

    /**
     * the constructor
     */
    public WorkletService() {
        super();
        _log = LogManager.getLogger(WorkletService.class);
        _server = new WorkletEventServer();
        _rdr = new Rdr();
        _loader = new WorkletLoader();
        INSTANCE = this;
    }

    /**
     * @return a reference to the current WorkletService instance
     */
    public static WorkletService getInstance() {
        if (INSTANCE == null) INSTANCE = new WorkletService();
        return INSTANCE;
    }

    /**
     * allows the Exception Service to register an instance of itself
     */
    protected void registerExceptionService(ExceptionService es) {
        _exService = es;
    }

    public void initEngineURI(String uri) {
        _engineURI = uri;
        _interfaceAClient =
                new InterfaceA_EnvironmentBasedClient(_engineURI.replaceFirst("/ib", "/ia"));
    }


    public String getExternalServiceHandle(CostGatewayClient costClient)
            throws IOException {
        return costClient.connect(engineLogonName, engineLogonPassword);
    }


    protected void setWorkletURI() {
        _workletURI = "http://localhost:8080/workletService/ib";       // a default
        if (connected()) {
            Set<YAWLServiceReference> services =
                    _interfaceAClient.getRegisteredYAWLServices(_sessionHandle);
            if (services != null) {
                for (YAWLServiceReference service : services) {
                    if (service.getURI().contains("workletService")) {
                        _workletURI = service.getURI();
                        break;
                    }
                }
            }
        }
    }


    public void setExceptionServiceEnabled(boolean enable) {
        _exceptionServiceEnabled = enable;
        _log.info("Exception monitoring and handling is {}",
                (enable ? "enabled" : "disabled"));
    }

    public boolean isExceptionServiceEnabled() { return _exceptionServiceEnabled; }

    public WorkletEventServer getServer() { return _server; }

    public Rdr getRdrInterface() { return _rdr; }

    public String getResourceServiceURL() { return Library.resourceServiceURL; }

    public WorkletLoader getLoader() { return _loader; }


    /**
     * completes the initialisation of the service load-up (mainly persistence)
     * called from servlet WorkletGateway after contexts are loaded
     */
    public void completeInitialisation() {
        _persisting = Library.wsPersistOn;

        // init persistence engine
        if (_db == null) {
            _db = Persister.getInstance(_persisting);
        }
        _persisting = (_db != null);                 // turn it off if no connection

        // reload running cases data
        if ((_persisting) && (!restored)) restoreDataSets();
    }


    public void shutdown() {
        if (_db != null) _db.closeFactory();
        _server.shutdownListeners();
    }

    public String uploadWorklet(String workletXML) {
        try {
            return _interfaceAClient.uploadSpecification(workletXML, _sessionHandle);
        }
        catch (IOException ioe) {
            return "<failure>Unsuccessful worklet specification upload : "
                    + ioe.getMessage() + "</failure>";
        }
    }


    //***************************************************************************//

    /************************************
     * 1. OVERRIDDEN BASE CLASS METHODS *
     ***********************************/

    /**
     * Handles a message from the engine that a workitem has been enabled
     * (see InterfaceBWebsideController for more details)
     * In this case, it either starts a worklet substitution process, or, if
     * the workitem denotes the end of a worklet case, it completes the
     * substitution process by checking the original workitem back into
     * the engine.
     *
     * @param workItemRecord - a record describing the enabled workitem
     */

    public void handleEnabledWorkItemEvent(WorkItemRecord workItemRecord) {

        _log.info("HANDLE ENABLED WORKITEM EVENT");        // note to log

        if (connected()) {
            if (!handleWorkletSelection(workItemRecord)) {
                declineWorkItem(workItemRecord, null);
                _log.info("Workitem returned to Engine: {}", workItemRecord.getID());
            }
        } else _log.error("Could not connect to YAWL engine");
    }


    /**
     * Handles a message from the engine that a workitem has been cancelled
     * (see InterfaceBWebsideController for more details)
     * In this case, it cancels any worklet(s) running in place of the
     * workitem.
     * Only deals with child workitems currently checked out - not interested
     * in workitems that haven't been handled by the service, or parent
     * workitems, since handling all the children takes care of the parent.
     * Includes MI items when threshold has been reached.
     *
     * @param wir - a record describing the cancelled workitem
     */

    public void handleCancelledWorkItemEvent(WorkItemRecord wir) {

        // ignore cancelled parents with no child runners
        if (wir.getStatus().equals("Is parent") &&
                _runners.getRunnersForParentWorkItem(wir.getID()).isEmpty()) {
            return;
        }

        _log.info("HANDLE CANCELLED WORKITEM EVENT");
        String itemId = wir.getID();
        _log.info("ID of cancelled workitem: {}", itemId);

        Set<WorkletRunner> runnerSet = _runners.getRunnersForWorkItem(itemId);
        if (! runnerSet.isEmpty()) {
            if (connected()) {
                if (cancelWorkletSet(runnerSet)) {
                    String parentWirID = wir.getParentID();
                    _log.info("Removed from handled child workitems: {}", itemId);
                    if (! _runners.hasRunnersForParentWorkItem(parentWirID)) {
                        _log.info("Completed handling of workitem: {}", parentWirID);
                    }
                }
                else _log.error("Failed to cancel worklet(s) for item: {}", itemId);
            }
            else _log.error("Could not connect to YAWL Engine");
        }
        else _log.info("No worklets running for workitem: {}", itemId);
    }


    /**
     * Handles a message from the engine that a (worklet) case has
     * completed (see InterfaceBWebsideController for more details).
     * <p/>
     * Only those services that register as an 'observer' for the case will
     * receive these events. All worklets launched (through launchCase())
     * register as an observer.
     *
     * @param caseID   - the id of the completed case
     * @param casedata - an (XML) string containing the output data for
     *                 the case
     */

    public void handleCompleteCaseEvent(String caseID, String casedata) {
        _log.info("HANDLE COMPLETE CASE EVENT");
        _log.info("ID of completed case: {}", caseID);

        if (_runners.isWorklet(caseID)) {
            if (connected()) {
                handleCompletingSelectionWorklet(caseID, casedata);
            }
            else _log.error("Could not connect to YAWL Engine");
        }
        else _log.info("Completing case is not a worklet selection: {}", caseID);
    }


    public synchronized void handleCancelledCaseEvent(String caseID) {
        _log.info("HANDLE CANCELLED CASE EVENT");
        _log.info("ID of cancelled case: {}", caseID);

        if (isWorkletCase(caseID)) {
            handleCancelledWorklet(caseID);
        }
        else {
            Set<WorkletRunner> runnerSet = _runners.getRunnersForAncestorCase(caseID);
            if (! runnerSet.isEmpty()) {
                if (connected()) {
                    cancelWorkletSet(runnerSet);
                    _log.info("Handling of cancelled case complete");
                }
                else _log.error("Could not connect to YAWL Engine");
            }
            else _log.info("No worklets running for case: {}", caseID);
        }
    }


    /**
     * displays a web page describing the service
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuilder output = new StringBuilder();
        String fileName = Library.wsHomeDir + "welcome.htm";
        String welcomePage = StringUtil.fileToString(fileName);

        // load the full welcome page if possible
        if (welcomePage != null) output.append(welcomePage);
        else {

            // otherwise load a boring default
            output.append("<html><head>" +
                    "<title>Worklet Dynamic Process Selection Service</title>" +
                    "</head><body>" +
                    "<H3>Welcome to the Worklet Dynamic Process Selection Service</H3>" +
                    "</body></html>");
        }
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }


    public synchronized void handleEngineInitialisationCompletedEvent() {
        if (_initCompleted) {             // if engine has restarted
            String uriA = _interfaceAClient.getBackEndURI();
            String uriB = _interfaceBClient.getBackEndURI();
            _interfaceAClient = new InterfaceA_EnvironmentBasedClient(uriA);
            setUpInterfaceBClient(uriB);
        }
        if (engineIsAvailable()) {
            setWorkletURI();
            if (_exceptionServiceEnabled && (_exService != null)) {
                _exService.setupInterfaceXListener(_workletURI);
            }
        }

        _initCompleted = true;
    }

    // make sure the engine is contactable
    private boolean engineIsAvailable() {
        String errMsg = "Failed to locate a running YAWL engine at URL '" +
                _engineURI + "'. ";
        int timeout = 5;
        boolean available = false;
        try {
            available = HttpURLValidator.pingUntilAvailable(_engineURI, timeout);
            if (!available) {
                _log.error(errMsg + "Service functionality may be limited.");
            }
        } catch (MalformedURLException mue) {
            _log.error(errMsg + mue.getMessage());
        }
        return available;
    }

    /**
     * Override of InterfaceB_EnvironmentBasedClient.launchCase() to provide
     * the ability to send the worklet service as a case completed observer
     */
    private String launchCase(YSpecificationID specID, String caseParams,
                              String sessionHandle, boolean observer)
            throws IOException {
        if (_workletURI == null) setWorkletURI();
        String obsURI = observer ? _workletURI : null;
        YLogDataItem logData = new YLogDataItem("service", "name", "workletService", "string");
        YLogDataItemList logDataList = new YLogDataItemList(logData);
        return _interfaceBClient.launchCase(specID, caseParams, sessionHandle, logDataList, obsURI);
    }

    //***************************************************************************//


    /****************************************
     * 2. TOP LEVEL WORKITEM EVENT HANDLERS *
     ***************************************/

    /**
     * Attempt to substitute the enabled workitem with a worklet
     *
     * @param wir - the enabled workitem record
     */
    private boolean handleWorkletSelection(WorkItemRecord wir) {
        YSpecificationID specId = new YSpecificationID(wir);
        String itemId = wir.getID();
        int launchedCount = 0;

        _log.info("Received workitem for worklet substitution: {}", itemId);
        _log.info("   specId = {}", specId);

        // locate rdr conclusion for this task, if any
        RdrPair pair = evaluate(wir);
        if (! (pair == null || pair.hasNullConclusion())) {

            // OK - this workitem has an associated ruleset so check it out
            // all the child items get checked out here
            _log.info("Rule set found for workitem: {}", itemId);
            Set<WorkItemRecord> checkedOutItems = checkOutItem(wir);

            // launch a worklet case for each checked out child workitem
            for (WorkItemRecord childWir : checkedOutItems) {
                if (processWorkItemSubstitution(pair, childWir)) launchedCount++;
            }

            if (launchedCount == 0) {
                _log.warn("No worklets launched for workitem: {}", itemId);
            }

            // if MI Task, store threshold and log summary of selections
            if (checkedOutItems.size() > 1) {
                logSelectionForMISummary(wir, checkedOutItems.size(), launchedCount);
            }
        }
        else _log.warn("Rule set does not contain rules for task: {}" +
                " OR No rule set found for specId: {}", wir.getTaskName(), specId);

        return launchedCount > 0;
    }


    /**
     * Deals with the end of a selection worklet case.
     *
     * @param caseId     - the id of the completing case
     * @param wlCasedata - the completing case's datalist Element
     */
    private void handleCompletingSelectionWorklet(String caseId, String wlCasedata) {

        // get the id of the workitem this worklet was selected for
        WorkletRunner runner = _runners.remove(caseId);
        _log.info("Workitem this worklet case ran in place of is: {}",
                runner.getWir().getID());

        // log the worklet's case completion event
        EventLogger.log(EventLogger.eComplete, caseId, runner.getWorkletSpecID(),
                "", runner.getParentCaseID(), -1);
        _log.info("Removed from cases started: {}", caseId);

        // if all worklets for this item have completed, check it back in
        if (! _runners.hasRunnersForWorkItem(runner.getWorkItemID())) {
            _log.info("Handling of workitem completed - checking it back in to engine");
            checkInHandledWorkItem(runner, wlCasedata);
        }
    }


    /**
     * Gets a worklet running for a checked-out workitem
     *
     * @param pair the RdrTree rule pair for the task that the checked-out
     *             workitem is an instance of. PRE: pair contains a valid conclusion
     * @param wir the checked out workitem
     */
    private boolean processWorkItemSubstitution(RdrPair pair, WorkItemRecord wir) {
        String childId = wir.getID();
        _log.info("Processing worklet substitution for workitem: {}", childId);

        Set<WorkletSpecification> wSelected =
                _loader.parseTarget(pair.getConclusion().getTarget(1));
        _log.info("Rule search returned {} worklet(s)", wSelected.size());

        if (launchWorkletList(wir, wSelected)) {
            _server.announceSelection(_runners.getRunnersForWorkItem(childId),
                    pair.getLastTrueNode());
            return true;
        }
        else _log.warn("Could not launch worklet(s): {}", wSelected);

        return false;
    }


    /**
     * Removes all remaining worklet cases and completes handling of a workitem
     * which is a spawned item of a multi-instance task and that task has reached
     * its threshold and already completed in the engine.
     *
     * @param wir - a record describing one of the remaining spawned workitems
     */

    private void cancelWorkletsForCompletedMITask(WorkItemRecord wir) {
        cancelWorkletsForCompletedMITask(wir,
                _runners.getRunnersForParentWorkItem(wir.getParentID()));
    }

    /**
     * overloaded (see above)
     *
     * @param runners the set of worklet runners for the checked out child item that's
     *                a member workitem of the MI Task that has reached its threshold
     */
    private void cancelWorkletsForCompletedMITask(WorkItemRecord wir,
                                                  Set<WorkletRunner> runners) {
        _log.info("Threshold reached for multi-instance task {}. " +
                "Removing remaining worklets launched for this task.", wir.getTaskID());

        if (connected()) {
            cancelWorkletSet(runners);
            _log.info("Completed handling of workitem: {}", wir.getParentID());
        }
        else _log.error("Failed to connect to the YAWL engine");
    }


    //***************************************************************************//

    /*******************************
     * 3. CHECKOUT/CHECKIN METHODS *
     ******************************/

    /**
     * Checks if there is a connection to the engine, and
     * if there isn't, attempts to connect
     *
     * @return true if connected to the engine
     */
    protected boolean connected() {
        try {
            // if not connected
            if (_sessionHandle == null || !checkConnection(_sessionHandle)) {
                _sessionHandle = connect(engineLogonName, engineLogonPassword);
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to connect to engine", ioe);
        }
        boolean success = successful(_sessionHandle);
        _log.info("Connection to engine is " + (success ? "" : "in") + "active");

        if (!success && _log.isErrorEnabled()) {
            _log.error(JDOMUtil.strip(_sessionHandle));
        }
        return success;
    }


    /**
     * Manages the checking out of a workitem and its children
     *
     * @param wir - the WorkItemRecord of the workitem to check out
     * @return a Set of checked out child workitem.
     */
    protected Set<WorkItemRecord> checkOutItem(WorkItemRecord wir) {
        return checkOutWorkItem(wir) ? checkOutChildren(wir) :
                Collections.<WorkItemRecord>emptySet();
    }


    private List<WorkItemRecord> getChildren(String parentID) {
        try {
            return getChildren(parentID, _sessionHandle);
        }
        catch (IOException ioe) {
            return Collections.emptyList();
        }
    }


    /**
     * Checks out all the child workitems of the parent item specified
     *
     * @param wir - the parent work item
     */
    protected Set<WorkItemRecord> checkOutChildren(WorkItemRecord wir) {
        _log.info("Checking out child workitems...");

        // get all the child instances of this workitem
        for (WorkItemRecord itemRec : getChildren(wir.getID())) {

            // if its 'fired', check it out
            if (WorkItemRecord.statusFired.equals(itemRec.getStatus())) {
                if (checkOutWorkItem(itemRec))
                    EventLogger.log(EventLogger.eCheckOut, itemRec, -1);
            }

            // if its 'executing', it means it got checked out with the parent
            else if (WorkItemRecord.statusExecuting.equals(itemRec.getStatus())) {
                EventLogger.log(EventLogger.eCheckOut, itemRec, -1);
            }
        }

        // get refreshed child item list after checkout (to capture status changes)
        Set<WorkItemRecord> checkedOutItems = new HashSet<WorkItemRecord>();
        for (WorkItemRecord w : getChildren(wir.getID())) {
            if (WorkItemRecord.statusExecuting.equals(w.getStatus())) {
                checkedOutItems.add(w);
            }
        }
        return checkedOutItems;
    }


    /**
     * Check the workitem out of the engine
     *
     * @param wir - the workitem to check out
     * @return true if checkout was successful
     */
    protected boolean checkOutWorkItem(WorkItemRecord wir) {

        try {
            if (null != checkOut(wir.getID(), _sessionHandle)) {
                _log.info("   checkout successful: {}", wir.getID());
                return true;
            } else {
                _log.info("   checkout unsuccessful: {}", wir.getID());
                return false;
            }
        } catch (YAWLException ye) {
            _log.error("YAWL Exception with checkout: " + wir.getID(), ye);
            return false;
        } catch (IOException ioe) {
            _log.error("IO Exception with checkout: " + wir.getID(), ioe);
            return false;
        }
    }


    /**
     * Checks in the workitem after its subbed worklets have (all) completed and,
     * if the original parent workitem has no more children after this workitem
     * is checked in, removes its record from the dynamic datsets of currently
     * handled workitems
     *
     * @param runner       - the checkedOutChildItem for the workitem in question
     * @param wlCasedata - the completing case's datalist Element
     */
    private void checkInHandledWorkItem(WorkletRunner runner, String wlCasedata) {

        // get the actual workitem this worklet case substituted
        WorkItemRecord childItem = runner.getWir();
        if (childItem != null) {

            // get the workitem's input data list
            Element in = childItem.getDataList();

            // update workitem's datalist with the worklet's output values
            Element out = updateDataList(in, JDOMUtil.stringToElement(wlCasedata));

            // check in original workitem
            if (checkInItem(childItem, in, out)) {
                _log.info("Removed from handled child workitems: {}",
                        childItem.getID());

                // if there is no more child cases, we're done with this parent
                String parentId = runner.getParentWorkItemID();
                if (!_runners.hasRunnersForParentWorkItem(parentId)) {
                    _log.info("No more child cases running for workitem: {}",
                            parentId);
                    _log.info("Completed handling of workitem: {}", parentId);
                }
            }
            else
                _log.warn("Failed to check in child workitem: {}", childItem.getID());
        }
    }


    /**
     * Checks a (checked out) workitem back into the engine
     *
     * @param wir - workitem to check into the engine
     * @param in  - a JDOM Element containing the input params of the workitem
     * @param out - a JDOM Element containing the output params of the workitem
     * @return true if check in is successful
     */
    private boolean checkInItem(WorkItemRecord wir, Element in, Element out) {

        // make sure the wir is locally cached (esp. important after a restore)
        checkCacheForWorkItem(wir);

        try {
            if (getEngineStoredWorkItem(wir) != null) {

                String result = checkInWorkItem(wir.getID(), in, out, null,
                        _sessionHandle);
                if (successful(result)) {

                    // log the successful check in event
                    EventLogger.log(EventLogger.eCheckIn, wir, -1);
                    _log.info("Successful check in of work item: {}", wir.getID());
                    return true;
                } else {
                    _log.error("Check in unsuccessful for: {}", wir.getID());
                    _log.error("Diagnostic string: {}", result);
                }
            } else {
                // assumption: workitem not in engine means it was a spawned item of
                // a MI task which has completed
                cancelWorkletsForCompletedMITask(wir);
            }
        } catch (IOException ioe) {
            _log.error("checkInItem method caused java IO Exception", ioe);
        } catch (JDOMException jde) {
            _log.error("checkInItem method caused JDOM Exception", jde);
        }
        return false;                                 // check-in unsuccessful
    }


    /**
     * Returns control of a workitem back to the engine for processing, in the event
     * that there is no matching rule found in the ruleset, given the context of the
     * item.
     *
     * @param child - the record for the child to undo the checkout for
     */
    private void undoCheckOutWorkItem(WorkItemRecord child) {
        if (declineWorkItem(child, EventLogger.eUndoCheckOut)) {
            _log.info("Undo checkout successful: {}", child.getID());
        }
    }


    // an atomic task can be rolled back and rehandled in engine's default worklist
    private boolean declineWorkItem(WorkItemRecord wir, String eventType) {
        if (wir == null) return false;
        try {
            _interfaceBClient.rejectAnnouncedEnabledTask(wir.getID(), _sessionHandle);

            // log the rollback checkout event
            if (eventType == null) eventType = EventLogger.eDecline;
            EventLogger.log(eventType, wir, -1);
            return true;
        } catch (IOException ioe) {
            _log.error("IO Exception with undo checkout: " + wir.getID(), ioe);
            return false;
        }
    }


    // if a worklet is cancelled independently to it's parent case, pass the checked
    // out work item back to the engine so the parent case can progress
    private void handleCancelledWorklet(String caseID) {
        WorkletRunner runner = _runners.remove(caseID);
        if (runner != null) {
            undoCheckOutWorkItem(runner.getWir());
        }
    }

    //***************************************************************************//

    /************************************************
     * 4. UPLOADING, LAUNCHING & CANCELLING METHODS *
     ***********************************************/

    /**
     * Uploads a worklet specification into the engine
     *
     * @param worklet - the id of the worklet specification to upload
     * @return true if upload is successful or spec is already loaded in engine
     */
    protected boolean uploadWorklet(WorkletSpecification worklet) {
        if (worklet != null) {
            if (isUploaded(worklet.getSpecID())) {
                _log.info("Worklet specification '{}' is already loaded in Engine",
                        worklet.getName());
                return true;
            }
            try {
                if (successful(_interfaceAClient.uploadSpecification(
                        worklet.getXML(), _sessionHandle))) {
                    _log.info("Successfully uploaded worklet specification: {}",
                            worklet.getName());
                    return true;
                }
                else {
                    _log.error("Unsuccessful worklet specification upload : {}",
                            worklet.getName());
                }
            }
            catch (IOException ioe) {
                _log.error("Unsuccessful worklet specification upload : {}",
                        worklet.getName());
            }
        }
        return false;
    }


    /**
     * Launches each of the worklets listed in the wr for starting
     *
     * @param wir   - the child workitem to launch worklets for
     * @param specs - the ids of the worklets to launch
     * @return true if *any* of the worklets are successfully launched
     */
    protected boolean launchWorkletList(WorkItemRecord wir, Set<WorkletSpecification> specs) {
        boolean launchSuccess = false;

        // for each worklet listed in the conclusion (in case of multiple worklets)
        for (WorkletSpecification spec : specs) {

            // load spec & launch case as substitute for checked out workitem
            if (uploadWorklet(spec)) {
                String caseID = launchWorklet(wir, spec.getSpecID(), true,
                        RuleType.ItemSelection);
                if (caseID != null) launchSuccess = true;
            }
        }
        return launchSuccess;
    }


    /**
     * Cancels each of the worklets listed in the wr as running
     *
     * @param runnerSet - the worklet record containing the list of worklets to cancel
     * @return true if *any* of the worklets are successfully cancelled
     */
    protected boolean cancelWorkletSet(Set<WorkletRunner> runnerSet) {
        boolean cancelSuccess = false;

        // cancel each worklet running for the workitem
        for (WorkletRunner runner : runnerSet) {
            if (cancelWorkletCase(runner)) {
                _runners.remove(runner);
                cancelSuccess = true;
            }
        }
        return cancelSuccess;
    }


    /**
     * Starts a worklet case executing in the engine
     *
     * @param wir - the checked out child item to start the worklet for
     * @return - the case id of the started worklet case
     */
    protected String launchWorklet(WorkItemRecord wir, YSpecificationID specID,
                                   boolean setObserver, RuleType ruleType) {

        // fill the case params with matching data values from the workitem
        String caseData = wir != null ? mapItemParamsToWorkletCaseParams(wir, specID) : null;
        String caseId = null;

        try {
            // launch case (and set completion observer)
            caseId = launchCase(specID, caseData, _sessionHandle, setObserver);

            if (successful(caseId)) {

                // save the runner
                WorkletRunner runner = new WorkletRunner(caseId, specID, wir);
                _runners.add(runner);

                // log launch event
                EventLogger.log(EventLogger.eLaunch, caseId, specID, "",
                        runner.getParentCaseID(), runner.getRuleType().ordinal());
                _log.info("Launched case for worklet {} with ID: {}",
                        specID.getUri(), caseId);

            } else {
                _log.warn("Unable to launch worklet: {}", specID.getUri());
                _log.warn("Diagnostic message: {}", caseId);
            }
        } catch (IOException ioe) {
            _log.error("IO Exception when attempting to launch case", ioe);
        }
        return caseId;
    }


    /**
     * Replaces a running worklet case with another worklet case after an
     * amendment to the ruleset for this task. Called by WorkletGateway after a call
     * from the Editor that the ruleset has been updated.
     *
     * @param wirID the id of the original checked out workitem
     * @return a string of messages describing the success or otherwise of
     *         the process
     */
    public String replaceWorklet(String wirID) {
        _log.info("REPLACE WORKLET REQUEST");
        String result = "";
        Set<WorkletRunner> runners = _runners.getRunnersForWorkItem(wirID);

        // if there's current worklets for workitem
        if (! runners.isEmpty()) {
            _log.info("Item received found in handled items: {}", wirID);

            // cancel the worklet(s) running for the workitem
            if (cancelWorkletSet(runners)) {

                // go through the selection process again
                _log.info("Launching new replacement worklet case(s) based on revised rule set");
                WorkItemRecord wir = runners.iterator().next().getWir();
                YSpecificationID specId = new YSpecificationID(wir);
                String taskId = wir.getTaskID();
                RdrPair pair = evaluate(specId, taskId, getSearchData(wir));
                if (! (pair == null || pair.hasNullConclusion())) {
                    _log.info("Ruleset found for workitem: {}", wirID);
                    if (processWorkItemSubstitution(pair, wir)) {

                        // update set of runners
                        runners = _runners.getRunnersForWorkItem(wirID);
                        result = runners.size() + " worklet(s) launched";
                    }
                }
                else {
                    _log.warn("Failed to locate rule set for workitem.");
                    result = "Failed to locate rule set for workitem.";
                }
            }
            else {
                _log.warn("Failed to cancel running worklet(s)");
                result = "Failed to cancel running worklet(s)";
            }
        }
        else {
            _log.warn("Itemid not found in handleditems: {}", wirID);
            result = "There are no checked out workitems with id : " + wirID;
        }

        return result;
    }


    /**
     * Cancels an executing worklet process
     *
     * @param runner - the id of the case to cancel
     * @return true if case is successfully cancelled
     */
    private boolean cancelWorkletCase(WorkletRunner runner) {
        String caseId = runner.getCaseID();
        _log.info("Cancelling worklet case: {}", caseId);
        try {
            _interfaceBClient.cancelCase(caseId, _sessionHandle);

            // log successful cancellation event
            EventLogger.log(EventLogger.eCancel, caseId, runner.getWorkletSpecID(),
                    "", runner.getParentCaseID(), -1);
            _log.info("Worklet case successfully cancelled: {}", caseId);
            return true;
        }
        catch (IOException ioe) {
            _log.error("IO Exception when attempting to cancel case", ioe);
        }
        return false;
    }

    //***************************************************************************//

    /************************************
     * 5. DATALIST MANIPULATION METHODS *
     ***********************************/

    /**
     * updates the input datalist with the changed data in the output datalist
     *
     * @param in  - the JDOM Element containing the input params
     * @param out - the JDOM Element containing the output params
     * @return a JDOM Element with the data updated
     */
    protected Element updateDataList(Element in, Element out) {

        // get a copy of the 'in' list   	
        Element result = in.clone();

        // for each child in 'out' list, get its value and copy to 'in' list
        for (Element e : out.getChildren()) {

            // if there's a matching 'in' data item, update its value
            Element resData = result.getChild(e.getName());
            if (resData != null) {
                if (resData.getContentSize() > 0) resData.setContent(e.cloneContent());
                else resData.setText(e.getText());
            } else {
                // if the item is not in the 'in' list, add it.
                result.getChildren().add(e.clone());
            }
        }

        return result;
    }


    /**
     * Maps the values of the data attributes in the datalist of a
     * checked out workitem to the input params of the worklet case that will
     * run as a substitute for the checked out workitem.
     * The input params for the worklet case are required by the interface's
     * launchcase() method.
     *
     * @param wir the checked out work item
     * @return the loaded input params of the new worklet case
     *         (launchCase() requires the input params as a String)
     */
    private String mapItemParamsToWorkletCaseParams(WorkItemRecord wir,
                                                    YSpecificationID workletSpecID) {

        Element itemData = wir.getDataList();       // get datalist of work item
        Element wlData = new Element(workletSpecID.getUri());   // new datalist for worklet
        List<YParameter> inParams = getInputParams(workletSpecID);  // worklet input params

        // if worklet has no net-level inputs, or workitem has no datalist, we're done
        if ((inParams == null) || (itemData == null)) return null;

        // extract the name of each worklet input param
        for (YParameter param : inParams) {
            String paramName = param.getName();

            // get the data element of the workitem with the same name as
            // the one for the worklet (assigns null if no match)
            Element wlElem = itemData.getChild(paramName);

            try {
                // if matching element, copy it and add to worklet datalist
                if (wlElem != null) {
                    Element copy = wlElem.clone();
                    wlData.addContent(copy);
                }

                // no matching data for input param, so add empty element
                else
                    wlData.addContent(new Element(paramName));
            } catch (IllegalAddException iae) {
                _log.error("Exception adding content to worklet data list", iae);
            }
        }

        // return the datalist as as string (as required by launchcase)
        return JDOMUtil.elementToString(wlData);
    }


    //***************************************************************************//

    /****************************
     * 6. RULE SET METHODS *
     ***************************/

    private RdrPair evaluate(WorkItemRecord wir) {
        try {
            Element data = JDOMUtil.stringToElement(
                    _interfaceBClient.getStartingDataSnapshot(wir.getID(), _sessionHandle));
            if (data != null) {
                Element searchData = getSearchData(wir, data);
                return evaluate(new YSpecificationID(wir), wir.getTaskName(), searchData);
            }
        }
        catch (IOException fallthrough) {

        }
        return null;
    }


    private RdrPair evaluate(YSpecificationID specID, String taskID, Element data) {
        if (data != null) {
            RdrTree tree = getTree(specID, taskID, RuleType.ItemSelection);
            if (tree != null) return tree.search(data);
        }
        return null;
    }


    private Element getSearchData(WorkItemRecord wir) {
        return getSearchData(wir, wir.getDataList());
    }


    private Element getSearchData(WorkItemRecord wir, Element data) {
        Element processData = data.clone();

        //convert the wir contents to an Element
        Element wirElement = JDOMUtil.stringToElement(wir.toXML()).detach();

        Element eInfo = new Element("process_info");     // new Element for process data
        eInfo.addContent(wirElement);
        processData.addContent(eInfo);                     // add element to case data
        return processData;
    }

    /**
     * returns the rule tree (if any) for the parameters passed
     */
    protected RdrTree getTree(YSpecificationID specID, String taskID, RuleType treeType) {
        return _rdr.getRdrTree(specID, taskID, treeType);
    }


    /**
     * Reloads the rule set from file (after a rule update) for the spec passed
     */
    public void refreshRuleSet(YSpecificationID specID) {
        // _rdr.refreshRdrSet(specID);
    }


    //***************************************************************************//

    /****************************
     * 7. INFORMATIONAL METHODS *
     ***************************/


    /**
     * fill an array with details of each spec loaded into engine
     */
    private void getLoadedSpecs() {

        try {
            _loadedSpecs = _interfaceBClient.getSpecificationList(_sessionHandle);
        } catch (IOException ioe) {
            _log.error("IO Exception in getLoadedSpecs", ioe);
        }
    }


    /**
     * get the list of input params for a specified specification
     */
    private List<YParameter> getInputParams(YSpecificationID specId) {

        // refresh list of specifications loaded into the engine
        getLoadedSpecs();

        // locate input params for the specified spec id
        for (SpecificationData thisSpec : _loadedSpecs) {
            if (specId.equals(thisSpec.getID()))
                return thisSpec.getInputParams();
        }
        return null;
    }


    private String getMITaskInfo(WorkItemRecord wir) {
        try {
            return _interfaceBClient.getMITaskAttributes(
                    new YSpecificationID(wir),
                    wir.getTaskID(), _sessionHandle);
        } catch (IOException ioe) {
            _log.error("IO Exception in dumpMITaskInfo", ioe);
            return null;
        }
    }


    /**
     * writes a summary of substitution outcomes for MI tasks to log
     */
    private void logSelectionForMISummary(WorkItemRecord wir, int itemCount, int workletCount) {
        String taskInfo = getMITaskInfo(wir);
        String min = RdrConversionTools.getChildValue(taskInfo, "minimum");
        String max = RdrConversionTools.getChildValue(taskInfo, "maximum");
        String thres = RdrConversionTools.getChildValue(taskInfo, "threshold");

        _log.info("Summary result of worklet selections for multi-instance task {}:",
                wir.getTaskID());
        _log.info("   Task attributes: Minimum - {}, Maximum - {}, Threshold - {}",
                min, max, thres);
        _log.info("   WorkItems created by engine: {}", itemCount);
        _log.info("   Worklets launched: {}", workletCount);
    }


    // re-adds checkedout item to local cache after a restore (if required)
    private void checkCacheForWorkItem(WorkItemRecord wir) {
        WorkItemRecord wiTemp = getCachedWorkItem(wir.getID());
        if (wiTemp == null) {

            // if the item is not locally cached, it means a restore has occurred
            // after a checkout & the item is still checked out, so lets put it back
            // so that it can be checked back in
            getIBCache().addWorkItem(wir);
        }
    }


    public WorkItemRecord getEngineStoredWorkItem(WorkItemRecord wir) throws IOException {
        return wir != null ? getEngineStoredWorkItem(wir.getID()) : null;
    }


    public WorkItemRecord getEngineStoredWorkItem(String wirID) throws IOException {
        return connected() ? getEngineStoredWorkItem(wirID, _sessionHandle) : null;
    }


    public Set<WorkletRunner> getAllRunners() {
        return _runners.getAll();
    }


    /**
     * Checks if a worklet spec has already been loaded into engine
     *
     * @param workletSpec the specification id to check
     * @return true if the specification is already loaded in the engine
     */
    private boolean isUploaded(YSpecificationID workletSpec) {

        if (workletSpec == null) return false;

        // refresh list of specifications loaded into the engine
        getLoadedSpecs();

        // check if any loaded specids match the worklet spec selected
        for (SpecificationData spec : _loadedSpecs) {
            if (workletSpec.equals(spec.getID())) return true;
        }
        return false;                                           // no matches
    }


    /**
     * returns true if the session specified is an admin session
     */
    public boolean isAdminSession(String sessionHandle) {
        try {
            String msg = _interfaceAClient.checkConnection(sessionHandle);
            return successful(msg);
        } catch (IOException ioe) {
            return false;
        }
    }


    public boolean isWorkletCase(String caseID) { return _runners.isWorklet(caseID); }


    //***************************************************************************//

    /*******************************
     * 10. ADMIN TASKS MGT METHODS *
     ******************************/

    /**
     * add case-level admin task (called from jsp)
     */
    public void addAdministrationTask(String caseID, String title, String scenario,
                                      String process, int taskType) {

        AdministrationTask adminTask =
                _adminTasksMgr.addTask(caseID, title, scenario, process, taskType);
        Persister.insert(adminTask);

        // suspend case pending admin action
        _exService.suspendCase(caseID);
    }


    /**
     * add item-level admin task (called from jsp)
     */
    public void addAdministrationTask(String caseID, String itemID, String title,
                                      String scenario, String process, int taskType) {

        AdministrationTask adminTask =
                _adminTasksMgr.addTask(caseID, itemID, title, scenario, process, taskType);
        Persister.insert(adminTask);

        // suspend item pending admin action
        _exService.suspendWorkItem(itemID);
    }


    /**
     * returns complete list of titles of all outstanding adimn tasks
     */
    public List getAdminTaskTitles() {
        return _adminTasksMgr.getAllTaskTitles();
    }


    /**
     * marks the specified task as completed (removes it from list of tasks)
     */
    public void completeAdminTask(String adminTaskID) {
        AdministrationTask adminTask = _adminTasksMgr.removeTask(adminTaskID);
        Persister.delete(adminTask);
    }


    /**
     * returns complete list of all outstanding adimn tasks
     */
    public List getAllAdminTasksAsList() {
        return _adminTasksMgr.getAllTasksAsList();
    }


    /**
     * returns the admin task with the id specified
     */
    public AdministrationTask getAdminTask(String id) {
        return _adminTasksMgr.getTask(id);
    }

    //***************************************************************************//

    /*******************************
     * 11. PERSISTENCE MGT METHODS *
     *******************************/

    /**
     * restores class hashmaps from persistence
     */
    private void restoreDataSets() {
        if (!restored) {
            _runners.restore(RunnerMap.SELECTION_RUNNERS);
            _adminTasksMgr = restoreAdminTasksManager();      // admin tasks
            restored = true;                                   // only restore once
        }
    }


    /**
     * rebuilds admin task manager from persistence
     */
    private AdminTasksManager restoreAdminTasksManager() {
        AdminTasksManager result = new AdminTasksManager();
        List items = _db.getObjectsForClass(AdministrationTask.class.getName());

        if (items != null) {
            for (Object o : items) {
                result.addTask((AdministrationTask) o);
            }
        }
        return result;
    }

    //***************************************************************************//
    //***************************************************************************//

} // end of WorkletService class


