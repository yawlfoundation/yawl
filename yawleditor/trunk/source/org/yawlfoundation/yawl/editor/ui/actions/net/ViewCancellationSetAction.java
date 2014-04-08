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
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToggleToolBarButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class ViewCancellationSetAction extends YAWLSelectedNetAction
        implements CancellationSetModelListener, GraphStateListener, FileStateListener,
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
    private boolean isSelected;
    private YAWLToggleToolBarButton toolBarButton;

    private static final ViewCancellationSetAction INSTANCE =
            new ViewCancellationSetAction();


    private ViewCancellationSetAction() {
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.OneTaskSelected,
                        GraphState.NoElementSelected,
                        GraphState.OneElementSelected,
                        GraphState.MultipleVerticesSelected));
        Publisher.getInstance().subscribe((FileStateListener) this);
        setEnabled(false);
    }

    public static ViewCancellationSetAction getInstance() {
        return INSTANCE;
    }


    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(isSelected || state == GraphState.OneTaskSelected);
    }

    public void actionPerformed(ActionEvent event) {
        NetGraph graph = getGraph();
        if (graph != null) {
            toolBarButton = (YAWLToggleToolBarButton) event.getSource();
            isSelected = toolBarButton.isSelected();
            saveChangesToCancellationSet();
            setTask(isSelected ? (YAWLCell) graph.getSelectionCell() : null);
            toolBarButton.setToolTipText(getEnabledTooltipText());
            graph.changeCancellationSet(task);
            setEnabled(isTaskSelected());
        }
    }

    // CancellationSetModelListener
    public void notify(int notificationType, YAWLTask triggeringTask) {
        if (notificationType == CancellationSetModel.SET_CHANGED) {
            if (toolBarButton == null) {
                toolBarButton = (YAWLToggleToolBarButton)
                        YAWLEditor.getToolBar().getButtonWithAction(this);
            }
            isSelected = triggeringTask != null;
            toolBarButton.setSelected(isSelected);
            setTask(triggeringTask);
            toolBarButton.setToolTipText(getEnabledTooltipText());
        }
    }

    public void specificationFileStateChange(FileState state) {
        if (state == FileState.Busy && isSelected && task != null) {
            task.getCancellationSet().save();
        }
        else if (state == FileState.Closed && toolBarButton != null) {
            toolBarButton.setSelected(false);
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

    private void setTask(YAWLCell cell) {
        task = (YAWLTask) ((cell instanceof VertexContainer) ?
              ((VertexContainer) cell).getVertex() : cell);

    }

    private void saveChangesToCancellationSet() {
        if (! (isSelected || task == null)) {
            task.getCancellationSet().save();
        }
    }

    private boolean isTaskSelected() {
        Object o = getGraph().getSelectionCell();
        if (o instanceof VertexContainer) {
            o = ((VertexContainer) o).getVertex();
        }
        return (o instanceof YAWLTask);
    }

}
