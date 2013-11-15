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

package org.yawlfoundation.yawl.editor.ui.actions.datatypedialog;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CutDataTypeDialogAction extends YAWLBaseAction {

    {
        putValue(Action.SHORT_DESCRIPTION, " Cut the selected text to the Clipboard");
        putValue(Action.NAME, "Cut");
        putValue(Action.LONG_DESCRIPTION, "Cut the selected text");
        putValue(Action.SMALL_ICON, getPNGIcon("cut"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_U));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("X"));
    }

    public CutDataTypeDialogAction() { }

    public void actionPerformed(ActionEvent event) {
        DataTypeDialogToolBarMenu.getEditorPane().getEditor().cut();
        DataTypeDialogToolBarMenu.getInstance().getButton("paste").setEnabled(true);
    }
}