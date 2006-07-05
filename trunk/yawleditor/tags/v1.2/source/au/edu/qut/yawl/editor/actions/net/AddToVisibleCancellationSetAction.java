/*
 * Created on 09/10/2003
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
 *
 */

package au.edu.qut.yawl.editor.actions.net;

import java.awt.event.ActionEvent;

import javax.swing.Action;


import au.edu.qut.yawl.editor.net.CancellationSetModel;
import au.edu.qut.yawl.editor.net.CancellationSetModelListener;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class AddToVisibleCancellationSetAction extends YAWLSelectedNetAction 
       implements CancellationSetModelListener, TooltipTogglingWidget  {

  private static final AddToVisibleCancellationSetAction INSTANCE 
    = new AddToVisibleCancellationSetAction();

  {
    putValue(Action.SHORT_DESCRIPTION, " Add selected items to visible cancellation set ");
    putValue(Action.NAME, "Add to Cancellation Set");
    putValue(Action.LONG_DESCRIPTION, " Add selected items to visible cancellation set ");
    putValue(Action.SMALL_ICON, getIconByName("AddToCancellationSet"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_MASK));
  }
  
  private AddToVisibleCancellationSetAction() {};  
  
  public static AddToVisibleCancellationSetAction getInstance() {
    return INSTANCE; 
  }
  
  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      graph.addSelectedCellsToVisibleCancellationSet();
    }
  }
  
  public void notify(int notificationType, YAWLTask triggeringTask) {
    if (notificationType == CancellationSetModel.NO_VALID_SELECTION_FOR_SET_MEMBERSHIP) {
      this.setEnabled(false);
      return; 
    } 
    if (notificationType == CancellationSetModel.VALID_SELECTION_FOR_SET_MEMBERSHIP) {
      if (getGraph() != null && getGraph().getCancellationSetModel().getValidSelectedCellsForInclusion().length > 0) {
        this.setEnabled(true);
      } else {
        this.setEnabled(false);
      }
    }
    if (notificationType == CancellationSetModel.SET_CHANGED) {
      if (getGraph() != null && getGraph().getCancellationSetModel().getValidSelectedCellsForInclusion().length > 0) {
        this.setEnabled(true);
      } else {
        this.setEnabled(false);
      }
    }
  }
  
  public String getEnabledTooltipText() {
    return " Add selected items to visible cancellation set ";
  }
  
  public String getDisabledTooltipText() {
    return " You must be viewing a task's cancellation set" + 
           " and have selected net elements that are not" + 
           " set members to add them to the set ";
  }
}
