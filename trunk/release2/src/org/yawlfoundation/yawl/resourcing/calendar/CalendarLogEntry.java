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

package org.yawlfoundation.yawl.resourcing.calendar;

/**
 * @author Michael Adams
 * @date 21/10/2010
 */
public class CalendarLogEntry {

    private long entryID;
    private String caseID;
    private String activityName;
    private String resourceID;
    private String resourceRec;
    private long timestamp;
    private String status;
    private int workload;
    private String phase;
    private String agent;
    private long calendarKey;

    public CalendarLogEntry() { }

    public CalendarLogEntry(String caseID, String activityID, String resourceID,
                               long timestamp, String status, long key) {
        setCaseID(caseID);
        setActivityName(activityID);
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String name) {
        activityName = name;
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
        return caseID + ":" + activityName;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String p) {
        phase = p;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agt) {
        agent = agt;
    }

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int load) {
        workload = load;
    }

    public String getResourceRec() {
        return resourceRec;
    }

    public void setResourceRec(String rec) {
        resourceRec = rec;
    }
}
