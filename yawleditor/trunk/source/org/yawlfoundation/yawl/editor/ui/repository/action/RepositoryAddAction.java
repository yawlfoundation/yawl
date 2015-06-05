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

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.core.repository.YRepository;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.specification.YAWLOpenSpecificationAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.repository.dialog.AddDialog;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RepositoryAddAction extends YAWLOpenSpecificationAction {

    private final Repo selectedRepo;
    private Component caller;
    private final JDialog owner;

    {
        putValue(Action.SHORT_DESCRIPTION, "Store in Repository");
        putValue(Action.NAME, "Store in Repository...");
        putValue(Action.LONG_DESCRIPTION, "Store in Repository");
        putValue(Action.SMALL_ICON, getMenuIcon("repo_add"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("alt T"));
    }

    public RepositoryAddAction(JDialog owner, Repo repo) {
        this.owner = owner;
        selectedRepo = repo;
    }


    public RepositoryAddAction(JDialog owner, Repo repo, Component component) {
        this(owner, repo);
        caller = component;
    }

    public void actionPerformed(ActionEvent event) {
        AddDialog dialog = new AddDialog(owner, getDefaultText());
        dialog.setVisible(true);
        String name = dialog.getRecordName();
        if (!StringUtil.isNullOrEmpty(name)) {
            String description = dialog.getRecordDescription();
            YRepository repo = YRepository.getInstance();
            switch (selectedRepo) {
                case TaskDecomposition:
                    YDecomposition decomposition = getSelectedDecomposition();
                    if (decomposition != null) {
                        repo.getTaskDecompositionRepository().add(
                                name, description, (YAWLServiceGateway) decomposition);
                    }
                    else showError(
                            "Please first select a task with a decomposition", "Error");
                    break;
                case NetDecomposition:
                    YNet selectedNet = YAWLEditor.getNetsPane().getSelectedYNet();
                    repo.getNetRepository().add(name, description, selectedNet);
                    break;
                case ExtendedAttributes:
                    repo.getExtendedAttributesRepository().add(name, description,
                            ((ExtendedAttributesDialog) owner).getAttributes());
                    break;
                case DataDefinition: {
                    String content = ((DataTypeDialogToolBarMenu) caller)
                            .getSelectedTextQualified();
                    repo.getDataDefinitionRepository().add(name, description, content);
                    break;
                }
            }
        }
    }

    private String getDefaultText() {
        switch (selectedRepo) {
            case TaskDecomposition:
                YDecomposition selected = getSelectedDecomposition();
                return selected != null ? selected.getID() : null;
            case NetDecomposition:
                return YAWLEditor.getNetsPane().getSelectedYNet().getID();
            case ExtendedAttributes: break;
            case DataDefinition: break;
        }
        return "";
    }


    private YDecomposition getSelectedDecomposition() {
        Object cell = YAWLEditor.getNetsPane().getSelectedGraph().getSelectionCell();
        YAWLVertex vertex = NetCellUtilities.getVertexFromCell(cell);
        return vertex != null ? ((YAWLAtomicTask) vertex).getDecomposition() : null;
    }


    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message, title,
                JOptionPane.ERROR_MESSAGE);
    }

}