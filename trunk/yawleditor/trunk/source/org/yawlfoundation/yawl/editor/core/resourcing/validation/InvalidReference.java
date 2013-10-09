/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing.validation;

/**
 * Author: Michael Adams
 */
public abstract class InvalidReference {

    private String _id;
    private String _msgName;
    private String _netID;
    private String _taskID;


    protected InvalidReference(String id, String msgName) {
        _id = id;
        _msgName = msgName;
    }

    public String getID() { return _id; }

    public void setNetID(String id) { _netID = id; }

    public void setTaskID(String id) { _taskID = id; }

    public String getMessage() {
        StringBuilder s = new StringBuilder();
        s.append("Task [").append(_taskID).append("]");
        s.append(" in net [").append(_netID).append("]");
        s.append(" contains an invalid ");
        s.append(_msgName);
        s.append(" reference [").append(_id).append("].");
        return s.toString();
    }
}
