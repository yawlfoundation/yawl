package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class FilterTableModel extends AbstractResourceTableModel {

    private List<AbstractFilter> _filters;

    public FilterTableModel() { super(); }

    public FilterTableModel(Set<Object> values) {
        this();
        _filters = cast(values, AbstractFilter.class, new Comparator<AbstractFilter>() {
                public int compare(AbstractFilter f1, AbstractFilter f2) {
                    return f1.getName().compareTo(f2.getName());
                }
        });
    }


    public int getRowCount() {
        return _filters != null ? _filters.size() : 0;
    }

    public int getColumnCount() {
        return 1;
    }

    public Object getValueAt(int row, int col) {
        AbstractFilter f = _filters.get(row);
        return String.format("%s (%s)", f.getName(), getFilterType(f));
    }

    public void setValues(List<AbstractFilter> filters) {
        _filters = filters;
        fireTableDataChanged();
    }

    public List<AbstractFilter> getValues() { return _filters; }

    private String getFilterType(AbstractFilter f) {
        switch (f.getFilterType()) {
            case AbstractFilter.CAPABILITY_FILTER : return "Capability";
            case AbstractFilter.ORGANISATIONAL_FILTER : return "Org Structure";
            case AbstractFilter.HISTORICAL_FILTER : return "Historical";
        }
        return "Untyped";
    }

}

