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

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCompositeTask;

import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import org.yawlfoundation.yawl.editor.swing.element.SelectUnfoldingNetDialog;

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class SetUnfoldingNetAction extends YAWLSelectedNetAction implements TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final SelectUnfoldingNetDialog netDialog = new SelectUnfoldingNetDialog();

  private NetGraph graph;
  private YAWLCompositeTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Unfold to net...");
    putValue(Action.LONG_DESCRIPTION, "Specify the net this task unfolds to.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_U));
  }
  
  public SetUnfoldingNetAction(YAWLCompositeTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {

    netDialog.setTask(graph, task);
    netDialog.setVisible(true);

    graph.clearSelection();
  }
  
  public String getEnabledTooltipText() {
    return " Specify the net this task unfolds to ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a composite task selected" + 
           " to specify the net it unfolds to ";
  }
}
