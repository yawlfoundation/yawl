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

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.SpecificationUploadDialog;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 19/09/13
 */
public class UploadSpecificationAction extends YAWLOpenSpecificationAction
        implements TooltipTogglingWidget {

    {
      putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
      putValue(Action.NAME, "Upload");
      putValue(Action.LONG_DESCRIPTION, "Upload this specification.");
      putValue(Action.SMALL_ICON, getMenuIcon("upload-server-icon"));
      putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_U));
      putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("shift U"));
    }

    public void actionPerformed(ActionEvent event) {
        new SpecificationUploadDialog(YAWLEditor.getInstance()).setVisible(true);
    }

    public String getEnabledTooltipText() {
      return " Upload this specification to YAWL Engine ";
    }

    public String getDisabledTooltipText() {
      return " You must have an open specification to upload it ";
    }

}
