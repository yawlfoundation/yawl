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
import org.yawlfoundation.yawl.editor.ui.properties.dialog.DataDefinitionDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class DataDefinitionPropertyEditor extends DialogPropertyEditor {

    private String currentDefinition;

    public DataDefinitionPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return currentDefinition;
    }

    public void setValue(Object value) {
        currentDefinition = (String) value;
        ((DefaultCellRenderer) label).setValue(currentDefinition);
    }


    protected void showDialog() {
        DataDefinitionDialog dialog = new DataDefinitionDialog();
        dialog.setContent(currentDefinition);
        dialog.setVisible(true);
        String newContent = dialog.getContent();
        if (! (newContent == null || newContent.equals(currentDefinition))) {
            String oldContent = currentDefinition;
            setValue(newContent);
            firePropertyChange(oldContent, newContent);
        }

    }

}

