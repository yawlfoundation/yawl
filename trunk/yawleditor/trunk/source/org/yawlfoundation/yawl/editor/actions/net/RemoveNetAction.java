/*
 * Created on 28/10/2003
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

package org.yawlfoundation.yawl.editor.actions.net;

import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RemoveNetAction extends YAWLSelectedNetAction implements TooltipTogglingWidget {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Remove Net");
    putValue(Action.LONG_DESCRIPTION, "Remove the selected net ");
    putValue(Action.SMALL_ICON, getIconByName("RemoveNet"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
  }

  public void actionPerformed(ActionEvent event) {
    YAWLEditorDesktop.getInstance().removeActiveNet();          
    SpecificationUndoManager.getInstance().setDirty(true);
  }
  
  public void receiveSpecificationModelNotification(SpecificationModel.State state) {
  	if (!(state == SpecificationModel.State.SOME_NET_SELECTED)){
  	  super.receiveSpecificationModelNotification(state);
  	} else {
  	  if (YAWLEditorDesktop.getInstance().getSelectedGraph().getNetModel().isStartingNet()) {
        setEnabled(false); 		
      } else {
        setEnabled(true);
      }
    }
  }
  
  public String getEnabledTooltipText() {
    return " Remove the selected net ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a net (other than the starting net)" + 
           " selected to remove it ";
  }
}