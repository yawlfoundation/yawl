/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.actions.net;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jgraph.event.GraphSelectionEvent;

import java.awt.geom.Rectangle2D;
import java.awt.Point;
import java.awt.Dimension;


import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;

public class ZoomSelectedElementsAction extends YAWLSelectedNetAction implements SpecificationSelectionSubscriber  {

  private static final int ZOOM_PADDING = 20;
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final ZoomSelectedElementsAction INSTANCE = new ZoomSelectedElementsAction();
  {
    putValue(Action.SHORT_DESCRIPTION, " Zoom to selected elements ");
    putValue(Action.NAME, "Zoom to selected elements");
    putValue(Action.LONG_DESCRIPTION, "Zoom to selected elements.");
    putValue(Action.SMALL_ICON, getIconByName("ZoomSelectedElements"));
  }
  
  private ZoomSelectedElementsAction() {
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { 
          SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED
        }
    );
  };  
  
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
  
  public void receiveGraphSelectionNotification(int state,GraphSelectionEvent event) {
    switch(state) {
      case SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED: {
        setEnabled(false);
        break;
      }
      case SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED: {
        setEnabled(true);
        break;
      }
    }
  }
}
