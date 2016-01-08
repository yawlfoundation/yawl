package org.yawlfoundation.yawl.worklet.support;

import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_ServiceSideClient;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.util.AbstractEngineClient;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;

import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 5/01/2016
 */
public class EngineClient extends AbstractEngineClient {

    protected final WorkletEventServer _server;               // announces events
    protected final InterfaceBWebsideController _controller;  // parent service listener
    private InterfaceX_ServiceSideClient _ixClient ;    // interface client to engine

    private static final String DEF_URI = "http://localhost:8080/workletService/ib";
    private static final String SERVICE_NAME = "workletService";


    public EngineClient(String logonName, String password,
                        InterfaceBWebsideController controller) {
        super(logonName, password, DEF_URI, SERVICE_NAME);
        _server = new WorkletEventServer();
        _controller = controller;
    }


    public void setupIXClient() {
        _ixClient = new InterfaceX_ServiceSideClient(_engineURI.replaceFirst("/ib", "/ix"));
    }


    public void addIXListener() {
        try {
            String uri = _serviceURI.replaceFirst("/ib", "/ix");
            _ixClient.addInterfaceXListener(uri);
        }
        catch (IOException ioe) {
            _log.error("Error attempting to register worklet service as " +
                    " an Interface X Listener with the engine", ioe);
        }
    }


    public String uploadWorklet(String workletXML) {
            return uploadSpecification(workletXML, getSessionHandle());
    }


    public WorkletEventServer getServer() { return _server; }


    public void rejectAnnouncedEnabledTask(String wirID) throws IOException {
        _interfaceBClient.rejectAnnouncedEnabledTask(wirID, getSessionHandle());
    }


    public Element getStartingData(String wirID) throws IOException {
        String data = _interfaceBClient.getStartingDataSnapshot(wirID, getSessionHandle());
        return _interfaceBClient.successful(data) ?
                JDOMUtil.stringToElement(data) : null;
    }


    public String cancelWorkItem(String wirID) throws IOException {
        return _ixClient.cancelWorkItem(wirID, null, false, getSessionHandle());
    }


    public String failWorkItem(WorkItemRecord wir) throws IOException {
        return _ixClient.cancelWorkItem(wir.getID(), wir.getDataListString(), true,
                getSessionHandle());
    }


    public String forceCompleteWorkItem(WorkItemRecord wir, Element data) throws IOException {
        return _ixClient.forceCompleteWorkItem(wir, data, getSessionHandle());
    }


    public String restartWorkItem(String wirID) throws IOException {
        return _ixClient.restartWorkItem(wirID, getSessionHandle());
    }


    public WorkItemRecord continueWorkItem(String wirID) throws IOException {
        return _ixClient.unsuspendWorkItem(wirID, getSessionHandle());
    }


    public String updateWorkItemData(WorkItemRecord wir, Element data) throws IOException {
        return _ixClient.updateWorkItemData(wir, data, getSessionHandle());
    }


    public String updateCaseData(String caseID, Element data) throws IOException {
        return _ixClient.updateCaseData(caseID, data, getSessionHandle());
    }


    public List<WorkItemRecord> getLiveWorkItemsForSpec(YSpecificationID specID) {
        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>() ;
        try {
            for (WorkItemRecord wir : getAllLiveWorkItems()) {
                YSpecificationID wirSpecID = new YSpecificationID(wir);
                if (wirSpecID.equals(specID))
                    result.add(wir);
            }
        }
        catch (IOException ioe) {
            _log.error("Exception attempting to get work items for: " +
                    specID.toString(), ioe);
        }
        return result ;
    }


    /**
     * Retrieves a List of live workitems for the case or spec id passed
     * @param idType "case" for a case's workitems, "spec" for a specification's,
     *        "task" for a specific taskID
     * @param id the identifier for the case/spec/task
     * @return the List of live workitems
     */
    public List<WorkItemRecord> getLiveWorkItemsForIdentifier(String idType, String id) {
        try {
            return super.getLiveWorkItemsForIdentifier(idType, id);
        }
        catch (Exception e) {
            _log.error("Exception attempting to get work items for: " + id, e);
            return Collections.emptyList();
        }
    }


    // an atomic task can be rolled back and rehandled in engine's default worklist
    public boolean declineWorkItem(WorkItemRecord wir, String eventType) {
        if (wir == null) return false;
        try {
            rejectAnnouncedEnabledTask(wir.getID());

            // log the rollback checkout event
            if (eventType == null) eventType = EventLogger.eDecline;
            EventLogger.log(eventType, wir, -1);
            return true;
        } catch (IOException ioe) {
            _log.error("IO Exception with undo checkout: " + wir.getID(), ioe);
            return false;
        }
    }


    /**
     * Returns control of a workitem back to the engine for processing, in the event
     * that there is no matching rule found in the ruleset, given the context of the
     * item.
     *
     * @param child - the record for the child to undo the checkout for
     */
    public void undoCheckOutWorkItem(WorkItemRecord child) {
        if (declineWorkItem(child, EventLogger.eUndoCheckOut)) {
            _log.info("Undo checkout successful: {}", child.getID());
        }
    }



    /**
     * Cancels an executing worklet process
     *
     * @param runner - the id of the case to cancel
     * @return true if case is successfully cancelled
     */
    public boolean cancelWorkletCase(WorkletRunner runner) {
        String caseId = runner.getCaseID();
        _log.info("Cancelling worklet case: {}", caseId);
        try {
            cancelCase(caseId);

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



    /*******************************************************************************/

    /**
     * Manages the checking out of a workitem and its children
     *
     * @param wir - the WorkItemRecord of the workitem to check out
     * @return a Set of checked out child workitem.
     */
    public Set<WorkItemRecord> checkOutItem(WorkItemRecord wir) {
        return checkOutWorkItem(wir) ? checkOutChildren(wir) :
                Collections.<WorkItemRecord>emptySet();
    }


    private List<WorkItemRecord> getChildren(String parentID) {
        try {
            return _controller.getChildren(parentID, getSessionHandle());
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
            if (null != _controller.checkOut(wir.getID(), getSessionHandle())) {
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


    public WorkItemRecord getEngineStoredWorkItem(WorkItemRecord wir) throws IOException {
        return wir != null ? getEngineStoredWorkItem(wir.getID()) : null;
    }


    public WorkItemRecord getEngineStoredWorkItem(String wirID) throws IOException {
        return _controller.getEngineStoredWorkItem(wirID, getSessionHandle());
    }


    public SpecificationData getSpecData(YSpecificationID specID) {
       try {
           String specData = _interfaceBClient.getSpecificationData(specID,
                   getSessionHandle());
           if (successful(specData)) {
               List<SpecificationData> dataList = Marshaller.unmarshalSpecificationSummary(
                       StringUtil.wrap(specData, "list"));
               if (! dataList.isEmpty()) {
                   return dataList.get(0);
               }
           }
       }
       catch (IOException ioe) {
           _log.error(ioe.getMessage(), ioe);
       }
       return null;
   }


    public List<YParameter> getTaskInputParams(WorkItemRecord wir) {
        try {
            TaskInformation taskInfo = _controller.getTaskInformation(
                    new YSpecificationID(wir), wir.getTaskID(), getSessionHandle());
            return taskInfo.getParamSchema().getInputParams();
        }
        catch (IOException ioe) {
            return Collections.emptyList();
        }
    }


   /**
    * get the list of input params for a specified specification
    */
   public List<YParameter> getInputParams(YSpecificationID specId) {
       SpecificationData specData = getSpecData(specId);
       return specData != null ? specData.getInputParams() : null;
   }


    /**
     * Uploads a worklet specification into the engine
     *
     * @param worklet - the id of the worklet specification to upload
     * @return true if upload is successful or spec is already loaded in engine
     */
    public boolean uploadWorklet(WorkletSpecification worklet) {
        if (worklet != null) {
            if (isUploaded(worklet.getSpecID())) {
                _log.info("Worklet specification '{}' is already loaded in Engine",
                        worklet.getName());
                return true;
            }
            if (successful(uploadWorklet(worklet.getXML()))) {
                _log.info("Successfully uploaded worklet specification: {}",
                        worklet.getName());
                return true;
            }
            else {
                _log.error("Unsuccessful worklet specification upload : {}",
                        worklet.getName());
            }
        }
        return false;
    }

    /**
     * Checks if a worklet spec has already been loaded into engine
     * @param workletSpec the specification id to check
     * @return true if the specification is already loaded in the engine
     */
    private boolean isUploaded(YSpecificationID workletSpec) {
        return ! (workletSpec == null || getSpecData(workletSpec) == null);
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
    public String mapItemParamsToWorkletCaseParams(WorkItemRecord wir,
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


    /**
     * Starts a worklet case executing in the engine
     *
     * @param wir - the checked out child item to start the worklet for
     * @return - the case id of the started worklet case
     */
    protected WorkletRunner launchWorklet(WorkItemRecord wir, YSpecificationID specID,
                                          RuleType ruleType) {

        // fill the case params with matching data values from the workitem
        String caseData = wir != null ? mapItemParamsToWorkletCaseParams(wir, specID) : null;
        WorkletRunner runner = null;

        try {
            // launch case (and set completion observer)
            String caseId = launchCase(specID, caseData);

            if (successful(caseId)) {

                // save the runner
                runner = new WorkletRunner(caseId, specID, wir, ruleType);

                // log launch event
                EventLogger.log(EventLogger.eLaunch, caseId, specID, "",
                        runner.getParentCaseID(), ruleType.ordinal());
                _log.info("Launched case for worklet {} with ID: {}",
                        specID.getUri(), caseId);

            } else {
                _log.warn("Unable to launch worklet: {}", specID.getUri());
                _log.warn("Diagnostic message: {}", caseId);
            }
        } catch (IOException ioe) {
            _log.error("IO Exception when attempting to launch case", ioe);
        }
        return runner;
    }


    private String launchCase(YSpecificationID specID, String caseParams)
            throws IOException {
        YLogDataItem logData = new YLogDataItem("service", "name", "workletService", "string");
        YLogDataItemList logDataList = new YLogDataItemList(logData);
        return launchCase(specID, caseParams, logDataList);
    }


    /**
     * Launches each of the worklets listed in the wr for starting
     *
     * @param wir   - the child workitem to launch worklets for
     * @param specs - the ids of the worklets to launch
     * @return the set of worklets runners successfully launched
     */
    public Set<WorkletRunner> launchWorkletList(WorkItemRecord wir,
                                        Set<WorkletSpecification> specs, RuleType ruleType) {
        Set<WorkletRunner> runners = new HashSet<WorkletRunner>();

        // for each worklet listed in the conclusion (in case of multiple worklets)
        for (WorkletSpecification spec : specs) {

            // load spec & launch case as substitute for checked out workitem
            if (uploadWorklet(spec)) {
                WorkletRunner runner = launchWorklet(wir, spec.getSpecID(), ruleType);
                if (runner != null) {
                    runners.add(runner);
                }
            }
        }
        return runners;
    }


    // re-adds checkedout item to local cache after a restore (if required)
    public void checkCacheForWorkItem(WorkItemRecord wir) {
        WorkItemRecord wiTemp = _controller.getCachedWorkItem(wir.getID());
        if (wiTemp == null) {

            // if the item is not locally cached, it means a restore has occurred
            // after a checkout & the item is still checked out, so lets put it back
            // so that it can be checked back in
            _controller.getIBCache().addWorkItem(wir);
        }
    }

}
