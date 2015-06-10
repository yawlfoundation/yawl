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

package org.yawlfoundation.yawl.editor.ui.net.utilities;

import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.PrettyOutputStateManager;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YFlow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A library of standard utilities that return information on, or manipulate 
 * selected nets. 
 *
 * @author Lindsay Bradford
 */

public final class NetUtilities {

    private static final ImageIcon rootNetIcon = ResourceLoader.getMenuIcon("rootnet");

    private static final ImageIcon subNetIcon = ResourceLoader.getMenuIcon("subnet");


    /**
     * Returns all tasks in the selected net. Specifically,
     * all those vertices in the net that conform to the <code>YAWLTask</code> interface.
     * @param net The net to search within.
     * @return The set of <code>YAWLTask</code> objects within the selected net.
     * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask
     */

    public static Set<YAWLTask> getAllTasks(NetGraphModel net) {
        Set<YAWLTask> tasks = new HashSet<YAWLTask>();

        for (Object netRoot: NetGraphModel.getRoots(net)) {
            YAWLTask task = NetCellUtilities.getTaskFromCell(netRoot);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Returns all atomic tasks in the selected net. Specifically,
     * all those vertices in the net that conform to the <code>YAWLAtomicTask</code> interface.
     * @param net The net to search within.
     * @return The set of <code>YAWLAtomicTask</code> objects within the selected net.
     * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask
     */
    public static Set<YAWLAtomicTask> getAtomicTasks(NetGraphModel net) {
        Set<YAWLAtomicTask> atomicTasks = new HashSet<YAWLAtomicTask>();
        for (Object netRoot: NetGraphModel.getRoots(net)) {
            YAWLAtomicTask task = NetCellUtilities.getAtomicTaskFromCell(netRoot);
            if (task != null) {
                atomicTasks.add(task);
            }
        }
        return atomicTasks;
    }


    /**
     * Returns all composite tasks in the selected net. Specifically,
     * all those vertices in the net that conform to the <code>YAWLCompositeTask</code> interface.
     * @param net The net to search within.
     * @return The set of <code>YAWLCompositeTask</code> objects within the selected net.
     * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask
     */
    public static Set<YAWLCompositeTask> getCompositeTasks(NetGraphModel net) {
        Set<YAWLCompositeTask> compositeTasks = new HashSet<YAWLCompositeTask>();
        for (Object netRoot : NetGraphModel.getRoots(net)) {
            YAWLCompositeTask task = NetCellUtilities.getCompositeTaskFromCell(netRoot);
            if (task != null) {
                compositeTasks.add(task);
            }
        }
        return compositeTasks;
    }

    /**
     * Returns those tasks in the selected net that trigger cancellation set behaviour.
     * @param net The net to search within.
     * @return The set of tasks that trigger cancellation set behaviour.
     * @see org.yawlfoundation.yawl.editor.ui.net.CancellationSet
     */
    public static Set<YAWLTask> getTasksWithCancellationSets(NetGraphModel net) {
        HashSet<YAWLTask> tasks = new HashSet<YAWLTask>();
        for (Object netRoot : NetGraphModel.getRoots(net)) {
            YAWLTask task = NetCellUtilities.getTaskFromCell(netRoot);
            if (task != null && task.getCancellationSet().size() > 0) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Returns all the tasks that have flows requiring predicates from the set of net elements supplied
     * @param cells
     * @return
     */
    public static Set<YAWLTask> getTasksRequiringFlowPredicates(Set<Object> cells) {
        HashSet<YAWLTask> tasks = new HashSet<YAWLTask>();
        for(Object cell: cells) {
            if (cell instanceof YAWLFlowRelation) {
                YAWLFlowRelation flow = (YAWLFlowRelation) cell;
                if (flow.requiresPredicate()) {
                    tasks.add(flow.getSourceTask());
                }
            }
        }
        return tasks;
    }


    public static Set<YCompoundFlow> rationaliseFlows(Set<YFlow> flows,
                                                      Set<YCondition> implicitConditions) {
        Set<YCompoundFlow> compoundFlows = new HashSet<YCompoundFlow>();
        for (YCondition condition : implicitConditions) {
            if (condition.isImplicit()) {
                YFlow flowFromSource = condition.getPresetFlows().iterator().next();
                YFlow flowIntoTarget = condition.getPostsetFlows().iterator().next();
                compoundFlows.add(
                        new YCompoundFlow(flowFromSource, condition, flowIntoTarget));
                flows.remove(flowFromSource);
                flows.remove(flowIntoTarget);
            }
        }
        for (YFlow flow : flows) {
            compoundFlows.add(new YCompoundFlow(flow));
        }
        return compoundFlows;
    }


    public static String getPortID(YAWLPort port) {
        YAWLCell cell = (YAWLCell) port.getParent();
        if (cell instanceof Decorator) {
            Decorator decorator = (Decorator) cell;
            return (getContainerID((VertexContainer) decorator.getParent()));
         }
        else {
            return ((YAWLVertex) cell).getID();
        }
    }


    public static String getContainerID(VertexContainer container) {
        if (container != null) {
            for (Object o : container.getChildren()) {
                if (o instanceof YAWLVertex)
                    return ((YAWLVertex) o).getID();
            }
        }
        return "null";
    }



    /**
     * This should only be called by the PasteAction class as a cleanup. Trying to
     * premptively stop invalid flows from being copied/cut/deleted is simply too hard to implement
     * without significant changes to JGraph.  Instead, we allow all flows to be copied/cut, and
     * then trim out those that no longer have a valid source or target port on the paste action.
     * @param net
     * @return Object[] a set of objects that are not copyable.
     */

    public static Set<YAWLFlowRelation> getIllegallyCopiedFlows(NetGraphModel net) {
        Set<YAWLFlowRelation> illegalFlows = new HashSet<YAWLFlowRelation>();
        for (Object netRoot : NetGraphModel.getRoots(net)) {
            if (netRoot instanceof YAWLFlowRelation) {
                YAWLFlowRelation flow = (YAWLFlowRelation) netRoot;
                if (!net.contains(flow.getSource()) || !net.contains(flow.getTarget())) {
                    illegalFlows.add(flow);
                }
            }
        }
        return illegalFlows;
    }

    /**
     * Returns the input condition of the net specified.
     * @param net
     * @return the input condition of the net specified
     */
    public static InputCondition getInputCondition(NetGraphModel net) {
        for(Object cell: NetGraphModel.getRoots(net)) {
            InputCondition condition = NetCellUtilities.getInputConditionFromCell(cell);
            if (condition != null) {
                return condition;
            }
        }
        return null;
    }

    /**
     * Returns the output condition of the net specified.
     * @param net
     * @return the output condition of the net specified
     */
    public static OutputCondition getOutputCondition(NetGraphModel net) {
        for(Object cell : NetGraphModel.getRoots(net)) {
            OutputCondition condition = NetCellUtilities.getOutputConditionFromCell(cell);
            if (condition != null) {
                return condition;
            }
        }
        return null;
    }

    /**
     * Returns all the outgoing flows from the specified cell.
     * @param cell
     * @return A set of outgoing flows from the cell
     */
    public static Set<YAWLFlowRelation> getOutgoingFlowsFrom(YAWLCell cell) {
        YAWLVertex vertex = NetCellUtilities.getVertexFromCell(cell);
        return vertex != null ? new HashSet<YAWLFlowRelation>(vertex.getOutgoingFlows()) :
                Collections.<YAWLFlowRelation>emptySet();
    }



    /**
     * Returns the image icon used for the starting net of a specification
     * @return
     */
    public static ImageIcon getRootNetIcon() {
        return rootNetIcon;
    }


    public static ImageIcon getSubNetIcon() {
        return subNetIcon;
    }

    public static ImageIcon getIconForNetModel(NetGraphModel model) {
        return model.isRootNet() ? getRootNetIcon() : getSubNetIcon();
    }

    public static void toPNGfile(NetGraph graph, int imageBuffer, String fullFileName) {
        if (! fullFileName.toLowerCase().endsWith("png")) {
            fullFileName += ".png";
        }
        BufferedImage image = toBufferedImage(graph, imageBuffer);

        try {
            ImageIO.write(image, "png", new File(fullFileName));
        }
        catch (IOException ioe) {
            LogWriter.error("Could not write image", ioe);
        }
    }


    public static BufferedImage toBufferedImage(NetGraph net, int imageBuffer) {
        Object[] cells = net.getRoots();
        Rectangle2D bounds = net.getCellBounds(cells);
        Dimension d = bounds.getBounds().getSize();

        String label = "Specification: " + SpecificationModel.getHandler().getURI() +
                ", Net:  " + net.getName();

        int characterHeight = net.getFontMetrics(net.getFont()).getHeight();
        int characterWidth = (int) net.getFontMetrics(net.getFont())
                .getStringBounds(label, net.getGraphics()).getWidth();

        BufferedImage image = new BufferedImage(
                  Math.max(characterWidth, d.width) + imageBuffer*2,
                  d.height + imageBuffer*2 + characterHeight,
                  BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.setFont(net.getFont());

        // Note: drawing a string, the coordinates are the _bottom, left_ of the string.
        // We must cater for the height of the string in its coordinates.
        graphics.drawString(label, imageBuffer, imageBuffer/4 + characterHeight);

        PrettyOutputStateManager stateManager = new PrettyOutputStateManager(net);
        stateManager.makeGraphOutputReady();
        graphics.drawImage(net.getImage(Color.WHITE, 0), imageBuffer,
                imageBuffer + characterHeight, null);
        stateManager.revertNetGraphToPreviousState();

        return image;
    }


}
