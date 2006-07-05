/*
 * Created on 09/10/2003
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

package au.edu.qut.yawl.editor.actions.specification;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import au.edu.qut.yawl.editor.actions.YAWLBaseAction;

import au.edu.qut.yawl.editor.specification.SpecificationFileModel;
import au.edu.qut.yawl.editor.specification.SpecificationFileModelListener;
import au.edu.qut.yawl.editor.specification.SpecificationUndoManager;

import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;

public class CreateSpecificationAction extends YAWLBaseAction 
                                       implements SpecificationFileModelListener,
                                       TooltipTogglingWidget {
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Create Specification");
    putValue(Action.LONG_DESCRIPTION, "Create a new specification");
    putValue(Action.SMALL_ICON, getIconByName("New"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
    SpecificationFileModel.getInstance().subscribe(this);
  }
  
  public void actionPerformed(ActionEvent event) {
    SpecificationFileModel.getInstance().incrementFileCount();
    YAWLEditorDesktop.getInstance().newNet();
    SpecificationUndoManager.getInstance().discardAllEdits();
  }
  
  public void specificationFileModelStateChanged(int state) {
    switch(state) {
      case SpecificationFileModel.IDLE: {
        setEnabled(true);
        break;
      }
      case SpecificationFileModel.EDITING: {
        setEnabled(false);
        break;
      }
      case SpecificationFileModel.BUSY: {
        setEnabled(false);
        break;
      }
    }
  }
  
  public String getEnabledTooltipText() {
    return " Create a new specification ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have no specification" + 
           " open to in order to create a new one ";
  }
}
