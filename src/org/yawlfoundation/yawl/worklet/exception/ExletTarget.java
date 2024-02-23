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

package org.yawlfoundation.yawl.worklet.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 2/04/12
 */
public enum ExletTarget {

    Workitem("workitem"),
    Case("case"),
    AllCases("allcases"),
    AncestorCases("ancestorCases"),
    Invalid("invalid");

    private String _targetString;

    ExletTarget(String s) { _targetString = s; }

    private static final Map<String, ExletTarget> _fromStringMap =
            new HashMap<String, ExletTarget>();

    static {
        for (ExletTarget action : values()) {
            _fromStringMap.put(action.toString(), action);
        }
    }

    public String toString() {
        return _targetString;
    }

    public static ExletTarget fromString(String s) {
        if (s != null) {
            ExletTarget target = _fromStringMap.get(s);
            if (target != null) return target;
        }
        return ExletTarget.Invalid;
    }


    public static Set<String> getStrings() { return _fromStringMap.keySet(); }

    public boolean isInvalidTarget() {
        return this == ExletTarget.Invalid;
    }

}
