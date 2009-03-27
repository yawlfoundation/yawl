/*
 * Created on 27/05/2005
 * YAWLEditor v1.3 
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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.resourcing.ManageResourcingDialog;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ManageResourcingAction extends YAWLSelectedNetAction
                                    implements TooltipTogglingWidget {
  
  private static final long serialVersionUID = 1L;

  private YAWLTask task;
  private NetGraph graph;
  
  private ManageResourcingDialog dialog = new ManageResourcingDialog();
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Manage Resourcing...");
    putValue(Action.LONG_DESCRIPTION, "Manage the resourcing requirements of this task.");
    putValue(Action.SMALL_ICON, getPNGIcon("group"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_N));
  }
  
  public ManageResourcingAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {
    if (! isEmptyOrgModel()) {
        dialog.setTask(task, graph);
        dialog.setVisible(true);
    }
  }
 
  public String getEnabledTooltipText() {
    return " Manage the resourcing requirements of this task ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an atomic task with a worklist decomposition selected" +
           " to update its resourcing requirements ";
  }
  
  public boolean shouldBeEnabled() {
    Decomposition decomp = task.getDecomposition();
    return ((decomp != null) && decomp.invokesWorklist() && decomp.isManualInteraction());
  }

  private boolean isEmptyOrgModel() {
      if (ResourcingServiceProxy.getInstance().isLiveService()) {
          if (ResourcingServiceProxy.getInstance().getAllRoles().isEmpty() &&
              ResourcingServiceProxy.getInstance().getAllParticipants().isEmpty()) {
              JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                   "The organisational model supplied by the " +
                   "Resource Service contains no particpants or roles.\n" +
                   "There are no resources available to assign to the selected task.",
                   "No Available Resources", JOptionPane.WARNING_MESSAGE);
              return true;
          }
      }
      return false;
  }
    
}
