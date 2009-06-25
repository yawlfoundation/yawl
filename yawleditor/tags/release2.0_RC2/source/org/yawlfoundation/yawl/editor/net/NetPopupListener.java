/*
 * Created on 05/12/2003
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

package org.yawlfoundation.yawl.editor.net;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import java.util.Hashtable;

import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;

import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCondition;

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.swing.menu.FlowPopupMenu;
import org.yawlfoundation.yawl.editor.swing.menu.PalettePopupMenu;
import org.yawlfoundation.yawl.editor.swing.menu.VertexPopupMenu;

public class NetPopupListener extends MouseAdapter {
  private static final PalettePopupMenu palettePopup = new PalettePopupMenu();
  private final Hashtable vertexPopupHash = new Hashtable();

  private NetGraph graph;

  public NetPopupListener(NetGraph graph) {
    this.graph = graph;
  }
  
  public void mousePressed(MouseEvent event) {
    if (!SwingUtilities.isRightMouseButton(event)) {
      return;
    }
    
    YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
        graph.getFirstCellForLocation(
            event.getX(), 
            event.getY()
        )
    );
    
    if (vertex != null && (vertex instanceof YAWLTask || 
                           vertex instanceof YAWLCondition))  {
      getCellPopup(vertex).show(
          graph,
          event.getX(), 
          event.getY()
      );
      return;
    }
    
    YAWLFlowRelation flow = NetCellUtilities.getFlowRelationFromCell(
        graph.getFirstCellForLocation(
            event.getX(), 
            event.getY()
        )
    );
    
    if (flow != null) {
      FlowPopupMenu flowPopup = new FlowPopupMenu(graph, flow, event.getPoint());
      flowPopup.show(
          graph,
          event.getX(), 
          event.getY()
      );
      
      return;
    }

    palettePopup.show(graph,event.getX(), event.getY());

  }
  
  private VertexPopupMenu getCellPopup(Object cell) {
    if(!vertexPopupHash.containsKey(cell)) {
      vertexPopupHash.put(cell, new VertexPopupMenu((YAWLCell) cell, graph));
    }
    return (VertexPopupMenu) vertexPopupHash.get(cell);
  }
}
