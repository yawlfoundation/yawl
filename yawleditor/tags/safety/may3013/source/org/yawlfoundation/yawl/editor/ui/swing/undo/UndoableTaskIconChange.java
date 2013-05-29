/*
 * Created on 27/04/2004
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

package org.yawlfoundation.yawl.editor.ui.swing.undo;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableTaskIconChange extends AbstractUndoableEdit {

  private NetGraph net;
  private YAWLTask task;
  
  private String oldIconPath;
  private String newIconPath;


  public UndoableTaskIconChange(NetGraph net, YAWLVertex vertex,
                                String oldIconPath, String newIconPath) {
    this.net = net;
    this.task = (YAWLTask) vertex;
    this.oldIconPath = oldIconPath;
    this.newIconPath = newIconPath;
  }
  
  public void redo() {
    task.setIconPath(newIconPath);
    net.repaint();
  }
  
  public void undo() {
    task.setIconPath(oldIconPath);
    net.repaint();
  }
}
