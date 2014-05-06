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
import org.yawlfoundation.yawl.configuration.menu.action.*;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToggleToolBarButton;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToolBarButton;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 19/12/2013
 */
public class ConfigurationToolBar extends JToolBar implements ProcessConfigurationModelListener {

    private YAWLToggleToolBarButton previewProcessConfigurationButton;
    private YAWLToggleToolBarButton applyProcessConfigurationButton;


    public ConfigurationToolBar() {
        super("Process Configuration", JToolBar.HORIZONTAL);
        setRollover(true);
        previewProcessConfigurationButton =
                new YAWLToggleToolBarButton(PreviewConfigurationProcessAction.getInstance());
        applyProcessConfigurationButton =
                new YAWLToggleToolBarButton(ApplyProcessConfigurationAction.getInstance());
        ProcessConfigurationModel.getInstance().subscribe(this);
        addContent();
    }


    public void addContent() {
        add(buildConfigurableTaskItem());
        add(new YAWLToolBarButton(new InputPortConfigurationAction()));
        add(new YAWLToolBarButton(new OutputPortConfigurationAction()));
        add(new YAWLToolBarButton(new MultipleInstanceConfigurationAction()));
        add(new YAWLToolBarButton(new CancellationRegionConfigurationAction()));
        addSeparator();

        add(new YAWLToolBarButton(new CheckProcessCorrectness()));
        add(previewProcessConfigurationButton);
        add(applyProcessConfigurationButton);
        addSeparator();

        add(new YAWLToolBarButton(new ConfigurationSettingsAction()));
    }

    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        previewProcessConfigurationButton.setSelected(
                previewState != ProcessConfigurationModel.PreviewState.OFF);

        applyProcessConfigurationButton.setSelected(
                applyState == ProcessConfigurationModel.ApplyState.ON);
    }

    private YAWLToggleToolBarButton buildConfigurableTaskItem() {
        ConfigurableTaskAction action = new ConfigurableTaskAction();
        YAWLToggleToolBarButton configurableTaskToggle =
                new YAWLToggleToolBarButton(action);
        action.setToolBarButton(configurableTaskToggle);
        return configurableTaskToggle;
    }

}
