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
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCondition;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import au.edu.qut.yawl.editor.specification.SpecificationModel;

import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.data.TaskDecompositionUpdateDialog;
import au.edu.qut.yawl.editor.swing.element.LabelElementDialog;
import au.edu.qut.yawl.editor.swing.element.TaskDecompositionSelectionDialog;
import au.edu.qut.yawl.editor.swing.element.SelectUnfoldingNetDialog;

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
    
    if (selectedCell instanceof YAWLAtomicTask) {
      doAtomicTaskDoubleClickProcessing((YAWLAtomicTask) selectedCell);
    }
    if (selectedCell instanceof YAWLCompositeTask) {
      doCompositeTaskDoubleClickProcessing((YAWLCompositeTask) selectedCell );
    }
    if (selectedCell instanceof YAWLCondition) {
      doConditionDoubleClickProcessing((YAWLCondition) selectedCell);
    }
  }

  private void doAtomicTaskDoubleClickProcessing(YAWLAtomicTask atomicTask) {
    if (atomicTask.getWSDecomposition() == null) {
      TaskDecompositionSelectionDialog dialog = new TaskDecompositionSelectionDialog();
      
      dialog.setTask((YAWLTask) atomicTask, net);
      dialog.setVisible(true);
    } else {
      TaskDecompositionUpdateDialog dialog = new TaskDecompositionUpdateDialog(((YAWLTask) atomicTask).getDecomposition());

      JUtilities.centreWindowUnderVertex(net, dialog, (YAWLTask) atomicTask, 10);
      dialog.setVisible(true);
    }
  }
  
  private void doCompositeTaskDoubleClickProcessing(YAWLCompositeTask compositeTask) {
    NetGraphModel unfoldingNet = SpecificationModel.getInstance().getNetModelFromName(
      compositeTask.getUnfoldingNetName()    
    );
    
    if (unfoldingNet == null) {
      SelectUnfoldingNetDialog dialog = new SelectUnfoldingNetDialog();

      dialog.setTask(net, compositeTask);
      dialog.setVisible(true);
    } else {
      try {
        unfoldingNet.getGraph().getFrame().setIcon(true);  // this tricks the frame into taking focus.
        unfoldingNet.getGraph().getFrame().setIcon(false);
      } catch (Exception e) {}
      unfoldingNet.getGraph().getFrame().moveToFront();
      unfoldingNet.getGraph().getFrame().requestFocus();
    }

  }
  
  private void doConditionDoubleClickProcessing(YAWLCondition condition) {
    LabelElementDialog labelConditionDialog = new LabelElementDialog();    

    labelConditionDialog.setVertex(condition, net);
    labelConditionDialog.setVisible(true);
  }
}
