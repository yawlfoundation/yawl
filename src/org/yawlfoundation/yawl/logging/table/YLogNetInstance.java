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
 * One row of the logNetInstance table, representing a runtime instance of a net. Note
 * that the engineInstanceID of a root net = the case id of the running case instance
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogNetInstance {

    private long netInstanceID ;          // PK - auto generated
    private String engineInstanceID;      // the 'case id' of the net instance
    private long netID ;                  // FK to YLogNet
    private long parentTaskInstanceID ; // FK to YLogTaskInstance (composite tasks only)

    public YLogNetInstance() { }

    public YLogNetInstance(String engineInstanceID, long netID, long parentTaskInstanceID) {
        this.engineInstanceID = engineInstanceID;
        this.netID = netID;
        this.parentTaskInstanceID = parentTaskInstanceID;
    }

    public long getNetInstanceID() {
        return netInstanceID;
    }

    public void setNetInstanceID(long netInstanceID) {
        this.netInstanceID = netInstanceID;
    }

    public String getEngineInstanceID() {
        return engineInstanceID;
    }

    public void setEngineInstanceID(String engineInstanceID) {
        this.engineInstanceID = engineInstanceID;
    }

    public long getNetID() {
        return netID;
    }

    public void setNetID(long netID) {
        this.netID = netID;
    }

    public long getParentTaskInstanceID() {
        return parentTaskInstanceID;
    }

    public void setParentTaskInstanceID(long parentTaskInstanceID) {
        this.parentTaskInstanceID = parentTaskInstanceID;
    }


    public boolean equals(Object other) {
        return (other instanceof YLogNetInstance) &&
                (this.getNetInstanceID() == ((YLogNetInstance) other).getNetInstanceID());
    }

    public int hashCode() {
        return (int) (31 * getNetInstanceID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(140);
        xml.append(String.format("<netInstance key=\"%d\">", netInstanceID));
        xml.append(StringUtil.wrap(engineInstanceID, "caseid"));
        xml.append(StringUtil.wrap(String.valueOf(netID), "netKey"));
        xml.append(StringUtil.wrap(String.valueOf(parentTaskInstanceID), "parentTaskInstanceKey"));
        xml.append("</netInstance>");
        return xml.toString();
    }

}
