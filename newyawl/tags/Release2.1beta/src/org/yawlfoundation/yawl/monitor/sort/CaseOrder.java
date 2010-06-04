package org.yawlfoundation.yawl.monitor.sort;

/**
 * Author: Michael Adams
* Creation Date: 9/12/2009
*/
public class CaseOrder {
    private TableSorter.CaseColumn _column;
    private boolean _ascending;

    public CaseOrder(TableSorter.CaseColumn column) {
        _column = column;
        _ascending = true;
    }

    public void setOrder(TableSorter.CaseColumn column) {
        _ascending = (column != _column) || ! _ascending;  // toggle order if same column
        _column = column;
    }

    public TableSorter.CaseColumn getColumn() { return _column; }

    public boolean isAscending() { return _ascending; }
}
