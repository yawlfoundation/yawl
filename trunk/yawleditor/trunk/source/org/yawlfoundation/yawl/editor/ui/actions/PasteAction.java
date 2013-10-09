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

import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PasteAction extends YAWLBaseAction {

    private static final PasteAction INSTANCE = new PasteAction();

    {
        putValue(Action.SHORT_DESCRIPTION, " Paste contents of clipboard ");
        putValue(Action.NAME, "Paste");
        putValue(Action.LONG_DESCRIPTION, "Paste contents of clipboard");
        putValue(Action.SMALL_ICON, getPNGIcon("page_paste"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("V"));
    }

    private PasteAction() {
        setEnabled(false);
    }

    public static PasteAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent event) {
        SpecificationUndoManager.getInstance().startCompoundingEdits();

        TransferHandler.getPasteAction().actionPerformed(
                new ActionEvent(getGraph(), event.getID(), event.getActionCommand()));

        getGraph().getNetModel().removeCells(NetUtilities.getIllegallyCopiedFlows(
                getGraph().getNetModel()).toArray());

        SpecificationUndoManager.getInstance().stopCompoundingEdits();
    }
}
