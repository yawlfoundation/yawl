package org.yawlfoundation.yawl.worklet.exception;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 8/02/2016
 */
public class CaseStartEventMap {

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

    private final Map<String, WorkItemConstraintData> _constraintMap =
            new HashMap<String, WorkItemConstraintData>();

    private final Set<String> _casesStarted = new HashSet<String>();


    public void addCasePreCheckComplete(String caseID) { _casesStarted.add(caseID); }


    public boolean isCasePreCheckComplete(String caseID) {
        return _casesStarted.contains(caseID);
    }


    public void caseCancelled(String caseID) {
        _casesStarted.remove(caseID);
    }


    public void caseCompleted(String caseID) {
        _casesStarted.remove(caseID);
    }

    /** stores the data passed to the event for later processing */
    public void addCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                  boolean preCheck) {
        _constraintMap.put(wir.getRootCaseID(),
                new WorkItemConstraintData(wir, data, preCheck));
    }


    /** retrieves the data passed to the initial event and recalls it */
    public WorkItemConstraintData getCheckWorkItemConstraintEvent(String caseID) {
        return _constraintMap.remove(caseID);
    }

}
