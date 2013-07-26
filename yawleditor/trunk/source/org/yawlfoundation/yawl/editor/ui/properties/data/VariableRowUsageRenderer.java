package org.yawlfoundation.yawl.editor.ui.properties.data;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 10/08/12
 */
public class VariableRowUsageRenderer extends DefaultCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setValue(YDataHandler.getScopeName((Integer) value));
        VariableRow varRow = ((VariableTable) table).getTableModel().getVariableAtRow(row);
        setForeground(varRow.isMultiInstance() ? Color.BLUE : Color.BLACK);
        return this;
    }

}
