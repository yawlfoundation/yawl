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

public class CompositeTaskView extends VertexView {

    private static final CompositeTaskRenderer renderer = new CompositeTaskRenderer();

    public CompositeTaskView(Object vertex) {
        super(vertex);
    }

    public CellViewRenderer getRenderer() {
        return renderer;
    }


    public static class CompositeTaskRenderer extends YAWLVertexRenderer {

        protected static final int INNER_GAP_DIVISOR = 8;

        protected int innerHorizontalGap;
        protected int innerVerticalGap;

        protected int doubleInnerHorizontalGap;
        protected int doubleInnerVerticalGap;


        protected void fillVertex(Graphics graphics, Dimension size) {
            innerHorizontalGap = size.width/INNER_GAP_DIVISOR;
            innerVerticalGap = size.height/INNER_GAP_DIVISOR;

            doubleInnerHorizontalGap = innerHorizontalGap * 2;
            doubleInnerVerticalGap = innerVerticalGap * 2;

            graphics.fillRect(0, 0, size.width, size.height);
        }

        protected void drawVertex(Graphics graphics, Dimension size) {
            graphics.drawRect(0, 0,size.width - 1, size.height - 1);

            // inner rect should always have a pen width of 1, regardless of outer pen width
            ((Graphics2D) graphics).setStroke(new BasicStroke(1));
            graphics.drawRect(innerHorizontalGap, innerVerticalGap,
                    size.width  - (1 + doubleInnerHorizontalGap),
                    size.height - (1 + doubleInnerVerticalGap));
        }
    }
}


