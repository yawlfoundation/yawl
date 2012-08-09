package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class VariableTablePanel extends JPanel implements ActionListener {

    private NetVarTable table;
    private YNet net;

    // toolbar buttons
    private JButton btnUp;
    private JButton btnDown;
    private JButton btnAdd;
    private JButton btnDel;
    private JToggleButton btnEdit;
    private JLabel status;

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public VariableTablePanel(YNet net) {
        this.net = net;
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setPreferredSize(new Dimension(400, 180));
        add(createToolBar(), BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        enableButtons(true);
    }


    public NetVarTable getTable() { return table; }


    public void showErrorStatus(String msg) {
        status.setForeground(Color.RED);
        status.setText("    " + msg);
        btnEdit.setEnabled(false);
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
         String action = event.getActionCommand();
         if (action.equals("Edit")) {
             setEditMode(btnEdit.isSelected());
         }
         else if (action.equals("Add")) {
             table.addRow();
             btnEdit.setSelected(true);
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
    }

    public NetVariableRow getVariableAtRow(int row) {
        return table.getVariables().get(row);

    }

    private JTable createTable() {
        table = new NetVarTable();
        table.setVariables(createTableRows());
        NetVariableRowUsageEditor usageEditor = new NetVariableRowUsageEditor();
        table.setDefaultEditor(NetVariableRow.Usage.class, usageEditor);
        NetVariableRowStringEditor stringEditor = new NetVariableRowStringEditor(this);
        table.setDefaultEditor(String.class, stringEditor);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
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
        toolbar.add(createEditButton("pencil", "Edit", " Edit "));
        status = new JLabel();
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

    private JToggleButton createEditButton(String iconName, String action, String tip) {
        btnEdit = new JToggleButton(getIcon(iconName));
        btnEdit.setActionCommand(action);
        btnEdit.setToolTipText(tip);
        btnEdit.addActionListener(this);
        btnEdit.setSelectedIcon(getIcon("editPressed"));
        return btnEdit;
    }


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    private void enableButtons(boolean enable) {
        boolean hasRows = table.getRowCount() > 0;
        btnAdd.setEnabled(enable);
        btnDel.setEnabled(enable && hasRows);
        btnUp.setEnabled(enable && hasRows);
        btnDown.setEnabled(enable && hasRows);
        btnEdit.setEnabled(hasRows);
    }

    private void setEditMode(boolean editing) {
        table.setEditable(editing);
        enableButtons(!editing);
    }


    private java.util.List<NetVariableRow> createTableRows() {
        Set<String> ioNames = new HashSet<String>();
        java.util.List<NetVariableRow> rows = new ArrayList<NetVariableRow>();
        for (YVariable variable : net.getLocalVariables().values()) {
            rows.add(new NetVariableRow(variable));
        }

        // join inputs & outputs
        for (String name : net.getInputParameterNames()) {
            YParameter input = net.getInputParameters().get(name);
            if (net.getOutputParameterNames().contains(name)) {
                YParameter output = net.getOutputParameters().get(name);
                if (input.getDataTypeName().equals(output.getDataTypeName())) {
                    ioNames.add(name);
                }
            }
            rows.add(new NetVariableRow(input, ioNames.contains(name)));
        }

        for (String name : net.getOutputParameterNames()) {
            if (! ioNames.contains(name)) {
                rows.add(new NetVariableRow(net.getOutputParameters().get(name)));
            }
        }

        return rows;
    }





}
