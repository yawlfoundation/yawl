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
import org.yawlfoundation.yawl.editor.ui.specification.YNetElementEdit;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

public class DeleteAction extends YAWLSelectedNetAction implements TooltipTogglingWidget, GraphStateListener {

    private static final DeleteAction INSTANCE = new DeleteAction();

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Delete");
        putValue(Action.LONG_DESCRIPTION, "Deletes currently selected net elements.");
        putValue(Action.SMALL_ICON, getMenuIcon("bin_empty"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
    }

    private DeleteAction() {
        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.DeletableElementSelected));
    }

    public static DeleteAction getInstance() {
        return INSTANCE;
    }


    public void actionPerformed(ActionEvent event) {
        YNetElementEdit.delete(getGraph().removeSelectedCellsAndTheirEdges().toArray());
    }


    public String getEnabledTooltipText() {
        return " Delete currently selected net elements ";
    }

    public String getDisabledTooltipText() {
        return " You must have a number of net elements selected" +
                " to delete them ";
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        setEnabled(state == GraphState.DeletableElementSelected);
    }
}
