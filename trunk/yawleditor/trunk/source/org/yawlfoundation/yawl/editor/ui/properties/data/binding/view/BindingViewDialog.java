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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.view;

import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableTable;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableTablePanel;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;
import org.yawlfoundation.yawl.elements.YTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 11/07/2014
 */
public class BindingViewDialog extends JDialog implements ActionListener {

    private BindingViewTable inputTable;
    private BindingViewTable outputTable;
    private VariableTablePanel parentListener;         // the VariableTablePanel invoker


    public BindingViewDialog(VariableTablePanel listener, DataVariableDialog dataDialog) {
        super(dataDialog);
        parentListener = listener;
        add(getContent(dataDialog.getTaskTable().getVariables(),
                dataDialog.getOutputBindings()));
        setTitle(makeTitle(dataDialog.getTask()));
        setMinimumSize(new Dimension(600, 320));
        setLocationRelativeTo(dataDialog);
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("Close")) setVisible(false);
        else {

            // have to set the parent table to the selected row of the view dialog
            BindingViewTable viewTable = cmd.startsWith("In") ? inputTable : outputTable;
            String name = (String) viewTable.getValueAt(viewTable.getSelectedRow(), 1);
            VariableTable parentTable = parentListener.getTable();
            int selectedRow = parentTable.getSelectedRow();
            java.util.List<VariableRow> parentRows = parentTable.getVariables();
            for (int i=0; i < parentTable.getRowCount(); i++) {
                if (parentRows.get(i).getName().equals(name)) {
                    parentTable.selectRow(i);
                    break;                    // todo - get task vr for output from ob
                }
            }
            parentListener.actionPerformed(event);
            parentTable.selectRow(selectedRow);
        }
    }


    private String makeTitle(YTask task) {
        return String.format("Binding Summary for Decomposition %s [Task: %s]",
                task.getDecompositionPrototype().getID(), task.getID());
    }


    private JPanel getContent(java.util.List<VariableRow> inputRows,
                                 OutputBindings outputBindings) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(7, 7, 7, 7));
        JPanel subContent = new JPanel(new GridLayout(0, 1, 10, 10));
        BindingViewTablePanel inputPanel = new BindingViewTablePanel(this, inputRows);
        inputTable = inputPanel.getTable();
        BindingViewTablePanel outputPanel = new BindingViewTablePanel(this, outputBindings);
        outputTable = outputPanel.getTable();
        subContent.add(inputPanel);
        subContent.add(outputPanel);
        content.add(subContent, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10,0,0,0));
        panel.add(createButton("Close"));
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(70,25));
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.addActionListener(this);
        return button;
    }

}
