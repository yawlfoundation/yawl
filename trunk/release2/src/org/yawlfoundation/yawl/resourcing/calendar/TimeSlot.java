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

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * @author Michael Adams
 * @date 6/09/2010
 */
public class TimeSlot {

    private long _start;
    private long _end;

    public TimeSlot(long from, long to) {
        _start = from;
        _end = to;
    }


    public long getStart() { return _start; }

    public void setStart(long start) { _start = start; }

    public long getEnd() { return _end; }

    public void setEnd(long end) { _end = end; }


    public String toXML() {
        return toXNode().toString();
    }

    public XNode toXNode() {
        XNode node = new XNode("timeslot");
        node.addChild("start", _start);
        node.addChild("end", _end);
        return node;
    }

    public void fromXML(String xml) throws NumberFormatException {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) {
            _start = new Long(node.getChildText("start"));
            _end = new Long(node.getChildText("end"));
        }
    }

}
