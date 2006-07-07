/*
 * Created on 12/11/2004
 * YAWLEditor v1.01 
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
 */
package au.edu.qut.yawl.editor.net.utilities;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import javax.swing.JViewport;

import org.jgraph.graph.CellView;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexView;


import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.net.NetGraphModel;


public class NetCellUtilities {

  public static void propogateFontChangeAcrossNet(NetGraph net, Font font) {
    net.setFont(font);
    
    if (net.getNetModel() == null) {
     return; 
    }
    
    net.getNetModel().beginUpdate();
      
    List cells = NetGraphModel.getDescendants(
        net.getNetModel(), 
        NetGraphModel.getRoots(net.getNetModel())
    );
    
    Iterator vertexContainerIterator = cells.iterator();
    while(vertexContainerIterator.hasNext()) {
      GraphCell cell = (GraphCell) vertexContainerIterator.next();
      if (cell instanceof VertexContainer) {
        VertexContainer container = (VertexContainer) cell;
        if (container.getLabel() != null) {
          String label = container.getLabel().getLabel();
          
          // cheap and nasty way of getting the label to position nicely
          // with the vertex is to delete, and set it again, letting 
          // the method do the math for positioning them in relation 
          // to each other.
          
          net.setElementLabelInsideUpdate(container.getVertex(),"");
          net.setElementLabelInsideUpdate(container.getVertex(),label);
        }
      }
    }

    net.getNetModel().endUpdate();
  }
  
  public static void translateView(NetGraph net, CellView view, double x, double y) {
    translateViews(net, new CellView[] { view }, x, y);
  }
  
  public static void translateViews(NetGraph net, CellView[] views, double x, double y) {
    for(int i = 0; i < views.length; i++) {
      Rectangle2D oldBounds = views[i].getBounds();
      Rectangle2D.Double newBounds = 
        new Rectangle2D.Double(
            oldBounds.getX() + x,
            oldBounds.getY() + y,
            oldBounds.getWidth(),
            oldBounds.getHeight()
        );
      if (views[i] instanceof VertexView) {
        ((VertexView)views[i]).setBounds(newBounds);
      }
      if (views[i] instanceof EdgeView) {
        EdgeView edgeView = (EdgeView) views[i];
        for (int j = 0; j < edgeView.getPointCount(); j++) {
          Point2D oldPoint = edgeView.getPoint(j);
          Point2D.Double newPoint = new Point2D.Double(
              oldPoint.getX() + x,
              oldPoint.getY() + y
          );
          edgeView.setPoint(j, newPoint);
        }
      }
    }
    net.setGridEnabled(false);
    applyViewChange(net, views);
    net.setGridEnabled(true);
  }
  
  public static void moveViewToLocation(NetGraph net, CellView view, double x, double y) {
    moveViewsToLocation(net, new CellView[] { view }, x, y);
  }
  
  public static void moveViewsToLocation(NetGraph net, CellView[] views, double x, double y) {
    for(int i = 0; i < views.length; i++) {
      Rectangle2D oldBounds = views[i].getBounds();
      Rectangle2D.Double newBounds = 
        new Rectangle2D.Double(
            x,
            y,
            oldBounds.getWidth(),
            oldBounds.getHeight()
        );
      if (views[i] instanceof VertexView) {
        ((VertexView)views[i]).setBounds(newBounds);
      }
    }
    net.setGridEnabled(false);
    applyViewChange(net, views);
    net.setGridEnabled(true);
  }
  
  public static void resizeViews(NetGraph net, CellView[] views, double width, double height) {
    for(int i = 0; i < views.length; i++) {
      resizeView(net,views[i],width,height);
    }
  }

  public static void resizeView(NetGraph net, CellView view, double width, double height) {
    Rectangle2D oldBounds = view.getBounds();

    Rectangle2D.Double newBounds = 
      new Rectangle2D.Double(
          oldBounds.getX(),
          oldBounds.getY(),
          oldBounds.getWidth() + width,
          oldBounds.getHeight() + height
      );
    
    net.setGridEnabled(false);

    HashMap map = new HashMap();
    GraphConstants.setBounds(map,newBounds);
    
    net.getGraphLayoutCache().editCell(view.getCell(),map);
    net.setGridEnabled(true);
  }
  
  public static void applyViewChange(NetGraph net, CellView view) {
    applyViewChange(net, new CellView[] { view } );
  }
  
  public static void applyViewChange(NetGraph net, CellView[] views) {
    CellView[] allViews =
      VertexView.getDescendantViews(views);
    Map attributes = GraphConstants.createAttributes(allViews, null);
    net.getModel().edit(attributes, null, null, null);
  }
  
  public static void alignCellsAlongTop(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      for (int i = 0; i < cells.length; i++) {
        Rectangle2D bounds = net.getCellBounds(cells[i]);
        net.moveElementBy(
            (GraphCell) cells[i], 
            0, 
            (-1 * bounds.getY()) + r.getY()
        );
      }
      net.getNetModel().endUpdate();
    }
  }

  public static void alignCellsAlongHorizontalCentre(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      double cy = r.getHeight() / 2;
      net.getNetModel().beginUpdate();
      for (int i = 0; i < cells.length; i++) {
        Rectangle2D bounds = net.getCellBounds(cells[i]);
        net.moveElementBy(
            (GraphCell) cells[i], 
            0, 
            (-1 * bounds.getY())
            + r.getY() + cy - bounds.getHeight() / 2
        );
      }
      net.getNetModel().endUpdate();
    }
  }
  
  public static void alignCellsAlongBottom(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      for (int i = 0; i < cells.length; i++) {
        Rectangle2D bounds = net.getCellBounds(cells[i]);
        net.moveElementBy((GraphCell) cells[i], 0, (-1 * bounds.getY())
            + r.getY() + r.getHeight() - bounds.getHeight());
      }
      net.getNetModel().endUpdate();
    }
  }

  public static void alignCellsAlongLeft(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      for (int i = 0; i < cells.length; i++) {
        Rectangle2D bounds = net.getCellBounds(cells[i]);
        net.moveElementBy((GraphCell) cells[i], (-1 * bounds.getX())
            + r.getX(), 0);
      }
      net.getNetModel().endUpdate();
    }
  }

  public static void alignCellsAlongVerticalCentre(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      double cx = r.getWidth() / 2;
      net.getNetModel().beginUpdate();
      for (int i = 0; i < cells.length; i++) {
        Rectangle2D bounds = net.getCellBounds(cells[i]);
        net.moveElementBy(
            (GraphCell) cells[i], 
            (-1 * bounds.getX()) + r.getX()
            + cx - bounds.getWidth() / 2, 0);
      }
      net.getNetModel().endUpdate();
    }
  }
  
  public static void alignCellsAlongRight(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      for (int i = 0; i < cells.length; i++) {
        Rectangle2D bounds = net.getCellBounds(cells[i]);
        net.moveElementBy(
            (GraphCell) cells[i], 
            (-1 * bounds.getX())
            + r.getX() + r.getWidth() - bounds.getWidth(), 
            0
        );
      }
      net.getNetModel().endUpdate();
    }
  }

  public static void scrollNetToShowElement(NetGraph net, CellView element) {
    while (element.getParentView() != null) {
      element = element.getParentView();
    }
    if (net.getFrame() != null) {
      ((JViewport) net.getParent()).scrollRectToVisible(
          element.getBounds().getBounds()
      );
    }
  }

}
