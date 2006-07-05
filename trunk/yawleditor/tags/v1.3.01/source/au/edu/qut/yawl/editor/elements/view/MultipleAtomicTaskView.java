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
import java.awt.Color;
import org.jgraph.graph.VertexView;
import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;

public class MultipleAtomicTaskView extends VertexView {

  private static final MultipleAtomicTaskRenderer renderer = new MultipleAtomicTaskRenderer();

  public MultipleAtomicTaskView(Object vertex, JGraph graph, CellMapper cm) {
    super(vertex, graph, cm);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }

  public static class MultipleAtomicTaskRenderer extends YAWLVertexRenderer {
    public static final int GAP_DIVISOR = 4;

    private int horizontalGap = 0;
    private int verticalGap   = 0;

    protected void fillVertex(Graphics graphics, Dimension size) {
      horizontalGap = size.width/GAP_DIVISOR;
      verticalGap   = size.height/GAP_DIVISOR;
      
      graphics.fillRect(horizontalGap, 0,
                        size.width - (1 + horizontalGap), 
                        size.height - (1+ verticalGap));
      graphics.fillRect(1, horizontalGap +1,
                        size.width - (2 + horizontalGap), 
                        size.height - (2+ verticalGap));
    }
  
    protected void drawVertex(Graphics graphics, Dimension size) {
      Color foreground = graphics.getColor();  

      graphics.drawRect(horizontalGap, 0,
                        size.width - (1 + horizontalGap), 
                        size.height - (1+verticalGap));

      graphics.setColor(super.getBackground());
      graphics.fillRect(1, verticalGap+1,
                        size.width - (2 + horizontalGap), 
                        size.height - (2+verticalGap));
      graphics.setColor(foreground);

      graphics.drawRect(0, verticalGap,
                        size.width - (1 + horizontalGap), 
                        size.height - (1 + verticalGap));
    }
  }
}

