/*
 * Created on 28/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

/**
 * @author Lindsay Bradford
 *
 */
public class CopyAction extends YAWLBaseAction {

  private static final CopyAction INSTANCE = new CopyAction();

  {
    putValue(Action.SHORT_DESCRIPTION, " Copy the selected elements ");
    putValue(Action.NAME, "Copy");
    putValue(Action.LONG_DESCRIPTION, "Copy the selected elements");
    putValue(Action.SMALL_ICON, getIconByName("Copy"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
  }
  
  private CopyAction() {};  
  
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
}
