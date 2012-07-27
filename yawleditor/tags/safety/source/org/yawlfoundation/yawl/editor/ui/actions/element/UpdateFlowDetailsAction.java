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
import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class UpdateFlowDetailsAction extends YAWLSelectedNetAction 
                                     implements TooltipTogglingWidget {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected static final FlowPriorityDialog dialog = new FlowPriorityDialog();

  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Update Flow Detail...");
    putValue(Action.LONG_DESCRIPTION, "Update flow detail for this task.");
    putValue(Action.SMALL_ICON, getPNGIcon("arrow_divide"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
  }
  
  public UpdateFlowDetailsAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  } 
  
  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
  }
  
  public String getEnabledTooltipText() {
    return " Update flow detail for this task ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a task with an XOR-Split or OR-Split decoration selected" + 
           " to update its flow detail ";
  }
  
  public boolean shouldBeVisible() {
      Decorator split = task.getSplitDecorator();
      return ! (split == null || split.getType() == Decorator.AND_TYPE);
  }
}

