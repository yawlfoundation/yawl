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

package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TimerDialog;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class TaskTimeoutDetailAction extends YAWLSelectedNetAction 
                                           implements TooltipTogglingWidget {

  private static final long serialVersionUID = 1L;

  private TimerDialog dialog = new TimerDialog();
  
  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Set Task Timer...");
    putValue(Action.LONG_DESCRIPTION, "Manage the timer behaviour of this task. ");
    putValue(Action.SMALL_ICON, getPNGIcon("clock"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
  }
  
  public TaskTimeoutDetailAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  
  
  public String getEnabledTooltipText() {
    return " Manage the timeout behaviour of this task ";
  }
  
  public String getDisabledTooltipText() {
    return " Only atomic tasks may have timeout information set for them ";
  }

  public void actionPerformed(ActionEvent event) {
    dialog.setVisible(true);

    graph.clearSelection();
  }
  
  public boolean shouldBeEnabled() {     
    return (task instanceof AtomicTask) && (task.getDecomposition() != null);
  }
}

