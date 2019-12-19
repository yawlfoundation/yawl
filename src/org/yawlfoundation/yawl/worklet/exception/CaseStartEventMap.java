/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
     * event is sometimes received before a start-of-case check constraint event. We
     * should ensure that the start-of-case check is always done first. Also,
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
