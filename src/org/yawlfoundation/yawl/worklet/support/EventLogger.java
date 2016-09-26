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

package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

/**
 *  An event log file implementation.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class EventLogger {

    private static long lastID = 0;

    // event type descriptors
    public static final String eCheckOut = "CheckOutWorkItem";
    public static final String eDecline = "DeclineWorkItem";
    public static final String eUndoCheckOut = "UndoCheckOutWorkItem";
    public static final String eLaunch = "WorkletLaunched";
    public static final String eCheckIn = "CheckInWorkItem";
    public static final String eCancel = "WorkletCancelled";
    public static final String eComplete = "WorkletCompleted";


    /**
     *  writes an event to the event log
     *  @param event - the type of event to log
     *  @param caseId - the case that caused the event
     *  @param specId - the specification id of the case
     *  @param taskId - the id of the task the worklet was subbed for
     *  @param parentCaseId - the case id of the original workitem
     *  @param xType - the reason for raising a worklet case (maps to WorkletService.XTYPE)
     */
    public static boolean log(String event, String caseId,
                           YSpecificationID specId, String taskId,
                           String parentCaseId, int xType) {

        if (Persister.getInstance().isPersisting()) {
            return Persister.insert(new WorkletEvent(getNextID(), event, caseId, specId, taskId,
                                parentCaseId, xType));
        }
        return false;
    }


    /**
     *  writes an event to the event log
     *  @param event - the type of event to log
     *  @param wir - the workitem that triggered the event
     */
    public static boolean log(String event, WorkItemRecord wir, int xType) {
        return log(event, wir.getCaseID(), new YSpecificationID(wir),
                wir.getTaskID(), "", xType);
    }


    /**
     * This non-native private key generation method is required because:
     *   - on recursive calls, the original implementation (using currentTimeMillis) was
     *     producing duplicate keys; and
     *   - H2 databases do not respond well to changing hibernate's key method from
     *     assigned to native
     * @return a id key unique to the eventlog table
     */
    private static long getNextID() {
        long id = System.currentTimeMillis();
        if (lastID == id) {
            id += 4;
        }
        lastID = id;
        return id;
    }

}

