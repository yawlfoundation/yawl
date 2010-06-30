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

package org.yawlfoundation.yawl.engine;

public class YCaseData {
    
	/**
     * Case execution is on a normal executing state.
     */
    public static final int SUSPEND_STATUS_NORMAL = 0;

    /**
     * Case is attempting to suspend but has one or more workitems either enabled, fired or executing.
     */
    public static final int SUSPEND_STATUS_SUSPENDING = 1;

    /**
     * Case is currently suspended with no workitems outstanding
     */
    public static final int SUSPEND_STATUS_SUSPENDED = 2;

    /**
     * Case is resuming execution from a previously suspended or suspending state.
     *
     * Note: This state should never be observed via the public API.
     */
    public static final int SUSPEND_STATUS_RESUMING = 3;

    int executionState = 0;

    String data = null;
    String id = null;

    public YCaseData() {
    }

    public YCaseData(String caseID) { id = caseID ; }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the execution status of this case.
     *
     * @return Case execution state
     */
    public int getExecutionState()
    {
        return executionState;
    }

    /**
     * Sets the execution state of this case.
     * 
     * @param executionState
     */
    public void setExecutionState(int executionState)
    {
        this.executionState = executionState;
    }


    public boolean isSuspending() { return executionState == SUSPEND_STATUS_SUSPENDING; }

    public boolean isSuspended() { return executionState == SUSPEND_STATUS_SUSPENDED; }

    public boolean isInSuspense() { return isSuspending() || isSuspended(); }
}