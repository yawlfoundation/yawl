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

package org.yawlfoundation.yawl.worklet.dialog;

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class DataContextTablePanel extends JPanel {

    private DataContextTable table;


    public DataContextTablePanel(ListSelectionListener listener) {
        super();
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Data Context"));
        JScrollPane scrollPane = new JScrollPane(createTable(listener));
        scrollPane.setSize(new Dimension(600, 200));
        add(scrollPane, BorderLayout.CENTER);
    }


    public void setVariables(java.util.List<VariableRow> rows) {
        table.getTableModel().setVariables(rows);
        table.setPreferredScrollableViewportSize(getPreferredSize());
        table.updateUI();
    }


    public VariableRow getVariableAtRow(int index) {
        return table.getTableModel().getVariableAtRow(index);
    }


    public Element getDataElement(String rootName) {
        XNode root = new XNode(rootName);
        for (VariableRow row : table.getTableModel().getVariables()) {
            root.addChild(row.getName(), row.getValue());
        }
        return root.toElement();
    }


    private DataContextTable createTable(ListSelectionListener listener) {
        table = new DataContextTable();
        table.setDefaultEditor(String.class, new DataContextValueEditor());
        table.getSelectionModel().addListSelectionListener(listener);
        return table;
    }

}
