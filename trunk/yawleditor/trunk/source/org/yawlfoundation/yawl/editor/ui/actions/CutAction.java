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

package org.yawlfoundation.yawl.editor.ui.actions;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Lindsay Bradford
 *
 */
public class CutAction extends YAWLBaseAction
        implements TooltipTogglingWidget, GraphStateListener {

    private static final CutAction INSTANCE = new CutAction();

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Cut");
        putValue(Action.LONG_DESCRIPTION, "Cut the selected elements");
        putValue(Action.SMALL_ICON, getMenuIcon("cut"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("X"));
    }

    private CutAction() {
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.DeletableElementSelected));
    }

    public static CutAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent event) {
        NetGraph graph = getGraph();
        YAWLTask task = graph.viewingCancellationSetOf();
        boolean cutCellsIncludeCancellationTask = false;
        Object[] selectedCells = graph.getSelectionCells();    // can return null
        Set connectingFlows = graph.getNetModel().getConnectingFlows(selectedCells);

        if (! (task == null || selectedCells == null)) {
            for (Object o : selectedCells) {
                if (o instanceof VertexContainer) {
                    o = ((VertexContainer) o).getVertex();
                }
                if (task.equals(o)) {
                    cutCellsIncludeCancellationTask = true;
                }
            }

            graph.stopUndoableEdits();
            graph.changeCancellationSet(null);
            graph.startUndoableEdits();
        }

        removeNetElements(selectedCells);

        TransferHandler.getCutAction().actionPerformed(
                new ActionEvent(getGraph(), event.getID(), event.getActionCommand()));
        PasteAction.getInstance().setEnabled(true);
        removeConnectingFlows(connectingFlows);

        if (! (task == null || cutCellsIncludeCancellationTask)) {
            graph.stopUndoableEdits();
            graph.changeCancellationSet(task);
            graph.startUndoableEdits();
        }
    }

    public String getEnabledTooltipText() {
        return " Cut the selected elements ";
    }

    public String getDisabledTooltipText() {
        return " You must have a number of net elements selected" +
                " to cut them ";
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.DeletableElementSelected);
    }


    private void removeConnectingFlows(Set connectingFlows) {
        NetGraph graph = getGraph();
        if (graph != null) {
            YControlFlowHandler handler = SpecificationModel.getHandler().getControlFlowHandler();
            for (Object o : connectingFlows) {
                YAWLFlowRelation flow = (YAWLFlowRelation) o;
                handler.removeFlow(graph.getName(), flow.getSourceID(), flow.getTargetID());
            }
            graph.getNetModel().removeCells(connectingFlows.toArray());
        }
    }


    private void removeNetElements(Object[] selectedCells) {
        NetGraph graph = getGraph();
        if (! (graph == null || selectedCells == null)) {
            YControlFlowHandler handler = SpecificationModel.getHandler().getControlFlowHandler();

            for (Object o : selectedCells) {
                if (o instanceof Condition) {
                    handler.removeCondition(graph.getName(), ((Condition) o).getID());
                }
                else if (o instanceof AtomicTask)  {
                    handler.removeAtomicTask(graph.getName(), ((AtomicTask) o).getID());
                }
                else if (o instanceof MultipleAtomicTask) {
                    handler.removeAtomicTask(graph.getName(), ((MultipleAtomicTask) o).getID());
                }
                else if (o instanceof CompositeTask) {
                    handler.removeCompositeTask(graph.getName(), ((CompositeTask) o).getID());
                }
                else if (o instanceof MultipleCompositeTask) {
                    handler.removeCompositeTask(graph.getName(), ((MultipleCompositeTask) o).getID());
                }
                else if (o instanceof YAWLFlowRelation) {
                    YAWLFlowRelation flow = (YAWLFlowRelation) o;
                    handler.removeFlow(graph.getName(), flow.getSourceID(), flow.getTargetID());
                }
            }
        }
    }
}
