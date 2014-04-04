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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.internal.YInternalType;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A Data variable combo-box that shows only variables in the given
 * decomposition scope and usage type that are of the XMLSchema 'duration' type.
 * @author bradforl
 *
 */

public class TimerDataVariableComboBox extends JComboBox {

    private YNet _net;

    public TimerDataVariableComboBox() {
        super();
    }

    public void setNet(YNet net) {
        _net = net;
        refresh();
    }

    public YDecomposition getNet() {
        return _net;
    }

    public YVariable getSelectedVariable() {
        String selectedVariableName = (String) getSelectedItem();
        if (_net != null) {
            return _net.getLocalOrInputVariable(selectedVariableName);
        }
        return null;
    }


    protected void refresh() {
        removeAllItems();
        addDataVariables();
    }


    public void setEnabled(boolean enabled) {
        if ((enabled && getItemCount() > 0) || !enabled) {
            super.setEnabled(enabled);
        }
    }

    protected void addDataVariables() {
        if (_net != null) {
            Set<YVariable> variables = new HashSet<YVariable>(_net.getLocalVariables().values());
            variables.addAll(_net.getInputParameters().values());

            for (YVariable variable : variables) {
                if (YInternalType.YTimerType.name().equals(variable.getDataTypeName())) {
                    addItem(variable.getName());
                }
            }
        }
    }
}
