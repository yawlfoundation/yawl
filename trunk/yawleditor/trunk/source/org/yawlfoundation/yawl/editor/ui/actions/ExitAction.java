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

package org.yawlfoundation.yawl.editor.ui.actions;

import org.yawlfoundation.yawl.editor.ui.actions.specification.YAWLOpenSpecificationAction;
import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends YAWLOpenSpecificationAction implements TooltipTogglingWidget {

    {
        putValue(Action.SHORT_DESCRIPTION, getEnabledTooltipText());
        putValue(Action.NAME, "Exit");
        putValue(Action.LONG_DESCRIPTION, "Exit the application.");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_X));
    }

    public ExitAction(JComponent menu) {}

    public void actionPerformed(ActionEvent event) {
        FileOperations.exit();
    }

    public void specificationFileStateChange(FileState state) {
        setEnabled(state != FileState.Busy);
    }

    public String getEnabledTooltipText() {
        return " Exit the application ";
    }

    public String getDisabledTooltipText() {
        return " You cannot exit the application until the current file operation completes ";
    }
}
