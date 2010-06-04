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

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Lindsay Bradford
 *
 */
public class CopyAction extends YAWLBaseAction implements TooltipTogglingWidget, SpecificationSelectionSubscriber {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final CopyAction INSTANCE = new CopyAction();

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Copy");
    putValue(Action.LONG_DESCRIPTION, "Copy the selected elements");
    putValue(Action.SMALL_ICON, getPNGIcon("page_copy"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("C"));
  }
  
  private CopyAction() {
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { 
          SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_COPYABLE_ELEMENTS_SELECTED
        }
    );
  };  
  
  public static CopyAction getInstance() {
    return INSTANCE; 
  }
  
  public void actionPerformed(ActionEvent event) {
    YAWLTask task = this.getGraph().viewingCancellationSetOf();

    getGraph().stopUndoableEdits();
    getGraph().changeCancellationSet(null);
    
    TransferHandler.getCopyAction().actionPerformed(
      new ActionEvent(getGraph(), 
                      event.getID() , 
                      event.getActionCommand() ));
    PasteAction.getInstance().setEnabled(true);   

    getGraph().changeCancellationSet(task);
    getGraph().startUndoableEdits();
  }
  
  public String getEnabledTooltipText() {
    return " Copy the selected net elements ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to copy them ";
  }
  
  public void receiveGraphSelectionNotification(int state,GraphSelectionEvent event) {
    switch(state) {
      case SpecificationSelectionListener.STATE_COPYABLE_ELEMENTS_SELECTED: {
        setEnabled(true);
        break;
      }
      default: {
        setEnabled(false);
        break;
      }
    }
  }
}
