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

package org.yawlfoundation.yawl.cost.evaluate;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public class TaskCost {

    private String _taskID;
    private String _resourceID;
    private double _cost;

    public TaskCost(String taskID, double cost) {
        _taskID = taskID;
        _cost = cost;
    }

    public String getTaskID() {
        return _taskID;
    }

    public void setTaskID(String taskID) {
        _taskID = taskID;
    }

    public String getResourceID() {
        return _resourceID;
    }

    public void setResourceID(String resourceID) {
        _resourceID = resourceID;
    }

    public double getCost() {
        return _cost;
    }

    public void setCost(double cost) {
        _cost = cost;
    }

}
