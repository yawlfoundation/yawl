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

package org.yawlfoundation.yawl.editor.ui.elements.view;

import org.jgraph.graph.*;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 30/08/13
 */
public class VertexContainerView extends VertexView {

    private static final VertexContainerRenderer renderer = new VertexContainerRenderer();

    public VertexContainerView(Object vertex) {
        super(vertex);
    }

    public CellViewRenderer getRenderer() {
        return renderer;
    }


    /**
     * Overridden to prevent size handles from being drawn on selected containers
     */
    @Override
    public CellHandle getHandle(GraphContext context) {
        return null;
    }
}


class VertexContainerRenderer extends VertexRenderer {

    // only need to paint the container if its selected, and overridden to not draw
    // those pesky, but useless, size handles
    @Override
    public void paint(Graphics g) {
        if (selected) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke stroke = g2.getStroke();
            g2.setStroke(GraphConstants.SELECTION_STROKE);
            g2.setColor(highlightColor);
            g2.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
            g2.setStroke(stroke);
        }
    }
}
