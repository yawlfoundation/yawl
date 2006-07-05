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

import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;

import au.edu.qut.yawl.editor.swing.element.LabelElementDialog;

import au.edu.qut.yawl.editor.net.NetGraph;

public class ElementDoubleClickListener extends MouseAdapter {
  private NetGraph net;

  public ElementDoubleClickListener(NetGraph net) {
    this.net = net;
  }
  
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() != 2) {
      return;
    }
    if (net.getSelectionCount() != 1) {
      return;
    }
    Object selectedCell = net.getSelectionCell();

    if (selectedCell instanceof VertexContainer) {
      selectedCell = ((VertexContainer) selectedCell).getVertex();
    }
    
    if (selectedCell instanceof YAWLVertex) {
      showLabelElementDialog((YAWLVertex) selectedCell);
    }
  }

  private void showLabelElementDialog(YAWLVertex vertex) {
    LabelElementDialog labelConditionDialog = new LabelElementDialog();    
    labelConditionDialog.setVertex(vertex, net);
    labelConditionDialog.setVisible(true);
    
  }
}
