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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.util.XNode;

import java.util.Hashtable;

/**
 * @author Michael Adams
 * @date 14/09/2010
 */
public class KeyValuePair extends Hashtable<String, String> {

    public KeyValuePair() { super(); }

    public String toXML() {
        XNode node = new XNode("pairs");
        node.addChildren(this);
        return node.toString();
    }


    public String toJSON() {
        StringBuilder s = new StringBuilder("{");
        for (String key : this.keySet()) {
            if (s.length() > 1) s.append(",");
            s.append(String.format("\"%s\":\"%s\"", key, get(key)));
        }
        s.append("}");
        return s.toString();
    }
}
