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

package org.yawlfoundation.yawl.balancer;

import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 22/9/17
 */
public class Actions {

    private static final List<String> SESSION_ACTIONS = Arrays.asList(
            "checkConnection", "connect", "checkIsAdmin", "disconnect");

    private static final List<String> ITEM_ACTIONS = Arrays.asList(
            "checkout", "checkin", "getChildren", "getWorkItem", "getStartingDataSnapshot",
            "checkAddInstanceEligible", "createInstance", "rejectAnnouncedEnabledTask",
            "suspend", "rollback", "unsuspend", "skip");

    private static final List<String> CASE_ACTIONS = Arrays.asList(
            "cancelCase", "getSpecificationForCase", "getSpecificationIDForCase",
            "getCaseState", "getCaseData", "getWorkItemInstanceSummary");

    private static final List<String> SPEC_ACTIONS = Arrays.asList(
            "upload", "unload");


    public boolean isSessionAction(String action) {
        return SESSION_ACTIONS.contains(action);
    }


    public boolean isItemAction(String action) {
        return ITEM_ACTIONS.contains(action);
    }


    public boolean isLaunchAction(String action) {
        return action.equals("launchCase");
    }


    public boolean isCaseAction(String action) {
        return CASE_ACTIONS.contains(action);
    }


    public boolean isSpecAction(String action) {
        return SPEC_ACTIONS.contains(action);
    }

}
