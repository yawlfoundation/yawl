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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;
import org.yawlfoundation.yawl.elements.YDecomposition;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class DataVariablePropertyEditor extends DialogPropertyEditor {

    private NetTaskPair netTaskPair;

    public DataVariablePropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return netTaskPair;
    }

    public void setValue(Object value) {
        netTaskPair = (NetTaskPair) value;
        ((DefaultCellRenderer) label).setValue(netTaskPair.getSimpleText());
    }

    protected void showDialog() {
        DataVariableDialog dialog;
        YDecomposition decomposition = netTaskPair.getDecomposition();
        if (decomposition == null) {
            dialog = new DataVariableDialog(netTaskPair.getNet());
        }
        else {
            dialog = new DataVariableDialog(netTaskPair.getNet(), decomposition,
                    netTaskPair.getTask());
        }
        dialog.setVisible(true);
        NetTaskPair oldPair = netTaskPair;
        netTaskPair = new NetTaskPair(oldPair.getNet(), oldPair.getDecomposition(),
                oldPair.getTask());
        firePropertyChange(oldPair, netTaskPair);
    }

}

