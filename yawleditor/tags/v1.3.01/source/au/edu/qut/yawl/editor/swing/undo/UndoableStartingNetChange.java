/*
 * Created on 17/09/2005
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

package au.edu.qut.yawl.editor.swing.undo;

import javax.swing.undo.AbstractUndoableEdit;

import au.edu.qut.yawl.editor.net.NetGraphModel;

public class UndoableStartingNetChange extends AbstractUndoableEdit {
  private NetGraphModel newStartingNet;
  private NetGraphModel oldStartingNet;
    
  public UndoableStartingNetChange(NetGraphModel newStartingNet, NetGraphModel oldStartingNet) {
    this.newStartingNet = newStartingNet;
    this.oldStartingNet = oldStartingNet;
  }
  
  public void redo() {
    oldStartingNet.setIsStartingNet(false);
    newStartingNet.setIsStartingNet(true);
  }
  
  public void undo() {
    oldStartingNet.setIsStartingNet(true);
    newStartingNet.setIsStartingNet(false);
  }
}
