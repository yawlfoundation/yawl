package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * @author Michael Adams
 * @date 16/05/13
 */
public class AbstractResourceTableModel extends AbstractTableModel {

    public AbstractResourceTableModel() { super(); }


    public int getRowCount() {
        return 0;
    }

    public int getColumnCount() {
        return 0;
    }

    public Object getValueAt(int i, int i2) {
        return null;
    }


    protected <T> List<T> cast(Set<Object> list, Class T, Comparator<T> comparator) {
        if (list == null) return Collections.emptyList();
        List<T> casted = new ArrayList<T>(list.size());
        for (Object o : list) {
            casted.add((T) o);
        }
        Collections.sort(casted, comparator);
        return casted;
    }

}
