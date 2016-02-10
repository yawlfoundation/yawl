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
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;
import org.yawlfoundation.yawl.worklet.rdr.Rdr;
import org.yawlfoundation.yawl.worklet.rdr.RdrPair;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.rdrutil.RdrEvaluator;
import org.yawlfoundation.yawl.worklet.selection.RunnerMap;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * This class and its support classes represent an implementation for YAWL
 * of the Worklet paradigm.
 * <p/>
 * The WorkletService class is the main class for the selection and exception
 * handling processes. For selection, it receives an enabled workitem from the
 * engine and attempts to substitute it with a worklet.
 *
 *  @author Michael Adams
 *  @version 0.8, 09/10/2006
 */

public class WorkletService extends InterfaceBWebsideController {

    // required data for interfacing with the engine
    protected EngineClient _engineClient;
    protected RunnerMap _runners = new RunnerMap();

    protected boolean _persisting;                      // is persistence enabled?
    private static Logger _log;                         // debug log4j file
    private static WorkletService INSTANCE;             // reference to self
    private static ExceptionService _exService;         // reference to ExceptionService
    protected RdrEvaluator _rdr;                                 // rule set interface
    protected WorkletLoader _loader;                    // manages worklet persistence

    private boolean _initCompleted = false;             // has engine initialised?
    private boolean restored = false;
    private boolean _exceptionServiceEnabled = false;

    /**
     * the constructor
     */
    protected WorkletService() {
        super();
        _log = LogManager.getLogger(WorkletService.class);
        _engineClient = new EngineClient(engineLogonName, engineLogonPassword, this);
        _rdr = new RdrEvaluator(_engineClient);
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



    public String getExternalServiceHandle(CostGatewayClient costClient)
            throws IOException {
        return costClient.connect(engineLogonName, engineLogonPassword);
    }


    public void setExceptionServiceEnabled(boolean enable) {
        _exceptionServiceEnabled = enable;
        _log.info("Exception monitoring and handling is {}",
                (enable ? "enabled" : "disabled"));
    }

    public boolean isExceptionServiceEnabled() { return _exceptionServiceEnabled; }

    public WorkletEventServer getServer() { return _engineClient.getServer(); }

    public Rdr getRdrInterface() { return _rdr.getRdrInterface(); }

    public String getResourceServiceURL() { return WorkletConstants.resourceServiceURL; }

    public WorkletLoader getLoader() { return _loader; }

    public EngineClient getEngineClient() { return _engineClient; }


    /**
     * completes the initialisation of the service load-up (mainly persistence)
     * called from servlet WorkletGateway after contexts are loaded
     */
    public void completeInitialisation() {
        _persisting = WorkletConstants.wsPersistOn;
        Persister.getInstance().setPersisting(_persisting);

        // reload running cases data
        if ((_persisting) && (!restored)) restoreDataSets();
    }


    public void shutdown() {
        Persister.getInstance().closeFactory();
        getServer().shutdownListeners();
    }


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

        if (!handleWorkletSelection(workItemRecord)) {
            _engineClient.declineWorkItem(workItemRecord, null);
            _log.info("Workitem returned to Engine: {}", workItemRecord.getID());
        }
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
            if (cancelWorkletSet(runnerSet)) {
                String parentWirID = wir.getParentID();
                _log.info("Removed from handled child workitems: {}", itemId);
                if (! _runners.hasRunnersForParentWorkItem(parentWirID)) {
                    _log.info("Completed handling of workitem: {}", parentWirID);
                }
            }
            else _log.error("Failed to cancel worklet(s) for item: {}", itemId);
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
            handleCompletingSelectionWorklet(caseID, casedata);
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
                cancelWorkletSet(runnerSet);
                _log.info("Handling of cancelled case complete");
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
        InputStream is = this.getClass().getResourceAsStream("welcome.htm");
        String welcomePage = StringUtil.streamToString(is);

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
            String uriB = _interfaceBClient.getBackEndURI();
            setUpInterfaceBClient(uriB);
            _engineClient.reestablishClients(_interfaceBClient);
        }
        else {
            _engineClient.setInterfaceBClient(_interfaceBClient);
        }
        _engineClient.setServiceURI();   // overwrite default with engine stored uri

        if (_engineClient.engineIsAvailable()) {
            if (_exceptionServiceEnabled && (_exService != null)) {
                _engineClient.addIXListener();
            }
        }

        _initCompleted = true;
    }

    //***************************************************************************//

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
        RdrPair pair = _rdr.evaluate(wir);
        if (! (pair == null || pair.hasNullConclusion())) {

            // OK - this workitem has an associated ruleset so check it out
            // all the child items get checked out here
            _log.info("Rule set found for workitem: {}", itemId);
            Set<WorkItemRecord> checkedOutItems = _engineClient.checkOutItem(wir);

            // launch a worklet case for each checked out child workitem
            try {
                for (WorkItemRecord childWir : checkedOutItems) {
                    launchedCount += processWorkItemSubstitution(pair, childWir);
                }
            }
            catch (IOException ioe) {
                _log.error(ioe.getMessage());
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
                " OR No rule set found for specId: {}", wir.getTaskID(), specId);

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
    private int processWorkItemSubstitution(RdrPair pair, WorkItemRecord wir)
            throws IOException {
        String childId = wir.getID();
        _log.info("Processing worklet substitution for workitem: {}", childId);

        Set<WorkletSpecification> wSelected =
                _loader.parseTarget(pair.getConclusion().getTarget(1));
        _log.info("Rule search returned {} worklet(s)", wSelected.size());

        Set<WorkletRunner> runners =
                _engineClient.launchWorkletList(wir, wSelected, RuleType.ItemSelection);
        if (runners.isEmpty()) {
            raise("Failed launch worklet(s): " + wSelected);
        }

        for (WorkletRunner runner : runners) {
            runner.setRuleNodeId(pair.getLastTrueNode().getNodeId());
            runner.setParentCaseID(wir.getRootCaseID());
            runner.logLaunchEvent();
        }
        _runners.addAll(runners);
        getServer().announceSelection(runners, pair.getLastTrueNode());
        return runners.size();
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

        cancelWorkletSet(runners);
        _log.info("Completed handling of workitem: {}", wir.getParentID());
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
        _engineClient.checkCacheForWorkItem(wir);

        try {
            if (_engineClient.getEngineStoredWorkItem(wir) != null) {

                String result = checkInWorkItem(wir.getID(), in, out, null,
                        _engineClient.getSessionHandle());
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


    // if a worklet is cancelled independently to it's parent case, pass the checked
    // out work item back to the engine so the parent case can progress
    private void handleCancelledWorklet(String caseID) {
        WorkletRunner runner = _runners.remove(caseID);
        if (runner != null) {
            _engineClient.undoCheckOutWorkItem(runner.getWir());
        }
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
            if (_engineClient.cancelWorkletCase(runner)) {
                _runners.remove(runner);
                cancelSuccess = true;
            }
        }
        return cancelSuccess;
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
    public String replaceWorklet(String wirID) throws IOException {
        _log.info("REPLACE WORKLET REQUEST");
         Set<WorkletRunner> runners = _runners.getRunnersForWorkItem(wirID);

        // if there's current worklets for workitem
        if (runners.isEmpty()) {
            raise("Unable to find workitem with id: " + wirID);
        }
        _log.info("Item received found in handled items: {}", wirID);

        // cancel the worklet(s) running for the workitem
        if (! cancelWorkletSet(runners)) {
            raise("Failed to cancel running worklet(s)");
        }

        // go through the selection process again
        _log.info("Launching new replacement worklet case(s) based on revised rule set");
        WorkItemRecord wir = runners.iterator().next().getWir();
        RdrPair pair = _rdr.evaluate(wir);
        if (pair == null || pair.hasNullConclusion()) {
            raise("Unable to locate rule set for workitem");
        }
        _log.info("Ruleset found for workitem: {}", wirID);

        // get list of runner ids
        processWorkItemSubstitution(pair, wir);
        return getRunnerCaseIdList(_runners.getRunnersForWorkItem(wirID));
    }


    protected void raise(String msg) throws IOException {
        _log.error(msg);
        throw new IOException(msg);
    }


    protected String getRunnerCaseIdList(Set<WorkletRunner> runners) {
        List<String> caseIDs = new ArrayList<String>();
        for (WorkletRunner runner : runners) {
            caseIDs.add(runner.getCaseID());
        }
        Collections.sort(caseIDs);
        return StringUtil.join(caseIDs, ',');
    }


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


    private String getMITaskInfo(WorkItemRecord wir) {
        try {
            return _engineClient.getMITaskAttributes(new YSpecificationID(wir),
                    wir.getTaskID());
        } catch (IOException ioe) {
            _log.error("IO Exception in dumpMITaskInfo", ioe);
            return null;
        }
    }


    /**
     * writes a summary of substitution outcomes for MI tasks to log
     */
    private void logSelectionForMISummary(WorkItemRecord wir, int itemCount, int workletCount) {
        Element taskInfo = JDOMUtil.stringToElement(getMITaskInfo(wir));
        if (taskInfo != null) {
            String min = taskInfo.getChildText("minimum");
            String max = taskInfo.getChildText("maximum");
            String threshold = taskInfo.getChildText("threshold");

            _log.info("Summary result of worklet selections for multi-instance task {}:",
                    wir.getTaskID());
            _log.info("   Task attributes: Minimum - {}, Maximum - {}, Threshold - {}",
                    min, max, threshold);
            _log.info("   WorkItems created by engine: {}", itemCount);
            _log.info("   Worklets launched: {}", workletCount);
        }
    }


    public Set<WorkletRunner> getAllRunners() {
        Set<WorkletRunner> runners = _runners.getAll();
        runners.addAll(_exService.getRunningWorklets());
        return runners;
    }


    /**
     * returns true if the session specified is an admin session
     */
    public boolean isAdminSession(String sessionHandle) {
        return _engineClient.isAdminSession(sessionHandle);
    }


    public boolean isWorkletCase(String caseID) { return _runners.isWorklet(caseID); }


     /**
     * restores runner hashmap from persistence
     */
    private void restoreDataSets() {
        if (!restored) {
            _runners.restore(null);                        // null means all selections
            restored = true;                               // only restore once
        }
    }

} // end of WorkletService class


