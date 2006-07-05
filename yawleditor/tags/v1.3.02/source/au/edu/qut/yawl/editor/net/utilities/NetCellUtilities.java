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
import java.util.Set;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

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
      
    Set cells = NetGraphModel.getDescendants(
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
  
  public static void translateView(NetGraph net, CellView view, int x, int y) {
    Rectangle bounds = view.getBounds();
    bounds.translate(x,y);
    if (view instanceof VertexView) {
      ((VertexView)view).setBounds(bounds);
    }
    if (view instanceof EdgeView) {
      EdgeView edgeView = (EdgeView) view;
      for (int i = 0; i < edgeView.getPointCount(); i++) {
        Point point = edgeView.getPoint(i);
        point.translate(x,y);
      }
    }
    applyViewChange(net, view);
  }

  public static void applyViewChange(NetGraph net, CellView view) {
    CellView[] allViews =
      VertexView.getDescendantViews(
        new CellView[] { view });
    Map attributes = GraphConstants.createAttributes(allViews, null);
    net.getGraphLayoutCache().edit(attributes, null, null, null);
  }
}
