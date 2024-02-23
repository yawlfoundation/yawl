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

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

/** class/structure used to store and retrieve event data items */
class WorkItemConstraintData {

    private WorkItemRecord _wir ;
    private String _data ;
    private boolean _preCheck ;

    public WorkItemConstraintData(WorkItemRecord wir, String data, boolean preCheck) {
        _wir = wir ;
        _data = data ;
        _preCheck = preCheck ;
    }

    public WorkItemRecord getWIR() { return _wir; }

    public String getData() { return _data; }

    public boolean getPreCheck() { return _preCheck; }

}
