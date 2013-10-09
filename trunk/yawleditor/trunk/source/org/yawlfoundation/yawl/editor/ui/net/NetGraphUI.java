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

package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.graph.CellView;
import org.jgraph.plaf.basic.BasicGraphUI;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * An override only for the paintCell method below
 *
 * @author Michael Adams
 * @date 30/08/13
 */
public class NetGraphUI extends BasicGraphUI {

    public NetGraphUI() {
        super();
    }


    // overridden to paint view children first (if any), then view, so as to have control
    // over how the a vertex container is drawn when selected
    public void paintCell(Graphics g, CellView view, Rectangle2D bounds,
   			boolean preview) {

        if (view == null || bounds == null) return;

   		// Paint Children first
   		if (!view.isLeaf()) {
            for (CellView child : view.getChildViews()) {
   				paintCell(g, child, child.getBounds(), preview);
            }
   		}

   		// Then Paint View
   		boolean bfocus = (view == this.focus);
   		boolean sel = graph.isCellSelected(view.getCell());
   		Component component = view.getRendererComponent(graph, sel, bfocus, preview);
   		rendererPane.paintComponent(g, component, graph, (int) bounds.getX(),
                (int) bounds.getY(), (int) bounds.getWidth(),
   			    (int) bounds.getHeight(), true);
   	}

}
