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

package org.yawlfoundation.yawl.editor.ui.elements.view;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexView;

import java.awt.*;

public class MultipleCompositeTaskView extends VertexView {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final MultipleCompositeTaskRenderer renderer = 
    new MultipleCompositeTaskRenderer();

  public MultipleCompositeTaskView(Object vertex) {
    super(vertex);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }
}

class MultipleCompositeTaskRenderer extends 
  CompositeTaskView.CompositeTaskRenderer {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final int GAP_DIVISOR = 
    MultipleAtomicTaskView.MultipleAtomicTaskRenderer.GAP_DIVISOR;

  private int   horizontalGap = 0;
  private int   verticalGap   = 0;

  protected int innerHorizontalGap;
  protected int innerVerticalGap;  

  protected int doubleInnerHorizontalGap;
  protected int doubleInnerVerticalGap;

  protected void fillVertex(Graphics graphics, Dimension size) {
    horizontalGap = size.width/GAP_DIVISOR;
    verticalGap   = size.height/GAP_DIVISOR;
    
    innerHorizontalGap = size.width/INNER_GAP_DIVISOR;
    innerVerticalGap   = size.height/INNER_GAP_DIVISOR;
    
    doubleInnerHorizontalGap = innerHorizontalGap * 2;
    doubleInnerVerticalGap   = innerVerticalGap * 2;
      
    graphics.fillRect(horizontalGap, 0,
                      size.width - (1 + horizontalGap), 
                      size.height - (1+ verticalGap));
    graphics.fillRect(0, verticalGap,
                      size.width - (1 + horizontalGap), 
                      size.height - (1+ verticalGap));

  }
  
  protected void drawVertex(Graphics graphics, Dimension size) {
    Color drawingColor = graphics.getColor();

    // inner rect should always have a pen width of 1, regardless of outer pen width
    Stroke outerStroke = ((Graphics2D) graphics).getStroke();
    Stroke innerStroke = new BasicStroke(1);      

    graphics.drawRect(horizontalGap, 0,
                      size.width - (1 + horizontalGap), 
                      size.height - (1 + verticalGap));

    ((Graphics2D) graphics).setStroke(innerStroke);
    graphics.drawRect(horizontalGap + innerHorizontalGap, innerVerticalGap,
                      size.width - (1 + horizontalGap + doubleInnerHorizontalGap), 
                      size.height - (1+ verticalGap + doubleInnerVerticalGap));

    graphics.setColor(getBackground());
    graphics.fillRect(1, verticalGap + 1,
                      size.width -  (2 + horizontalGap), 
                      size.height - (2 + verticalGap));
    graphics.setColor(drawingColor);

    ((Graphics2D) graphics).setStroke(outerStroke);
    graphics.drawRect(0, verticalGap,
                      size.width - (1 + horizontalGap), 
                      size.height - (1 + verticalGap));

    ((Graphics2D) graphics).setStroke(innerStroke);
    graphics.drawRect(innerHorizontalGap, verticalGap + innerVerticalGap,
                      size.width - (1 + horizontalGap + doubleInnerHorizontalGap), 
                      size.height - (1+ verticalGap + doubleInnerVerticalGap));
  }
}
