/*
 * Created on 20/09/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.internal.YInternalType;

import java.util.HashSet;
import java.util.Set;

/**
 * A Data variable combo-box that shows only variables in the given
 * decomposition scope and usage type that are of the XMLSchema 'duration' tupe.
 * @author bradforl
 *
 */

public class TimerDataVariableComboBox extends DataVariableComboBox {

    public TimerDataVariableComboBox() {
        super();
    }

    public void setEnabled(boolean enabled) {
        if ((enabled && getItemCount() > 0) || !enabled) {
            super.setEnabled(enabled);
        }
    }

    protected void addDataVariables() {
        if (getDecomposition() != null) {
            Set<YVariable> variables = new HashSet<YVariable>(
                    ((YNet) getDecomposition()).getLocalVariables().values());
            variables.addAll(getDecomposition().getInputParameters().values());

            for (YVariable variable : variables) {
                if (YInternalType.YTimerType.name().equals(variable.getDataTypeName())) {
                    addItem(variable.getName());
                }
            }
        }
    }
}
