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

import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.elements.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * @author Michael Adams
 * @date 26/07/13
 */
public class NetReloader {

    private static final int MARGIN = 50;

    private Map<String, YAWLVertex> vertices;

    public void reload(NetGraph graph) {
        Set<YFlow> flowSet = new HashSet<YFlow>();
        Set<YCondition> conditions = new HashSet<YCondition>();
        vertices = new HashMap<String, YAWLVertex>();
        YNet net = (YNet) graph.getNetModel().getDecomposition();
        addBaseConditions(net, graph);
        for (YExternalNetElement element : net.getNetElements().values()) {
            if (element instanceof YTask) {
                addTask(graph, (YTask) element);
            }
            else if (element instanceof YCondition) {
                YCondition condition = (YCondition) element;
                addCondition(graph, condition);
                conditions.add(condition);
            }
            flowSet.addAll(element.getPostsetFlows());
        }
        addFlows(graph, flowSet, conditions);
        new DefaultLayoutArranger().layoutNet(graph.getNetModel());
    }


    private void addBaseConditions(YNet net, NetGraph graph) {
        Rectangle bounds = getCanvasBounds();
        InputCondition inputCondition = new InputCondition(
                getInputConditionPoint(bounds, graph), net.getInputCondition());
        addCondition(graph, inputCondition);
        vertices.put(inputCondition.getID(), inputCondition);

        OutputCondition outputCondition = new OutputCondition(
                getOutputConditionPoint(bounds, graph), net.getOutputCondition());
        addCondition(graph, outputCondition);
        vertices.put(outputCondition.getID(), outputCondition);
    }



    private void addCondition(NetGraph graph, YAWLVertex vertex) {
        graph.addElement(vertex);
        String name = vertex.getName();
        if (name != null) graph.setElementLabel(vertex, name);
    }


    private void addCondition(NetGraph graph, YCondition yCondition) {
        if ((yCondition instanceof YInputCondition) ||
                (yCondition instanceof YOutputCondition)) return;

        Condition condition = new Condition(new Point2D.Double(), yCondition);
        graph.addElement(condition);
        String name = condition.getName();
        if (name != null) graph.setElementLabel(condition, name);
        vertices.put(condition.getID(), condition);
    }


    private void addTask(NetGraph graph, YTask yTask) {
        Point2D point = new Point2D.Double(50.0, 50.0);
        YAWLTask task = null;
        if (yTask instanceof YAtomicTask) {
            if (yTask.isMultiInstance()) {
                task = new MultipleAtomicTask(point, yTask);
            }
            else {
                task = new AtomicTask(point, yTask);
            }
        }
        else if (yTask instanceof YCompositeTask) {
            if (yTask.isMultiInstance()) {
                task = new MultipleCompositeTask(point, yTask);
            }
            else {
                task = new CompositeTask(point, yTask);
            }
        }
        if (task != null) {
            graph.addElement(task);
            String name = task.getName();
            if (name != null) graph.setElementLabel(task, name);
            vertices.put(task.getID(), task);
        }
    }


    private void addFlows(NetGraph graph, Set<YFlow> flows, Set<YCondition> conditions) {
        for (YCompoundFlow compoundFlow : rationaliseFlows(flows, conditions)) {
            YAWLFlowRelation flow = new YAWLFlowRelation(compoundFlow);
            graph.connect(flow, vertices.get(flow.getSourceID()),
                    vertices.get(flow.getTargetID()));

        }
    }


    private Set<YCompoundFlow> rationaliseFlows(Set<YFlow> flows, Set<YCondition> conditions) {
        Set<YCompoundFlow> compoundFlows = new HashSet<YCompoundFlow>();
        Set<YCondition> implicitConditions = new HashSet<YCondition>();
        for (YCondition condition : conditions) {
            if (condition.isImplicit()) {
                YFlow flowFromSource = condition.getPresetFlows().iterator().next();
                YFlow flowIntoTarget = condition.getPostsetFlows().iterator().next();
                compoundFlows.add(
                        new YCompoundFlow(flowFromSource, condition, flowIntoTarget));
                flows.remove(flowFromSource);
                flows.remove(flowIntoTarget);
                implicitConditions.add(condition);
            }
        }
        conditions.removeAll(implicitConditions);
        for (YFlow flow : flows) {
            compoundFlows.add(new YCompoundFlow(flow));
        }
        return compoundFlows;
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
