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
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.XQueryDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class XQueryPropertyEditor extends DialogPropertyEditor {

    private String currentQuery;

    public XQueryPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return currentQuery;
    }

    public void setValue(Object value) {
        currentQuery = (String) value;
        ((DefaultCellRenderer) label).setValue(currentQuery);
    }

    protected void showDialog() {
        XQueryDialog xqDialog = new XQueryDialog(YAWLEditor.getInstance());
        xqDialog.setTitle("Edit XQuery");
        xqDialog.setTypeValidator(new BindingTypeValidator(
                YAWLEditor.getNetsPane().getSelectedYNet()));
        xqDialog.setText(currentQuery);
        xqDialog.setVisible(true);
        if (! xqDialog.cancelled()) {
            String query = xqDialog.getText();
            if (! query.equals(currentQuery)) {
                String oldQuery = currentQuery;
                setValue(query);
                firePropertyChange(oldQuery, query);
            }
        }
    }

}



