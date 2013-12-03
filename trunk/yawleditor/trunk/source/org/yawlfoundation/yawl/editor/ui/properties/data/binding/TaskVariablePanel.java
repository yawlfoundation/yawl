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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding;

import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
class TaskVariablePanel extends AbstractBindingPanel {

    private JComboBox _varsCombo;
    private JLabel _prompt;
    private String _miVarName;


    TaskVariablePanel(String title, java.util.List<VariableRow> varList,
                      ActionListener listener) {
        super();
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(new TitledBorder(title),
                new EmptyBorder(0,17,0,0)));

        _varsCombo = buildComboBox(getVarNames(varList), "taskVarComboSelection", listener);
        _varsCombo.setBorder(new EmptyBorder(3,0,3,0));
        add(_varsCombo, BorderLayout.CENTER);
        _prompt = new JLabel("Task Variable: ");
        _prompt.setDisplayedMnemonic(KeyEvent.VK_T);
        _prompt.setLabelFor(_varsCombo);
        _prompt.setBorder(new EmptyBorder(0,0,0,20));
        add(_prompt, BorderLayout.WEST);
        setMiVarName(varList);
        setPreferredSize(new Dimension(410, 55));
    }

    protected void hideMiVar() {
        if (_miVarName != null) _varsCombo.removeItem(_miVarName);
    }

    protected String getSelectedItem() {
        return (String) _varsCombo.getSelectedItem();
    }

    protected void setSelectedItem(String item) {
        _varsCombo.setSelectedItem(item);
    }


    // when showing the MI parameter
    protected void disableSelections() {
        _varsCombo.setEnabled(false);
        _prompt.setEnabled(false);
    }


    private void setMiVarName(java.util.List<VariableRow> varList) {
        for (VariableRow row : varList) {
            if (row.isMultiInstance()) {
                _miVarName = row.getName();
                break;
            }
        }
    }
}
