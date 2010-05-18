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

public class CompositeTaskView extends VertexView {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final CompositeTaskRenderer renderer = new CompositeTaskRenderer();

  public CompositeTaskView(Object vertex) {
    super(vertex);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }

  public static class CompositeTaskRenderer extends YAWLVertexRenderer {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static final int INNER_GAP_DIVISOR = 8;

    protected int innerHorizontalGap;
    protected int innerVerticalGap;  

    protected int doubleInnerHorizontalGap;
    protected int doubleInnerVerticalGap;
    

    protected void fillVertex(Graphics graphics, Dimension size) {
      innerHorizontalGap = size.width/INNER_GAP_DIVISOR;
      innerVerticalGap = size.height/INNER_GAP_DIVISOR;

      doubleInnerHorizontalGap = innerHorizontalGap * 2;
      doubleInnerVerticalGap = innerVerticalGap * 2;

      graphics.fillRect(0, 0, size.width, size.height);
   }
  
    protected void drawVertex(Graphics graphics, Dimension size) {
      graphics.drawRect(0, 0,size.width - 1, size.height - 1);

      // inner rect should always have a pen width of 1, regardless of outer pen width  
      ((Graphics2D) graphics).setStroke(new BasicStroke(1));
      graphics.drawRect(innerHorizontalGap, innerVerticalGap, 
                        size.width  - (1 + doubleInnerHorizontalGap), 
                        size.height - (1 + doubleInnerVerticalGap));
    }
  }
}


