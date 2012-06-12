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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.actions.CopyAction;
import org.yawlfoundation.yawl.editor.ui.actions.CutAction;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.actions.element.*;
import org.yawlfoundation.yawl.editor.ui.actions.net.ConfigurableTaskAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.DeleteAction;
import org.yawlfoundation.yawl.editor.ui.plugin.YEditorPlugin;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginLoader;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.foundations.ResourceLoader;

import javax.swing.*;

public class VertexPopupMenu extends JPopupMenu {

  private static final long serialVersionUID = 1L;
  private NetGraph graph;
  private YAWLCell cell;

  private YAWLPopupMenuItem decomposeToDirectDataTransferItem;
  private YAWLPopupMenuItem updateParametersItem;
  private YAWLPopupMenuItem updateFlowDetailsItem;
  private YAWLPopupMenuItem decompositionDetailItem;
  private YAWLPopupMenuItem manageResourcingItem;
  private YAWLPopupMenuItem dropTaskDecompositionItem;
  private YAWLPopupMenuItem customFormItem;
  private YAWLPopupMenuItem setTimerItem;
  private YAWLPopupMenuItem multipleInstanceDetailItem;

  private JMenu processConfigurationMenu;
  private YAWLPopupMenuCheckBoxItem configurableTaskItem;
  private YAWLPopupMenuItem inputPortConfigurationItem;
  private YAWLPopupMenuItem outputPortConfigurationItem;
  private YAWLPopupMenuItem multipleInstanceConfigurationItem;
  private YAWLPopupMenuItem cancelationRegionConfigurationItem;

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
    addConfigurableMenuItems();
      addPlugins();
  }

  private void addGraphSpecificMenuItems(YAWLVertex vertex) {
    add(new YAWLPopupMenuItem(new LabelElementAction(vertex, graph)));
    add(new YAWLPopupMenuItem(new DocumentationAction(vertex, graph)));
    if (! (vertex instanceof InputCondition || vertex instanceof OutputCondition)) {
        add(new YAWLPopupMenuItem(new SetElementFillColourAction(vertex, graph)));
        addSeparator();
    }

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
      addSeparator();
      setTimerItem = new YAWLPopupMenuItem(
              new TaskTimeoutDetailAction((AtomicTask) vertex, graph));
      add(setTimerItem);
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

      add(buildDropTaskDecompositionItem()) ;
    }

    add(buildDecompositionDetailItem());
    add(buildUpdateParametersItem());
    
    if (vertex instanceof YAWLMultipleInstanceTask) {
      add(buildMultipleInstanceDetailItem());
    }
    
    add(buildFlowDetailItem());
  }
  
  private void addResourcePerspectiveMenuItems(YAWLVertex vertex) {
    if (vertex instanceof YAWLAtomicTask) {
      addSeparator();
      add(buildManageResourcingItem());
      add(buildCustomFormItem());
    }
  }


    private void addConfigurableMenuItems() {
        if (cell instanceof YAWLTask) {
            addSeparator();
            processConfigurationMenu = new JMenu("Process Configuration");
            processConfigurationMenu.add(buildConfigurableTaskItem());
            processConfigurationMenu.add(buildInputPortConfigurationItem());
            processConfigurationMenu.add(buildOutputPortConfigurationItem());
            processConfigurationMenu.add(buildMultipleInstanceConfigurationItem());
            processConfigurationMenu.add(buildCancelationRegionConfigurationItem());
            processConfigurationMenu.setIcon(ResourceLoader.getImageAsIcon(
                    "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/wrench.png"));
            add(processConfigurationMenu);
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

  private YAWLPopupMenuItem buildCustomFormItem() {
    customFormItem =
        new YAWLPopupMenuItem(new SetCustomFormAction((YAWLTask) cell, graph));
    return customFormItem;
  }


  private YAWLPopupMenuItem buildDecompositionDetailItem() {
    decompositionDetailItem = 
    new YAWLPopupMenuItem(new TaskDecompositionDetailAction((YAWLTask) cell, graph));
    return decompositionDetailItem;
  }

  private YAWLPopupMenuItem buildDropTaskDecompositionItem() {
    dropTaskDecompositionItem =
       new YAWLPopupMenuItem(new DropTaskDecompositionAction((YAWLTask) cell, graph));
    return dropTaskDecompositionItem;
  }

  private YAWLPopupMenuItem buildFlowDetailItem() {
    updateFlowDetailsItem = 
      new YAWLPopupMenuItem(new UpdateFlowDetailsAction((YAWLTask) cell, graph));
    return updateFlowDetailsItem;
  }

  private YAWLPopupMenuItem buildMultipleInstanceDetailItem() {
    multipleInstanceDetailItem =
       new YAWLPopupMenuItem(new SetMultipleInstanceDetailAction(
                    (YAWLMultipleInstanceTask) cell, graph));
    return multipleInstanceDetailItem;
  }

    private YAWLPopupMenuCheckBoxItem buildConfigurableTaskItem() {
        ConfigurableTaskAction action = new ConfigurableTaskAction((YAWLTask)cell,graph);
        configurableTaskItem = new YAWLPopupMenuCheckBoxItem(action);
        action.setCheckBox(configurableTaskItem);
        return configurableTaskItem;
    }

  private YAWLPopupMenuItem buildInputPortConfigurationItem() {
	    inputPortConfigurationItem =
	      new YAWLPopupMenuItem(new InputPortConfigurationAction((YAWLTask)cell,graph));

	    return inputPortConfigurationItem;
	  }

  private YAWLPopupMenuItem buildOutputPortConfigurationItem() {
	  outputPortConfigurationItem =
	      new YAWLPopupMenuItem(new OutputPortConfigurationAction((YAWLTask)cell,graph));

	    return outputPortConfigurationItem;
	  }

  private YAWLPopupMenuItem buildMultipleInstanceConfigurationItem() {
	  multipleInstanceConfigurationItem =
	      new YAWLPopupMenuItem(new MultipleInstanceConfigurationAction((YAWLTask)cell));

	    return multipleInstanceConfigurationItem;
	  }

  private YAWLPopupMenuItem buildCancelationRegionConfigurationItem() {
	  cancelationRegionConfigurationItem =
	      new YAWLPopupMenuItem(new CancellationRegionConfigurationAction((YAWLTask)cell));

	    return cancelationRegionConfigurationItem;
	  }

    private int addPlugins() {
        int addedItemCount = 0;
        for (YEditorPlugin plugin : YPluginLoader.getInstance().getPlugins()) {
            YAWLBaseAction action = plugin.getPopupMenuAction();
            if (action != null) {
                if (addedItemCount == 0) addSeparator();
                add(new YAWLPopupMenuItem(action));
                addedItemCount++;
            }
        }
        return addedItemCount;
    }


  public YAWLCell getCell() {
    return cell;
  }
  
  public void setVisible(boolean state) {
    if (state) {

      // We didn't necessarilly create items we don't need. 
      // For those that we did create, let them dictate 
      // whether they''re enabled or visible.
      

    	if(cell instanceof YAWLTask){
	    	YAWLTask task = (YAWLTask)cell;
	    	configurableTaskItem.setEnabled(true);
	    	configurableTaskItem.setState(task.isConfigurable());
	    	inputPortConfigurationItem.setEnabled(task.isConfigurable());
	    	outputPortConfigurationItem.setEnabled(task.isConfigurable());
	    	multipleInstanceConfigurationItem.setEnabled((task.isConfigurable())&&(task instanceof YAWLMultipleInstanceTask ));
	    	cancelationRegionConfigurationItem.setEnabled(task.isConfigurable()&&(task.hasCancellationSetMembers()));

    	}

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
      if (dropTaskDecompositionItem != null) {
        dropTaskDecompositionItem.setEnabled(
            dropTaskDecompositionItem.shouldBeEnabled()
        );
      }
      if (multipleInstanceDetailItem != null) {
          multipleInstanceDetailItem.setEnabled(
              multipleInstanceDetailItem.shouldBeEnabled()
          );
      }
      if (customFormItem != null) {
        customFormItem.setEnabled(
            customFormItem.shouldBeEnabled()
        );
      }
          if (setTimerItem != null) {
            setTimerItem.setEnabled(
                setTimerItem.shouldBeEnabled()
            );
      }

    }
    super.setVisible(state);
  }
}
