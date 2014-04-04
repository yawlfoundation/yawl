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

public class RemoveFromVisibleCancellationSetAction extends YAWLSelectedNetAction
        implements CancellationSetModelListener, TooltipTogglingWidget {

    private static final RemoveFromVisibleCancellationSetAction INSTANCE
            = new RemoveFromVisibleCancellationSetAction();

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Remove from Cancellation Set");
        putValue(Action.LONG_DESCRIPTION, " Remove selected items from visible cancellation set ");
        putValue(Action.SMALL_ICON, getPNGIcon("removeFromCancelSet"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,InputEvent.CTRL_MASK));
    }

    private RemoveFromVisibleCancellationSetAction() {
        setEnabled(false);
    }

    public static RemoveFromVisibleCancellationSetAction getInstance() {
        return INSTANCE;
    }

    public void actionPerformed(ActionEvent event) {
        final NetGraph graph = getGraph();
        if (graph != null) {
            graph.removeSelectedCellsFromVisibleCancellationSet();
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
            case NetsExist: {
                break;
            }
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
        return " Remove selected items from visible cancellation set ";
    }

    public String getDisabledTooltipText() {
        return " You must be viewing a task's cancellation set" +
                " and have selected some of its set members to" +
                " remove them from the set ";
    }

    private boolean shouldEnable() {
        return getGraph() != null && getGraph().getCancellationSetModel()
                                        .hasValidSelectedCellsForExclusion();
    }

}
