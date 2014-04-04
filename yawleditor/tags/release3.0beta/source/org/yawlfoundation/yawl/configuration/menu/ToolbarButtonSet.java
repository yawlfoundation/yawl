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

package org.yawlfoundation.yawl.configuration.menu;

import org.yawlfoundation.yawl.configuration.ProcessConfigurationModel;
import org.yawlfoundation.yawl.configuration.ProcessConfigurationModelListener;
import org.yawlfoundation.yawl.configuration.menu.action.ApplyProcessConfigurationAction;
import org.yawlfoundation.yawl.configuration.menu.action.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToggleToolBarButton;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 19/12/2013
 */
public class ToolbarButtonSet implements ProcessConfigurationModelListener {

    YAWLToggleToolBarButton previewProcessConfigurationButton;
    YAWLToggleToolBarButton applyProcessConfigurationButton;


    public ToolbarButtonSet() {
        previewProcessConfigurationButton =
                new YAWLToggleToolBarButton(PreviewConfigurationProcessAction.getInstance());
        applyProcessConfigurationButton =
                new YAWLToggleToolBarButton(ApplyProcessConfigurationAction.getInstance());
        ProcessConfigurationModel.getInstance().subscribe(this);
    }


    public Set<AbstractButton> getButtons() {
        Set<AbstractButton> buttons = new HashSet<AbstractButton>();
        buttons.add(previewProcessConfigurationButton);
        buttons.add(applyProcessConfigurationButton);
        return buttons;
    }

    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        previewProcessConfigurationButton.setSelected(
                previewState != ProcessConfigurationModel.PreviewState.OFF);

        applyProcessConfigurationButton.setSelected(
                applyState == ProcessConfigurationModel.ApplyState.ON);
    }

}
