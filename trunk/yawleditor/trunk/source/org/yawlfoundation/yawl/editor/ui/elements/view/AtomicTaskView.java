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

public class AtomicTaskView extends VertexView {

    private static final AtomicTaskRenderer renderer = new AtomicTaskRenderer();

    public AtomicTaskView(Object vertex) {
        super(vertex);
    }

    public CellViewRenderer getRenderer() {
        return renderer;
    }

}

class AtomicTaskRenderer extends YAWLVertexRenderer {

    protected void fillVertex(Graphics graphics, Dimension size) {
        graphics.fillRect(0, 0, size.width, size.height);
    }

    protected void drawVertex(Graphics graphics, Dimension size) {
        graphics.drawRect(0, 0, size.width-1, size.height-1);
    }

}
