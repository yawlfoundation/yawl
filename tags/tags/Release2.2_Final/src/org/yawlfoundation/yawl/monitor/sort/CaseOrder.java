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

package org.yawlfoundation.yawl.monitor.sort;

/**
 * Author: Michael Adams
* Creation Date: 9/12/2009
*/
public class CaseOrder {
    private TableSorter.CaseColumn _column;
    private boolean _ascending;

    public CaseOrder(TableSorter.CaseColumn column) {
        _column = column;
        _ascending = true;
    }

    public void setOrder(TableSorter.CaseColumn column) {
        _ascending = (column != _column) || ! _ascending;  // toggle order if same column
        _column = column;
    }

    public TableSorter.CaseColumn getColumn() { return _column; }

    public boolean isAscending() { return _ascending; }
}
