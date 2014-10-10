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
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.editor.core.layout.YNetLayout;
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
import org.yawlfoundation.yawl.editor.ui.specification.io.LayoutImporter;
import org.yawlfoundation.yawl.editor.ui.swing.DefaultLayoutArranger;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.*;

public class RepositoryGetAction extends YAWLOpenSpecificationAction {

    private final YRepository repository;
    private final Repo selectedRepo;
    private Component caller;
    private final JDialog owner;

    {
        putValue(Action.SHORT_DESCRIPTION, "Load From Repository");
        putValue(Action.NAME, "Load From Repository...");
        putValue(Action.LONG_DESCRIPTION, "Load From Repository");
        putValue(Action.SMALL_ICON, getMenuIcon("repo_get"));
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
            Set<String> unknownTypes = getUnknownDataTypes(gateway);
            if (! unknownTypes.isEmpty()) {
                warnOnUnknownTypes(unknownTypes);
            }
        }
        catch (Exception e) {
            // ?
        }
    }


    private void loadNet(String name) {
        try {
            Set<String> unknownTypes = new HashSet<String>();
            Set<YNet> nets = new HashSet<YNet>();                   // may have subnets

            // have to load the task decompositions first
            for (YDecomposition decomposition : repository.getNetRepository()
                    .getNetAndDecompositions(name)) {
                if (decomposition instanceof YNet) {
                    nets.add((YNet) decomposition);
                }
                else {
                    getHandler().addTaskDecomposition((YAWLServiceGateway) decomposition);
                }
                unknownTypes.addAll(getUnknownDataTypes(decomposition));
            }
            for (YNet net : nets) {
                getHandler().addNet(net);
                NetGraph graph = new NetGraph(net);
                SpecificationModel.getNets().add(graph.getNetModel());
                YAWLEditor.getNetsPane().openNet(graph);
                new NetReloader().reload(graph);
                setNetLayout(net, graph);
            }
            if (nets.size() > 1) {
                YAWLEditor.getNetsPane().setSelectedTab(name);
            }
            if (! unknownTypes.isEmpty()) {
                warnOnUnknownTypes(unknownTypes);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private YControlFlowHandler getHandler() {
        return SpecificationModel.getHandler().getControlFlowHandler();
    }


    private void setNetLayout(YNet net, NetGraph graph)
            throws YSyntaxException, YLayoutParseException {
        YNetLayout layout = null;
        XNode layoutNode = repository.getNetRepository().getLayout(net.getID());
        if (layoutNode != null) {
            layout = new YNetLayout(net, NumberFormat.getInstance(Locale.getDefault()));
            layout.parse(layoutNode.getChild());
        }
        if (layout != null) {
            LayoutImporter.setNetLayout(graph.getNetModel(), layout);
            graph.getGraphLayoutCache().reload();
        }
        else {
            new DefaultLayoutArranger().layoutNet(graph.getNetModel());
        }
    }


    private Set<String> getUnknownDataTypes(YDecomposition decomposition) {
        Set<String> unknownDataTypes = new HashSet<String>();
        Set<String> udTypeNames = getHandler().getSpecification()
                .getDataValidator().getPrimaryTypeNames();
        Set<YVariable> variables = new HashSet<YVariable>();
        variables.addAll(decomposition.getInputParameters().values());
        variables.addAll(decomposition.getOutputParameters().values());
        if (decomposition instanceof YNet) {
            variables.addAll(((YNet) decomposition).getLocalVariables().values());
        }
        for (YVariable variable : variables) {
            String dataType = variable.getDataTypeNameUnprefixed();
            if (! isValidDataType(dataType, udTypeNames)) {
                unknownDataTypes.add(dataType);
            }
        }
        return unknownDataTypes;
    }


    private boolean isValidDataType(String dataType, Set<String> udTypeNames) {
        return XSDType.isBuiltInType(dataType) || YInternalType.isType(dataType) ||
                            udTypeNames.contains(dataType);
    }


    private void warnOnUnknownTypes(Set<String> types) {
        boolean multiple = types.size() > 1;
        String message = "The decomposition" + (multiple? "s" : "") +
                " imported from the repository contain" + (multiple? "" : "s") +
                "\nvariables with the following unknown data type"
                + (multiple? "s" : "") + ":\n\n" + listDataTypes(types) +
                "\n\nUntil a definition for each listed data type is created, or\n" +
                "imported from the repository if it exists there, this\n" +
                "specification will not validate successfully.";
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message,
                "Unknown Data Types in Imported Decompositions",
                JOptionPane.WARNING_MESSAGE);
    }


    private String listDataTypes(Set<String> types) {
        java.util.List<String> sortedList = new ArrayList<String>(types);
        Collections.sort(sortedList);
        StringBuilder s = new StringBuilder("  - ");
        int lineLimit = 55;
        for (String typeName : sortedList) {
            if (s.length() + typeName.length() > lineLimit) {
                lineLimit = s.length() + 55;
                s.append("\n    ");
            }
            s.append(typeName).append(", ");
        }
        s.replace(s.length()-2, s.length()-1, ".");
        return s.toString();
    }
}