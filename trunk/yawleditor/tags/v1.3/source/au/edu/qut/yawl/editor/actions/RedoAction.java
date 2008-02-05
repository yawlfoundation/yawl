/*
 * Created on 9/10/2003
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

package au.edu.qut.yawl.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import au.edu.qut.yawl.editor.actions.specification.*;
import au.edu.qut.yawl.editor.specification.SpecificationUndoManager;

public class RedoAction extends YAWLActiveOpenSpecificationAction {

   private static final RedoAction INSTANCE = new RedoAction();

  {
    putValue(Action.SHORT_DESCRIPTION, " Redo the last undone action ");
    putValue(Action.NAME, "Redo");
    putValue(Action.LONG_DESCRIPTION, "Redoes last undone action");
    putValue(Action.SMALL_ICON, getIconByName("Redo"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
  }
  
  private RedoAction() {};  
  
  public static RedoAction getInstance() {
    return INSTANCE; 
  }
  
  public void actionPerformed(ActionEvent event) {
    SpecificationUndoManager.getInstance().redo();
  }
}