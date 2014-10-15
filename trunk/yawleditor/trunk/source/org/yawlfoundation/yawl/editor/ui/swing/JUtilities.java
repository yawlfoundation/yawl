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

package org.yawlfoundation.yawl.editor.ui.swing;


import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 *  A non-instantiable class, collecting together a loosely related set of stateless
 *  swing utility methods.
 *  @author Lindsay Bradford
 */

public class JUtilities {


    public static Window getWindow(Component c) {
        if (c == null) {
            return YAWLEditor.getInstance();
        } else if (c instanceof Window) {
            return (Window) c;
        } else {
            return getWindow(c.getParent());
        }
    }

    /**
     *  Centres the supplied Window in the middle of the screen.
     *  @param window  the window to centre on-screen
     */
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();
        window.setLocation((screenSize.width  / 2) - (windowSize.width  / 2),
                (screenSize.height / 2) - (windowSize.height / 2));
    }

    /**
     * Makes the supplied list of Component objects the same size (width and height) as the largest
     * component in the list.
     * @param components  the list of components that are to be resized.
     */
    public static void equalizeComponentSizes(List<JComponent> components) {
        Dimension  maxComponentSize = getMaxDimension(components);
        for (Component component : components) {
            component.setPreferredSize(maxComponentSize);
            component.setMaximumSize(maxComponentSize);
            component.setMinimumSize(maxComponentSize);
        }
    }


    private static Dimension getMaxDimension(List<JComponent> components) {
        Dimension maxComponentSize = new Dimension(0,0);
        Dimension currentComponentSize;
        for (Component component : components) {
            currentComponentSize = component.getPreferredSize();
            maxComponentSize.width = Math.max(maxComponentSize.width,
                    (int) currentComponentSize.getWidth());
            maxComponentSize.height = Math.max(maxComponentSize.height,
                    (int) currentComponentSize.getHeight());
        }
        return maxComponentSize;
    }

    public static void centreWindowUnderVertex(NetGraph graph,
                                               Window window,
                                               YAWLVertex vertex,
                                               int distance){
        Rectangle2D viewBounds = (vertex.getParent() != null) ?
                graph.getCellBounds(vertex.getParent()) :
                graph.getCellBounds(vertex);

        Point newLocation = graph.getLocationOnScreen();
        centreWindowUnderRectangle(window, viewBounds, newLocation, distance);
    }


    public static void centreWindowUnderRectangle(Window window,
                                                  Rectangle2D rectangle,
                                                  Point offset,
                                                  int distance) {

        offset.translate((int) (rectangle.getX() +
                        (rectangle.getWidth()/2) - window.getWidth()/2),
                (int) (rectangle.getY() + rectangle.getHeight() + distance));

        // pushing window back onto screen if it off the edge.
        final int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        final int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        // We do the RHS first, JIC the window is wider than the screen.
        // The LHS side of the window will be visible at least in this case.
        if (offset.x + window.getWidth() > screenWidth) {
            offset.x = screenWidth - window.getWidth() - 5;
        }

        if (offset.x < 0) {
            offset.translate((offset.x * -1) + 5,0);
        }

        // Same again for the vertical.

        if (offset.y + window.getHeight() > screenHeight) {
            offset.y = screenHeight - window.getHeight() - 5;
        }

        if (offset.y < 0) {
            offset.translate(0,(offset.y * -1) + 5);
        }

        window.setLocation(offset);
    }


}
