package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class NetParamTableModel extends AbstractResourceTableModel {

    private List<DynParam> _dynParams;


    public NetParamTableModel() { super(); }

    public NetParamTableModel(Set<Object> values) {
        this();
        _dynParams = cast(values, DynParam.class, new Comparator<DynParam>() {
            public int compare(DynParam d1, DynParam d2) {
                return d1.getName().compareTo(d2.getName());
            }
        });
    }

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

}

