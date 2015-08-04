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

package org.yawlfoundation.yawl.monitor.sort;

import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;

import java.util.Comparator;

/**
 * Author: Michael Adams
 * Creation Date: 10/12/2009
 * Based on code sourced from http://stackoverflow.com/
 */
public enum ItemInstanceComparator implements Comparator<WorkItemInstance> {
    ItemID {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareStrings(o1.getID(), o2.getID());
        }},
    TaskID {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareStrings(o1.getTaskID(), o2.getTaskID());
        }},
    Status {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareStrings(o1.getStatus(), o2.getStatus());
        }},
    Service {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareStrings(o1.getResourceName(), o2.getResourceName());
        }},
    EnabledTime {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareLongs(o1.getEnabledTime(), o2.getEnabledTime());
        }},
    StartTime {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareLongs(o1.getStartTime(), o2.getStartTime());
        }},
    CompletionTime {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareLongs(o1.getCompletionTime(), o2.getCompletionTime());
        }},
    TimerStatus {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareStrings(o1.getTimerStatus(), o2.getTimerStatus());
        }},
    TimerExpiry {
        public int compare(WorkItemInstance o1, WorkItemInstance o2) {
            return compareLongs(o1.getTimerExpiry(), o2.getTimerExpiry());
        }};



   private static int compareStrings(String a, String b) {
       if (a == null) return -1;
       if (b == null) return 1;
       return a.compareTo(b);
   }

   private static int compareLongs(long a, long b) {
        long difference = a - b;

        // guard against integer overrun
        if (difference > Integer.MAX_VALUE)
            difference = 1;
        else if (difference < Integer.MIN_VALUE)
            difference = -1;

        return (int) difference;
    }



    public static Comparator<WorkItemInstance> descending(final Comparator<WorkItemInstance> other) {
        return new Comparator<WorkItemInstance>() {
            public int compare(WorkItemInstance o1, WorkItemInstance o2) {
                return -1 * other.compare(o1, o2);
            }
        };
    }

    public static Comparator<WorkItemInstance> getComparator(final ItemInstanceComparator... multipleOptions) {
        return new Comparator<WorkItemInstance>() {
            public int compare(WorkItemInstance o1, WorkItemInstance o2) {
                for (ItemInstanceComparator option : multipleOptions) {
                    int result = option.compare(o1, o2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }

    
    public static Comparator<WorkItemInstance> getComparator(ItemOrder itemOrder) {
        Comparator<WorkItemInstance> comparator = null;
        switch (itemOrder.getColumn()) {
            case ItemID : comparator = getComparator(ItemID); break;
            case TaskID : comparator = getComparator(TaskID, ItemID); break;
            case Status : comparator = getComparator(Status, ItemID); break;
            case Service : comparator = getComparator(Service, ItemID); break;
            case EnabledTime : comparator = getComparator(EnabledTime, ItemID); break;
            case StartTime : comparator = getComparator(StartTime, ItemID); break;
            case CompletionTime : comparator = getComparator(CompletionTime, ItemID); break;
            case TimerStatus : comparator = getComparator(TimerStatus, ItemID); break;
            case TimerExpiry : comparator = getComparator(TimerExpiry, ItemID);
        }
        if ((comparator != null) && ! itemOrder.isAscending()) {
            comparator = descending(comparator);
        }
        return comparator;
    }

}