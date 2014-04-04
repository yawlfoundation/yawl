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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModel;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSetModelListener;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class AddToVisibleCancellationSetAction extends YAWLSelectedNetAction
        implements CancellationSetModelListener, TooltipTogglingWidget  {


    private static final AddToVisibleCancellationSetAction INSTANCE
            = new AddToVisibleCancellationSetAction();

    {
        putValue(Action.SHORT_DESCRIPTION, " Add selected items to visible cancellation set ");
        putValue(Action.NAME, "Add to Cancellation Set");
        putValue(Action.LONG_DESCRIPTION, " Add selected items to visible cancellation set ");
        putValue(Action.SMALL_ICON, getPNGIcon("addToCancelSet"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_MASK));
    }

    private AddToVisibleCancellationSetAction() {
        setEnabled(false);
    }

    public static AddToVisibleCancellationSetAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent event) {
        final NetGraph graph = getGraph();
        if (graph != null) {
            graph.addSelectedCellsToVisibleCancellationSet();
            SpecificationUndoManager.getInstance().setDirty(true);
        }
    }

    public void notify(int notificationType, YAWLTask triggeringTask) {
        switch (notificationType) {
            case CancellationSetModel.NO_VALID_SELECTION_FOR_SET_MEMBERSHIP:
                setEnabled(false);
                break;

            case CancellationSetModel.VALID_SELECTION_FOR_SET_MEMBERSHIP:
            case CancellationSetModel.SET_CHANGED:
                setEnabled(shouldEnable());
                break;
        }
    }

    public void specificationStateChange(SpecificationState state) {
        switch(state) {
            case NoNetsExist:
            case NoNetSelected: {
                setEnabled(false);
                break;
            }
            case NetSelected: {
                setEnabled(shouldEnable());
                break;
            }
        }
    }

    public String getEnabledTooltipText() {
        return " Add selected items to visible cancellation set ";
    }

    public String getDisabledTooltipText() {
        return " You must be viewing a task's cancellation set" +
                " and have selected net elements that are not" +
                " set members to add them to the set ";
    }

    private boolean shouldEnable() {
        return getGraph() != null && getGraph().getCancellationSetModel()
                                        .hasValidSelectedCellsForInclusion();
    }
}
