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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.swing.AbstractOrderedRowTableModel;

import java.util.List;

public class CodeletSelectTableModel extends AbstractOrderedRowTableModel {

    private List<CodeletData> codeletDataList;

    private static final String[] COLUMN_LABELS = { "Name", "Description" };

    private static final int NAME_COLUMN = 0;
    private static final int DESC_COLUMN = 1;


    public CodeletSelectTableModel(List<CodeletData> codeletDataList) {
        super();
        setCodeletDataList(codeletDataList);
    }

    public List<CodeletData> getCodeletDataList() {
        return this.codeletDataList;
    }

    public void setCodeletDataList(List<CodeletData> codeletDataList) {
        this.codeletDataList = codeletDataList;
        if (codeletDataList != null) {
            codeletDataList.add(0, new CodeletData("None", "Remove a previously selected codelet"));
            setOrderedRows(codeletDataList);
        }
    }

    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }

    public String getColumnName(int columnIndex) {
        return COLUMN_LABELS[columnIndex];
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public int getRowCount() {
        if (codeletDataList != null) {
            return getCodeletDataList().size();
        }
        return 0;
    }

    public CodeletData getCodeletAt(int row) {
        return getCodeletDataList().get(row);
    }

    public String getNameAt(int row) {
        return getCodeletAt(row).getSimpleName();
    }

    public String getCanonicalNameAt(int row) {
        return getCodeletAt(row).getName();
    }

    public String getDescriptionAt(int row) {
        return getCodeletAt(row).getDescription();
    }



    public Object getValueAt(int row, int col) {
        switch (col) {
            case NAME_COLUMN:  {
                return getNameAt(row);
            }
            case DESC_COLUMN:  {
                return getDescriptionAt(row);
            }
            default: {
                return null;
            }
        }
    }
}