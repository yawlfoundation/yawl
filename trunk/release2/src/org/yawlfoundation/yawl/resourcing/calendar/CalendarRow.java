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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michael Adams
 * @date 12/04/2011
 */
public class CalendarRow extends CalendarEntry {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");
    private static final SimpleDateFormat extdFormat = new SimpleDateFormat("H:mm '('dd'/'MM')'");
    private static final long MSECS_IN_ONE_DAY = 86400000;

    private String name;
    private Date baseDate;

    public CalendarRow() {
        super();
    }

    public CalendarRow(CalendarEntry entry) {
        super(entry.getResourceID(), entry.getStartTime(), entry.getEndTime(),
              entry.getStatus(), entry.getWorkload(),entry.getAgent(), entry.getComment());
        setEntryID(entry.getEntryID());
        setChainID(entry.getChainID());
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public Date getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(Date date) {
        baseDate = date;
    }

    public String getStartTimeAsString() {
        return getTimeAsString(getStartTime());
    }

    public String getEndTimeAsString() {
        return getTimeAsString(getEndTime());
    }

    private String getTimeAsString(long time) {
        long diff = time - baseDate.getTime();
        return ((diff >= 0) && (diff <= MSECS_IN_ONE_DAY)) ? timeFormat.format(time) :
                extdFormat.format(time);
    }


    // required for hibernate
    public CalendarEntry toCalendarEntry() {
        CalendarEntry entry = new CalendarEntry(getResourceID(), getStartTime(),
                getEndTime(), getStatus(), getWorkload(), getAgent(), getComment());
        entry.setChainID(getChainID());
        entry.setEntryID(getEntryID());
        return entry;
    }

}
