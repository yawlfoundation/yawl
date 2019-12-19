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

package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logNet table, representing a unique net 'template' of a specification 
 *
 * Author: Michael Adams
 * Creation Date: 9/04/2009
 */
public class YLogNet {

    private long netID;                                       // PK - auto generated 
    private String name;
    private long specKey;                                     // FK to YLogSpecification

    public YLogNet() { }

    public YLogNet(String name, long specKey) {
        this.name = name;
        this.specKey = specKey;
    }


    public long getSpecKey() {
        return specKey;
    }

    public void setSpecKey(long specKey) {
        this.specKey = specKey;
    }

    public long getNetID() {
        return netID;
    }

    public void setNetID(long netID) {
        this.netID = netID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        return (other instanceof YLogNet) &&
                (this.getNetID() == ((YLogNet) other).getNetID());
    }

    public int hashCode() {
        return (int) (31 * getNetID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(90);
        xml.append(String.format("<net key=\"%d\">", netID)) ;
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(String.valueOf(specKey), "specKey"));
        xml.append("</net>");
        return xml.toString();
    }

}
