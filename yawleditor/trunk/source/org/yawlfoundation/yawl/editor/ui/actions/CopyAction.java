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
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/**
 * @author Lindsay Bradford
 *
 */
public class CopyAction extends YAWLBaseAction
        implements TooltipTogglingWidget, GraphStateListener {

    private static final CopyAction INSTANCE = new CopyAction();

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Copy");
        putValue(Action.LONG_DESCRIPTION, "Copy the selected elements");
        putValue(Action.SMALL_ICON, getMenuIcon("page_copy"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("C"));
    }


    private CopyAction() {
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.CopyableElementSelected));
    }


    public static CopyAction getInstance() {
        return INSTANCE;
    }


    public void actionPerformed(ActionEvent event) {
        NetGraph graph = getGraph();
        YAWLTask task = graph.viewingCancellationSetOf();

        graph.stopUndoableEdits();
        graph.changeCancellationSet(null);

        TransferHandler.getCopyAction().actionPerformed(
                new ActionEvent(graph, event.getID(), event.getActionCommand()));
        PasteAction.getInstance().setEnabled(true);

        graph.changeCancellationSet(task);
        graph.startUndoableEdits();
    }

    public String getEnabledTooltipText() {
        return " Copy the selected net elements ";
    }

    public String getDisabledTooltipText() {
        return " You must have a number of net elements selected" +
                " to copy them ";
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.CopyableElementSelected);
    }
}
