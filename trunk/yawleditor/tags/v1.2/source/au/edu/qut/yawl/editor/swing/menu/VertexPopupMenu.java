/*
 * Created on 05/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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
import au.edu.qut.yawl.editor.actions.element.ViewingCancellationSetAction;
import au.edu.qut.yawl.editor.actions.element.SelectTaskDecompositionAction;
import au.edu.qut.yawl.editor.actions.element.TaskDecompositionDetailAction;

import au.edu.qut.yawl.editor.actions.net.DeleteAction;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.YAWLCondition;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.YAWLMultipleInstanceTask;

import au.edu.qut.yawl.editor.net.NetGraph;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;


public class VertexPopupMenu extends JPopupMenu {

  private NetGraph graph;
  private YAWLCell cell;
  
  private JMenuItem updateParametersItem;
  private JMenuItem updateFlowDetailsItem;
  private JMenuItem decompositionDetailItem;
  
  public VertexPopupMenu(YAWLCell cell, NetGraph graph) {
    super();
    this.cell = cell;
    this.graph = graph;
    addMenuItems();
  }
  
  private void addMenuItems() {
    YAWLVertex vertex = (YAWLVertex) cell;
    
    if (vertex.isCopyable()) {
      add(CutAction.getInstance());
      add(CopyAction.getInstance());
      addSeparator();
    }
    if (vertex.isRemovable()) {
      add(DeleteAction.getInstance());
      addSeparator();
    }
    if (vertex instanceof YAWLTask) {
      add(new DecorateTaskAction((YAWLTask) vertex, graph));
    }
    if (vertex instanceof YAWLCondition) {
      add(new LabelElementAction(vertex, graph));
    }
    if (vertex instanceof YAWLTask) {
      addSeparator();
      add(buildUpdateParametersItem());
      add(buildFlowDetailItem());
    }
    if(vertex instanceof YAWLCompositeTask) {
      addSeparator();
      add(new SetUnfoldingNetAction((YAWLCompositeTask) vertex, graph));
    }
    if(vertex instanceof YAWLAtomicTask) {
      addSeparator();
      
      add(new SelectTaskDecompositionAction((YAWLTask) vertex, graph));
    }
    if (vertex instanceof YAWLTask) {
      add(buildDecompositionDetailItem());
    }
    
    if(vertex instanceof YAWLMultipleInstanceTask) {
      addSeparator();
      add(new SetMultipleInstanceDetailAction((YAWLMultipleInstanceTask) vertex, graph));
    }
    if (vertex instanceof YAWLTask) {
      addSeparator();
      add(buildViewCancellationSetItem());
    }
  }
  
  private JMenuItem buildViewCancellationSetItem() {
    ViewingCancellationSetAction action = 
      new ViewingCancellationSetAction((YAWLTask) cell, graph);
    JCheckBoxMenuItem checkBoxItem = 
      new JCheckBoxMenuItem(action);
    action.setCheckBox(checkBoxItem);
    return checkBoxItem;
  }
  
  private JMenuItem buildUpdateParametersItem() {
    updateParametersItem = 
      new JMenuItem(new UpdateParametersAction((YAWLTask) cell, graph));
    return updateParametersItem;
  }

  private JMenuItem buildDecompositionDetailItem() {
    decompositionDetailItem = 
    new JMenuItem(new TaskDecompositionDetailAction((YAWLTask) cell, graph));
    return decompositionDetailItem;
  }

  private JMenuItem buildFlowDetailItem() {
    updateFlowDetailsItem = 
      new JMenuItem(new UpdateFlowDetailsAction((YAWLTask) cell, graph));
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
      } else {
        decompositionDetailItem.setEnabled(true);
        updateParametersItem.setEnabled(true);
      }
    }
  }
}
