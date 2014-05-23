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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

/**
 * An override for a couple of paint methods
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


    // overridden to fix selection problem - see below
    protected MouseListener createMouseListener() {
   		return new NetMouseHandler();
   	}


    // overridden to tile images
    protected void paintBackgroundImage(Graphics g, Rectangle clip) {
   		Component component = graph.getBackgroundComponent();
   		if (component != null) {
   			paintBackgroundComponent(g, component, clip);
   		}
   		ImageIcon icon = graph.getBackgroundImage();
   		if (icon == null || icon.getImage() == null) {
   			return;
   		}
   		Graphics2D g2 = (Graphics2D) g;
   		AffineTransform transform = null;
   		if (graph.isBackgroundScaled()) {
   			transform = g2.getTransform();
   			g2.scale(graph.getScale(), graph.getScale());
   		}
        tileBackgroundImage(g2, icon);
   		if (transform != null) {
   			g2.setTransform(transform);
   		}
   	}


    private void tileBackgroundImage(Graphics g, ImageIcon icon) {
        ImageObserver observer = icon.getImageObserver();
        int width = graph.getWidth();
        int height = graph.getHeight();
        Image image = icon.getImage();
        int imageW = image.getWidth(observer);
        int imageH = image.getHeight(observer);

        // Tile the image to fill graph background area
        for (int x = 0; x < width; x += imageW) {
            for (int y = 0; y < height; y += imageH) {
                g.drawImage(image, x, y, graph);
            }
        }
    }

    /*********************************************************************/

    public class NetMouseHandler extends MouseHandler {

        public NetMouseHandler() { super(); }

        // overridden to ignore 'wasSelected' value, so that a cell can never
        // be selected inside its vertex container
        protected void postProcessSelection(MouseEvent e, Object cell,
                                            boolean wasSelected) {
            if (graph.isCellSelected(cell) && e.getModifiers() != 0) {
                Object parent = cell;
                Object nextParent;
                while (((nextParent = graphModel.getParent(parent)) != null)
                        && graphLayoutCache.isVisible(nextParent))
                    parent = nextParent;
                if (! SwingUtilities.isRightMouseButton(e)) {
                    selectCellForEvent(parent, e);
                }
                lastFocus = focus;
                focus = graphLayoutCache.getMapping(parent, false);
            }
        }
    }
}
