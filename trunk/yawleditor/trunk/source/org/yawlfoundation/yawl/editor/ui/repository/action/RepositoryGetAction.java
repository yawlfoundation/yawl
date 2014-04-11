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

package org.yawlfoundation.yawl.editor.ui.repository.action;

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.core.repository.RepoDescriptor;
import org.yawlfoundation.yawl.editor.core.repository.YRepository;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.specification.YAWLOpenSpecificationAction;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.repository.dialog.DescriptorListDialog;
import org.yawlfoundation.yawl.editor.ui.specification.NetReloader;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RepositoryGetAction extends YAWLOpenSpecificationAction {

    private final YRepository repository;
    private final Repo selectedRepo;
    private Component caller;
    private final JDialog owner;

    {
        putValue(Action.SHORT_DESCRIPTION, "Load From Repository");
        putValue(Action.NAME, "Load From Repository...");
        putValue(Action.LONG_DESCRIPTION, "Load From Repository");
        putValue(Action.SMALL_ICON, getPNGIcon("repo_get"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("alt L"));
    }

    public RepositoryGetAction(JDialog owner, Repo repo) {
        this.owner = owner;
        selectedRepo = repo;
        repository = YRepository.getInstance();
    }

    public RepositoryGetAction(JDialog owner, Repo repo, Component component) {
        this(owner, repo);
        caller = component;
    }

    public void actionPerformed(ActionEvent event) {
        DescriptorListDialog dialog = new DescriptorListDialog(owner, selectedRepo,
                DescriptorListDialog.GET_ACTION);
        dialog.setVisible(true);
        RepoDescriptor descriptor = dialog.getSelection();
        if (descriptor != null) {
            String name = descriptor.getName();
            switch (selectedRepo) {
                case TaskDecomposition:
                    loadTaskDecomposition(name);
                    break;
                case NetDecomposition:
                    loadNet(name);
                    break;
                case ExtendedAttributes:
                    ((ExtendedAttributesDialog) owner).loadAttributes(
                            repository.getExtendedAttributesRepository().get(name));
                    break;
                case DataDefinition: {
                    String text = repository.getDataDefinitionRepository().get(name);
                    ((DataTypeDialogToolBarMenu) caller).insertText(text, true);
                    break;
                }
            }
        }
    }

    private void loadTaskDecomposition(String name) {
        try {
            YAWLServiceGateway gateway = repository.getTaskDecompositionRepository()
                    .get(name);
            if (gateway != null) {
                getHandler().addTaskDecomposition(gateway);
            }
        }
        catch (Exception e) {
            // ?
        }
    }


    private void loadNet(String name) {
        try {
            YNet net = null;

            // have to load the task decompositions first
            for (YDecomposition decomposition : repository.getNetRepository()
                    .getNetAndDecompositions(name)) {
                if (decomposition instanceof YNet) {
                    net = (YNet) decomposition;
                }
                else {
                    getHandler().addTaskDecomposition((YAWLServiceGateway) decomposition);
                }
            }
            if (net != null) {
                getHandler().addNet(net);
                NetGraph graph = new NetGraph(net);
                SpecificationModel.getNets().add(graph.getNetModel());
                new NetReloader().reload(graph);
                YAWLEditor.getNetsPane().openNet(graph);
            }
        }
        catch (Exception e) {
            // ?
        }
    }


    private YControlFlowHandler getHandler() {
        return SpecificationModel.getHandler().getControlFlowHandler();
    }
}