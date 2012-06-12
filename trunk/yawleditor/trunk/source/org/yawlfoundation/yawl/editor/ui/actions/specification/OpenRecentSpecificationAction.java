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

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.ArchivingThread;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class OpenRecentSpecificationAction extends YAWLSpecificationAction implements TooltipTogglingWidget {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.LONG_DESCRIPTION, "Open a recently loaded specification");
    putValue(Action.SMALL_ICON, getPNGIcon("folder_page"));
  }

  private String _fullFileName = null;

  public OpenRecentSpecificationAction() { }  

  public OpenRecentSpecificationAction(String fullFileName) {
      setFileName(fullFileName);
  }


  public void setFileName(String fullFileName) {
      _fullFileName = fullFileName ;
      putValue(Action.NAME, getShortFileName());
  }


  public void actionPerformed(ActionEvent event) {
      ArchivingThread.getInstance().open(_fullFileName) ;
  }

  public String getEnabledTooltipText() {
    return _fullFileName;
  }

  public String getDisabledTooltipText() {
    return " You must have no specification" +
           " open to in order to open another ";
  }

  private String getShortFileName() {
      File file = new File(_fullFileName);
      return file.getName();
  }
}