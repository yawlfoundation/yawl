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

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.FontColor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FontDialog;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class FontPropertyEditor extends DialogPropertyEditor {

    private Font currentFont;
    private Color colour;

    public FontPropertyEditor() {
        super(new FontColorRenderer());
    }

    public Object getValue() {
        return colour != null ? new FontColor(currentFont, colour) : currentFont;
    }

    public void setValue(Object value) {
        FontColor fontColor = null;
        if (value instanceof FontColor) {
            fontColor = (FontColor) value;
            currentFont = fontColor.getFont();
            colour = fontColor.getColour();
        }
        else {
            currentFont = (Font) value;
            colour = null;
        }
        ((FontColorRenderer) label).setValue(fontColor != null ? fontColor :
                new FontColor(currentFont, null));
    }


    protected void showDialog() {
        FontDialog dialog = new FontDialog(YAWLEditor.getInstance(), currentFont);
        if (colour != null) dialog.setColour(colour);
        Font newFont = dialog.showDialog();

        if (newFont != null) {
            boolean colourChange = false;
            Color newColour = null;
            if (colour != null) {
                newColour = dialog.getColour();
                colourChange = ! newColour.equals(colour);
            }
            if (colourChange || ! newFont.equals(currentFont)) {
                Object oldValue = getValue();
                Object newValue = colour != null ? new FontColor(newFont, newColour) : newFont;
                setValue(newValue);
                firePropertyChange(oldValue, newFont);
            }
        }
    }

}

