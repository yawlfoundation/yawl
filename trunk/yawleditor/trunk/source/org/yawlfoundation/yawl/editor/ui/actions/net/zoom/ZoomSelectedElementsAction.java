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

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class ZoomSelectedElementsAction extends YAWLSelectedNetAction implements GraphStateListener {

  private static final int ZOOM_PADDING = 20;
  private static final ZoomSelectedElementsAction INSTANCE = new ZoomSelectedElementsAction();
  {
    putValue(Action.SHORT_DESCRIPTION, " Zoom to selected elements ");
    putValue(Action.NAME, "Zoom to selected elements");
    putValue(Action.LONG_DESCRIPTION, "Zoom to selected elements.");
    putValue(Action.SMALL_ICON, getPNGIcon("magnifier"));
  }
  
  private ZoomSelectedElementsAction() {
      Publisher.getInstance().subscribe(this,
              Arrays.asList(GraphState.NoElementSelected, GraphState.ElementsSelected));
  }
  
  public static ZoomSelectedElementsAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
     graph.setScale(1);
     Rectangle2D bounds = padOutBounds(graph.getCellBounds(graph.getSelectionCells()));
     
     double xZoomFraction = graph.getBounds().getWidth()/bounds.getWidth();
     double yZoomFraction = graph.getBounds().getHeight()/bounds.getHeight();
     double usedZoomFraction = Math.min(xZoomFraction, yZoomFraction);

     graph.setScale(usedZoomFraction);

     final Dimension viewportSize = graph.getFrame().getScrollPane().getViewport().getVisibleRect().getSize();
  
     graph.getFrame().getScrollPane().getViewport().setViewPosition(
         new Point(
             (int) (bounds.getX()*usedZoomFraction - ((viewportSize.getWidth() - bounds.getWidth()*usedZoomFraction)/2)), 
             (int) (bounds.getY()*usedZoomFraction - ((viewportSize.getHeight()- bounds.getHeight()*usedZoomFraction)/2))
         )
     );

     graph.getSelectionModel().clearSelection();
     graph.revalidate();
    }
  }
  
  private Rectangle2D padOutBounds(Rectangle2D bounds) {
    bounds.add(
        bounds.getMinX() - ZOOM_PADDING,
        bounds.getMinY() - ZOOM_PADDING
    );
    bounds.add(
        bounds.getMaxX() + ZOOM_PADDING,
        bounds.getMaxY() + ZOOM_PADDING
    );
    return bounds;
  }
  
    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.ElementsSelected);
    }
}
