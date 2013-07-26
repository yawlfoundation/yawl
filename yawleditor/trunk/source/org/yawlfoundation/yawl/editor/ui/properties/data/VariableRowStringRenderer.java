package org.yawlfoundation.yawl.editor.ui.properties.data;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 10/08/12
 */
public class VariableRowStringRenderer extends DefaultCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        reset();
        VariableRow varRow = ((VariableTable) table).getTableModel().getVariableAtRow(row);
        if (table.getColumnName(column).equals("Value")) {
            if (! (varRow.isOutputOnlyTask() || varRow.isLocal())) {
                setValue("###");
                setHorizontalAlignment(CENTER);
                setForeground(Color.GRAY);
            }
            else if (varRow.getDataType().equals("boolean")) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(Boolean.valueOf((String) value));
                return checkBox;
            }
        }
        if (isTaskTable(table)) {
            if (varRow.getMapping() == null) {
                setFont(getFont().deriveFont(Font.ITALIC));
            }
            if (varRow.isMultiInstance()) {
                setForeground(Color.BLUE);
            }
        }
        return this;
    }


    private void reset() {
        setForeground(Color.BLACK);
        setHorizontalAlignment(LEFT);
        setFont(getFont().deriveFont(Font.PLAIN));
    }


    private boolean isTaskTable(JTable table) {
        return ! (((VariableTable) table).getTableModel() instanceof NetVarTableModel);
    }

}
