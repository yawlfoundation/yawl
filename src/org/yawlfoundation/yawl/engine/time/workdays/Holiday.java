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

package org.yawlfoundation.yawl.engine.time.workdays;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author Michael Adams
 * @date 26/5/17
 */
public class Holiday implements Comparable<Holiday> {

    private Calendar _date;
    private String _name;

    protected Holiday() { }                                         // for persistence

    public Holiday(int day, int month, int year, String name) {
        _date = new GregorianCalendar(year, month, day);
        _name = name;
    }

    public Holiday(XNode node) {
        fromXNode(node);
    }


    public boolean matches(Calendar other) {
        return _date != null && other != null &&
            _date.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            _date.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isAfter(Calendar date) {
        return _date != null && date != null && _date.after(date);
    }

    public int getYear() {
        return _date != null ? _date.get(Calendar.YEAR) : -1;
    }


    public String getName() { return _name; }

    public void setName(String name) { _name = name; }


    public long getTime() { return _date != null ? _date.getTimeInMillis() : -1; }

    public void setTime(long time) {
        if (time > -1) {
            _date = new GregorianCalendar();
            _date.setTimeInMillis(time);
        }
    }


    public String toString() {
        String date = _date != null ? _date.toString() : "";
        return _name + ": " + date;
    }


    public int compareTo(Holiday other) {
        if (other == null) return 1;
        return Long.signum(this.getTime() - other.getTime());
    }


    private void fromXNode(XNode node) {
        XNode dateNode = node.getChild("date");
        if (dateNode != null) {
            int day = getIntValue(dateNode, "day");
            int month = getIntValue(dateNode, "month");
            int year = getIntValue(dateNode, "year");
            if (day > 0 && month > 0 && year > 0) {
                _date = new GregorianCalendar(year, month, day);
            }
        }
        _name = node.getChildText("localName", true);     // escape
    }
    

    private int getIntValue(XNode dateNode, String name) {
        return StringUtil.strToInt(dateNode.getChildText(name), 0);
    }

}
