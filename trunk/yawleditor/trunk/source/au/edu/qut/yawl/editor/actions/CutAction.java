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

package au.edu.qut.yawl.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;

/**
 * @author Lindsay Bradford
 *
 */
public class CutAction extends YAWLBaseAction implements TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final CutAction INSTANCE = new CutAction();

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Cut");
    putValue(Action.LONG_DESCRIPTION, "Cut the selected elements");
    putValue(Action.SMALL_ICON, getIconByName("Cut"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
  }
  
  private CutAction() {};  
  
  public static CutAction getInstance() {
    return INSTANCE; 
  }
  
  public void actionPerformed(ActionEvent event) {
    YAWLTask task = this.getGraph().viewingCancellationSetOf();

    getGraph().stopUndoableEdits();
    getGraph().changeCancellationSet(null);
    getGraph().startUndoableEdits();
    
    TransferHandler.getCutAction().actionPerformed(
      new ActionEvent(getGraph(), 
                      event.getID() , 
                      event.getActionCommand() ));
    PasteAction.getInstance().setEnabled(true);   

    getGraph().stopUndoableEdits();
    getGraph().changeCancellationSet(task);
    getGraph().startUndoableEdits();
  }
  
  public String getEnabledTooltipText() {
    return " Cut the selected elements ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to cut them ";
  }
}
