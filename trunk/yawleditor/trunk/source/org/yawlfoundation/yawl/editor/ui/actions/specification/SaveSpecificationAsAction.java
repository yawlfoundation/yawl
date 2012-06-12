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
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SaveSpecificationAsAction extends YAWLOpenSpecificationAction
        implements TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Save Specification As...");
    putValue(Action.LONG_DESCRIPTION, "Save this specification to a different filename ");
    putValue(Action.SMALL_ICON, getPNGIcon("disk_multiple"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift S"));
  }
  
  public void actionPerformed(ActionEvent event) {
      String fileName = SpecificationModel.getInstance().getFileName();
      if (StringUtil.isNullOrEmpty(fileName)) {                   // never been saved
          ExportConfigDialog dialog = new ExportConfigDialog();
          dialog.setLocationRelativeTo(YAWLEditor.getInstance());
          dialog.showOrHideSpecIDField();
          dialog.setVisible(true);
      }
      else ArchivingThread.getInstance().saveAs();
  }
  
  public String getEnabledTooltipText() {
    return " Save this specification to a different filename";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an open specification" + 
           " to save it to a different filename ";
  }
}
