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

import javax.swing.undo.AbstractUndoableEdit;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;

public class UndoableTaskDecompositionChange extends AbstractUndoableEdit {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Decomposition oldDecomposition;
  private Decomposition newDecomposition;
    
  private YAWLTask task;
    
  public UndoableTaskDecompositionChange(YAWLTask      task,
                                         Decomposition oldDecomposition, 
                                         Decomposition newDecomposition) {
    this.task = task;
    
    this.oldDecomposition = oldDecomposition;
    this.newDecomposition = newDecomposition;
  }
  
  public void redo() {
    task.setDecomposition(newDecomposition);
  }
  
  public void undo() {
    task.setDecomposition(oldDecomposition);
  }
}
