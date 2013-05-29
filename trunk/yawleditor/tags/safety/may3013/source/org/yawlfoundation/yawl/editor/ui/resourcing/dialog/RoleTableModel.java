package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class RoleTableModel extends AbstractResourceTableModel {

    private List<Role> _roles;

    public RoleTableModel() { super(); }

    public RoleTableModel(Set<Object> values) {
        this();
        _roles = cast(values, Role.class, new Comparator<Role>() {
            public int compare(Role r1, Role r2) {
                return r1.getName().compareTo(r2.getName());
            }
        });
    }

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

    public List<Role> getValues() { return _roles; }

}

