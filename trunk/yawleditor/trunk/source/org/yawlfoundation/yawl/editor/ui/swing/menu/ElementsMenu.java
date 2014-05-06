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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.ui.actions.net.*;
import org.yawlfoundation.yawl.editor.ui.actions.net.align.*;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;

import javax.swing.*;
import java.awt.event.KeyEvent;


public class ElementsMenu extends YAWLOpenSpecificationMenu {


    public ElementsMenu() {
        super("Elements",KeyEvent.VK_L);
    }

    protected void buildInterface() {
        add(getAlignmentMenu());
        add(getRepositoryMenu());

        addSeparator();
        add(new YAWLMenuItem(new SetSelectedElementsFillColourAction()));
        add(new YAWLMenuItem(IncreaseSizeAction.getInstance()));
        add(new YAWLMenuItem(DecreaseSizeAction.getInstance()));

        addSeparator();
        add(new YAWLCheckBoxMenuItem(ViewCancellationSetAction.getInstance()));
        add(new YAWLMenuItem(AddToVisibleCancellationSetAction.getInstance()));
        add(new YAWLMenuItem(RemoveFromVisibleCancellationSetAction.getInstance()));
    }

    private JMenu getAlignmentMenu() {
        JMenu alignmentMenu = new JMenu("Alignment");
        alignmentMenu.setMnemonic(KeyEvent.VK_L);

        alignmentMenu.add(new YAWLMenuItem(AlignTopAction.getInstance()));
        alignmentMenu.add(new YAWLMenuItem(AlignMiddleAction.getInstance()));
        alignmentMenu.add(new YAWLMenuItem(AlignBottomAction.getInstance()));
        alignmentMenu.addSeparator();
        alignmentMenu.add(new YAWLMenuItem(AlignLeftAction.getInstance()));
        alignmentMenu.add(new YAWLMenuItem(AlignCentreAction.getInstance()));
        alignmentMenu.add(new YAWLMenuItem(AlignRightAction.getInstance()));

        return alignmentMenu;
    }


    private JMenu getRepositoryMenu() {
        JMenu repositoryMenu = new JMenu("Decomposition");
        repositoryMenu.setMnemonic(KeyEvent.VK_D);
        repositoryMenu.add(new YAWLMenuItem(
                new RepositoryAddAction(null, Repo.TaskDecomposition, null)));
        repositoryMenu.add(new YAWLMenuItem(
                new RepositoryGetAction(null, Repo.TaskDecomposition, null)));
        repositoryMenu.add(new YAWLMenuItem(
                new RepositoryRemoveAction(null, Repo.TaskDecomposition, null)));
        return repositoryMenu;
    }

}
