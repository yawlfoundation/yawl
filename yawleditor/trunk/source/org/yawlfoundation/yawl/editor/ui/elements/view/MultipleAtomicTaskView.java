/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.elements.view;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.VertexView;

import javax.swing.*;
import java.awt.*;

public class MultipleAtomicTaskView extends VertexView {

  private static final MultipleAtomicTaskRenderer renderer = new MultipleAtomicTaskRenderer();

  public MultipleAtomicTaskView(Object vertex) {
    super(vertex);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }


  public static class MultipleAtomicTaskRenderer extends YAWLVertexRenderer {

    public static final int GAP_DIVISOR = 4;

    protected int horizontalGap = 0;
    protected int verticalGap   = 0;

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
      
      drawIcon(graphics, getSize()); 

      graphics.drawRect(0, verticalGap,
                        size.width - (1 + horizontalGap), 
                        size.height - (1 + verticalGap));
    }

    protected int getIconHorizontalOffset(Dimension size, Icon icon) {
      return (size.width - horizontalGap - icon.getIconWidth())/2;
    }
    
    protected int getIconVerticalOffset(Dimension size, Icon icon) {
      return (size.height - verticalGap - icon.getIconHeight())/2 + verticalGap;
    }
  }
}

