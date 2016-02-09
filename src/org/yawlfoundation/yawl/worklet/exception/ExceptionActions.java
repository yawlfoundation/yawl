package org.yawlfoundation.yawl.worklet.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.EngineClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 6/01/2016
 */
public class ExceptionActions {

    private final EngineClient _engineClient;
    private final Logger _log;

    protected ExceptionActions(EngineClient client) {
        _engineClient = client;
        _log = LogManager.getLogger(this.getClass());
    }

    /**
     * Suspends each workitem in the list of items passed
     * @param items - items a list of workitems to suspend
     * @return true if all were successfully suspended
     */
    protected boolean suspendWorkItemList(Set<WorkItemRecord> items) {
        String itemID = "";

        try {
            for (WorkItemRecord item : items) {
                itemID = item.getID();
                if (! _engineClient.successful(_engineClient.suspendWorkItem(itemID)))
                    throw new IOException() ;
                _log.debug("Successful work item suspend: {}", itemID);
            }
        }
        catch (IOException ioe) {
            _log.error("Failed to suspend workitem '{}': {}", itemID, ioe.getMessage());
            return false;
        }
        return true ;
    }


    /**
     * Suspends all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    protected boolean suspendAncestorCases(ExletRunner runner, Set<WorkItemRecord> items) {
        String caseID = runner.getCaseID();                    // i.e. id of parent case
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


    protected Set<WorkItemRecord> getSuspendableWorkItems(YSpecificationID id) {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();
        for (WorkItemRecord wir : _engineClient.getLiveWorkItemsForSpec(id)) {
            if (wir.hasLiveStatus()) result.add(wir);
        }
        return result ;
    }


    /**
     * Suspends all live workitems in all live cases for the specification passed
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    protected boolean suspendAllCases(ExletRunner hr) {
        YSpecificationID specID = _engineClient.getSpecificationIDForCase(hr.getCaseID());
        if (specID == null) {
            _log.error("Failed to get specification id for case: {}", hr.getCaseID());
            return false;
        }

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


    /**
     * Suspends all live workitems in the specified case
     * @param hr the HandlerRunner instance with the workitem to suspend
     * @return a List of the workitems suspended
     */
    protected boolean suspendCase(ExletRunner hr) {
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


    /**
     * Suspends all live workitems in the specified case
     * @param caseID - the id of the case to suspend
     * @return true on successful suspend
     */
    protected boolean suspendCase(String caseID) {
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


    public boolean suspendWorkItem(WorkItemRecord wir) {
        if (wir == null) return false;

        Set<WorkItemRecord> children = new HashSet<WorkItemRecord>();
        if (wir.hasLiveStatus()) {
            children.add(wir);                          // put item in list for next call
            if (suspendWorkItemList(children)) {        // suspend the item (list)
                return true ;
            }
        }
        else {
            _log.error("Can't suspend a workitem with a status of {}", wir.getStatus());
        }
        return false ;
    }



    /**
     * Retrieves a list of 'suspendable' workitems (ie. enabled, fired or executing)
     * for a specified scope.
     * @param scope - either case (all items in a case) or spec (all items in all case
     *                instances of that specification) or task (all workitem instances
     *                of that task)
     * @param id - the id of the case/spec/task
     * @return a list of the requested workitems
     */
    protected Set<WorkItemRecord> getSuspendableWorkItems(String scope, String id) {
        Set<WorkItemRecord> result = new HashSet<WorkItemRecord>();
        for (WorkItemRecord wir : _engineClient.getLiveWorkItemsForIdentifier(scope, id)) {
            if (wir.hasLiveStatus()) result.add(wir);
        }
        return result ;
    }


    /**
     * Returns all suspendable workitems in the hierarchy of ancestor cases
     * @param caseID
     * @return the list of suspendable workitems
     */
    protected Set<WorkItemRecord> getSuspendableWorkItemsInChain(
            ExletRunnerCache runners, String caseID) {
        Set<WorkItemRecord> result = getSuspendableWorkItems("case", caseID);

        // if parent is also a worklet, get it's list too
        WorkletRunner worklet = runners.getWorkletRunner(caseID);
        while (worklet != null) {

            // get parent's caseID
            String parentCaseID = worklet.getParentCaseID();
            result.addAll(getSuspendableWorkItems("case", parentCaseID));
            worklet = runners.getWorkletRunner(parentCaseID);
        }
        return result;
    }


    /**
     * Suspends all running worklet cases in the hierarchy of handlers
     * @param runner - the runner for the child worklet case
     */
    protected boolean suspendAncestorCases(ExletRunnerCache runners,
                                         ExletRunner runner) {
        String caseID = runner.getCaseID();                    // i.e. id of parent case
        Set<WorkItemRecord> items = getSuspendableWorkItemsInChain(runners, caseID);
        return suspendAncestorCases(runner, items);
    }


    /**
     * Moves a workitem from suspended to its previous status
     * @param wir - the workitem to unsuspend
     * @return the unsuspended workitem (record)
     */
    protected WorkItemRecord unsuspendWorkItem(WorkItemRecord wir) {
        WorkItemRecord result = null ;
        if (wir.getStatus().equals(WorkItemRecord.statusSuspended)) {
            try {
                result = _engineClient.continueWorkItem(wir.getID());
                _log.debug("Successful work item unsuspend: {}", wir.getID());
            }
            catch (IOException ioe) {
                _log.error("Failed to unsuspend workitem '{}': {}", wir.getID(),
                        ioe.getMessage());
            }
        }
        else _log.error("Can't unsuspend a workitem with a status of {}", wir.getStatus());

        return result ;
    }


    /** unsuspends all previously suspended workitems in this case and/or spec */
    protected void unsuspendList(ExletRunner runner) {
        Set<String> suspendedItems = runner.getSuspendedItems();
        if (suspendedItems != null) {
            for (String wirID : suspendedItems) {
                try {
                    unsuspendWorkItem(_engineClient.getEngineStoredWorkItem(wirID));
                }
                catch (IOException ioe) {
                    _log.error("Failed to unsuspend workitem '{}': {}", wirID,
                            ioe.getMessage());
                }
            }
            _log.debug("Completed unsuspend for all suspended work items");
        }
        else _log.info("No suspended workitems to unsuspend") ;
    }

}
