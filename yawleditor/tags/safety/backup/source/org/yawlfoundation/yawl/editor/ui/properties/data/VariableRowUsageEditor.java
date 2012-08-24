package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class VariableRowUsageEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox usageCombo;

    public VariableRowUsageEditor() {
        usageCombo = new JComboBox(YDataHandler.getScopeNames().toArray());
    }


    public Object getCellEditorValue() {
        return usageCombo.getSelectedIndex() - 1;    // values start at -1
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        usageCombo.setSelectedItem(((Integer) value) + 1);
        return usageCombo;
    }

}
