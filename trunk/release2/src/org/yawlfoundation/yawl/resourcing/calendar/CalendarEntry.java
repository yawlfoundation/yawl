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

package org.yawlfoundation.yawl.resourcing.calendar;

/**
 * Author: Michael Adams
 * Creation Date: 12/03/2010
 */
public class CalendarEntry implements Cloneable {

    private long entryID;                              // hibernate PK
    private String resourceID;
    private long startTime;
    private long endTime;
    private String status;
    private int workload;
    private String agent;                             // user/service that made the entry
    private long chainID;                             // opt. FK relation between entries
    private String comment;

    public CalendarEntry() {}

    public CalendarEntry(String resID, long start, long end,
                         ResourceCalendar.Status st, int wload, String agt, String cmt) {
        resourceID = resID;
        startTime = start;
        endTime = end;
        status = st.name();
        workload = wload;
        agent = agt;
        comment = cmt;
    }

    public long getEntryID() {
        return entryID;
    }

    public void setEntryID(long id) {
        entryID = id;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resID) {
        resourceID = resID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long time) {
        startTime = time;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long time) {
        endTime = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String st) {
        status = st;
    }

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int load) {
        if (load < 1) load = 100;
        workload = load;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agt) {
        agent = agt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String cmt) {
        comment = cmt;
    }

    public long getChainID() {
        return chainID;
    }

    public void setChainID(long id) {
        chainID = id;
    }

    public CalendarEntry clone()  throws CloneNotSupportedException {
        return (CalendarEntry) super.clone();
    }
}
