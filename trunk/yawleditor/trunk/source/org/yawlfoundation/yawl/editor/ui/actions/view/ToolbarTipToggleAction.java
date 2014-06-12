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

package org.yawlfoundation.yawl.editor.ui.actions.view;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ToolbarTipToggleAction extends YAWLBaseAction {

    private boolean selected;

    {
        putValue(Action.SHORT_DESCRIPTION, "Show Tooltips");
        putValue(Action.NAME, "Show Tooltips");
        putValue(Action.SMALL_ICON, getMenuIcon("balloon"));
        putValue(Action.LONG_DESCRIPTION, "Show Tooltips");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    }

    public ToolbarTipToggleAction() {
        selected = UserSettings.getShowToolTips();
        ToolTipManager.sharedInstance().setEnabled(selected);
    }

    public void actionPerformed(ActionEvent event) {
        selected = !selected;
        ToolTipManager.sharedInstance().setEnabled(selected);
        UserSettings.setShowToolTips(selected);
    }

    public boolean isSelected() {
        return selected;
    }
}
