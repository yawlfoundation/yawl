/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.view;

import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.MiniToolBar;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Michael Adams
 * @date 9/08/13
 */
public class BindingViewTablePanel extends JPanel implements ListSelectionListener {

    private BindingViewTable table;
    private MiniToolBar toolBar;

    public BindingViewTablePanel(ActionListener listener, List<VariableRow> variableRows) {
        table = createTable(variableRows);
        init(listener, "In");
    }


    public BindingViewTablePanel(ActionListener listener, DataVariableDialog dataDialog) {
        table = createTable(dataDialog);
        init(listener, "Out");
    }


    public BindingViewTable getTable() { return table; }


    public void valueChanged(ListSelectionEvent event) {
        toolBar.enableComponents(table.getSelectedRowCount() > 0);
    }


    private void init(ActionListener listener, String prefix) {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(prefix + "put Bindings"));
        add(createToolBar(listener, prefix), BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setSize(new Dimension(400, 120));
        scrollPane.setPreferredSize(new Dimension(400, 125));
        add(scrollPane, BorderLayout.CENTER);
    }


    private BindingViewTable createTable(List<VariableRow> rows) {
        table = new InputBindingViewTable(new InputBindingViewTableModel());
        table.setRows(rows);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private BindingViewTable createTable(DataVariableDialog dataDialog) {
        table = new OutputBindingViewTable(new OutputBindingViewTableModel());
        table.setRows(dataDialog);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar(ActionListener listener, String prefix) {
        toolBar = new MiniToolBar(listener);
        toolBar.addButton(prefix.toLowerCase() + "Binding",
                prefix + "Binding", " Show " + prefix + "put Binding Dialog ");
        toolBar.addButton("minus", prefix + "Remove", " Remove ");
        return toolBar;
    }

}
