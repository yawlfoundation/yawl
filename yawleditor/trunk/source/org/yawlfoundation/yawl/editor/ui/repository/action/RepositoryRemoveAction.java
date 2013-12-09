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
import org.yawlfoundation.yawl.editor.core.repository.RepoDescriptor;
import org.yawlfoundation.yawl.editor.core.repository.RepoMap;
import org.yawlfoundation.yawl.editor.core.repository.YRepository;
import org.yawlfoundation.yawl.editor.ui.actions.specification.YAWLOpenSpecificationAction;
import org.yawlfoundation.yawl.editor.ui.repository.dialog.DescriptorListDialog;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RepositoryRemoveAction extends YAWLOpenSpecificationAction {

    private final Repo selectedRepo;
    private Component caller;
    private final JDialog owner;

    {
        putValue(Action.SHORT_DESCRIPTION, "Remove From Repository");
        putValue(Action.NAME, "Remove From Repository...");
        putValue(Action.LONG_DESCRIPTION, "Remove From Repository");
        putValue(Action.SMALL_ICON, getPNGIcon("repo_remove"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("alt R"));
    }

    public RepositoryRemoveAction(JDialog owner, Repo repo) {
        this.owner = owner;
        selectedRepo = repo;
    }

    public RepositoryRemoveAction(JDialog owner, Repo repo, Component component) {
        this(owner, repo);
        caller = component;
    }

    public void actionPerformed(ActionEvent event) {
        DescriptorListDialog dialog = new DescriptorListDialog(owner, selectedRepo,
                DescriptorListDialog.REMOVE_ACTION);
        dialog.setVisible(true);
        java.util.List<RepoDescriptor> selections = dialog.getSelections();
        if (selections != null) {
            YRepository repo = YRepository.getInstance();
            removeSelections(repo.getRepository(selectedRepo), selections);
        }
    }


    private void removeSelections(RepoMap repoMap,
                                  java.util.List<RepoDescriptor> selections) {
        for (RepoDescriptor descriptor : selections) {
            repoMap.removeRecord(descriptor.getName());
        }
    }
}