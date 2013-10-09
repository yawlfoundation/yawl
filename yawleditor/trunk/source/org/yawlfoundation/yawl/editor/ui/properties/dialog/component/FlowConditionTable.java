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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class FlowConditionTable extends JSingleSelectTable {

    private boolean orderChanged;

    public FlowConditionTable(FlowConditionTableModel model) {
        super();
        consumeEnterKeyWraps();
        setModel(model);
        setRowHeight(getRowHeight() + 5);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
    }


    public FlowConditionTableModel getTableModel() {
        return (FlowConditionTableModel) getModel();
    }


    public void setFlows(List<YAWLFlowRelation> flows) {
        getTableModel().setFlows(flows);
        setPreferredScrollableViewportSize(getPreferredSize());
        updateUI();
    }


    public List<YAWLFlowRelation> getFlows() {
        return getTableModel().getFlows();
    }


    public YAWLFlowRelation getSelectedFlow() {
        return getTableModel().getFlowAtRow(getSelectedRow());
    }


    public void moveSelectedRowUp() {
        int selectedRow = getSelectedRow();
        if (selectedRow > 0) {
            getTableModel().swapRows(selectedRow, selectedRow - 1);
            selectRow(selectedRow - 1);
            orderChanged = true;
        }
    }

    public void moveSelectedRowDown() {
        int selectedRow = getSelectedRow();
        if (selectedRow < getRowCount() - 1) {
            getTableModel().swapRows(selectedRow, selectedRow + 1);
            selectRow(selectedRow + 1);
            orderChanged = true;
        }
    }


    public boolean hasChangedRowOrder() { return orderChanged; }


    public boolean allowPredicateEdit() {
        YAWLFlowRelation flow = getSelectedFlow();
        return ! (flow != null && flow.hasXorSplitAsSource() && flow.isDefaultFlow());
    }


    /**
     * This method adds a custom action to prevent wrapping to the first table row
     * when the enter key is pressed while on the last table row - that is, it
     * overrides default enter key behaviour and stays on the last row.
     *
     * Based on code sourced from stackoverflow.com
     */
    private void consumeEnterKeyWraps() {
        Object key = getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .get(KeyStroke.getKeyStroke("ENTER"));
        final Action action = getActionMap().get(key);

        Action custom = new AbstractAction("wrap") {
            public void actionPerformed(ActionEvent e) {
                int row = getSelectionModel().getLeadSelectionIndex();
                if (row == getRowCount() - 1) {
                    if (isEditing()) getCellEditor().stopCellEditing();
                    return;     // stop wrapping to top of table
                }
                action.actionPerformed(e);
            }

        };
        getActionMap().put(key, custom);
    }

}
