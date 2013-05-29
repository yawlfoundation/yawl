/*
 * Created on 05/10/2003
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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import java.awt.event.KeyEvent;

import org.yawlfoundation.yawl.editor.ui.actions.CopyAction;
import org.yawlfoundation.yawl.editor.ui.actions.CutAction;
import org.yawlfoundation.yawl.editor.ui.actions.PasteAction;
import org.yawlfoundation.yawl.editor.ui.actions.RedoAction;
import org.yawlfoundation.yawl.editor.ui.actions.UndoAction;
import org.yawlfoundation.yawl.editor.ui.actions.net.DeleteAction;

class EditMenu extends YAWLOpenSpecificationMenu {
    
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public EditMenu() {
    super("Edit", KeyEvent.VK_E);
  }
  
  protected void buildInterface() {
    add(new YAWLMenuItem(UndoAction.getInstance()));
    add(new YAWLMenuItem(RedoAction.getInstance()));
    addSeparator();
    add(new YAWLMenuItem(CutAction.getInstance()));
    add(new YAWLMenuItem(CopyAction.getInstance()));
    add(new YAWLMenuItem(PasteAction.getInstance()));
    addSeparator();
    add(new YAWLMenuItem(DeleteAction.getInstance()));
  }
}
