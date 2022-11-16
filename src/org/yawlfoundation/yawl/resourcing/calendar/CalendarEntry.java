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

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

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
        this(resID, start, end, st.name(), wload, agt, cmt);
    }

    public CalendarEntry(String resID, long start, long end,
                         String st, int wload, String agt, String cmt) {
        resourceID = resID;
        startTime = start;
        endTime = end;
        status = st;
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

    public boolean hasPeriod(long start, long end) {
        return (startTime == start) && (endTime == end);
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


    public String toXML() {
        return toXNode().toString();
    }


    public void fromXML(String xml) {
        fromXNode(new XNodeParser().parse(xml));
    }


    public XNode toXNode() {
        XNode node = new XNode("calendarentry");
        node.addChild("entryid", entryID);
        node.addChild("resourceid", resourceID);
        node.addChild("chainid", chainID);
        node.addChild("from", startTime);
        node.addChild("to", endTime);
        node.addChild("status", status);
        node.addChild("workload", workload);
        node.addChild("agent", agent);
        node.addChild("comment", comment);
        return node;
    }


    public void fromXNode(XNode node) {
        if (node != null) {
            setEntryID(StringUtil.strToLong(node.getChildText("entryid"), 0));
            setResourceID(node.getChildText("resourceid"));
            setChainID(StringUtil.strToLong(node.getChildText("chainid"), 0));
            setStartTime(StringUtil.strToLong(node.getChildText("from"), 0));
            setEndTime(StringUtil.strToLong(node.getChildText("to"), 0));
            setStatus(node.getChildText("status"));
            setWorkload(StringUtil.strToInt(node.getChildText("workload"), 0));
            setAgent(node.getChildText("agent"));
            setComment(node.getChildText("comment"));
        }
    }

}
