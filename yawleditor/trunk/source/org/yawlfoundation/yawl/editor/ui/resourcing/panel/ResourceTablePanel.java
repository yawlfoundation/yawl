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

package org.yawlfoundation.yawl.editor.ui.resourcing.panel;

import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.MiniToolBar;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceTable;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceTableType;
import org.yawlfoundation.yawl.editor.ui.resourcing.tablemodel.AbstractResourceTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class ResourceTablePanel extends JPanel
        implements ActionListener, ListSelectionListener, TableModelListener {

    private ResourceTable table;
    private MiniToolBar toolbar;

    // toolbar buttons
    private JButton btnAdd;
    private JButton btnDel;
    private JButton btnEdit;
    private JLabel status;


    public ResourceTablePanel(ResourceTableType tableType) {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(tableType.getName()));
        JScrollPane scrollPane = new JScrollPane(createTable(tableType));
        scrollPane.setPreferredSize(tableType.getPreferredSize());
        add(createToolBar(), BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
    }


    public void valueChanged(ListSelectionEvent event) {
        enableButtons(true);
        clearStatus();
    }


    public ResourceTable getTable() { return table; }

    public AbstractResourceTableModel getTableModel() {
        return (AbstractResourceTableModel) table.getModel();
    }


    public void showErrorStatus(String msg) {
        status.setForeground(Color.RED);
        status.setText("    " + msg);
        if (table.isEditing()) btnEdit.setEnabled(false);
    }


    public void showOKStatus(String msg) {
        status.setForeground(Color.GRAY);
        status.setText("    " + msg);
        btnEdit.setEnabled(true);
    }


    public void clearStatus() {
        status.setText(null);
        btnEdit.setEnabled(true);
    }


    public void actionPerformed(ActionEvent event) {
        clearStatus();
        String action = event.getActionCommand();
        if (action.equals("Add")) {
            getTableModel().handleAddRequest();
        }
        else if (action.equals("Del")) {
            getTableModel().handleRemoveRequest(table.getSelectedRows());
        }
        else if (action.equals("Edit")) {
            getTableModel().handleEditRequest(table.getSelectedRow());
        }
        enableButtons(true);
    }


    public void tableChanged(TableModelEvent e) {
        enableButtons(true);
    }


    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        table.setEnabled(enabled);
        toolbar.setEnabled(enabled);
        enableButtons(enabled);
    }


    public void showEditButton(boolean show) {
        btnEdit.setVisible(show);
    }

    public void showToolBar(boolean show) { toolbar.setVisible(show); }

    public void setToolbarOrientation(int orientation) {
        toolbar.setOrientation(orientation);
        String location = (orientation == JToolBar.VERTICAL) ? BorderLayout.WEST :
                BorderLayout.SOUTH;
        getLayout().removeLayoutComponent(toolbar);
        getLayout().addLayoutComponent(location, toolbar);
    }

    private JTable createTable(ResourceTableType tableType) {
        table = new ResourceTable(tableType.getModel());
        table.getSelectionModel().addListSelectionListener(this);
        table.getModel().addTableModelListener(this);
        return table;
    }


    private JToolBar createToolBar() {
        toolbar = new MiniToolBar(this);
        btnAdd = toolbar.addButton("plus", "Add", " Add ");
        btnDel = toolbar.addButton("minus", "Del", " Remove ");
        btnEdit = toolbar.addButton("pencil", "Edit", " Edit ");

        status = new JLabel();
        toolbar.add(status);
        return toolbar;
    }


    protected void enableButtons(boolean enable) {
        btnAdd.setEnabled(enable);
        btnDel.setEnabled(enable && table.getSelectedRowCount() > 0);
        btnEdit.setEnabled(enable && table.getSelectedRowCount() == 1);
    }

}
