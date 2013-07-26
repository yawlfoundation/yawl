package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public abstract class AbstractResourceTableModel extends AbstractTableModel {

    private boolean _enabled;
    private boolean _allowDuplicates;
    private ResourceDialog _owner;

    @SuppressWarnings("unchecked")
    protected <T> List<T> cast(Set<Object> list, Class T, Comparator<T> comparator) {
        if (list == null) return Collections.emptyList();
        List<T> casted = new ArrayList<T>(list.size());
        for (Object o : list) {
            casted.add((T) o);      // warning suppressed
        }
        Collections.sort(casted, comparator);
        return casted;
    }

    public boolean isEnabled() { return _enabled; }

    public void setEnabled(boolean enabled) { _enabled = enabled; }


    public void setAllowDuplicates(boolean allow) { _allowDuplicates = allow; }

    public boolean isAllowedDuplicates() { return _allowDuplicates; }


    public ResourceDialog getOwner() { return _owner; }

    public void setOwner(ResourceDialog owner) {
        _owner = owner;
        addTableModelListener(owner);
    }


    public abstract void handleAddRequest();

    public abstract void handleEditRequest(int selectedRow);

    public abstract void handleRemoveRequest(int[] selectedRows);

}
