/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.data;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 10/08/12
 */
public class VariableRowStringRenderer extends DefaultCellRenderer {

    private OutputBindings _outputBindings;
    private Font _selectorFont;

    public VariableRowStringRenderer(OutputBindings outputBindings) {
        _outputBindings = outputBindings;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        reset();
        VariableRow varRow = ((VariableTable) table).getTableModel().getVariableAtRow(row);
        if (column == 0) {
            if (_selectorFont == null) _selectorFont = getSelectorFont((String) value);
            if (_selectorFont != null) setFont(_selectorFont);
        }
        else if (isTaskTable(table)) {
            if (! hasBinding(varRow)) {
                setFont(getFont().deriveFont(Font.ITALIC));
            }
            if (varRow.isMultiInstance()) {
                setForeground(Color.BLUE);
            }
        }
        if (table.getColumnName(column).endsWith("Value")) {
            if (! (varRow.isOutputOnlyTask() || varRow.isLocal())) {
                setValue("###");
                setHorizontalAlignment(CENTER);
                if (! varRow.isMultiInstance()) setForeground(Color.GRAY);
            }
            else if (varRow.getDataType().equals("boolean")) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(Boolean.valueOf((String) value));
                return checkBox;
            }
            if (! varRow.isValidValue()) {
                setForeground(Color.RED);
            }
        }
        else if (table.getColumnName(column).equals("Name") && ! varRow.isValidName()) {
            setForeground(Color.RED);
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


    private boolean hasBinding(VariableRow row) {
        return (row.isInput() && row.getMapping() != null) ||
               (row.isOutput() && _outputBindings.hasBinding(row.getName()));
    }


    /**
     * Finds the first font that can display the 'selector' unicode character
     * @param s the selector string
     * @return the first matching font (any one will do)
     */
    private Font getSelectorFont(String s) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (Font font : ge.getAllFonts()) {
        	if (font.canDisplayUpTo(s) == -1) {
                return font.deriveFont(12.0f);
        	}
        }
        return null;
    }

}
