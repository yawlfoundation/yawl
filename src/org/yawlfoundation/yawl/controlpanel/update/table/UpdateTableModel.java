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

package org.yawlfoundation.yawl.controlpanel.update.table;

import org.yawlfoundation.yawl.controlpanel.update.Differ;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * @author Michael Adams
 * @date 17/08/2014
 */
public class UpdateTableModel extends AbstractTableModel {

    private static final String[] COLUMN_LABELS =
            {"Name", "Description", "Current", "Latest", "(Un)Install"};

    protected static final int COL_NAME = 0;
    protected static final int COL_DESCRIPTION = 1;
    protected static final int COL_CURRENT = 2;
    protected static final int COL_LATEST = 3;
    protected static final int COL_INSTALL = 4;

    private List<UpdateRow> _rows;
    private Differ _differ;

    public UpdateTableModel(Differ differ) {
        super();
        setDiffer(differ);
    }

    public boolean hasUpdates() {
        for (UpdateRow row : _rows) {
            if (row.hasUpdates()) return true;
        }
        return false;
    }

    // refresh rows
    public void setDiffer(Differ differ) {
        _differ = differ;
        _rows = initRows();
        fireTableRowsUpdated(0, getRowCount()-1);
    }

    public Differ getDiffer() { return _differ; }

    public List<UpdateRow> getRows() { return _rows; }

    public int getRowCount() { return _rows.size(); }

    public int getColumnCount() { return COLUMN_LABELS.length; }

    public String getColumnName(int column) { return COLUMN_LABELS[column]; }


    public boolean isCellEditable(int row, int column) {
        return column == COL_INSTALL && _rows.get(row).isInstallable();
    }

    public Class<?> getColumnClass(int column) {
        return column == COL_INSTALL ? Boolean.class: String.class;
    }


    public Object getValueAt(int row, int column) {
        UpdateRow selected = _rows.get(row);
        switch (column) {
            case COL_NAME : return selected.getName();
            case COL_DESCRIPTION : return selected.getDescription();
            case COL_CURRENT : return selected.getCurrentBuild();
            case COL_LATEST : return selected.getLatestBuild();
            case COL_INSTALL : return selected.getInstallAction();
            default: return null;
        }
    }


    public void setValueAt(Object value, int row, int column) {
        if (column == COL_INSTALL) {
            _rows.get(row).setInstallAction((Boolean) value);
            fireTableRowsUpdated(row, row);
        }
    }


    private List<UpdateRow> initRows() {
        return new UpdateRowFactory(_differ).get();
    }

}
