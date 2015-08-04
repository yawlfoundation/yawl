/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.resourcing.calendar.CalendarRow;

import java.util.Comparator;

/**
 * Allows CalendarRow objects to be sorted on start times for display in
 * 'Calendar Mgt' form
 *
 * Author: Michael Adams
 * Creation Date: 28/03/2011
 */
public class CalendarRowComparator implements Comparator<CalendarRow> {


	public int compare(CalendarRow row1, CalendarRow row2)	{

      // if one object is null, ignore it and return the other as having precedence
      if (row1 == null) return -1;
      if (row2 == null) return 1;

      long diff = row1.getStartTime() - row2.getStartTime();     // compare start times
      if (diff == 0) {
          diff = row1.getEndTime() - row2.getEndTime();          // then end times
      }
      if (diff == 0) {
          diff = row1.getName().compareTo(row2.getName());       // then names
      }
      
      return (int) diff;
    }

}