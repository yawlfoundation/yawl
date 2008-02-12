/*
 * Created on 05/10/2003
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
 */

package org.yawlfoundation.yawl.editor.swing.menu;

import org.yawlfoundation.yawl.editor.actions.CutAction;
import org.yawlfoundation.yawl.editor.actions.CopyAction;
import org.yawlfoundation.yawl.editor.actions.element.LabelElementAction;
import org.yawlfoundation.yawl.editor.actions.element.ManageResourcingAction;
import org.yawlfoundation.yawl.editor.actions.element.SetMultipleInstanceDetailAction;
import org.yawlfoundation.yawl.editor.actions.element.SetUnfoldingNetAction;
import org.yawlfoundation.yawl.editor.actions.element.TaskTimeoutDetailAction;
import org.yawlfoundation.yawl.editor.actions.element.UpdateParametersAction;
import org.yawlfoundation.yawl.editor.actions.element.UpdateFlowDetailsAction;
import org.yawlfoundation.yawl.editor.actions.element.ViewCancellationSetAction;
import org.yawlfoundation.yawl.editor.actions.element.DecomposeToDirectDataTransferAction;
import org.yawlfoundation.yawl.editor.actions.element.SelectTaskDecompositionAction;
import org.yawlfoundation.yawl.editor.actions.element.TaskDecompositionDetailAction;

import org.yawlfoundation.yawl.editor.actions.net.DeleteAction;

import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLMultipleInstanceTask;

import org.yawlfoundation.yawl.editor.net.NetGraph;

import javax.swing.JPopupMenu;

public class VertexPopupMenu extends JPopupMenu {

  private static final long serialVersionUID = 1L;
  private NetGraph graph;
  private YAWLCell cell;

  private YAWLPopupMenuItem decomposeToDirectDataTransferItem;
  private YAWLPopupMenuItem updateParametersItem;
  private YAWLPopupMenuItem updateFlowDetailsItem;
  private YAWLPopupMenuItem decompositionDetailItem;
  private YAWLPopupMenuItem manageResourcingItem;

  
  public VertexPopupMenu(YAWLCell cell, NetGraph graph) {
    super();
    this.cell = cell;
    this.graph = graph;
    addMenuItems();
  }
  
  private void addMenuItems() {
    YAWLVertex vertex = (YAWLVertex) cell;
     
    addGraphSpecificMenuItems(vertex);
    addControlFlowPerspectiveMenuItems(vertex);
    addDataPerspectiveMenuItems(vertex);
    addResourcePerspectiveMenuItems(vertex);
  }
  
  private void addGraphSpecificMenuItems(YAWLVertex vertex) {
    add(new YAWLPopupMenuItem(
        new LabelElementAction(vertex, graph)    
        )
    );

    addSeparator();
    
    addCopyableMenuItems(vertex);
    addRemoveableMenuItems(vertex);
  }
  
  private void addCopyableMenuItems(YAWLVertex vertex) {
    if (!vertex.isCopyable()) {
      return;
    }
    add(new YAWLPopupMenuItem(CutAction.getInstance()));
    add(new YAWLPopupMenuItem(CopyAction.getInstance()));
  }
  
  private void addRemoveableMenuItems(YAWLVertex vertex) {
    if (!vertex.isRemovable()) {
      return;
    }
    add(new YAWLPopupMenuItem(DeleteAction.getInstance()));
  }
  
  private void addControlFlowPerspectiveMenuItems(YAWLVertex vertex) {
    if (getComponentCount() > 0 && vertex instanceof YAWLTask) {
      addSeparator();
    }
    
    if (vertex instanceof YAWLTask) {
      add(buildViewCancellationSetItem());
    }
    
    if (vertex instanceof AtomicTask) {
      add(
          new YAWLPopupMenuItem(
              new TaskTimeoutDetailAction(
                  (AtomicTask) vertex, graph
              )
          )
      );
    }
  }

  private void addDataPerspectiveMenuItems(YAWLVertex vertex) {
    if (!(vertex instanceof YAWLTask)) {
      return;
    }

    if (getComponentCount() > 0) {
      addSeparator();
    }
    
    if (vertex instanceof YAWLCompositeTask) {
      add(new YAWLPopupMenuItem(
              new SetUnfoldingNetAction(
                  (YAWLCompositeTask) vertex, graph)
              )
      );
    }
    if (vertex instanceof YAWLAtomicTask) {
      add(buildDecomposeToDirectDataTransferItem());
      
      add(new YAWLPopupMenuItem(
          new SelectTaskDecompositionAction(
              (YAWLTask) vertex, graph)
          )
      );
    }

    add(buildDecompositionDetailItem());
    add(buildUpdateParametersItem());
    
    if (vertex instanceof YAWLMultipleInstanceTask) {
      add(new YAWLPopupMenuItem(
          new SetMultipleInstanceDetailAction(
              (YAWLMultipleInstanceTask) vertex, graph)
          )
      );
    }
    
    add(buildFlowDetailItem());
  }
  
  private void addResourcePerspectiveMenuItems(YAWLVertex vertex) {
    if (vertex instanceof YAWLAtomicTask) {
      addSeparator();
      add(buildManageResourcingItem());
    }
  }
  
  private YAWLPopupMenuCheckBoxItem buildViewCancellationSetItem() {
    ViewCancellationSetAction action = 
      new ViewCancellationSetAction((YAWLTask) cell, graph);
    YAWLPopupMenuCheckBoxItem checkBoxItem = 
      new YAWLPopupMenuCheckBoxItem(action);
    action.setCheckBox(checkBoxItem);
    return checkBoxItem;
  }
  
  
  private YAWLPopupMenuItem buildDecomposeToDirectDataTransferItem() {
    decomposeToDirectDataTransferItem = 
      new YAWLPopupMenuItem(
        new DecomposeToDirectDataTransferAction(
            (YAWLTask) cell, graph)
      );
    return decomposeToDirectDataTransferItem;
  }

  private YAWLPopupMenuItem buildUpdateParametersItem() {
    updateParametersItem = 
      new YAWLPopupMenuItem(new UpdateParametersAction((YAWLTask) cell, graph));
    return updateParametersItem;
  }
  
  private YAWLPopupMenuItem buildManageResourcingItem() {
    manageResourcingItem = 
      new YAWLPopupMenuItem(new ManageResourcingAction((YAWLTask) cell, graph));
    return manageResourcingItem;
  }

  private YAWLPopupMenuItem buildDecompositionDetailItem() {
    decompositionDetailItem = 
    new YAWLPopupMenuItem(new TaskDecompositionDetailAction((YAWLTask) cell, graph));
    return decompositionDetailItem;
  }

  private YAWLPopupMenuItem buildFlowDetailItem() {
    updateFlowDetailsItem = 
      new YAWLPopupMenuItem(new UpdateFlowDetailsAction((YAWLTask) cell, graph));
    return updateFlowDetailsItem;
  }

  public YAWLCell getCell() {
    return cell;
  }
  
  public void setVisible(boolean state) {
    if (state == true) {

      // We didn't necessarilly create items we don't need. 
      // For those that we did create, let them dictate 
      // whether they''re enabled or visible.
      
      if (updateFlowDetailsItem != null) {
        updateFlowDetailsItem.setVisible(
            updateFlowDetailsItem.shouldBeVisible()
        );
      }

      if (decomposeToDirectDataTransferItem != null) {
        decomposeToDirectDataTransferItem.setEnabled(
            decomposeToDirectDataTransferItem.shouldBeEnabled()
        );
      }
      
      if (updateParametersItem != null) {
        updateParametersItem.setEnabled(
            updateParametersItem.shouldBeEnabled()
        );
      }
      
      if (updateParametersItem != null) {
        updateParametersItem.setEnabled(
            updateParametersItem.shouldBeEnabled()
        );
      }

      if (decompositionDetailItem != null) {
        decompositionDetailItem.setEnabled(
            decompositionDetailItem.shouldBeEnabled()
        );
      }
      
      if (manageResourcingItem != null) {
        manageResourcingItem.setEnabled(
            manageResourcingItem.shouldBeEnabled()
        );
      }
    }
    super.setVisible(state);
  }
}
