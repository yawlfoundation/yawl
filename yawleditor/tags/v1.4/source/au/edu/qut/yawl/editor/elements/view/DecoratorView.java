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
import org.jgraph.graph.VertexView;
import org.jgraph.graph.CellViewRenderer;

import au.edu.qut.yawl.editor.elements.model.Decorator;

public class DecoratorView extends VertexView {
  
  private DecoratorRenderer decoratorRenderer;

  public DecoratorView(Decorator decorator) {
    super(decorator);
    
    decoratorRenderer = new DecoratorRenderer(decorator);
  }

  public CellViewRenderer getRenderer() {
    return decoratorRenderer;
  }
}

class DecoratorRenderer extends YAWLVertexRenderer {
  private Decorator decorator;
  
  public DecoratorRenderer(Decorator decorator) {
    this.decorator = decorator;
  }
  
  protected void fillVertex(Graphics graphics, Dimension size) {
    graphics.fillRect(0, 0, size.width - 1, size.height - 1);
  }
  
  protected void drawVertex(Graphics graphics, Dimension size) {
    graphics.drawRect(0, 0,size.width - 1, size.height - 1);

    switch(decorator.getCardinalPosition()) {
      case Decorator.TOP: {
        switch(decorator.getType()) {
          case Decorator.AND_TYPE: {
            drawDownwardTriangle(graphics,size);
            return;
          }
          case Decorator.OR_TYPE: {
            drawDiamond(graphics, size);
            return;
          }
          case Decorator.XOR_TYPE: {
            drawUpwardTriangle(graphics,size);
            return;
          }
        }
        break;
      }
      case Decorator.BOTTOM: {
        switch(decorator.getType()) {
          case Decorator.AND_TYPE: {
            drawUpwardTriangle(graphics,size);
            return;
          }
          case Decorator.OR_TYPE: {
            drawDiamond(graphics, size);
            return;
          }
          case Decorator.XOR_TYPE: {
            drawDownwardTriangle(graphics,size);
            return;
          }
        }
        break;
      }
      case Decorator.LEFT: {
        switch(decorator.getType()) {
          case Decorator.AND_TYPE: {
            drawRightwardTriangle(graphics, size);
            return;
          }
          case Decorator.OR_TYPE: {
            drawDiamond(graphics, size);
            return;
          }
          case Decorator.XOR_TYPE: {
            drawLeftwardTriangle(graphics, size);
            return;
          }
        }
        break;
      }
      case Decorator.RIGHT: {
        switch(decorator.getType()) {
          case Decorator.AND_TYPE: {
            drawLeftwardTriangle(graphics, size);
            return;
          }
          case Decorator.OR_TYPE: {
            drawDiamond(graphics, size);
            return;
          }
          case Decorator.XOR_TYPE: {
            drawRightwardTriangle(graphics, size);
            return;
          }
        }
        break;
      }
    }
  }
  
  private void drawDownwardTriangle(Graphics graphics,Dimension size) {
    graphics.drawLine(1,1, Math.round(size.width/2), size.height - 1);
    graphics.drawLine(Math.round(size.width/2), size.height - 1,
                      size.width - 1,1);
  }

  private void drawLeftwardTriangle(Graphics graphics,Dimension size) {
    graphics.drawLine(size.width - 1, 1,
                      1, Math.round(size.height/2));
    graphics.drawLine(1, Math.round(size.height/2),
                      size.width - 1, size.height - 1);
  }  

  private void drawRightwardTriangle(Graphics graphics,Dimension size) {
    graphics.drawLine(1, 1, 
                      size.width - 1, 
                      Math.round(size.height/2));
    graphics.drawLine(size.width - 1, 
                      Math.round(size.height/2),
                      1, size.height - 1);
  }  
  
  private void drawUpwardTriangle(Graphics graphics,Dimension size) {
    graphics.drawLine(1, size.height - 1, 
                      Math.round(size.width/2), 1);
    graphics.drawLine(Math.round(size.width/2), 1,
                      size.width - 1, size.height - 1);
  }

  private void drawDiamond(Graphics graphics,Dimension size) {
    graphics.drawLine(1, 
                      Math.round(size.height/2),
                      Math.round(size.width/2),
                      1);
    graphics.drawLine(Math.round(size.width/2),
                      1,
                      size.width - 1,
                      Math.round(size.height/2));
    graphics.drawLine(size.width - 1,
                      Math.round(size.height/2),
                      Math.round(size.width/2),
                      size.height - 1);
    graphics.drawLine(Math.round(size.width/2),
                      size.height - 1,
                      1, 
                      Math.round(size.height/2));
  }
}
