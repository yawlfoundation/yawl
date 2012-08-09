package org.yawlfoundation.yawl.editor.ui.properties.data;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class NetVariableRowUsageEditor extends AbstractCellEditor implements TableCellEditor {

    private JComboBox usageCombo;


    public NetVariableRowUsageEditor() {
        usageCombo = new JComboBox(NetVariableRow.Usage.values());
    }


    public Object getCellEditorValue() {
        return usageCombo.getSelectedItem();
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        usageCombo.setSelectedItem(value);
        return usageCombo;
    }

}
