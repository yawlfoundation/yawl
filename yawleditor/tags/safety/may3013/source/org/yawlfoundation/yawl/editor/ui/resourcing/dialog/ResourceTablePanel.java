package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 9/08/12
 */
public class ResourceTablePanel extends JPanel implements ActionListener, ListSelectionListener {

    private ResourceTable table;
    private ResourceDialog parent;
    private JToolBar toolbar;
    private ResourceTableType tableType;

    // toolbar buttons
    private JButton btnAdd;
    private JButton btnDel;
    private JToggleButton btnEdit;
    private JLabel status;

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public ResourceTablePanel(ResourceTableType tableType, ResourceDialog parent) {
        this.parent = parent;
        this.tableType = tableType;
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(tableType.getName()));
        JScrollPane scrollPane = new JScrollPane(createTable(tableType));
        scrollPane.setPreferredSize(tableType.getPreferredSize());
        add(createToolBar(), BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(this);
        enableButtons(true);
    }


    public void valueChanged(ListSelectionEvent event) {
        clearStatus();
    }


    public ResourceTable getTable() { return table; }

    public TableModel getTableModel() {
        return table.getModel();
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
        if (action.equals("Edit")) {
            // do something
        }
        else if (action.equals("Add")) {
            // show list
            btnEdit.setSelected(true);

        }
        else if (action.equals("Del")) {
            // show list
            enableButtons(true);
        }
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


    public ResourceDialog getVariableDialog() { return parent; }

    private JTable createTable(ResourceTableType tableType) {
        table = new ResourceTable(tableType.getModel());
//        if (table.getRowCount() > 0) table.selectRow(0);
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


    protected void enableButtons(boolean enable) {
        boolean hasRows = table.getRowCount() > 0;
        btnAdd.setEnabled(enable);
        btnDel.setEnabled(enable && hasRows);
        btnEdit.setEnabled(hasRows);
    }


}
