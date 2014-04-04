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

import org.yawlfoundation.yawl.editor.ui.resourcing.subdialog.ListDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.listmodel.NonHumanResourceListModel;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class NonHumanResourceTableModel extends AbstractResourceTableModel {

    private List<NonHumanResource> _resources;

    public NonHumanResourceTableModel() { super(); }


    public int getRowCount() {
        return _resources != null ? _resources.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        return _resources.get(row).getName();
    }

    public void setValues(List<NonHumanResource> resources) {
        _resources = resources;
        fireTableDataChanged();
    }

    public List<NonHumanResource> getValues() {
        return _resources != null ? _resources : Collections.<NonHumanResource>emptyList();
    }


    public void handleAddRequest() {
        ListDialog listDialog = new ListDialog(getOwner(), new NonHumanResourceListModel(),
                "All Non-human Resources");
        listDialog.setVisible(true);
        if (_resources == null) _resources = new ArrayList<NonHumanResource>();
        for (Object o : listDialog.getSelections()) {
            NonHumanResource resource = (NonHumanResource) o;
            if (! _resources.contains(resource)) _resources.add(resource);
        }
        Collections.sort(_resources);
        fireTableDataChanged();
    }


    public void handleRemoveRequest(int[] selectedRows) {
        List<NonHumanResource> toRemove = new ArrayList<NonHumanResource>();
        for (int row : selectedRows) {
            toRemove.add(_resources.get(row));
        }
        _resources.removeAll(toRemove);
        fireTableDataChanged();
    }

    // editing not required for this model
    public void handleEditRequest(int selectedRow) { }

}

