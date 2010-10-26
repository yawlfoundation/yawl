/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

/**
 * @author Michael Adams
 * @date 21/10/2010
 */
public class UtilisationLogEntry {

    public enum Phase { pre, start, end }

    private long entryID;
    private String caseID;
    private String activityID;
    private String resourceID;
    private long timestamp;
    private String status;
    private Phase phase;
    private long calendarKey;

    public UtilisationLogEntry() { }

    public UtilisationLogEntry(String caseID, String activityID, String resourceID,
                               long timestamp, String status, long key) {
        setCaseID(caseID);
        setActivityID(activityID);
        setResourceID(resourceID);
        setTimestamp(timestamp);
        setStatus(status);
        setCalendarKey(key);
    }

    public long getEntryID() {
        return entryID;
    }

    public void setEntryID(long id) {
        entryID = id;
    }

    public String getCaseID() {
        return caseID;
    }

    public void setCaseID(String id) {
        caseID = id;
    }

    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String id) {
        activityID = id;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String id) {
        resourceID = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long time) {
        timestamp = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        status = s;
    }

    public long getCalendarKey() {
        return calendarKey;
    }

    public void setCalendarKey(long key) {
        calendarKey = key;
    }

    public String getKey() {
        return caseID + ":" + activityID;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase p) {
        phase = p;
    }
}
