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

package au.edu.qut.yawl.editor.net;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import java.util.Hashtable;

import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;

import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLCondition;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.menu.PalettePopupMenu;
import au.edu.qut.yawl.editor.swing.menu.VertexPopupMenu;

public class NetPopupListener extends MouseAdapter {
  private static final PalettePopupMenu palettePopup = new PalettePopupMenu();
  private final Hashtable vertexPopupHash = new Hashtable();

  private NetGraph graph;

  public NetPopupListener(NetGraph graph) {
    this.graph = graph;
  }
  
  public void mousePressed(MouseEvent event) {
    Object cell = 
      graph.getFirstCellForLocation(event.getX(), event.getY());

    if (SwingUtilities.isRightMouseButton(event)) {
      if (cell instanceof YAWLTask || cell instanceof YAWLCondition)  {
        getCellPopup(cell).show(graph,event.getX(), event.getY());
      }
      if (cell instanceof VertexContainer) {
        VertexContainer container = (VertexContainer) cell;
        getCellPopup(container.getVertex()).show(graph,event.getX(), event.getY());
      }
      if (cell == null) {
        
        // Nasty hack below to solve problem where the pallet popup menu appears
        // after a user has removed a knee from a flow relation.
        
        if (graph.getSelectionCell() instanceof YAWLFlowRelation) {
          return;
        }
        
        // Back to your regularly scheduled program ...
        
        palettePopup.show(graph,event.getX(), event.getY());
      }
    }
  }
  
  private VertexPopupMenu getCellPopup(Object cell) {
    if(!vertexPopupHash.containsKey(cell)) {
      vertexPopupHash.put(cell, new VertexPopupMenu((YAWLCell) cell, graph));
    }
    return (VertexPopupMenu) vertexPopupHash.get(cell);
  }
}
