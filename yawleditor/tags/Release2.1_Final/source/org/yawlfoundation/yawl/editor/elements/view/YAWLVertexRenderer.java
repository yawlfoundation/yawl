/*
 * Created on 20/12/2003
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

package org.yawlfoundation.yawl.editor.elements.view;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;

import javax.swing.*;
import java.awt.*;

abstract class YAWLVertexRenderer extends VertexRenderer {

    protected static final int CONFIGURED_TASK_STOKE_WIDTH = 4;

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    boolean tmp = selected;
    if (isOpaque()) {
      g.setColor(super.getBackground());
      fillVertex(g, getSize());
      g.setColor(super.getForeground());
    }
    try {
      setBorder(null);
      setOpaque(false);
      selected = false;
      drawIcon(g, getSize());
      setStroke(g2);
      drawVertex(g, getSize());
    } finally {
      selected = tmp;
    }
    if (bordercolor != null) {
      g2.setStroke(new BasicStroke(1));
      g.setColor(bordercolor);
      drawIcon(g, getSize()); 
      drawVertex(g, getSize());
    }
    if (selected) {
      g2.setStroke(GraphConstants.SELECTION_STROKE);
      g.setColor(highlightColor);
      drawIcon(g, getSize()); 
      drawVertex(g, getSize());
    }

    g2.setStroke(new BasicStroke(1));
    if (view.getCell() instanceof YAWLTask) {
        YAWLTask task = (YAWLTask) view.getCell();
        if (task.hasCancellationSetMembers()) {
            drawCancelSetMarker(g, getSize());
        }
        if (isAutomatedTask(task)) {
            drawAutomatedMarker(g, getSize());
            if (hasCodelet(task)) {
                drawCodeletMarker(g, getSize());
            }
        }
        if (task instanceof AtomicTask) {
            if (((AtomicTask) task).hasTimerEnabled()) {
                drawTimerMarker(g, getSize());                
            }
        }
    }
  }
 
  protected void drawIcon(Graphics graphics, Dimension size) {
    if (!(view.getCell() instanceof YAWLVertex) || 
        ((YAWLVertex) view.getCell()).getIconPath() == null) {
     return; 
    }

    /*
     * We try loading the icon from interal to the Jar first. If
     * that fails, we assume it's external, and try again.
     */
    
    Icon icon = null;
    
    try {
      icon = ResourceLoader.getImageAsIcon(
          ((YAWLVertex) view.getCell()).getIconPath()
      );
    } catch (Exception e) {}
    
    if (icon == null) {
      try {
        icon = ResourceLoader.getExternalImageAsIcon(
            FileUtilities.getAbsoluteTaskIconPath(
                ((YAWLVertex) view.getCell()).getIconPath()
            )
        );
      } catch (Exception e) {}
    }
    
    /*
     * If everything else fails, default to a default broken icon
     */
    
    if (icon == null) {
      try {
        icon = ResourceLoader.getImageAsIcon(
            "/org/yawlfoundation/yawl/editor/resources/taskicons/BrokenIcon.png"
        );
      } catch (Exception e) {}
    }

      if (icon != null) {
          icon.paintIcon(
                  null,
                  graphics,
                  getIconHorizontalOffset(size, icon),
                  getIconVerticalOffset(size,icon)
          );
      }
  }
  
  protected int getIconHorizontalOffset(Dimension size, Icon icon) {
    return (size.width - icon.getIconWidth())/2;
  }
  
  protected int getIconVerticalOffset(Dimension size, Icon icon) {
    return (size.height - icon.getIconHeight())/2;
  }

    protected void setStroke(Graphics2D g2) {
        if ((view.getCell() instanceof YAWLTask) && ((YAWLTask) view.getCell()).isConfigurable()) {
            g2.setStroke(new BasicStroke(CONFIGURED_TASK_STOKE_WIDTH));
        }
    }

    // these indicator marker graphics are designed to occupy 25% of the width of
    // a task, and 25% of the height, across the top of the task

    protected void drawCancelSetMarker(Graphics graphics, Dimension size) {
       int height = getMarkerHeight(size);
       graphics.setColor(Color.red);
       graphics.fillOval(Math.round(3 * size.width/4) - 2, 1, height, height);
    }

    protected void drawTimerMarker(Graphics graphics, Dimension size) {
       int height = getMarkerHeight(size);
       int centre = height/2 + 1; 
       graphics.setColor(Color.white);
       graphics.fillOval(1, 1, height, height);
       graphics.setColor(Color.black);
       graphics.drawOval(1, 1, height, height);
       graphics.drawLine(centre, 1, centre, centre);
       graphics.drawLine(centre, centre, height + 1, centre); 
    }

    protected void drawAutomatedMarker(Graphics graphics, Dimension size) {
        int height = getMarkerHeight(size);
        int midWidth = Math.round(size.width/2);
        int eighthwidth = Math.round(size.width/8);
        graphics.setColor(Color.black);
        int[] x = { midWidth - eighthwidth, midWidth - eighthwidth, midWidth + eighthwidth };
        int[] y = { 2, height, height/2 + 1 };
        graphics.drawPolygon(x, y, 3);
    }

    protected void drawCodeletMarker(Graphics graphics, Dimension size) {
        int height = getMarkerHeight(size);
        int midWidth = Math.round(size.width/2);
        int eighthwidth = Math.round(size.width/8);
        graphics.setColor(Color.green.darker());
        int[] x = { midWidth - eighthwidth, midWidth - eighthwidth, midWidth + eighthwidth };
        int[] y = { 2, height, height/2 + 1 };
         graphics.fillPolygon(x, y, 3);
    }

    private int getMarkerHeight(Dimension size) {
        return Math.round(size.height/4);
    }

  private boolean isAutomatedTask(YAWLTask task) {
      WebServiceDecomposition decomp = getWebServiceDecomposition(task);
      return (decomp != null) && (! decomp.isManualInteraction());
  }

  private boolean hasCodelet(YAWLTask task) {
      WebServiceDecomposition decomp = getWebServiceDecomposition(task);
      return decomp.getCodelet() != null;
  }

  private WebServiceDecomposition getWebServiceDecomposition(YAWLTask task) {
      WebServiceDecomposition result = null;
      Decomposition decomp = task.getDecomposition();
      if (decomp instanceof WebServiceDecomposition) {
          result = (WebServiceDecomposition) decomp;
      }
      return result;
  }


  abstract protected void fillVertex(Graphics graphics, Dimension size);
  
  abstract protected void drawVertex(Graphics graphics, Dimension size);
}
