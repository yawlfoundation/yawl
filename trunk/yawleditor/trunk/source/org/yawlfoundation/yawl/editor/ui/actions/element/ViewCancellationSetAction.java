/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ViewCancellationSetAction extends YAWLBaseAction 
                                          implements CancellationSetModelListener{

  private boolean selected;
  
  private YAWLTask task;
  private NetGraph graph;
  private JCheckBoxMenuItem checkBox = null;

  {
    putValue(Action.SHORT_DESCRIPTION, " Toggle viewing of this task's cancellation set. ");
    putValue(Action.NAME, "View Cancellation Set");
    putValue(Action.LONG_DESCRIPTION, " Toggle viewing of this task's cancellation set. ");
    putValue(Action.SMALL_ICON, getPNGIcon("zoom_out"));
    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_V));
  }

  public ViewCancellationSetAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }
  
  public void actionPerformed(ActionEvent event) {
    selected = !selected;
    
    if (selected) {
      graph.changeCancellationSet(task); 
    } else {
      graph.changeCancellationSet(null);     
    }
  }
  
  public boolean isSelected() {
    return selected; 
  }
  
  public void setSelected(boolean selected) {
  	this.selected = selected;
  }
  
  public void setCheckBox(JCheckBoxMenuItem checkBox) {
    this.checkBox = checkBox;
    this.graph.getCancellationSetModel().subscribe(this);
  }
  
  public void notify(int notificationType, YAWLTask triggeringTask) {
    if (notificationType == CancellationSetModel.SET_CHANGED) {
      if (triggeringTask == null || triggeringTask != this.task) {
        checkBox.setSelected(false);
        selected = false;
      } else {
        checkBox.setSelected(true);
        selected = true;
      }
    }
  }
}