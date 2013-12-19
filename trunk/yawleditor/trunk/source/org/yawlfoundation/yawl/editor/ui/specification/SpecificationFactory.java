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

package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.elements.YNet;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author Michael Adams
 * @date 26/07/13
 */
public class SpecificationFactory {

    private static final int MARGIN = 50;


    public void newSpecification() {
        NetGraph graph = SpecificationModel.newSpecification();
        YNet net = (YNet) graph.getNetModel().getDecomposition();
        populateGraph(net, graph);
        YPluginHandler.getInstance().netAdded(graph.getNetModel());
        YAWLEditor.getNetsPane().openNet(graph);
    }


    public void populateGraph(YNet net, NetGraph graph) {
        Rectangle bounds = getCanvasBounds();
        InputCondition inputCondition = new InputCondition(
                getInputConditionPoint(bounds, graph), net.getInputCondition());
        addCondition(graph, inputCondition);

        OutputCondition outputCondition = new OutputCondition(
                getOutputConditionPoint(bounds, graph), net.getOutputCondition());
        addCondition(graph, outputCondition);
    }


    private void addCondition(NetGraph graph, YAWLVertex vertex) {
        graph.addElement(vertex);
        String name = vertex.getName();
        if (name != null) graph.setElementLabel(vertex, name);
    }


    private Point2D getInputConditionPoint(Rectangle bounds, NetGraph graph) {
        Dimension size = InputCondition.getVertexSize();
        return graph.snap(new Point((MARGIN)  - (size.width/2),
                (int) (bounds.getHeight()/2) - (size.height/2)));
    }

    private Point2D getOutputConditionPoint(Rectangle bounds, NetGraph graph) {
        Dimension size = InputCondition.getVertexSize();
        return graph.snap(new Point((int) (bounds.getWidth()-MARGIN) - (size.width/2),
                (int) (bounds.getHeight()/2)  - (size.height/2)));
    }


    private Rectangle getCanvasBounds() {
        return cropRectangle(YAWLEditor.getNetsPane().getBounds(), 15);

    }

    private Rectangle cropRectangle(Rectangle r, int crop) {
         return new Rectangle(r.x, r.y, r.width - crop, r.height - crop);
    }

}
