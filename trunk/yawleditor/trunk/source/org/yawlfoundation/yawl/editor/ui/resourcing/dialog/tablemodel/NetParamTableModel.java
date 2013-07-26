package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel;

import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.subdialog.NetParamDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class NetParamTableModel extends AbstractResourceTableModel {

    private List<DynParam> _dynParams;


    public NetParamTableModel() { super(); }


    public int getRowCount() {
        return _dynParams != null ? _dynParams.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        DynParam d = _dynParams.get(row);
        return String.format("%s [%s]", d.getName(), d.getRefersString());
    }

    public void setValues(List<DynParam> roles) {
        _dynParams = roles;
        fireTableDataChanged();
    }

    public List<DynParam> getValues() { return _dynParams; }

    public void handleAddRequest() {
        NetParamDialog dialog = new NetParamDialog(getOwner());
        dialog.removeItems(_dynParams);          // don't list already selected params
        dialog.setVisible(true);
        DynParam selection = dialog.getSelection();
        if (selection != null) {
            if (_dynParams == null) _dynParams = new ArrayList<DynParam>();
            _dynParams.add(selection);
        }
        Collections.sort(_dynParams);
        fireTableDataChanged();
    }

    public void handleEditRequest(int selectedRow) {
        DynParam selected = _dynParams.get(selectedRow);
        NetParamDialog dialog = new NetParamDialog(getOwner());
        dialog.loadForEdit(selected);
        dialog.setVisible(true);
        DynParam updated = dialog.getSelection();
        if (updated != null) {
            selected.setRefers(updated.getRefers());
        }
        fireTableDataChanged();
    }

    public void handleRemoveRequest(int[] selectedRows) {
        List<DynParam> toRemove = new ArrayList<DynParam>();
        for (int row : selectedRows) {
            toRemove.add(_dynParams.get(row));
        }
        _dynParams.removeAll(toRemove);
        fireTableDataChanged();
    }

}

