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

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ElementControlClickListener extends MouseAdapter {
  private NetGraph net;

  public ElementControlClickListener(NetGraph net) {
    this.net = net;
  }
  
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() != 1) {
      return;
    }
    
    if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
      Object selectedCell = net.getFirstCellForLocation(e.getPoint().getX(), e.getPoint().getY());

      if (selectedCell instanceof VertexContainer) {
        selectedCell = ((VertexContainer) selectedCell).getVertex();
      }
      
      if (selectedCell instanceof YAWLCompositeTask) {
        doCompositeTaskCtrlClickProcessing(
            (YAWLCompositeTask) selectedCell
        );
      }
    }
    
  }

    private void doCompositeTaskCtrlClickProcessing(YAWLCompositeTask compositeTask) {
      NetGraphModel unfoldingNet = SpecificationModel.getInstance().getNets()
              .getNetModelFromName(compositeTask.getUnfoldingNetName());

        if (unfoldingNet != null) {

          YAWLEditor.getNetsPane().setSelectedComponent(
                  unfoldingNet.getGraph().getFrame());
//        try {
//          unfoldingNet.getGraph().getFrame().setIcon(true);  // this tricks the frame into taking focus.
//          unfoldingNet.getGraph().getFrame().setIcon(false);
//        } catch (Exception e) {}
//        unfoldingNet.getGraph().getFrame().moveToFront();
//        unfoldingNet.getGraph().getFrame().requestFocus();
      }
    }  
}
