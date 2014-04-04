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

package org.yawlfoundation.yawl.editor.ui.properties;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 24/07/12
 */
public class FontColor {

    private Font font;
    private Color colour;

    public FontColor(Font font, Color colour) {
        this.font = font;
        this.colour = colour;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public String toString() {
        return font != null ?
                font.getFamily() + getStyleName(font.getStyle()) + font.getSize() : "";
    }

    private String getStyleName(int style) {
        switch (style) {
            case Font.BOLD : return ", Bold, ";
            case Font.ITALIC : return ", Italic, ";
            case Font.BOLD | Font.ITALIC : return ", Bold+Italic, ";
            default : return ", ";       // plain
        }
    }

}
