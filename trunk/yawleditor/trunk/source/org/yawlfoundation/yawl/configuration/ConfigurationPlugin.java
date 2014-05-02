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

package org.yawlfoundation.yawl.configuration;

import org.jgraph.graph.VertexView;
import org.yawlfoundation.yawl.configuration.element.TaskConfiguration;
import org.yawlfoundation.yawl.configuration.element.TaskConfigurationCache;
import org.yawlfoundation.yawl.configuration.menu.ProcessConfigurationMenu;
import org.yawlfoundation.yawl.configuration.menu.ToolbarButtonSet;
import org.yawlfoundation.yawl.configuration.menu.action.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.configuration.net.NetConfiguration;
import org.yawlfoundation.yawl.configuration.net.NetConfigurationCache;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.plugin.YEditorPlugin;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YTask;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 18/12/2013
 */
public class ConfigurationPlugin implements YEditorPlugin {

    private ProcessConfigurationModel.PreviewState previewState;

    private static final int CONFIGURED_TASK_STOKE_WIDTH = 3;



    public String getName() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public YAWLSelectedNetAction getPluginMenuAction() {
        return null;
    }

    public JMenu getPluginMenu() {
        return new ProcessConfigurationMenu();
    }

    public Set<AbstractButton> getToolbarButtons() {
        return new ToolbarButtonSet().getButtons();
    }

    public void performPreFileSaveTasks() {
        configureTasksOnSave();

        // if the net has configuration preview on, turn it off temporarily
        previewState = ProcessConfigurationModel.getInstance().getPreviewState();
        if (previewState != ProcessConfigurationModel.PreviewState.OFF) {
            PreviewConfigurationProcessAction.getInstance().actionPerformed(null);
        }
    }


    public void performPostFileSaveTasks() {

         // put preview state back if necessary
        if (previewState != ProcessConfigurationModel.PreviewState.OFF) {
            PreviewConfigurationProcessAction.getInstance().actionPerformed(null);
        }
    }


    public void performPreFileOpenTasks() {

    }


    public void performPostFileOpenTasks() {
        configureTasksOnLoad();
        ConfigurationImporter.ApplyConfiguration();
    }


    public void performPreCellRenderingTasks(Graphics2D g2, VertexView view) {
        YAWLTask task = null;
        if (view.getCell() instanceof YAWLTask) {
            task = (YAWLTask) view.getCell();
        }
        else if (view.getCell() instanceof Decorator) {
            task = ((Decorator) view.getCell()).getTask();
        }
        if (task != null) {
            TaskConfiguration configuration = getConfiguration(task);
            if (configuration != null && configuration.isConfigurable()) {
                g2.setStroke(new BasicStroke(CONFIGURED_TASK_STOKE_WIDTH));
            }
        }
    }

    public void netElementAdded(NetGraphModel model, YAWLVertex element) {
        if (element instanceof YAWLTask) {
            NetConfiguration netConfiguration = getNetConfiguration(model);
            if (netConfiguration.getSettings().isNewElementsConfigurable()) {
     	        YAWLTask task = (YAWLTask) element;
                TaskConfiguration configuration =
                    TaskConfigurationCache.getInstance().add(model, task);
                if (configuration != null) {
                    configuration.setConfigurable(! configuration.isConfigurable());
                }
            }
        }
    }


    public void netElementsRemoved(NetGraphModel model, Set<Object> cells) {
        for (Object cell : cells) {
            if (cell instanceof YAWLFlowRelation) {
                YAWLFlowRelation flow = (YAWLFlowRelation) cell;
                YAWLTask source = flow.getSourceTask();
                YAWLTask target = flow.getTargetTask();
                flow.detach();
                if (source != null) {
                    TaskConfiguration config = getConfiguration(source);
                    if (config != null && config.isConfigurable()) {
                        config.configureReset();
                    }
                }
                if (target != null) {
                    TaskConfiguration config = getConfiguration(target);
                    if (config != null && config.isConfigurable()) {
                        config.configureReset();
                    }
                }
            }
        }
    }

    public void portsConnected(NetGraphModel model, YAWLPort source, YAWLPort target) {
        updateCPort(model, source);
        updateCPort(model, target);
    }


    public void closeSpecification() {
        ProcessConfigurationModel.getInstance().reset();
    }

    public void openSpecification() {

    }

    public void netAdded(NetGraphModel model) {
        NetConfigurationCache.getInstance().add(model);
    }

    public void netRemoved(NetGraphModel model) {
        NetConfigurationCache.getInstance().remove(model);
    }

    private void configureTasksOnSave() {
        TaskConfigurationCache cache = TaskConfigurationCache.getInstance();
        for (NetGraphModel netModel : SpecificationModel.getNets()) {
            for (YAWLTask task : NetUtilities.getAllTasks(netModel)) {
                TaskConfiguration configuration = cache.get(netModel, task);
                if (configuration != null && configuration.isConfigurable()) {
                    YTask yTask = (YTask) task.getYAWLElement();
                    yTask.setConfiguration(
                            new ConfigurationExporter().getTaskConfiguration(configuration));
                    yTask.setDefaultConfiguration(
                            new DefaultConfigurationExporter()
                                    .getTaskDefaultConfiguration(configuration));
                }
            }
        }
    }


    private void configureTasksOnLoad() {
        TaskConfigurationCache cache = TaskConfigurationCache.getInstance();
        for (NetGraphModel netModel : SpecificationModel.getNets()) {
            getNetConfiguration(netModel);
            for (YAWLTask task : NetUtilities.getAllTasks(netModel)) {
                TaskConfiguration configuration = cache.add(netModel, task);
                YTask yTask = task.getTask();
                if (yTask.getConfigurationElement() != null) {
                    ConfigurationImporter.CTaskList.add(configuration);
                    ConfigurationImporter.map.put(configuration,
                        yTask.getConfigurationElement());
                }
            }
        }
    }


    private TaskConfiguration getConfiguration(YAWLTask searchTask) {
        for (NetGraphModel netModel : SpecificationModel.getNets()) {
            for (YAWLTask task : NetUtilities.getAllTasks(netModel)) {
                if (task.equals(searchTask)) {
                    return TaskConfigurationCache.getInstance().get(netModel, task);
                }
            }
        }
        return null;
    }


    private NetConfiguration getNetConfiguration(NetGraphModel graphModel) {
        return NetConfigurationCache.getInstance().getOrAdd(graphModel);
    }

    private void updateCPort(NetGraphModel model, YAWLPort port) {
        TaskConfiguration config = TaskConfigurationCache.getInstance().get(model, port.getTask());
        if (config != null && config.isConfigurable()) {
            config.configureReset();
        }
    }


}
