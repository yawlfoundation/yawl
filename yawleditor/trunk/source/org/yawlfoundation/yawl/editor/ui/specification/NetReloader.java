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
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifier;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.editor.core.layout.YNetLayout;
import org.yawlfoundation.yawl.editor.core.repository.YRepository;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.io.LayoutImporter;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.util.XNode;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Michael Adams
 * @date 26/07/13
 */
public class NetReloader {

    private static final int MARGIN = 50;

    private Map<String, YAWLVertex> _vertices;
    private Set<YAWLFlowRelation> _flows;


    public void reload(String topID, Map<String, YDecomposition> netAndTaskDecompositions)
            throws YControlFlowHandlerException, YDataHandlerException, YSyntaxException,
                   YLayoutParseException {
        Set<YTask> tasks = getTasks(netAndTaskDecompositions.values());

        // have to load the task decompositions first
        for (YDecomposition decomposition : netAndTaskDecompositions.values()) {
            if (decomposition instanceof YAWLServiceGateway) {
                loadTaskDecomposition((YAWLServiceGateway) decomposition, tasks);
            }
        }

        // now nets
        int netCount = 0;
        for (String repoID : netAndTaskDecompositions.keySet()) {
            YDecomposition decomposition = netAndTaskDecompositions.get(repoID);
            if (! (decomposition instanceof YNet)) continue;

            YNet net = (YNet) decomposition;
            String oldID = net.getID();
            String idAdded = getHandler().addNet(net);
            if (! idAdded.equals(oldID)) {
                getDataHandler().updateDecompositionReferences(oldID, idAdded);
            }
            NetGraph graph = new NetGraph(net);
            SpecificationModel.getNets().add(graph.getNetModel());
            YAWLEditor.getNetsPane().openNet(graph);
            reload(graph);
            setNetLayout(net, graph, repoID);
            checkElementIDs(net);
            netCount++;
        }
        if (netCount > 1) {
            String firstNet = netAndTaskDecompositions.get(topID).getID();
            YAWLEditor.getNetsPane().setSelectedTab(firstNet);
        }
    }


    private YControlFlowHandler getHandler() {
        return SpecificationModel.getHandler().getControlFlowHandler();
    }


    private YDataHandler getDataHandler() {
        return SpecificationModel.getHandler().getDataHandler();
    }


    private Set<YTask> getTasks(Collection<YDecomposition> reloadedDecompositions) {
        Set<YTask> taskSet = new HashSet<YTask>();
        for (YDecomposition decomposition : reloadedDecompositions) {
             if (decomposition instanceof YNet) {
                 taskSet.addAll(((YNet) decomposition).getNetTasks());
             }
        }
        return taskSet;
    }


    private void loadTaskDecomposition(YAWLServiceGateway gateway, Set<YTask> tasks)
            throws YControlFlowHandlerException, YDataHandlerException {
        String oldID = gateway.getID();
        YAWLServiceGateway existing = getHandler().getTaskDecomposition(oldID);
        if (equivalentDecompositions(existing, gateway)) {
            return;                            // gateway already in spec, no more to do
        }
        String newID = getHandler().addTaskDecomposition(gateway);
        if (! newID.equals(oldID)) {
            getDataHandler().updateTaskDecompositionReferences(tasks, oldID, newID);
        }
    }


    private void setNetLayout(YNet net, NetGraph graph, String storedNetID)
            throws YSyntaxException, YLayoutParseException {
        YNetLayout layout = null;
        XNode layoutNode = YRepository.getInstance().getNetRepository().getLayout(storedNetID);
        if (layoutNode != null) {
            layout = new YNetLayout(net, NumberFormat.getInstance(Locale.getDefault()));
            layout.parse(layoutNode.getChild());
        }
        if (layout != null) {
            LayoutImporter.setNetLayout(graph.getNetModel(), layout);
            graph.getGraphLayoutCache().reload();
        }
        else {
            new DefaultLayoutArranger().layoutNet(graph.getNetModel());
        }
    }


    private void reload(NetGraph graph) {
        Set<YFlow> flowSet = new HashSet<YFlow>();
        Set<YCondition> implicitConditions = new HashSet<YCondition>();
        _vertices = new HashMap<String, YAWLVertex>();
        YNet net = (YNet) graph.getNetModel().getDecomposition();
        addBaseConditions(net, graph);
        for (YExternalNetElement element : net.getNetElements().values()) {
            if (element instanceof YTask) {
                addTask(graph, (YTask) element);
            }
            else if (element instanceof YCondition) {
                YCondition condition = (YCondition) element;
                addCondition(graph, condition);
                if (condition.isImplicit()) {
                    implicitConditions.add(condition);
                }
                else {
                    addCondition(graph, condition);                     // add to graph
                }
            }
            flowSet.addAll(element.getPostsetFlows());
        }
        addFlows(graph, flowSet, implicitConditions);
        DecoratorUtil.removeUnnecessaryDecorators(graph.getNetModel());
    }


    private void addBaseConditions(YNet net, NetGraph graph) {
        Rectangle bounds = getCanvasBounds();
        InputCondition inputCondition = new InputCondition(
                getInputConditionPoint(bounds, graph), net.getInputCondition());
        addCondition(graph, inputCondition);
        _vertices.put(inputCondition.getID(), inputCondition);

        OutputCondition outputCondition = new OutputCondition(
                getOutputConditionPoint(bounds, graph), net.getOutputCondition());
        addCondition(graph, outputCondition);
        _vertices.put(outputCondition.getID(), outputCondition);
    }


    private void addCondition(NetGraph graph, YAWLVertex vertex) {
        graph.addElement(vertex);
        String name = vertex.getName();
        if (name != null) graph.setElementLabel(vertex, name);
    }


    private boolean addCondition(NetGraph graph, YCondition yCondition) {
        if (yCondition instanceof YInputCondition ||
                yCondition instanceof YOutputCondition ||
                yCondition.isImplicit()) return false;

        Condition condition = new Condition(new Point2D.Double(), yCondition);
        graph.addElement(condition);
        String name = condition.getName();
        if (name != null) graph.setElementLabel(condition, name);
        _vertices.put(condition.getID(), condition);
        return true;
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
            String name = yTask.getName();
            if (! (name == null || name.equals("element"))) {
                graph.setElementLabel(task, name);
            }
            DecoratorUtil.setTaskDecorators(yTask, task, graph.getNetModel());
            _vertices.put(task.getID(), task);
        }
    }


    private void addFlows(NetGraph graph, Set<YFlow> yFlows, Set<YCondition> conditions) {
        _flows = new HashSet<YAWLFlowRelation>();
        for (YCompoundFlow compoundFlow : NetUtilities.rationaliseFlows(yFlows, conditions)) {
            YAWLFlowRelation flow = new YAWLFlowRelation(compoundFlow);
            graph.connect(flow, _vertices.get(flow.getSourceID()),
                    _vertices.get(flow.getTargetID()));
            _flows.add(flow);
        }
    }


    private Point2D getInputConditionPoint(Rectangle bounds, NetGraph graph) {
        Dimension size = InputCondition.getVertexSize();
        return graph.snap(new Point((MARGIN) - (size.width/2),
                (int) (bounds.getHeight()/2) - (size.height/2)));
    }

    private Point2D getOutputConditionPoint(Rectangle bounds, NetGraph graph) {
        Dimension size = InputCondition.getVertexSize();
        return graph.snap(new Point((int) (bounds.getWidth()-MARGIN) - (size.width/2),
                (int) (bounds.getHeight()/2) - (size.height/2)));
    }


    private Rectangle getCanvasBounds() {
        return cropRectangle(YAWLEditor.getNetsPane().getBounds(), 15);

    }

    private Rectangle cropRectangle(Rectangle r, int crop) {
        return new Rectangle(r.x, r.y, r.width - crop, r.height - crop);
    }


    private void checkElementIDs(YNet net) {
        for (YExternalNetElement element : net.getNetElements().values()) {
            if ((element instanceof YCondition) && ((YCondition) element).isImplicit()) {
                continue;
            }
            ElementIdentifier eId = new ElementIdentifier(element.getID());
            String newID = getHandler().checkID(eId.getName());
            if (! newID.equals(eId.toString())) {
                element.setID(newID);
            }
        }
        checkImplicitConditionIDs();
    }


    private void checkImplicitConditionIDs() {
        for (YAWLFlowRelation flow : _flows) {
            YCompoundFlow compoundFlow = flow.getYFlow();
            if (compoundFlow.isCompound()) {
                compoundFlow.rationaliseImplicitConditionID();
            }
        }
    }


    private boolean equivalentDecompositions(YAWLServiceGateway d1, YAWLServiceGateway d2) {
        return !(d1 == null || d2 == null) &&
                d1.getYawlService() == d2.getYawlService() &&
                equivalentAttributes(d1.getAttributes(), d2.getAttributes()) &&
                nullOrEquals(d1.getCodelet(), d2.getCodelet()) &&
                nullOrEquals(d1.getDocumentation(), d2.getDocumentation()) &&
                nullOrEquals(d1.getLogPredicate(), d2.getLogPredicate()) &&
                equivalentParameters(d1.getInputParameters(), d2.getInputParameters()) &&
                equivalentParameters(d1.getOutputParameters(), d2.getOutputParameters());
    }


    private boolean equivalentAttributes(YAttributeMap a1, YAttributeMap a2) {
        if (a1.size() != a2.size()) return false;
        for (String key : a1.keySet()) {
            if (! a2.keySet().contains(key)) return false;
        }
        return true;
    }


    private boolean equivalentParameters(Map<String, YParameter> m1, Map<String, YParameter> m2) {
        if (m1.size() != m2.size()) return false;
        for (String key : m1.keySet()) {
            if (! equivalentParameter(m1.get(key), m2.get(key))) return false;
        }
        return true;
    }


    private boolean equivalentParameter(YParameter p1, YParameter p2) {
        return !(p1 == null || p2 == null) && p1.toXML().equals(p2.toXML());
    }


    private boolean nullOrEquals(Object o1, Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
    }
}
