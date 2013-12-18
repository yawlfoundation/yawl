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

import org.yawlfoundation.yawl.configuration.menu.action.*;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModel;
import org.yawlfoundation.yawl.editor.ui.specification.ProcessConfigurationModelListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLMenuItem;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLPopupMenuItem;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;


public class ProcessConfigurationMenu extends JMenu
        implements ProcessConfigurationModelListener, FileStateListener {

    private JMenu _netMenu;
    private JMenu _taskMenu;

    public ProcessConfigurationMenu() {
        super("Process Configuration");
        add(getNetMenu());
        add(getTaskMenu());
        add(new YAWLMenuItem(new ConfigurationSettingsAction()));

        Publisher.getInstance().subscribe(this);
        ProcessConfigurationModel.getInstance().subscribe(this);
        setIcon(ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/wrench.png"));
    }

    public void specificationFileStateChange(FileState state) {
        setEnabled(state == FileState.Open);
    }

    private JMenu getNetMenu() {
        _netMenu = new JMenu("Net");
        _netMenu.setMnemonic('N');
        _netMenu.add(new YAWLPopupMenuCheckBoxItem(
                PreviewConfigurationProcessAction.getInstance()));
        _netMenu.add(new YAWLPopupMenuCheckBoxItem(
                ApplyProcessConfigurationAction.getInstance()));
        _netMenu.add(new YAWLMenuItem(new CheckProcessCorrectness()));

        return _netMenu;
    }


    private JMenu getTaskMenu() {
        _taskMenu = new JMenu("Task");
        _taskMenu.setMnemonic('T');
        _taskMenu.add(buildConfigurableTaskItem());
        _taskMenu.add(new YAWLPopupMenuItem(new InputPortConfigurationAction()));
        _taskMenu.add(new YAWLPopupMenuItem(new OutputPortConfigurationAction()));
        _taskMenu.add(new YAWLPopupMenuItem(new MultipleInstanceConfigurationAction()));
        _taskMenu.add(new YAWLPopupMenuItem(new CancellationRegionConfigurationAction()));
        return _taskMenu;
    }


    private YAWLPopupMenuCheckBoxItem buildConfigurableTaskItem() {
        ConfigurableTaskAction action = new ConfigurableTaskAction();
        YAWLPopupMenuCheckBoxItem configurableTaskItem =
                new YAWLPopupMenuCheckBoxItem(action);
        action.setCheckBox(configurableTaskItem);
        return configurableTaskItem;
    }



    public void processConfigurationModelStateChanged(
            ProcessConfigurationModel.PreviewState previewState,
            ProcessConfigurationModel.ApplyState applyState) {

        YAWLPopupMenuCheckBoxItem previewMenu =
                (YAWLPopupMenuCheckBoxItem) _netMenu.getItem(0);
        previewMenu.setSelected(previewState != ProcessConfigurationModel.PreviewState.OFF);

        YAWLPopupMenuCheckBoxItem applyMenu =
                (YAWLPopupMenuCheckBoxItem) _netMenu.getItem(1);
        applyMenu.setSelected(applyState == ProcessConfigurationModel.ApplyState.ON);
    }
}
