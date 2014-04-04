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
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;

/**
 * @author Michael Adams
 * @date 12/07/13
 */
public class ExtendedAttributesPropertyEditor extends DialogPropertyEditor {

    private NetTaskPair pair;

    public ExtendedAttributesPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return pair;
    }

    public void setValue(Object value) {
        pair = (NetTaskPair) value;
        ((DefaultCellRenderer) label).setValue(pair.getSimpleText());
    }


    protected void showDialog() {
        ExtendedAttributesDialog dialog =
                new ExtendedAttributesDialog(pair.getDecomposition());
        dialog.setVisible(true);
        NetTaskPair oldPair = pair;
        pair = new NetTaskPair(null, oldPair.getDecomposition(), null);
        String text = pair.getDecomposition().getAttributes().isEmpty() ? "None" :
                pair.getDecomposition().getAttributes().size() + " active";
        pair.setSimpleText(text);
        firePropertyChange(oldPair, pair);
    }

}

