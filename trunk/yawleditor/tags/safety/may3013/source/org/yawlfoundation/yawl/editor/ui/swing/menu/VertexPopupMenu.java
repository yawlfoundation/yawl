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
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.plugin.YEditorPlugin;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginLoader;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

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

  private Map<YAWLPopupMenuItem, YEditorPlugin> _item2pluginMap;

  public VertexPopupMenu(YAWLCell cell, NetGraph graph) {
    super();
    this.cell = cell;
    this.graph = graph;
    addMenuItems();
  }
  
  private void addMenuItems() {
    YAWLVertex vertex = (YAWLVertex) cell;
    addGraphSpecificMenuItems(vertex);
    addDataPerspectiveMenuItems(vertex);
    addResourcePerspectiveMenuItems(vertex);
    addConfigurableMenuItems();
      addPlugins();
  }

  private void addGraphSpecificMenuItems(YAWLVertex vertex) {
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
  }
  
  private void addResourcePerspectiveMenuItems(YAWLVertex vertex) {
    if (vertex instanceof YAWLAtomicTask) {
      addSeparator();
      add(buildManageResourcingItem());
//      add(buildCustomFormItem());
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
  
  


  private YAWLPopupMenuItem buildManageResourcingItem() {
    manageResourcingItem = 
      new YAWLPopupMenuItem(new ManageResourcingAction((YAWLTask) cell, graph));
    return manageResourcingItem;
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
                if (addedItemCount == 0) {
                    _item2pluginMap = new HashMap<YAWLPopupMenuItem, YEditorPlugin>();
                    addSeparator();
                }
                YAWLPopupMenuItem item = new YAWLPopupMenuItem(action);
                add(item);
                _item2pluginMap.put(item, plugin);
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

      // We didn't necessarily create items we don't need.
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

        if (_item2pluginMap != null) {
            for (YAWLPopupMenuItem item : _item2pluginMap.keySet()) {
                item.setEnabled(_item2pluginMap.get(item).setPopupMenuItemEnabled(cell));
            }
        }

    }
    super.setVisible(state);
  }
}
