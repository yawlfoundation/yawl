/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.menu;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLMenuItem;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.worklet.dialog.AddRuleDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Arrays;

/**
 * @author Michael Adams
 * @date 29/09/2014
 */
public class RulesMenu extends JMenu implements FileStateListener, GraphStateListener {

    private AtomicTask selectedTask;


    public RulesMenu() {
        super("Worklet Rules");
        addMenuItems();
        setIcon(getWorkletMenuIcon("worklet"));
        setMnemonic('W');
        setEnabled(false);
        Publisher.getInstance().subscribe(this);                     // file state
        Publisher.getInstance().subscribe(this,                      // graph state
              Arrays.asList(GraphState.NoElementSelected,
                      GraphState.ElementsSelected,
                      GraphState.OneElementSelected));
    }


    public void specificationFileStateChange(FileState state) {
        setEnabled(state == FileState.Open);
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        if (event == null) return;
        YAWLVertex vertex = null;
        switch(state) {
            case NoElementSelected: {
                vertex = null;
                break;
            }
            case OneElementSelected:
            case ElementsSelected: {
                Object cell = event.getCell();
                if (cell instanceof VertexContainer) {
                    vertex = ((VertexContainer) cell).getVertex();
                }
                else if (cell instanceof YAWLVertex) {
                    vertex = (YAWLVertex) cell;
                }
            }
        }
        setTask(vertex);
    }


    // only enable if task is assigned to the worklet service
    private void setTask(YAWLVertex vertex) {
        selectedTask = (vertex instanceof AtomicTask) ? (AtomicTask) vertex : null;
//        setEnabled(isWorkletTask());
    }


    private boolean isWorkletTask() {
        if (selectedTask == null) return false;
        YAWLServiceGateway decomposition =
                (YAWLServiceGateway) selectedTask.getDecomposition();
        if (decomposition != null) {
            YAWLServiceReference service = decomposition.getYawlService();
            if (service != null) {
                String uri = service.getServiceID();
                return uri != null && uri.contains("workletService/ib");
            }
        }
        return false;
    }


    private void addMenuItems() {
        add(new YAWLMenuItem(new AddRuleAction()));
    }


    private ImageIcon getWorkletMenuIcon(String name) {
        URL url = this.getClass().getResource("icon/" + name + ".png");
        return url != null ? new ImageIcon(url) : null;
    }


    /******************************************************************************/

    class AddRuleAction extends YAWLSelectedNetAction {

        {
            putValue(Action.SHORT_DESCRIPTION, "Add Rule");
            putValue(Action.NAME, "Add Rule");
            putValue(Action.LONG_DESCRIPTION, "Add Rule to Worklet Rule Set");
            putValue(Action.SMALL_ICON, getWorkletMenuIcon("add"));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
        }

        public void actionPerformed(ActionEvent event) {
            new AddRuleDialog(selectedTask).setVisible(true);
        }

    }
}
