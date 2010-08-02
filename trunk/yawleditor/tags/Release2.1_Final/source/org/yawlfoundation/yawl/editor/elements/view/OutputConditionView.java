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

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexView;

import java.awt.*;

public class OutputConditionView extends VertexView {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final OutputConditionRenderer renderer = new OutputConditionRenderer();

  public OutputConditionView(Object vertex) {
    super(vertex);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }
}

class OutputConditionRenderer extends ConditionView.ConditionRenderer {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Color fillColor = new Color(255,240,240);

  protected void drawVertex(Graphics graphics, Dimension size) {
    graphics.setColor(fillColor);
    super.fillVertex(graphics, size);
    graphics.setColor(Color.RED);
    graphics.fillRect(Math.round(size.width/4),
                      Math.round(size.height/4),
                      Math.round(size.width/2),
                      Math.round(size.height/2));
    graphics.setColor(Color.black);
    graphics.drawRect(Math.round(size.width/4),
                      Math.round(size.height/4),
                      Math.round(size.width/2 - 1),
                      Math.round(size.height/2 - 1));
    super.drawVertex(graphics, size);
  }
}
