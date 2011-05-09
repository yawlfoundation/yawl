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

import java.util.Comparator;

/**
 * Author: Michael Adams
 * Creation Date: 10/12/2009
 * Based on code sourced from http://stackoverflow.com/
 */
public enum CaseInstanceComparator implements Comparator<CaseInstance> {
    CaseID {
        public int compare(CaseInstance o1, CaseInstance o2) {
            return compareStrings(o1.getCaseID(), o2.getCaseID());
        }},
    SpecName {
        public int compare(CaseInstance o1, CaseInstance o2) {
            return compareStrings(o1.getSpecName(), o2.getSpecName());
        }},
    SpecVersion {
        public int compare(CaseInstance o1, CaseInstance o2) {
            return compareStrings(o1.getSpecVersion(), o2.getSpecVersion());
        }},
    StartTime {
        public int compare(CaseInstance o1, CaseInstance o2) {
            return compareLongs(o1.getStartTime(), o2.getStartTime());
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



    public static Comparator<CaseInstance> descending(final Comparator<CaseInstance> other) {
        return new Comparator<CaseInstance>() {
            public int compare(CaseInstance o1, CaseInstance o2) {
                return -1 * other.compare(o1, o2);
            }
        };
    }

    public static Comparator<CaseInstance> getComparator(final CaseInstanceComparator... multipleOptions) {
        return new Comparator<CaseInstance>() {
            public int compare(CaseInstance o1, CaseInstance o2) {
                for (CaseInstanceComparator option : multipleOptions) {
                    int result = option.compare(o1, o2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }

    public static Comparator<CaseInstance> getComparator(CaseOrder caseOrder) {
        Comparator<CaseInstance> comparator = null;
        switch (caseOrder.getColumn()) {
            case Case : comparator = getComparator(CaseID); break;
            case SpecName : comparator = getComparator(SpecName, SpecVersion, CaseID); break;
            case Version : comparator = getComparator(SpecVersion, SpecName, CaseID); break;
            case StartTime : comparator = getComparator(StartTime, CaseID);
        }
        if ((comparator != null) && ! caseOrder.isAscending()) {
            comparator = descending(comparator);
        }
        return comparator;
    }

}

