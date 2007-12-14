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

package org.yawlfoundation.yawl.editor.actions.net;

import java.awt.event.ActionEvent;

import javax.swing.Action;


import org.yawlfoundation.yawl.editor.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class RemoveFromVisibleCancellationSetAction extends YAWLSelectedNetAction
       implements CancellationSetModelListener, TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final RemoveFromVisibleCancellationSetAction INSTANCE 
    = new RemoveFromVisibleCancellationSetAction();

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Remove from Cancellation Set");
    putValue(Action.LONG_DESCRIPTION, " Remove selected items from visible cancellation set ");
    putValue(Action.SMALL_ICON, getIconByName("RemoveFromCancellationSet"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,InputEvent.CTRL_MASK));
  }
  
  private RemoveFromVisibleCancellationSetAction() {};  
  
  public static RemoveFromVisibleCancellationSetAction getInstance() {
    return INSTANCE; 
  }
  
  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      graph.removeSelectedCellsFromVisibleCancellationSet();
    }
  }
  
  public void notify(int notificationType, YAWLTask triggeringTask) {
    if (notificationType == CancellationSetModel.NO_VALID_SELECTION_FOR_SET_MEMBERSHIP) {
      this.setEnabled(false);
      return; 
    } 
    if (notificationType == CancellationSetModel.VALID_SELECTION_FOR_SET_MEMBERSHIP) {
      if (getGraph() != null && getGraph().getCancellationSetModel().getValidSelectedCellsForExclusion().length > 0) {
        this.setEnabled(true);
      } else {
        this.setEnabled(false);
      }
    }
    if (notificationType == CancellationSetModel.SET_CHANGED) {
      if (getGraph() != null && getGraph().getCancellationSetModel().getValidSelectedCellsForExclusion().length > 0) {
        this.setEnabled(true);
      } else {
        this.setEnabled(false);
      }
    }
  }
  
  public String getEnabledTooltipText() {
    return " Remove selected items from visible cancellation set ";
  }
  
  public String getDisabledTooltipText() {
    return " You must be viewing a task's cancellation set" + 
           " and have selected some of its set members to" + 
           " remove them from the set ";
  }
}
