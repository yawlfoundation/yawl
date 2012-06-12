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

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.ArchivingThread;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

public class ExportToEngineFormatAction extends YAWLOpenSpecificationAction implements TooltipTogglingWidget {

  protected static final Preferences prefs =  Preferences.userNodeForPackage(YAWLEditor.class);
  public static final String SHOW_EXPORT_DIALOG_PREFERENCE = "showExportDialog";
  boolean shouldShowExportDialog = true;
  private boolean isDialogShownPreviously = false;
 
  ExportConfigDialog dialog = new ExportConfigDialog();
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Export to YAWL Engine file...");
    putValue(Action.LONG_DESCRIPTION, "Export this specification to the YAWL engine file format. ");
    putValue(Action.SMALL_ICON, getPNGIcon("page_go"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_X));
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
    return " Export this specification to the YAWL engine file format ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an open specification" + 
           " to export it to engine format ";
  }
}


