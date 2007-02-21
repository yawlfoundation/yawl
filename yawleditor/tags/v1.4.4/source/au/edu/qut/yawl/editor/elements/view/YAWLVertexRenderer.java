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

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;

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
      drawVertex(g,getSize());
    } finally {
      selected = tmp;
    }
    if (bordercolor != null) {
      g2.setStroke(new BasicStroke(1));
      g.setColor(bordercolor);
      drawVertex(g, getSize());
    }
    if (selected) {
      g2.setStroke(GraphConstants.SELECTION_STROKE);
      g.setColor(highlightColor);
      drawVertex(g, getSize());
    }
  }
  
  abstract protected void fillVertex(Graphics graphics, Dimension size);
  
  abstract protected void drawVertex(Graphics graphics, Dimension size);
}
