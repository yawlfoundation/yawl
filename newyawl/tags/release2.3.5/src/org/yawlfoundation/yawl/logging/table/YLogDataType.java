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

package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logDataType table, representing a unique data type definition
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogDataType {

    private long dataTypeID;                                   // PK - auto generated
    private String definition ;
    private String name;

    public YLogDataType() { }

    public YLogDataType(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    public long getDataTypeID() {
        return dataTypeID;
    }

    public void setDataTypeID(long dataTypeID) {
        this.dataTypeID = dataTypeID;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        return (other instanceof YLogDataType) &&
                (this.getDataTypeID() == ((YLogDataType) other).getDataTypeID());
    }

    public int hashCode() {
        return (int) (31 * getDataTypeID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(150);
        xml.append(String.format("<datatype key=\"%d\">", dataTypeID));
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(definition, "definition"));
        xml.append("</datatype>");
        return xml.toString();
    }
}
