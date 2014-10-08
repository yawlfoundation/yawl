/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.dialog;

import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ExletTargetCellEditor extends DefaultCellEditor {

    private final JComboBox _combo = new JComboBox(getTargets());


    public ExletTargetCellEditor() {
        super(new JTextField());
        setClickCountToStart(1);
    }


    public Object getCellEditorValue() {
        return _combo.getSelectedItem();
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        RdrPrimitive primitive = ((ConclusionTable) table).getSelectedPrimitive();
        if (primitive.getAction().equals(ExletAction.Select.toString())) {
            // return  list of worklets
        }

        _combo.setSelectedItem(value);
        return _combo;
    }


    private Vector<String> getTargets() {
        Vector<String> targets = new Vector<String>();
        for (ExletTarget target : ExletTarget.values()) {
            if (target != ExletTarget.Invalid) {
                targets.add(target.toString());
            }
        }
        return targets;
    }

}
