/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf;

import javax.swing.*;
import java.awt.*;

/**
 * Simple class for estimating widths & heights of Strings in pixels
 *
 * Modified by Michael Adams for version 2.0
 * Date: 06/09/2009
 */
public class FontUtil {

    // Uses a generic JComponent (see below) and gives a quite accurate result.
    public static Dimension getFontMetrics(String s, Font font) {
        return new FontBox().getFontBounds(s, font) ;
    }


    public static int getTextWidth(String s, Font font) {
        if ((s == null) || (s.length() == 0)) return 0;
        return (int) Math.ceil(getFontMetrics(s, font).getWidth());
    }


    
    static class FontBox extends JComponent {

        FontBox() { }

        public Dimension getFontBounds(String s, Font font) {
            FontMetrics fontMetrics = getFontMetrics(font);
            return new Dimension(fontMetrics.stringWidth(s), fontMetrics.getHeight());
        }
    }

}


