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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class ViewCancellationSetAction extends YAWLSelectedNetAction
        implements CancellationSetModelListener, GraphStateListener,
        TooltipTogglingWidget  {

    {
        putValue(Action.SHORT_DESCRIPTION, " Add selected items to visible cancellation set ");
        putValue(Action.NAME, "Add to Cancellation Set");
        putValue(Action.LONG_DESCRIPTION, " Add selected items to visible cancellation set ");
        putValue(Action.SMALL_ICON, getPNGIcon("cancel"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_MASK));
    }

    private YAWLTask task;
    private boolean selected;


    public ViewCancellationSetAction() {
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.OneTaskSelected,
                        GraphState.NoElementSelected,
                        GraphState.OneElementSelected,
                        GraphState.MultipleVerticesSelected));

        setEnabled(false);
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(selected || state == GraphState.OneTaskSelected);
    }

    public void actionPerformed(ActionEvent event) {
        NetGraph graph = getGraph();
        if (graph != null) {
            selected = ((JToggleButton) event.getSource()).isSelected();
            if (selected) {
                setTask(graph.getSelectionCell());
            }
            else {
                task = null;
                setEnabled(false);
            }

            graph.changeCancellationSet(task);
        }
    }

    public void notify(int notificationType, YAWLTask triggeringTask) {

        switch (notificationType) {
            case CancellationSetModel.NO_VALID_SELECTION_FOR_SET_MEMBERSHIP:
                setEnabled(false);
                break;

            case CancellationSetModel.VALID_SELECTION_FOR_SET_MEMBERSHIP:
            case CancellationSetModel.SET_CHANGED:
                setEnabled(getGraph() != null && getGraph().getCancellationSetModel()
                                .hasValidSelectedCellsForInclusion());
                break;
        }
    }

    public String getEnabledTooltipText() {
        return task != null ?
                (" Viewing cancellation set of task " + task.getID() + " ") :
                 " View cancellation set of the selected task ";
    }

    public String getDisabledTooltipText() {
        return " View cancellation set of a selected task ";
    }

    private void setTask(Object cell) {
        if (cell instanceof VertexContainer) {
            task = (YAWLTask) ((VertexContainer) cell).getVertex();
        }
        else {
            task = (YAWLTask) cell;
        }
    }
}
