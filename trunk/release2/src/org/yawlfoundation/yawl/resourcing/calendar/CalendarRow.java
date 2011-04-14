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

/**
 * @author Michael Adams
 * @date 12/04/2011
 */
public class CalendarRow extends CalendarEntry {

    private String name;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");

    public CalendarRow() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String getStartTimeAsString() {
        return getTimeAsString(getStartTime());
    }

    public String getEndTimeAsString() {
        return getTimeAsString(getEndTime());
    }

    private String getTimeAsString(long time) {
        return sdf.format(time);
    }


}
