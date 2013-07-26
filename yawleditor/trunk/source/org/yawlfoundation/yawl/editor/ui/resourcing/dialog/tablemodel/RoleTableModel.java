package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog.ListDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel.RoleListModel;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.*;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class RoleTableModel extends AbstractResourceTableModel {

    private List<Role> _roles;

    public RoleTableModel() { super(); }


    public int getRowCount() {
        return _roles != null ? _roles.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        return _roles.get(row).getName();
    }

    public void setValues(List<Role> roles) {
        _roles = roles;
        fireTableDataChanged();
    }

    public List<Role> getValues() {
        return _roles != null ? _roles : Collections.<Role>emptyList();
    }


    public void handleAddRequest() {
        ListDialog listDialog = new ListDialog(getOwner(), new RoleListModel(), "All Roles");
        listDialog.setVisible(true);
        if (_roles == null) _roles = new ArrayList<Role>();
        for (Object o : listDialog.getSelections()) {
            Role role = (Role) o;
            if (isAllowedDuplicates() || ! _roles.contains(role)) _roles.add(role);
        }
        Collections.sort(_roles);
        fireTableDataChanged();
    }


    public void handleRemoveRequest(int[] selectedRows) {
        List<Role> toRemove = new ArrayList<Role>();
        for (int row : selectedRows) {
            toRemove.add(_roles.get(row));
        }
        _roles.removeAll(toRemove);
        fireTableDataChanged();
    }

    // editing not required for this model
    public void handleEditRequest(int selectedRow) { }

}

