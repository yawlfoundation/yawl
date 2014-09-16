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

package org.yawlfoundation.yawl.editor.ui.plugin;

import org.jgraph.graph.VertexView;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLPort;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 24/04/12
 */
public interface YEditorPlugin {

    /**
     * Gets the name of this plugin
     * @return the plugin name
     */
    String getName();

    /**
     * Gets a description of the plugin
     * @return a description of the purpose of the plugin
     */
    String getDescription();

    /**
     * Gets a menu action that will appear as a menu item on the 'Plugins'
     * menu, and that will take the desired action when the action is selected
     * @return the desired menu action
     */
    YAWLSelectedNetAction getPluginMenuAction();

    /**
     * Gets an entire menu structure (menu, item and sub-items) that will
     * appear as a menu tree on the 'Plugins' menu, and that will take the
     * desired action(s) when selected
     * @return a fully implemented JMenu containing all of the necessary menu
     * items and actions
     */
    JMenu getPluginMenu();

    /**
     * Gets a toolbar for the plugin's actions, to be displayed under the
     * main editor toolbar
     * @return a fully implemented toolbar
     */
    JToolBar getToolbar();

    /**
     * Called by the editor immediately before a specification file is saved
     */
    void performPreFileSaveTasks();

    /**
     * Called by the editor immediately after a specification file is saved
     */
    void performPostFileSaveTasks();

    /**
     * Called by the editor immediately before a specification file is opened
     */
    void performPreFileOpenTasks();

    /**
     * Called by the editor immediately after a specification file is saved
     */
    void performPostFileOpenTasks();

    /**
     * Called by the editor immediately before a task or condition is
     * rendered on the canvas. May be implemented to perform custom
     * rendering
     * @param g2 the graphics object doing the rendering
     * @param cell the cell (task or condition) being rendered
     */
    void performPreCellRenderingTasks(Graphics2D g2, VertexView cell);

    /**
     * Called by the editor immediately after a task or condition is
     * added to a net's canvas
     * @param model a reference to the net model representing the canvas
     * @param element the task or condition that has been added
     */
    void netElementAdded(NetGraphModel model, YAWLVertex element);

    /**
     * Called by the editor immediately after one or more tasks or conditions
     * are removed from a net's canvas
     * @param model a reference to the net model representing the net
     * @param cellsAndTheirEdges the set of cells (tasks, conditions and flows)
     *                           that have been removed
     */
    void netElementsRemoved(NetGraphModel model, Set<Object> cellsAndTheirEdges);

    /**
     * Called by the editor when a flow has been created between two net
     * elements (i.e. tasks and conditions)
     * @param model a reference to the net model representing the net
     * @param source the connecting port of the source element
     * @param target the connecting port of the target element
     */
    void portsConnected(NetGraphModel model, YAWLPort source, YAWLPort target);

    /**
     * Called by the editor when a specification has been closed
     * (removed from the editor)
     */
    void closeSpecification();

    /**
     * Called by the editor when a specification has been opened
     * (loaded into the editor)
     */
    void openSpecification();

    /**
     * Called by the editor when a net has been added to the current
     * specification
     * @param model a reference to the net model representing the net
     */
    void netAdded(NetGraphModel model);

    /**
     * Called by the editor when a net has been removed from the current
     * specification
     * @param model a reference to the net model representing the net
     */
    void netRemoved(NetGraphModel model);

}
