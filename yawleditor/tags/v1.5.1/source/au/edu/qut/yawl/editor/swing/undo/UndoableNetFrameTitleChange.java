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

package au.edu.qut.yawl.editor.swing.undo;

import javax.swing.undo.AbstractUndoableEdit;

import au.edu.qut.yawl.editor.swing.net.YAWLEditorNetFrame;

public class UndoableNetFrameTitleChange extends AbstractUndoableEdit {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private YAWLEditorNetFrame frame;
    
  private String oldName;
  private String newName;
    
  public UndoableNetFrameTitleChange(YAWLEditorNetFrame frame, String oldName, String newName) {
    this.frame = frame;
    this.oldName = oldName;
    this.newName = newName;
  }
  
  public void redo() {
    frame.setTitle(newName);
  }
  
  public void undo() {
    frame.setTitle(oldName);
  }
}
