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
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.DataDefinitionDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class DataDefinitionPropertyEditor extends DialogPropertyEditor {

    private YSpecificationHandler _handler = SpecificationModel.getHandler();

    public DataDefinitionPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return getLabelValue();
    }

    public void setValue(Object value) {
        ((DefaultCellRenderer) label).setValue(getLabelValue());
    }


    protected void showDialog() {
        String oldContent = _handler.getSchema();
        DataDefinitionDialog dialog = new DataDefinitionDialog();
        dialog.setContent(oldContent);
        dialog.setVisible(true);
        String newContent = dialog.getContent();
        if (! (newContent == null || newContent.equals(oldContent))) {
            setValue(newContent);
            setSchema(newContent);
            firePropertyChange(oldContent, newContent);
        }
    }


    private String getLabelValue() {
        try {
            int typeCount = _handler.getDataHandler().getUserDefinedTypeNames().size();
            return typeCount + " defined type" + (typeCount != 1 ? "s" : "");
        }
        catch (YDataHandlerException ydhe) {
            return null;
        }
    }


    private void setSchema(String schema) {
        try {
            _handler.setSchema(schema);
        }
        catch (Exception yse) {
            showWarning("Invalid Schema", yse.getMessage());
        }
    }


    protected void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message, title,
                JOptionPane.WARNING_MESSAGE);
    }

}

