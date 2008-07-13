/*
 * Created on 09/10/2003
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

package org.yawlfoundation.yawl.editor.actions.element;

import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.data.NetDecompositionUpdateDialog;
import org.yawlfoundation.yawl.editor.swing.data.TaskDecompositionUpdateDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

public class TaskDecompositionDetailAction extends YAWLSelectedNetAction 
                                           implements TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final HashMap taskDecompositionDialogs = new HashMap();
  
  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Task Decomposition Detail...");
    putValue(Action.LONG_DESCRIPTION, "Manage the decomposition this task points to.");
    putValue(Action.SMALL_ICON, getIconByName("DecompositionDetail"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
  }
  
  public TaskDecompositionDetailAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  
  
  public String getEnabledTooltipText() {
    return " Manage the decomposition this task points to ";
  }
  
  public String getDisabledTooltipText() {
    return " You need to have selected a decomposition for a task" + 
           " to manage its decomposition detail ";
  }

  public void actionPerformed(ActionEvent event) {
    NetDecompositionUpdateDialog dialog;
    if (!invokedAtLeastOnce(task.getDecomposition())) {
      if (task.getDecomposition() instanceof WebServiceDecomposition) {
        dialog = new TaskDecompositionUpdateDialog(task.getDecomposition(), graph);
      } else {
        dialog = new NetDecompositionUpdateDialog(task.getDecomposition());
      }

      JUtilities.centreWindowUnderVertex(graph, dialog, task, 10);
      taskDecompositionDialogs.put(task.getDecomposition(), dialog);
      dialog.setVisible(true);
      graph.clearSelection();
    } else {
      ((NetDecompositionUpdateDialog) taskDecompositionDialogs.get(task.getDecomposition())).setVisible(true);
    }
  }
  
  private boolean invokedAtLeastOnce(Decomposition decomposition) {
    if (taskDecompositionDialogs.containsKey(decomposition)) {
      return true;
    }
    return false;
  }
  
  public boolean shouldBeEnabled() {
    if (task.getDecomposition() == null) {
      return false;
    }
    return true;
  }
}