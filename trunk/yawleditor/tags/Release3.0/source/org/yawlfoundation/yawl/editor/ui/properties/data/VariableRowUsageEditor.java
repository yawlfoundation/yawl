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

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class VariableRowUsageEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private final VariableTablePanel tablePanel;
    private final JComboBox usageCombo;

    private static final int SCOPE_COUNT = YDataHandler.getScopeNames().size();


    public VariableRowUsageEditor(VariableTablePanel panel) {
        tablePanel = panel;
        usageCombo = new JComboBox(panel.getScopeNames().toArray());
        usageCombo.addActionListener(this);
    }


    public Object getCellEditorValue() {
        return usageCombo.getSelectedIndex() + adjustIndex(-1);
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        tablePanel.setEditMode(true);
        usageCombo.setSelectedItem(((Integer) value) + adjustIndex(1));
        return usageCombo;
    }


    public boolean stopCellEditing() {
        tablePanel.setEditMode(false);
        return super.stopCellEditing();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        tablePanel.setEditMode(false);
        tablePanel.notifyUsageChange((Integer) getCellEditorValue());
        fireEditingStopped();
    }


    private int adjustIndex(int adjustment) {

        // scope values start at -1 for net tables (with 'Local'), 0 for task tables
        return SCOPE_COUNT == usageCombo.getItemCount() ? adjustment : 0;
    }

}
