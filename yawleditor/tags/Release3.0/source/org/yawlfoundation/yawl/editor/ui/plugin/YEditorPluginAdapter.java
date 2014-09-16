/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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
import java.util.*;

/**
 * @author Michael Adams
 * @date 26/02/2014
 */
public abstract class YEditorPluginAdapter implements YEditorPlugin {

    public abstract String getName();

    public abstract String getDescription();

    public abstract YAWLSelectedNetAction getPluginMenuAction();

    public abstract JMenu getPluginMenu();


    public JToolBar getToolbar() {
        return null;
    }

    public void performPreFileSaveTasks() {

    }

    public void performPostFileSaveTasks() {

    }

    public void performPreFileOpenTasks() {

    }

    public void performPostFileOpenTasks() {

    }

    public void performPreCellRenderingTasks(Graphics2D g2, VertexView cell) {

    }

    public void netElementAdded(NetGraphModel model, YAWLVertex element) {

    }

    public void netElementsRemoved(NetGraphModel model, Set<Object> cellsAndTheirEdges) {

    }

    public void portsConnected(NetGraphModel model, YAWLPort source, YAWLPort target) {

    }

    public void closeSpecification() {

    }

    public void openSpecification() {

    }

    public void netAdded(NetGraphModel model) {

    }

    public void netRemoved(NetGraphModel model) {

    }
}
