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

package org.yawlfoundation.yawl.editor.swing.undo;

import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;

import javax.swing.undo.AbstractUndoableEdit;

public class UndoableNetAddition extends AbstractUndoableEdit {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private NetGraphModel addedNet;
    
  public UndoableNetAddition(NetGraphModel addedNet) {
    this.addedNet = addedNet;
  }

  public void redo() {
    SpecificationModel.getInstance().addNetNotUndoable(addedNet);
    addedNet.getGraph().getFrame().setVisible(true);
  }

  public void undo() {
    addedNet.getGraph().getFrame().setVisible(false);
    SpecificationModel.getInstance().removeNetNotUndoable(addedNet);
  }
}
