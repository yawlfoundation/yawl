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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.LookAndFeelTweaks;

import javax.swing.*;


public class IntegerPropertyEditor extends AbstractPropertyEditor {

    public IntegerPropertyEditor() {
        editor = new JTextField();
        ((JTextField) editor).setText(null);
        ((JTextField) editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
    }


    public Object getValue() {
        try {
            return Integer.parseInt(((JTextField) editor).getText());
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }


    public void setValue(Object value) {
        if (value instanceof Integer) {
            ((JTextField)editor).setText(value.toString());
        }
        else {
            ((JTextField) editor).setText(null);
        }
    }

}
