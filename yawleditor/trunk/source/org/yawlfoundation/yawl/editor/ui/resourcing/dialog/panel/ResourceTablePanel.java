package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceTable;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceTableType;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.AbstractResourceTableModel;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

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
    private JToolBar toolbar;

    // toolbar buttons
    private JButton btnAdd;
    private JButton btnDel;
    private JButton btnEdit;
    private JLabel status;

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public ResourceTablePanel(ResourceTableType tableType) {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder(tableType.getName()));
        JScrollPane scrollPane = new JScrollPane(createTable(tableType));
        scrollPane.setPreferredSize(tableType.getPreferredSize());
        add(createToolBar(), BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        enableButtons(true);
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
        toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        btnAdd = createToolBarButton("plus", "Add", " Add ");
        toolbar.add(btnAdd);
        btnDel = createToolBarButton("minus", "Del", " Remove ");
        toolbar.add(btnDel);
        btnEdit = createToolBarButton("pencil", "Edit", " Edit ");
        toolbar.add(btnEdit);
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


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    protected void enableButtons(boolean enable) {
        btnAdd.setEnabled(enable);
        btnDel.setEnabled(enable && table.getSelectedRowCount() > 0);
        btnEdit.setEnabled(enable && table.getSelectedRowCount() == 1);
    }

}
