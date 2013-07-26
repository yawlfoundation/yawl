package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel;

import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog.ListDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.listmodel.NonHumanResourceListModel;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class NonHumanResourceTableModel extends AbstractResourceTableModel {

    private List<NonHumanResource> _resources;

    public NonHumanResourceTableModel() { super(); }


    public int getRowCount() {
        return _resources != null ? _resources.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        return _resources.get(row).getName();
    }

    public void setValues(List<NonHumanResource> resources) {
        _resources = resources;
        fireTableDataChanged();
    }

    public List<NonHumanResource> getValues() {
        return _resources != null ? _resources : Collections.<NonHumanResource>emptyList();
    }


    public void handleAddRequest() {
        ListDialog listDialog = new ListDialog(getOwner(), new NonHumanResourceListModel(),
                "All Non-human Resources");
        listDialog.setVisible(true);
        if (_resources == null) _resources = new ArrayList<NonHumanResource>();
        for (Object o : listDialog.getSelections()) {
            NonHumanResource resource = (NonHumanResource) o;
            if (! _resources.contains(resource)) _resources.add(resource);
        }
        Collections.sort(_resources);
        fireTableDataChanged();
    }


    public void handleRemoveRequest(int[] selectedRows) {
        List<NonHumanResource> toRemove = new ArrayList<NonHumanResource>();
        for (int row : selectedRows) {
            toRemove.add(_resources.get(row));
        }
        _resources.removeAll(toRemove);
        fireTableDataChanged();
    }

    // editing not required for this model
    public void handleEditRequest(int selectedRow) { }

}

