/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenSpecificationAction extends YAWLSpecificationAction implements TooltipTogglingWidget {

  {
    putValue(Action.SHORT_DESCRIPTION,getDisabledTooltipText());
    putValue(Action.NAME, "Open...");
    putValue(Action.LONG_DESCRIPTION, "Open an existing specification");
    putValue(Action.SMALL_ICON, getPNGIcon("folder_page"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_O));
    putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("O"));
  }

  public void actionPerformed(ActionEvent event) {
    FileOperations.open();
  }
  
  public String getEnabledTooltipText() {
    return " Open an existing specification ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have no specification open in order to open another ";
  }
}
