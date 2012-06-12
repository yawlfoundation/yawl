/*
 * Created on 09/10/2003
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

package org.yawlfoundation.yawl.editor.ui.actions.tools;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetExternalFilePathsAction extends YAWLBaseAction {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static final ExternalFilePathsDialog dialog =
          new ExternalFilePathsDialog();

  private boolean invokedAtLeastOnce = false;

  {
    putValue(Action.SHORT_DESCRIPTION, " Specify the file paths for user-defined extended attributes. ");
    putValue(Action.NAME, "External File Paths...");
    putValue(Action.LONG_DESCRIPTION, "Specify the file paths for user-defined extended attributes.");
    putValue(Action.SMALL_ICON, getPNGIcon("drive_disk"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift F"));
  }

  public SetExternalFilePathsAction() {}

    public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      invokedAtLeastOnce = true;
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
    }
    dialog.setVisible(true);
  }
}