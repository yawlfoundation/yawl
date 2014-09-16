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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.jgraph.graph.GraphCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import java.util.*;

/**
 * @author Lindsay Bradford
 */

public class DefaultLayoutArranger {

    private static final int X_BUFFER = 32;
    private static final int Y_BUFFER = 32;

    private Set<GraphCell> breadthTraversedNodes;
    private LinkedList<LinkedList<GraphCell>> nodeBreadthsList;


    public void layoutSpecification() {
        for (NetGraphModel net : SpecificationModel.getNets()) {
            layoutNet(net);
        }
    }


    public void layoutNet(NetGraphModel netModel) {
        breadthTraversedNodes = new HashSet<GraphCell>();
        nodeBreadthsList = new LinkedList<LinkedList<GraphCell>>();
        traverseGraphToDetermineNodeBreadth(netModel);
        ensureOutputConditionIsLast(netModel);
        doGraphLayout(netModel);
    }


    private void traverseGraphToDetermineNodeBreadth(NetGraphModel netModel) {
        InputCondition inputCondition = NetUtilities.getInputCondition(netModel);
        LinkedList<GraphCell> startNodeList = new LinkedList<GraphCell>();
        startNodeList.add(inputCondition);
        nodeBreadthsList.add(startNodeList);
        breadthTraversedNodes.add(inputCondition);
        int largestNumberOfNodesAtSingleBreadth = 1;

        while (getUntraversedNodesFrom(nodeBreadthsList.getLast()).size() > 0) {
            nodeBreadthsList.add(new LinkedList<GraphCell>(
                    getUntraversedNodesFrom(nodeBreadthsList.getLast())));
            if (nodeBreadthsList.getLast().size() > largestNumberOfNodesAtSingleBreadth) {
                largestNumberOfNodesAtSingleBreadth = nodeBreadthsList.getLast().size();
            }
            breadthTraversedNodes.addAll(nodeBreadthsList.getLast());
        }
    }


    private Set<GraphCell> getUntraversedNodesFrom(LinkedList<GraphCell> nodesAtCurrentDepth) {
        Set<GraphCell> nodeSet = new HashSet<GraphCell>();
        for (GraphCell cell : nodesAtCurrentDepth) {
            for (YAWLFlowRelation flow : NetUtilities.getOutgoingFlowsFrom((YAWLCell) cell)) {
                GraphCell targetCell = NetCellUtilities.getVertexFromCell(flow.getTargetVertex());
                if (!breadthTraversedNodes.contains(targetCell)) {
                    nodeSet.add(targetCell);
                }
            }
        }
        return nodeSet;
    }


    private void ensureOutputConditionIsLast(NetGraphModel netModel) {
        OutputCondition outputCondition = NetUtilities.getOutputCondition(netModel);
        if (!nodeBreadthsList.getLast().contains(outputCondition)) {

            // It's somewhere earlier in the list. Locate, and remove from there.
            LinkedList<GraphCell> depthToRemove = null;
            for (LinkedList<GraphCell> nodes : nodeBreadthsList) {
                if (nodes.contains(outputCondition)) {
                    nodes.remove(outputCondition);
                    if (nodes.isEmpty()) {
                        depthToRemove = nodes;
                    }
                }
            }
            if (depthToRemove != null) {
                nodeBreadthsList.remove(depthToRemove);
            }

            // Put output condition last.
            LinkedList<GraphCell> endNodeList = new LinkedList<GraphCell>();
            endNodeList.add(outputCondition);
            nodeBreadthsList.add(endNodeList);
        }
    }


    private void doGraphLayout(NetGraphModel netModel) {
        int nodeX = X_BUFFER;
        for (LinkedList<GraphCell> nodesAtCurrentDepth : nodeBreadthsList) {
            double widestX = 0;
            int nodeY = Y_BUFFER;
            for (GraphCell cell : nodesAtCurrentDepth) {
                if (cell instanceof YAWLVertex && ((YAWLVertex) cell).getParent() != null) {
                    cell = (GraphCell) ((YAWLVertex) cell).getParent();
                }
                netModel.getGraph().moveElementTo(cell, nodeX, nodeY);
                if (netModel.getGraph().getCellBounds(cell).getWidth() > widestX) {
                    widestX = netModel.getGraph().getCellBounds(cell).getWidth();
                }
                nodeY = nodeY + (int) netModel.getGraph().getCellBounds(cell).getHeight()
                        + Y_BUFFER;

            }
            NetCellUtilities.alignCellsAlongVerticalCentre(netModel.getGraph(),
                    nodesAtCurrentDepth.toArray());
            nodeX = nodeX + (int) widestX + +X_BUFFER;
        }
    }
}