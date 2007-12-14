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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

import org.jgraph.graph.VertexView;
import org.jgraph.graph.CellViewRenderer;

public class InputConditionView extends VertexView {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final InputConditionRenderer renderer = new InputConditionRenderer();

  public InputConditionView(Object vertex) {
    super(vertex);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }
}

class InputConditionRenderer extends ConditionView.ConditionRenderer {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected void drawVertex(Graphics graphics, Dimension size) {
    super.drawVertex(graphics, size);

    Polygon startArrow = new Polygon();
    startArrow.addPoint(Math.round(size.width/3),
                        Math.round(size.height/4));

    startArrow.addPoint(Math.round(size.width/3),
                        Math.round((size.height/4)*3));

    startArrow.addPoint(Math.round((size.width/4)*3),
                        Math.round(size.height/2));
    
    graphics.fillPolygon(startArrow);   
  }
}
