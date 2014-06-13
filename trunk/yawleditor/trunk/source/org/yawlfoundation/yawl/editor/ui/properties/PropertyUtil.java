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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.elements.YDecomposition;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 2/06/2014
 */
public class PropertyUtil {


    protected static YDecomposition getCommonDecomposition(Object[] cells) {
        YDecomposition common = null;
        for (Object cell : cells) {
            if (cell instanceof VertexContainer) {
                cell = ((VertexContainer) cell).getVertex();
            }
            if (! (cell instanceof YAWLTask)) return null;
            YDecomposition decomposition = ((YAWLTask) cell).getDecomposition();
            if (decomposition == null) return null;
            if (common == null) common = decomposition;
            else if (! common.equals(decomposition)) return null;
        }
        return common;
    }


    protected static Set<YAWLVertex> makeVertexSet(Object[] cells) {
        Set<YAWLVertex> set = new HashSet<YAWLVertex>();
        if (cells != null) {
            for (Object o : cells) {
                if (o instanceof VertexContainer) {
                    set.add(((VertexContainer) o).getVertex());
                }
                else if (o instanceof YAWLVertex) {
                    set.add((YAWLVertex) o);
                }
            }
        }
        return set;
    }


    protected static Color hexToColor(String hexStr) {

        // expects the format #123456
        if ((hexStr == null) || (hexStr.length() < 7)) {
            return Color.WHITE;
        }

        try {
            int r = Integer.valueOf(hexStr.substring(1, 3), 16);
            int g = Integer.valueOf(hexStr.substring(3, 5), 16);
            int b = Integer.valueOf(hexStr.substring(5, 7), 16);
            return new Color(r, g, b);
        }
        catch (NumberFormatException nfe) {
            return Color.WHITE;
        }
    }

    protected static String colorToHex(Color color) {
        String hex = "#";
        hex += intToHex(color.getRed());
        hex += intToHex(color.getGreen());
        hex += intToHex(color.getBlue());
        return hex;
    }

    private static String intToHex(int i) {
        String hex = Integer.toHexString(i).toUpperCase();
        if (hex.length() == 1) hex = "0" + hex;
        return hex;
    }

}
