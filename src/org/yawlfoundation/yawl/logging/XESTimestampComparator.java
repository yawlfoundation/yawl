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

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.util.XNode;

import java.util.Comparator;

/**
 * Allows XES event nodes within a trace to be sorted based on timestamp
 * <p/>
 * Author: Michael Adams
 * Creation Date: 12/08/2011
 */
public class XESTimestampComparator implements Comparator<XNode> {

    public int compare(XNode node1, XNode node2) {

        // if one object is null, ignore it and return the other as having precedence
        if (node1 == null) return -1;
        if (node2 == null) return 1;

        // if either node is not an 'event' node, return it as having precedence
        if (!node1.getName().equals("event")) return -1;
        if (!node2.getName().equals("event")) return 1;

        String stamp1 = getTimestamp(node1);
        String stamp2 = getTimestamp(node2);

        // compare timestamp strings
        int result = stamp1.compareTo(stamp2);

        // if identical timestamps, return the longer id (i.e. the child item event)
        return result == 0 ? compareInstanceIDs(node1, node2) : result;
    }


    private String getTimestamp(XNode event) {
        for (XNode date : event.getChildren("date")) {
            if (date.getAttributeValue("key").equals("time:timestamp")) {
                return date.getAttributeValue("value");
            }
        }
        return "0";
    }


    private int compareInstanceIDs(XNode node1, XNode node2) {
        String id1 = getInstanceID(node1);
        String id2 = getInstanceID(node2);
        return id2.length() - id1.length();
    }


    private String getInstanceID(XNode event) {
        for (XNode string : event.getChildren("string")) {
            if (string.getAttributeValue("key").equals("lifecycle:instance")) {
                return string.getAttributeValue("value");
            }
        }
        return "";
    }

}