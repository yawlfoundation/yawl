/*
 * Created on 11/06/2003
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
 */

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DataTypeDefinitionsAction extends YAWLOpenSpecificationAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final DataTypeDefinitionDialog dialog = new DataTypeDefinitionDialog();

  private boolean invokedAtLeastOnce = false;

  {
    putValue(Action.SHORT_DESCRIPTION, " Update Data Type Definitions. ");
    putValue(Action.NAME, "Update Data Type Definitions");
    putValue(Action.LONG_DESCRIPTION, "Update Data Type Definitions.");
    putValue(Action.SMALL_ICON, getPNGIcon("page_white_code"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("D"));
  }

  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      invokedAtLeastOnce = true;       
    }
    dialog.setVisible(true);
  }
}
