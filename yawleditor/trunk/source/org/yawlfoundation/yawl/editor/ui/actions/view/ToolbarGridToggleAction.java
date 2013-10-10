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
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ToolbarGridToggleAction extends YAWLBaseAction {

    private boolean selected;

    {
        putValue(Action.SHORT_DESCRIPTION, " Toggle grid on net canvas ");
        putValue(Action.NAME, "Show Grid");
        putValue(Action.SMALL_ICON, getPNGIcon("grid"));
        putValue(Action.LONG_DESCRIPTION, "Toggle grid on net canvas");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
    }

    public ToolbarGridToggleAction() {
        selected = UserSettings.getShowGrid();
    }

    public void actionPerformed(ActionEvent event) {
        selected = !selected;
        UserSettings.setShowGrid(selected);
        for (NetGraphModel net : SpecificationModel.getInstance().getNets()) {
            net.getGraph().setGridVisible(selected);
        }
    }

    public boolean isSelected() {
        return selected;
    }
}
