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

package org.yawlfoundation.yawl.editor.ui.resourcing.tablemodel;

import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;
import org.yawlfoundation.yawl.editor.ui.resourcing.subdialog.NetParamDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class NetParamTableModel extends AbstractResourceTableModel {

    private List<DynParam> _dynParams;


    public NetParamTableModel() { super(); }


    public int getRowCount() {
        return _dynParams != null ? _dynParams.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        DynParam d = _dynParams.get(row);
        return String.format("%s [%s]", d.getName(), d.getRefersString());
    }

    public void setValues(List<DynParam> roles) {
        _dynParams = roles;
        fireTableDataChanged();
    }

    public List<DynParam> getValues() { return _dynParams; }

    public void handleAddRequest() {
        NetParamDialog dialog = new NetParamDialog(getOwner());
        dialog.removeItems(_dynParams);          // don't list already selected params
        dialog.setVisible(true);
        DynParam selection = dialog.getSelection();
        if (selection != null) {
            if (_dynParams == null) _dynParams = new ArrayList<DynParam>();
            _dynParams.add(selection);
        }
        Collections.sort(_dynParams);
        fireTableDataChanged();
    }

    public void handleEditRequest(int selectedRow) {
        DynParam selected = _dynParams.get(selectedRow);
        NetParamDialog dialog = new NetParamDialog(getOwner());
        dialog.loadForEdit(selected);
        dialog.setVisible(true);
        DynParam updated = dialog.getSelection();
        if (updated != null) {
            selected.setRefers(updated.getRefers());
        }
        fireTableDataChanged();
    }

    public void handleRemoveRequest(int[] selectedRows) {
        List<DynParam> toRemove = new ArrayList<DynParam>();
        for (int row : selectedRows) {
            toRemove.add(_dynParams.get(row));
        }
        _dynParams.removeAll(toRemove);
        fireTableDataChanged();
    }

}

