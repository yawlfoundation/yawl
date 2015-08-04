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

package org.yawlfoundation.yawl.worklet.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 2/04/12
 */
public enum ExletAction {

    Continue("continue"),
    Suspend("suspend"),
    Remove("remove"),
    Restart("restart"),
    Complete("complete"),
    Fail("fail"),
    Compensate("compensate"),
    Rollback("rollback"),
    Select("select"),
    Invalid("invalid");

    private String _actionString;

    ExletAction(String s) { _actionString = s; }

    private static final Map<String, ExletAction> _fromStringMap =
            new HashMap<String, ExletAction>();

    static {
        for (ExletAction action : values()) {
            _fromStringMap.put(action.toString(), action);
        }
    }

    public String toString() {
        return _actionString;
    }

    public static ExletAction fromString(String s) {
        if (s != null) {
            ExletAction action = _fromStringMap.get(s);
            if (action != null) return action;
        }
        return ExletAction.Invalid;
    }

}
