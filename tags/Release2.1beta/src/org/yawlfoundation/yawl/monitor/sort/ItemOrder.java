package org.yawlfoundation.yawl.monitor.sort;

/**
 * Author: Michael Adams
* Creation Date: 9/12/2009
*/
public class ItemOrder {
    private TableSorter.ItemColumn _column;
    private boolean _ascending;

    public ItemOrder(TableSorter.ItemColumn column) {
        _column = column;
        _ascending = true;
    }

    public void setOrder(TableSorter.ItemColumn column) {
        _ascending = (column != _column) || ! _ascending;  // toggle order if same column
        _column = column;
    }

    public TableSorter.ItemColumn getColumn() { return _column; }

    public boolean isAscending() { return _ascending; }
}