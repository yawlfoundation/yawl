/*
 * Created on 20/12/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import org.jgraph.graph.VertexView;
import org.jgraph.JGraph;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellViewRenderer;

public class CompositeTaskView extends VertexView {

  private static final CompositeTaskRenderer renderer = new CompositeTaskRenderer();

  public CompositeTaskView(Object vertex, JGraph graph, CellMapper cm) {
    super(vertex, graph, cm);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }

  public static class CompositeTaskRenderer extends YAWLVertexRenderer {
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
      graphics.drawRect(innerHorizontalGap, innerVerticalGap, 
                        size.width  - (1 + doubleInnerHorizontalGap), 
                        size.height - (1 + doubleInnerVerticalGap));
    }
  }
}


