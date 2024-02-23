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

package org.yawlfoundation.yawl.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Workitem completion types
 * @author Michael Adams
 * @date 26/09/2016
 */
public enum WorkItemCompletion {

    Normal(0),                    // a vanilla successful completion
    Force(1),                     // a forced, but successful, completion
    Fail(2),                      // a failed, and unsuccessful, completion
    Invalid(-1);

    private int _ord;

    WorkItemCompletion(int i) { _ord = i; }

    private static final Map<Integer, WorkItemCompletion> _fromMap =
            new HashMap<Integer, WorkItemCompletion>(4);

    static {
        for (WorkItemCompletion completion : values()) {
            _fromMap.put(completion._ord, completion);
        }
    }


    public static WorkItemCompletion fromInt(int i) {
        WorkItemCompletion completion = _fromMap.get(i);
        return completion != null ? completion : Invalid;
    }
}
