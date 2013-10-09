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
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TextAreaDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class TextPropertyEditor extends DialogPropertyEditor {

    private String currentText;

    public TextPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return currentText;
    }

    public void setValue(Object value) {
        currentText = (String) value;
        ((DefaultCellRenderer) label).setValue(currentText);
    }

    protected void showDialog() {
        String newText = new TextAreaDialog(YAWLEditor.getInstance(),
                "Update text", currentText).showDialog();
        if (! (newText == null || newText.equals(currentText))) {
            String oldText = currentText;
            setValue(newText);
            firePropertyChange(oldText, newText);
        }
    }

}

