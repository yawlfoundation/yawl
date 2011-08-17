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

package org.yawlfoundation.yawl.editor.actions.specification;

import org.yawlfoundation.yawl.editor.specification.ArchivingThread;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.editor.YAWLEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

public class SaveSpecificationAction extends YAWLOpenSpecificationAction implements TooltipTogglingWidget {
  /**
   * 
   */
  protected static final Preferences prefs =  Preferences.userNodeForPackage(YAWLEditor.class);
  public static final String SHOW_EXPORT_DIALOG_PREFERENCE = "showExportDialog";
  boolean shouldShowExportDialog = true;
  private boolean isDialogShownPreviously = false;

  ExportConfigDialog dialog = new ExportConfigDialog();
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Save Specification");
    putValue(Action.LONG_DESCRIPTION, "Save this specification");
    putValue(Action.SMALL_ICON, getPNGIcon("disk"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("S"));
  }
  
  public void actionPerformed(ActionEvent event) {
//    shouldShowExportDialog = prefs.getBoolean(SHOW_EXPORT_DIALOG_PREFERENCE, true);
    if (shouldShowExportDialog) {
      if (!isDialogShownPreviously) {
        dialog.setLocationRelativeTo(YAWLEditor.getInstance());
        isDialogShownPreviously = true;
      }
      dialog.showOrHideSpecIDField();
      dialog.setVisible(true);
    } else {
      ArchivingThread.getInstance().engineFileExport(
          SpecificationModel.getInstance()
      );
    }
  }
  
  public String getEnabledTooltipText() {
    return " Save this specification ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an open specification" + 
           " to save it ";
  }
}