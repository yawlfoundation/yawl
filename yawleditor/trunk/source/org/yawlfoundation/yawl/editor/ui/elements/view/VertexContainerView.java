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
