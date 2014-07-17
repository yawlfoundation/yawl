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
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * @author Michael Adams
 * @date 9/08/13
 */
public class BindingViewTablePanel extends JPanel {

    private BindingViewTable table;

    // toolbar button
    private JButton btnBinding;


    public BindingViewTablePanel(ActionListener listener, List<VariableRow> variableRows) {
        table = createTable(variableRows);
        init(listener, "In");
    }


    public BindingViewTablePanel(ActionListener listener, DataVariableDialog dataDialog) {
        table = createTable(dataDialog);
        init(listener, "Out");
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


    public BindingViewTable getTable() { return table; }


    private BindingViewTable createTable(List<VariableRow> rows) {
        table = new BindingViewTable(new InputBindingViewTableModel());
        table.setRows(rows);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private BindingViewTable createTable(DataVariableDialog dataDialog) {
        table = new BindingViewTable(new OutputBindingViewTableModel());
        table.setRows(dataDialog);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar(ActionListener listener, String prefix) {
        MiniToolBar toolBar = new MiniToolBar(listener);
        btnBinding = toolBar.addButton(prefix.toLowerCase() + "Binding",
                prefix + "Binding", " Show " + prefix + "put Binding Dialog ");
        return toolBar;
    }

}
