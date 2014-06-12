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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 24/10/2013
 */
public class GotoSubNetAction extends YAWLSelectedNetAction
        implements TooltipTogglingWidget {

    private final YAWLCompositeTask _task;

    {
       putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
       putValue(Action.NAME, "Go to sub-net");
       putValue(Action.LONG_DESCRIPTION, "Show the sub-net that this task unfolds to");
       putValue(Action.SMALL_ICON, getMenuIcon("shape_ungroup"));
       putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_G));
    }


    public GotoSubNetAction(YAWLCompositeTask task) {
        _task = task;
    }

    public void actionPerformed(ActionEvent event) {
        String netName = _task.getUnfoldingNetName();
        if (! netName.equals("")) {
            YAWLEditor.getNetsPane().setSelectedTab(netName);
        }
    }


    public String getEnabledTooltipText() {
      return " Show the sub-net that this task unfolds to ";
    }

    public String getDisabledTooltipText() {
      return " You must have a composite task selected to go to the net it unfolds to ";
    }

}
