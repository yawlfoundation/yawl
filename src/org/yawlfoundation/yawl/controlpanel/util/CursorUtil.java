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

package org.yawlfoundation.yawl.controlpanel.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

import static antlr.build.ANTLR.root;

/**
 * @author Michael Adams
 * @date 11/11/2015
 */
public class CursorUtil {

    private static final MouseAdapter _mouseAdapter =  new MouseAdapter() {};

    private static RootPaneContainer _root;

    private CursorUtil() {}

    /** Sets cursor for specified component to Wait cursor */
    public static void showWaitCursor() {
        _root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        _root.getGlassPane().addMouseListener(_mouseAdapter);
        _root.getGlassPane().setVisible(true);
    }


    /** Sets cursor for specified component to normal cursor */
    public static void showDefaultCursor() {
        _root.getGlassPane().setCursor(Cursor.getDefaultCursor());
        _root.getGlassPane().removeMouseListener(_mouseAdapter);
        _root.getGlassPane().setVisible(false);
    }


    public static void setContainer(RootPaneContainer root) { _root = root; }

}
