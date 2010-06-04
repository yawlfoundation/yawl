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

public class AtomicTaskView extends VertexView {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final AtomicTaskRenderer renderer = new AtomicTaskRenderer();

  public AtomicTaskView(Object vertex) {
    super(vertex);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }
  
  public static CellViewRenderer getClassRenderer() {
    return renderer;
  }
}

class AtomicTaskRenderer extends YAWLVertexRenderer {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected void fillVertex(Graphics graphics, Dimension size) {
    graphics.fillRect(0, 0, size.width, size.height);
  }
  
  protected void drawVertex(Graphics graphics, Dimension size) {
    graphics.drawRect(0, 0,size.width-1, size.height-1);
  }
}
