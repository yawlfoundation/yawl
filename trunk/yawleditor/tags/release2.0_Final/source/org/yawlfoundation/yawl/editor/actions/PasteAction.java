/*
 * Created on 28/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.actions;

import org.yawlfoundation.yawl.editor.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PasteAction extends YAWLBaseAction {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final PasteAction INSTANCE = new PasteAction();

  {
    putValue(Action.SHORT_DESCRIPTION, " Paste contents of clipboard ");
    putValue(Action.NAME, "Paste");
    putValue(Action.LONG_DESCRIPTION, "Paste contents of clipboard");
    putValue(Action.SMALL_ICON, getPNGIcon("page_paste"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
  }
  
  private PasteAction() {
    setEnabled(false);
  };  
  
  public static PasteAction getInstance() {
    return INSTANCE; 
  }
  
  public void actionPerformed(ActionEvent event) {
    SpecificationUndoManager.getInstance().startCompoundingEdits();

    TransferHandler.getPasteAction().actionPerformed(
      new ActionEvent(getGraph(), 
                      event.getID() , 
                      event.getActionCommand()
      )
    );

    getGraph().getNetModel().remove(
      NetUtilities.getIllegallyCopiedFlows(
          getGraph().getNetModel()
      ).toArray()
    );
    
    SpecificationUndoManager.getInstance().stopCompoundingEdits();
  }
}
