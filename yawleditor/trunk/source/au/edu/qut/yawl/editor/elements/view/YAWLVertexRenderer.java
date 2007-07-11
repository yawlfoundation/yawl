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

package au.edu.qut.yawl.editor.elements.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;

import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.foundations.ResourceLoader;

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
            ((YAWLVertex) view.getCell()).getIconPath()
        );
      } catch (Exception e) {}
    }
    
    if (icon == null) {
      return;
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
  
  abstract protected void fillVertex(Graphics graphics, Dimension size);
  
  abstract protected void drawVertex(Graphics graphics, Dimension size);
}
