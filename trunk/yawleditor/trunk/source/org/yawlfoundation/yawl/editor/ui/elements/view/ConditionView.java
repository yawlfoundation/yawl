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

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexView;

import java.awt.*;

public class ConditionView extends VertexView {
    private static final ConditionRenderer renderer = new ConditionRenderer();

    public ConditionView(Object vertex) {
        super(vertex);
    }

    public CellViewRenderer getRenderer() {
        return renderer;
    }

    public static class ConditionRenderer extends YAWLVertexRenderer {

        protected void fillVertex(Graphics graphics, Dimension size) {
            graphics.fillOval(0, 0, size.width, size.height);

        }

        protected void drawVertex(Graphics graphics, Dimension size) {
            graphics.drawOval(0, 0, size.width - 1, size.height - 1);
        }

        // for use by input & output conditions
        protected void overrideFill(Graphics graphics, Dimension size) {
            Color color = graphics.getColor();
            graphics.setColor(Color.WHITE);
            fillVertex(graphics, size);
            graphics.setColor(color);
        }

    }
}


