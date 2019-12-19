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
 * One row of the YLogTaskInstance table, representing a runtime instance of a task
 *
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogTaskInstance {

    private long taskInstanceID;                // PK - auto generated
    private String engineInstanceID;            // either workitem or composite task id
    private long taskID;                        // FK to YLogTask
    private long parentNetInstanceID;           // FK to YLogNetInstance
    private long parentTaskInstanceID;          // null if this is not a child workitem

    public YLogTaskInstance() { }

    public YLogTaskInstance(String engineInstanceID, long taskID,
                            long parentTaskInstanceID, long parentNetInstanceID) {
        this.engineInstanceID = engineInstanceID;
        this.taskID = taskID;
        this.parentTaskInstanceID = parentTaskInstanceID;
        this.parentNetInstanceID = parentNetInstanceID;
    }

    public long getTaskInstanceID() {
        return taskInstanceID;
    }

    public void setTaskInstanceID(long taskInstanceID) {
        this.taskInstanceID = taskInstanceID;
    }

    public String getEngineInstanceID() {
        return engineInstanceID;
    }

    public void setEngineInstanceID(String engineInstanceID) {
        this.engineInstanceID = engineInstanceID;
    }

    public long getTaskID() {
        return taskID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public long getParentTaskInstanceID() {
        return parentTaskInstanceID;
    }

    public void setParentTaskInstanceID(long parentTaskInstanceID) {
        this.parentTaskInstanceID = parentTaskInstanceID;
    }

    public long getParentNetInstanceID() {
        return parentNetInstanceID;
    }

    public void setParentNetInstanceID(long parentNetInstanceID) {
        this.parentNetInstanceID = parentNetInstanceID;
    }

    public boolean equals(Object other) {
        return (other instanceof YLogTaskInstance) &&
                (this.getTaskInstanceID() == ((YLogTaskInstance) other).getTaskInstanceID());
    }

    public int hashCode() {
        return (int) (31 * getTaskInstanceID()) % Integer.MAX_VALUE;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(170);
        xml.append(String.format("<taskInstance key=\"%d\">", taskInstanceID));
        xml.append(StringUtil.wrap(engineInstanceID, "caseid"));
        xml.append(StringUtil.wrap(String.valueOf(taskID), "taskKey"));
        xml.append(StringUtil.wrap(String.valueOf(parentNetInstanceID), "parentNetKey"));
        xml.append(StringUtil.wrap(String.valueOf(parentTaskInstanceID), "parentTaskKey"));
        xml.append("</taskInstance>");
        return xml.toString();
    }

}
