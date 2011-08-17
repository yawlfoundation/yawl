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
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DropTaskDecompositionAction extends YAWLSelectedNetAction
                                           implements TooltipTogglingWidget {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private NetGraph graph;
  private YAWLTask task;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Drop Task Decomposition...");
    putValue(Action.LONG_DESCRIPTION, "Remove the current decomposition from this task.");
    putValue(Action.SMALL_ICON, getPNGIcon("chart_organisation_delete"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
  }

  public DropTaskDecompositionAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }

  public void actionPerformed(ActionEvent event) {
      if (confirmed()) {
          graph.setTaskDecomposition(task, null);
          graph.clearSelection();
          SpecificationUndoManager.getInstance().setDirty(true);
      }    
  }

  public String getEnabledTooltipText() {
    return " Remove the current decomposition from this task ";
  }

  public String getDisabledTooltipText() {
    return " You need to have selected a task with a decomposition to drop ";
  }

    public boolean shouldBeEnabled() {
     return (task.getDecomposition() != null);
  }

  private boolean confirmed() {
      Object[] choices = {"Drop", "Cancel"};
      int selection = JOptionPane.showOptionDialog(null,
          "This will permanently remove the selected Decomposition\n" +
          "and all its associated mappings from this Task.\n" +
          "This action cannot be undone. Are you sure?",
          "Confirm Drop Decomposition", JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE, null, choices, choices[1]);

      return selection == 0 ;
  }
}