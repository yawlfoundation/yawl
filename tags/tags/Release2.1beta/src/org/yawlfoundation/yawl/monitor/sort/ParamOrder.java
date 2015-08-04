package org.yawlfoundation.yawl.monitor.sort;

/**
 * Author: Michael Adams
* Creation Date: 9/12/2009
*/
public class ParamOrder {
    private TableSorter.ParamColumn _column;
    private boolean _ascending;

    public ParamOrder(TableSorter.ParamColumn column) {
        _column = column;
        _ascending = true;
    }

    public void setOrder(TableSorter.ParamColumn column) {
        _ascending = (column != _column) || ! _ascending;  // toggle order if same column
        _column = column;
    }

    public TableSorter.ParamColumn getColumn() { return _column; }

    public boolean isAscending() { return _ascending; }
}