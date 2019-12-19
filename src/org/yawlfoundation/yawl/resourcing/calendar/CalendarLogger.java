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

package org.yawlfoundation.yawl.resourcing.calendar;

import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the Calendar Log - an archive of calendar entry and change activity.
 * NOTE: The caller is responsible for committing all db transactions performed by any
 * methods of this class.
 * 
 * @author Michael Adams
 * @date 18/10/2010
 */
public class CalendarLogger {

    private Persister _persister;

    public CalendarLogger() {
        _persister = Persister.getInstance();
    }


    public void log(CalendarLogEntry logEntry, CalendarEntry calEntry, boolean commit) {
        logEntry.setResourceID(calEntry.getResourceID());
        logEntry.setStatus(calEntry.getStatus());
        logEntry.setWorkload(calEntry.getWorkload());
        logEntry.setCalendarKey(calEntry.getEntryID());
        log(logEntry, commit);
    }


    public void log(CalendarLogEntry logEntry, boolean commit) {
        logEntry.setTimestamp(new Date().getTime());
        _persister.insert(logEntry, commit);
    }


    public CalendarLogEntry getLogEntry(long entryID) {
        return (CalendarLogEntry) _persister.get(CalendarLogEntry.class, entryID);
    }

    
    public CalendarLogEntry getLogEntryForCalendarKey(long calEntryID) {
        List list = getLogEntriesForCalendarKey(calEntryID);
        return (list == null) || list.isEmpty() ? null : (CalendarLogEntry) list.get(0);
    }


    public List getLogEntriesForCalendarKey(long calEntryID) {
        List list = _persister.createQuery("FROM CalendarLogEntry AS cle " +
                                           "WHERE cle.calendarKey=:key " +
                                           "ORDER BY cle.entryID DESC")
                .setLong("key", calEntryID)
                .list();
        return (list == null) || list.isEmpty() ? null : list;
    }


    public List getLogEntriesForReservation(String caseID, String activityName, 
                                            String resourceRec) {
        return _persister.createQuery("FROM CalendarLogEntry AS cle " +
                                           "WHERE cle.caseID=:caseID " +
                                           "AND cle.activityName=:activityName " +
                                           "AND cle.resourceRec=:resourceRec")
                .setString("caseID", caseID)
                .setString("activityName", activityName)
                .setString("resourceRec", resourceRec)
                .list();
    }


    public List getLogEntriesForActivity(String caseID, String activityName) {
        return _persister.createQuery("FROM CalendarLogEntry AS cle " +
                                           "WHERE cle.caseID=:caseID " +
                                           "AND cle.activityName=:activityName")
                .setString("caseID", caseID)
                .setString("activityName", activityName)
                .list();
    }


    public List getLogEntriesForCase(String caseID) {
        return _persister.createQuery("FROM CalendarLogEntry AS cle " +
                                           "WHERE cle.caseID=:caseID ")
                .setString("caseID", caseID)
                .list();
    }


    public Set<Long> getEntryIDsForActivity(String caseID, String activityName) {
        Set<Long> idSet = new HashSet<Long>();
        List list = getLogEntriesForActivity(caseID, activityName);
        if (list != null) {
            for (Object o : list) {
                idSet.add(((CalendarLogEntry) o).getCalendarKey());
            }
        }
        return idSet;
    }

    public Set<Long> getEntryIDsForCase(String caseID) {
        Set<Long> idSet = new HashSet<Long>();
        List list = getLogEntriesForCase(caseID);
        if (list != null) {
            for (Object o : list) {
                idSet.add(((CalendarLogEntry) o).getCalendarKey());
            }
        }
        return idSet;
    }


    public String getResourceRecord(long calendarKey) {
        CalendarLogEntry logEntry = getLogEntryForCalendarKey(calendarKey);
        return (logEntry != null) ? logEntry.getResourceRec() : null;
    }

}
