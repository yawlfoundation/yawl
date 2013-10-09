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

import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class VariableTablePanel extends JPanel
        implements ActionListener, ListSelectionListener {

    private VariableTable table;
    private DataVariableDialog parent;
    private JToolBar toolbar;
    private TableType tableType;
    private boolean isEditing;

    // toolbar buttons
    private JButton btnUp;
    private JButton btnDown;
    private JButton btnAdd;
    private JButton btnDel;
    private JButton btnMapping;
    private JButton btnMIVar;
    private JButton btnExAttributes;
    private StatusPanel status;

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public VariableTablePanel(java.util.List<VariableRow> rows, TableType tableType,
                              String netElementName, DataVariableDialog parent) {
        this.parent = parent;
        this.tableType = tableType;
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(createTable(rows, tableType, netElementName));
        scrollPane.setPreferredSize(new Dimension(tableType.getPreferredWidth(), 180));
        add(createToolBar(), BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        if (tableType == TableType.Net) {
            btnMapping.setVisible(false);
            btnMIVar.setVisible(false);
            btnExAttributes.setVisible(false);
        }
        table.getSelectionModel().addListSelectionListener(this);
        enableButtons(true);
    }


    public void valueChanged(ListSelectionEvent event) {
        enableButtons(! isEditing());
    }


    public VariableTable getTable() { return table; }


    public void showErrorStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.ERROR, more);
    }


    public void showOKStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.OK, more);
    }


    public void clearStatus() {
        status.clear();
     }


    public void showMIButton(boolean show) { btnMIVar.setVisible(show); }


    public void actionPerformed(ActionEvent event) {
        clearStatus();
        String action = event.getActionCommand();
        if (action.equals("Add")) {
            table.addRow();
            setEditMode(true);
        }
        else if (action.equals("Del")) {
            table.removeRow();
            enableButtons(true);
        }
        else if (action.equals("Up")) {
            table.moveSelectedRowUp();
        }
        else if (action.equals("Down")) {
            table.moveSelectedRowDown();
        }
        else if (action.equals("Map")) {
            new MappingDialog(parent.getNetTablePanel(),
                    table.getSelectedVariable()).setVisible(true);
            table.getTableModel().fireTableDataChanged();
        }
        else if (action.equals("MarkMI")) {
            if (! parent.setMultiInstanceRow(table.getSelectedVariable())) {
                showErrorStatus("Invalid MI data type", null);
            }
        }
        else if (action.equals("ExAt")) {
            VariableRow row = table.getSelectedVariable();
            if (row != null) {
                new ExtendedAttributesDialog(parent, row.getAttributes(), row.getName())
                        .setVisible(true);
                table.getTableModel().setTableChanged(true);     // to flag update
                parent.enableApplyButton();
            }
        }
    }


    public VariableRow getVariableAtRow(int row) {
        return table.getVariables().get(row);
    }

    public boolean isEditing() {return isEditing; }

    public void showToolBar(boolean show) { toolbar.setVisible(show); }


    public VariableTablePanel copy() {
        return new VariableTablePanel(table.getVariables(), tableType,
                table.getNetElementName(), parent);
    }

    public DataVariableDialog getVariableDialog() { return parent; }

    private JTable createTable(java.util.List<VariableRow> rows, TableType tableType,
                               String netElementName) {
        table = new VariableTable(tableType.getModel());
        table.setVariables(rows);
        table.setNetElementName(netElementName);
        VariableRowUsageEditor usageEditor = new VariableRowUsageEditor();
        table.setDefaultEditor(Integer.class, usageEditor);
        VariableRowStringEditor stringEditor = new VariableRowStringEditor(this);
        table.setDefaultEditor(String.class, stringEditor);
        VariableRowUsageRenderer usageRenderer = new VariableRowUsageRenderer();
        table.setDefaultRenderer(Integer.class, usageRenderer);
        VariableRowStringRenderer stringRenderer = new VariableRowStringRenderer();
        table.setDefaultRenderer(String.class, stringRenderer);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar() {
        toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        btnAdd = createToolBarButton("plus", "Add", " Add ");
        toolbar.add(btnAdd);
        btnDel = createToolBarButton("minus", "Del", " Remove ");
        toolbar.add(btnDel);
        btnUp = createToolBarButton("arrow_up", "Up", " Move up ");
        toolbar.add(btnUp);
        btnDown = createToolBarButton("arrow_down", "Down", " Move down ");
        toolbar.add(btnDown);
        btnMapping = createToolBarButton("mapping", "Map", " Map ");
        toolbar.add(btnMapping);
        btnMIVar = createToolBarButton("miVar", "MarkMI", " Mark as MI ");
        toolbar.add(btnMIVar);
        btnExAttributes = createToolBarButton("exat", "ExAt", " Ext. Attributes ");
        toolbar.add(btnExAttributes);
        status = new StatusPanel(parent);
        toolbar.add(status);
        return toolbar;
    }


    private JButton createToolBarButton(String iconName, String action, String tip) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    protected void enableButtons(boolean enable) {
        boolean hasRowSelected = table.getSelectedRow() > -1;
        btnAdd.setEnabled(enable);
        btnDel.setEnabled(enable && hasRowSelected);
        btnUp.setEnabled(enable && hasRowSelected);
        btnDown.setEnabled(enable && hasRowSelected);
        btnMapping.setEnabled(enable && hasRowSelected);
        btnMIVar.setEnabled(enable && hasRowSelected);
        btnExAttributes.setEnabled(enable && hasRowSelected);
    }


    protected void setEditMode(boolean editing) {
        isEditing = editing;
        parent.setInserting(editing);
        enableButtons(!editing);
    }

}
