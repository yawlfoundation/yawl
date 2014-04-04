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

package org.yawlfoundation.yawl.editor.ui.actions.net.zoom;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ZoomActualSizeAction extends YAWLSelectedNetAction {

    private static final ZoomActualSizeAction INSTANCE = new ZoomActualSizeAction();

    {
        putValue(Action.SHORT_DESCRIPTION, " Zoom to actual size ");
        putValue(Action.NAME, "Zoom to actual size");
        putValue(Action.LONG_DESCRIPTION, "Zoom to on this net.");
        putValue(Action.SMALL_ICON, getIconByName("ZoomActual"));
    }

    private ZoomActualSizeAction() {}

    public static ZoomActualSizeAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent event) {
        final NetGraph graph = getGraph();
        if (graph != null) {
            graph.setScale(1);
        }
    }
}
