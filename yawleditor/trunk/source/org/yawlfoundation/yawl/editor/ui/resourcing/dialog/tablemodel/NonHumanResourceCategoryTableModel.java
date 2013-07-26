package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel;

import org.yawlfoundation.yawl.editor.core.resourcing.GenericNonHumanCategory;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog.ListDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel.NonHumanResourceCategoryListModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class NonHumanResourceCategoryTableModel extends AbstractResourceTableModel {

    private List<GenericNonHumanCategory> _categories;

    public NonHumanResourceCategoryTableModel() { super(); }


    public int getRowCount() {
        return _categories != null ? _categories.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        return _categories.get(row).getListLabel();
    }

    public void setValues(List<GenericNonHumanCategory> resources) {
        _categories = resources;
        fireTableDataChanged();
    }

    public List<GenericNonHumanCategory> getValues() {
        return _categories != null ? _categories :
                Collections.<GenericNonHumanCategory>emptyList();
    }


    public void handleAddRequest() {
        ListDialog listDialog = new ListDialog(getOwner(),
                new NonHumanResourceCategoryListModel(),
                "All Non-human Categories");
        listDialog.setVisible(true);
        if (_categories == null) _categories = new ArrayList<GenericNonHumanCategory>();
        for (Object o : listDialog.getSelections()) {
            GenericNonHumanCategory category = (GenericNonHumanCategory) o;
            if (! _categories.contains(category)) _categories.add(category);
        }
        Collections.sort(_categories);
        fireTableDataChanged();
    }


    public void handleRemoveRequest(int[] selectedRows) {
        List<GenericNonHumanCategory> toRemove = new ArrayList<GenericNonHumanCategory>();
        for (int row : selectedRows) {
            toRemove.add(_categories.get(row));
        }
        _categories.removeAll(toRemove);
        fireTableDataChanged();
    }

    // editing not required for this model
    public void handleEditRequest(int selectedRow) { }

}

