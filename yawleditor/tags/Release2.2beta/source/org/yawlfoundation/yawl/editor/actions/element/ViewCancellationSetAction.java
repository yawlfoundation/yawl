/*
 * Created on 05/12/2003
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

import org.yawlfoundation.yawl.editor.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ViewCancellationSetAction extends YAWLBaseAction 
                                          implements CancellationSetModelListener{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

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