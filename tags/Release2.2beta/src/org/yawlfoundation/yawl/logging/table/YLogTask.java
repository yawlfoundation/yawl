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

package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logTask table, representing a unique task 'template' of a parent net
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogTask {

    private long taskID;                          // PK - auto generated
    private String name ;
    private long parentNetID ;                    // FK to YLogNet
    private long childNetID;                      // FK to YLogNet (composite tasks only)

    public YLogTask() { }

    public YLogTask(String name, long parentNetID, long childNetID) {
        this.name = name;
        this.parentNetID = parentNetID;
        this.childNetID = childNetID;
    }

    public long getTaskID() {
        return taskID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getParentNetID() {
        return parentNetID;
    }

    public void setParentNetID(long parentNetID) {
        this.parentNetID = parentNetID;
    }

    public long getChildNetID() {
        return childNetID;
    }

    public void setChildNetID(long childNetID) {
        this.childNetID = childNetID;
    }


    public boolean equals(Object other) {
        return (other instanceof YLogTask) &&
                (this.getTaskID() == ((YLogTask) other).getTaskID());
    }

    public int hashCode() {
        return (int) (31 * getTaskID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(130);
        xml.append(String.format("<task key=\"%d\">", taskID));
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(String.valueOf(parentNetID), "parentNetKey"));
        xml.append(StringUtil.wrap(String.valueOf(childNetID), "childNetKey"));
        xml.append("</task>");
        return xml.toString();
    }
}
