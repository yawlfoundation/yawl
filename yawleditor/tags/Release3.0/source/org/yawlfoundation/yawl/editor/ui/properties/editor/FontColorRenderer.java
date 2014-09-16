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

import com.l2fprod.common.swing.renderer.ColorCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.FontColor;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 1/07/2014
 */
public class FontColorRenderer extends ColorCellRenderer {

    protected String convertToString(Object value) {
        if (value instanceof FontColor) {
            FontColor fontColor = (FontColor) value;
            if (fontColor.getFont() != null) {
                return fontColor.toString();
            }
        }
        return null;
    }

    protected Icon convertToIcon(Object value) {
        if (value instanceof FontColor) {
            FontColor fontColor = (FontColor) value;
            if (fontColor.getColour() != null) {
                return new PaintIcon(fontColor.getColour());
            }
        }
        return null;
    }

}
