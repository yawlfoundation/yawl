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

package au.edu.qut.yawl.editor.swing.menu;

import au.edu.qut.yawl.editor.actions.CutAction;
import au.edu.qut.yawl.editor.actions.CopyAction;
import au.edu.qut.yawl.editor.actions.element.DecorateTaskAction;
import au.edu.qut.yawl.editor.actions.element.LabelElementAction;
import au.edu.qut.yawl.editor.actions.element.SetMultipleInstanceDetailAction;
import au.edu.qut.yawl.editor.actions.element.SetUnfoldingNetAction;
import au.edu.qut.yawl.editor.actions.element.UpdateParametersAction;
import au.edu.qut.yawl.editor.actions.element.UpdateFlowDetailsAction;
import au.edu.qut.yawl.editor.actions.element.ViewCancellationSetAction;
import au.edu.qut.yawl.editor.actions.element.SelectTaskDecompositionAction;
import au.edu.qut.yawl.editor.actions.element.TaskDecompositionDetailAction;
import au.edu.qut.yawl.editor.actions.element.ManageResourcingAction;

import au.edu.qut.yawl.editor.actions.net.DeleteAction;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;

import au.edu.qut.yawl.editor.net.NetGraph;

import javax.swing.JPopupMenu;

public class VertexPopupMenu extends JPopupMenu {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private NetGraph graph;
  private YAWLCell cell;
  
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
    
    if (vertex.isCopyable()) {
      add(new YAWLPopupMenuItem(CutAction.getInstance()));
      add(new YAWLPopupMenuItem(CopyAction.getInstance()));
    }
    if (vertex.isRemovable()) {
      add(new YAWLPopupMenuItem(DeleteAction.getInstance()));
      addSeparator();
    }
    add(new YAWLPopupMenuItem(
        new LabelElementAction(vertex, graph)    
        )
    );
    if (vertex instanceof YAWLTask) {
      add(new YAWLPopupMenuItem(
              new DecorateTaskAction(
                  (YAWLTask) vertex, graph
              )
         )
      );
      add(buildViewCancellationSetItem());
      addSeparator();
    }
    if(vertex instanceof YAWLCompositeTask) {
      add(new YAWLPopupMenuItem(
              new SetUnfoldingNetAction(
                  (YAWLCompositeTask) vertex, graph)
              )
      );
    }
    if(vertex instanceof YAWLAtomicTask) {
      add(new YAWLPopupMenuItem(
          new SelectTaskDecompositionAction(
              (YAWLTask) vertex, graph)
          )
      );
    }
    if (vertex instanceof YAWLTask) {
      add(buildDecompositionDetailItem());
    }
    if(vertex instanceof YAWLMultipleInstanceTask) {
      add(new YAWLPopupMenuItem(
          new SetMultipleInstanceDetailAction(
              (YAWLMultipleInstanceTask) vertex, graph)
          )
      );
    }
    if (vertex instanceof YAWLTask) {
      add(buildUpdateParametersItem());
      add(buildFlowDetailItem());
    }
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
      showTaskWithOrXorSplitItemIfNecessary();
      enableDecompositionSensitiveItemsIfNecessary();
    }
    super.setVisible(state);
  }
  
  private void showTaskWithOrXorSplitItemIfNecessary() {
    if (cell instanceof YAWLTask) {
      YAWLTask task = (YAWLTask) cell;
      if (task.hasSplitDecorator() && (
          task.getSplitDecorator().getType() == Decorator.OR_TYPE || 
          task.getSplitDecorator().getType() == Decorator.XOR_TYPE )) {
        updateFlowDetailsItem.setVisible(true);
        return; 
      }
      updateFlowDetailsItem.setVisible(false);
    }
  }
  
  private void enableDecompositionSensitiveItemsIfNecessary() {
    if (cell instanceof YAWLTask) {
      if (((YAWLTask) cell).getDecomposition() == null) {
        decompositionDetailItem.setEnabled(false);
        updateParametersItem.setEnabled(false);
        if (manageResourcingItem != null) {
          manageResourcingItem.setEnabled(false);
        }
      } else {
        decompositionDetailItem.setEnabled(true);
        updateParametersItem.setEnabled(true);
        if (manageResourcingItem != null) {
          if (((ManageResourcingAction) manageResourcingItem.getAction()).shouldBeEnabled()) {
            manageResourcingItem.setEnabled(true);
          } else {
            manageResourcingItem.setEnabled(false);
          }
        }
      }
    }
  }
}
