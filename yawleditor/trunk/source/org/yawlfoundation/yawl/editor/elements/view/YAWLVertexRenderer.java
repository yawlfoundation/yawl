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
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;
import org.yawlfoundation.yawl.editor.net.CancellationSet;

import javax.swing.*;
import java.awt.*;

abstract class YAWLVertexRenderer extends VertexRenderer {
  
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
      drawVertex(g,getSize());
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
    if (view.getCell() instanceof YAWLTask) {
        CancellationSet cSet = ((YAWLTask) view.getCell()).getCancellationSet();
        if ((cSet != null) && (cSet.size() > 0)) {
           drawCancelSetMarker(g, getSize());
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
    
    icon.paintIcon(
        null, 
        graphics,
        getIconHorizontalOffset(size, icon),
        getIconVerticalOffset(size,icon)
    );
  }
  
  protected int getIconHorizontalOffset(Dimension size, Icon icon) {
    return (size.width - icon.getIconWidth())/2;
  }
  
  protected int getIconVerticalOffset(Dimension size, Icon icon) {
    return (size.height - icon.getIconHeight())/2;
  }


    protected void drawCancelSetMarker(Graphics graphics, Dimension size) {
       Color oldcolor = graphics.getColor();
       graphics.setColor(Color.red);
       int[] x = {size.width-7, size.width-2, size.width-2};
       int[] y = { 1, 6, 1 };
       graphics.fillPolygon(x, y, 3);
       graphics.setColor(oldcolor);
    }

  
  abstract protected void fillVertex(Graphics graphics, Dimension size);
  
  abstract protected void drawVertex(Graphics graphics, Dimension size);
}
