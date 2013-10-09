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
import org.yawlfoundation.yawl.editor.ui.properties.dialog.CodeletDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class CodeletPropertyEditor extends DialogPropertyEditor {

    private String currentCodelet;
    private String simpleCodeletName;

    public CodeletPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return simpleCodeletName;
    }

    public void setValue(Object value) {
        currentCodelet = (String) value;
        ((DefaultCellRenderer) label).setValue(getSimpleName());
    }


    protected void showDialog() {
        CodeletDialog dialog = new CodeletDialog();
        dialog.setSelection(currentCodelet);
        dialog.setVisible(true);
        String newCodelet = dialog.getSelection();
        if (! (newCodelet == null || newCodelet.equals(currentCodelet))) {
            String oldCodelet = currentCodelet;
            setValue(newCodelet);
            firePropertyChange(oldCodelet, newCodelet);
        }
    }


    private String getSimpleName() {
        simpleCodeletName = null;
        if (currentCodelet != null) {
            int lastDot = currentCodelet.lastIndexOf('.');
            simpleCodeletName = lastDot > -1 ? currentCodelet.substring(lastDot + 1) :
                    currentCodelet;
        }
        return simpleCodeletName;
    }

}

