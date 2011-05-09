/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  An event log file implementation.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class EventLogger {

    // event type descriptors
    public static final String eCheckOut = "CheckOutWorkItem";
    public static final String eDecline = "DeclineWorkItem";
    public static final String eUndoCheckOut = "UndoCheckOutWorkItem";
    public static final String eLaunch = "WorkletLaunched";
    public static final String eCheckIn = "CheckInWorkItem";
    public static final String eCancel = "WorkletCancelled";
    public static final String eComplete = "WorkletCompleted";

    // path to eventlog csv file
    private static final String _logPath = Library.wsLogsDir + "eventLog.csv" ;

    // date format for the eventlog
    private	static SimpleDateFormat _sdfe  = new SimpleDateFormat (
            "yyyy.MM.dd hh:mm:ss:SS");

    // log for exception dumps
    private static Logger elog = Logger.getLogger("org.yawlfoundation.yawl.worklet.support.EventLogger");

//===========================================================================//

    /**
     *  writes an event to the event log
     *  @param event - the type of event to log
     *  @param caseId - the case that caused the event
     *  @param specId - the specification id of the case
     *  @param taskId - the id of the task the workletwas subbed for
     *  @param parentCaseId - the case id of the original workitem
     *  @param xType - the reason for raising a worklet case (maps to WorkletService.XTYPE)
     */
    public static void log(DBManager mgr, String event, String caseId, YSpecificationID specId,
                           String taskId, String parentCaseId, int xType) {

        if (mgr != null) {
            WorkletEvent we = new WorkletEvent(event, caseId, specId, taskId,
                    parentCaseId, xType);
            mgr.persist(we, DBManager.DB_INSERT);
        }
        else {
            logToCSV(event, caseId, specId, taskId, parentCaseId, xType);
        }
    }


    /** this version is used to log to a CSV file when persistence is OFF */
    private static void logToCSV(String event, String caseId, YSpecificationID specId,
                                 String taskId, String parentCaseId, int xType) {
        StringBuilder s = new StringBuilder() ;
        s.append(_sdfe.format(new Date())) ; s.append(",") ;
        s.append(event); s.append(",") ;
        s.append(caseId); s.append(",") ;
        s.append(specId.toString()); s.append(",") ;
        s.append(taskId); s.append(",") ;
        s.append(parentCaseId); s.append(",") ;
        s.append(xType);

        try {
            PrintWriter pLog = new PrintWriter(new FileWriter(_logPath, true));
            pLog.println(s.toString()) ;
            pLog.close() ;
        }
        catch (IOException e) {
            elog.error("Exception writing to CSV EventLog", e);
        }
    }

//===========================================================================//

    /**
     *  writes an event to the event log
     *  @param event - the type of event to log
     *  @param wir - the workitem that triggered the event
     */
    public static void log(DBManager mgr, String event, WorkItemRecord wir, int xType) {
        log(mgr, event, wir.getCaseID(), new YSpecificationID(wir),
                wir.getTaskID(), "", xType);
    }

//===========================================================================//
//===========================================================================//

}

