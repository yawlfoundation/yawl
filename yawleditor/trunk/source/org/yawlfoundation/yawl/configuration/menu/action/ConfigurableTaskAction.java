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

package org.yawlfoundation.yawl.configuration.menu.action;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.configuration.element.TaskConfiguration;
import org.yawlfoundation.yawl.configuration.element.TaskConfigurationCache;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToggleToolBarButton;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ConfigurableTaskAction extends ProcessConfigurationAction
        implements TooltipTogglingWidget {

    private boolean selected;
    private JCheckBoxMenuItem checkBox = null;
    private YAWLToggleToolBarButton toolBarButton = null;

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Set Task Configurable");
        putValue(Action.LONG_DESCRIPTION, "Set the task to be configurable");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
        putValue(Action.SMALL_ICON, getMenuIcon("sitemap_color"));

    }

    public ConfigurableTaskAction() {
        super();
        this.selected = false;
    }

    public ConfigurableTaskAction(YAWLTask task, NetGraph net) {
        this();
        this.net = net;
        this.task = task;
    }


    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        if (getCheckBox() == null && toolBarButton == null) return;
        super.graphSelectionChange(state, event);
        TaskConfiguration configuration = (net != null) ?
                TaskConfigurationCache.getInstance().get(net.getNetModel(),task) : null;
        boolean newState = task != null && configuration != null &&
                        configuration.isConfigurable();
        if (getCheckBox() != null) getCheckBox().setState(newState);
        if (toolBarButton != null) toolBarButton.setSelected(newState);
    }


    public void actionPerformed(ActionEvent event) {
        TaskConfiguration configuration = (net != null) ?
                TaskConfigurationCache.getInstance().getOrAdd(net.getNetModel(),task) :
                null;
        if (configuration != null) {
            configuration.setConfigurable(! configuration.isConfigurable());
            net.changeLineWidth(task);
        }
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setCheckBox(JCheckBoxMenuItem checkBox) {
        this.checkBox = checkBox;
    }

    public void setToolBarButton(YAWLToggleToolBarButton button) {
        toolBarButton = button;
    }

    public JCheckBoxMenuItem getCheckBox(){
        return this.checkBox;
    }

    public String getDisabledTooltipText() {
        return "Configure this task";
    }


    public String getEnabledTooltipText() {
        return "Set the task to be configurable";
    }

}
