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
 * @author Michael Adams
 * @date 6/09/2010
 */
public class TimeSlot {

    private long _start;
    private long _end;
    private String _status;
    private int _percentAvailable;

    public TimeSlot(long from, long to) {
        _start = from;
        _end = to;
        _percentAvailable = 100;
    }


    public TimeSlot(long from, long to, String status) {
        this(from, to);
        _status = status;
    }


    public TimeSlot(long from, long to, String status, int percentAvailable) {
        this(from, to, status);
        _percentAvailable = percentAvailable;
    }


    public TimeSlot(CalendarEntry entry) {
        this(entry.getStartTime(), entry.getEndTime(), entry.getStatus(),
                100 - entry.getWorkload());    // % free
    }


    /****************************************************************************/

    public long getStart() { return _start; }

    public void setStart(long start) { _start = start; }


    public long getEnd() { return _end; }

    public void setEnd(long end) { _end = end; }


    public String getStatus() { return _status; }

    public void setStatus(String status) { _status = status; }


    public int getAvailability() { return _percentAvailable; }

    public void setAvailability( int available) { _percentAvailable = available; }


    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("timeslot");
        node.addChild("start", StringUtil.longToDateTime(_start));
        node.addChild("end", StringUtil.longToDateTime(_end));
        node.addChild("status", _status);
        node.addChild("availability", _percentAvailable);
        return node;
    }

    public void fromXML(String xml) throws NumberFormatException {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) {
            _start = StringUtil.xmlDateToLong(node.getChildText("start"));
            _end = StringUtil.xmlDateToLong(node.getChildText("end"));
            _status = node.getChildText("status");
            _percentAvailable = new Integer(node.getChildText("availability"));
        }
    }

}
