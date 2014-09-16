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
import org.yawlfoundation.yawl.editor.ui.actions.specification.CreateNetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;

import java.awt.event.KeyEvent;

class NetMenu extends YAWLOpenSpecificationMenu {

    public NetMenu() {
        super("Net", KeyEvent.VK_N);
    }

    protected void buildInterface() {
        add(new YAWLMenuItem(new CreateNetAction()));
        add(new YAWLMenuItem(new RemoveNetAction()));

        addSeparator();
        add(new YAWLMenuItem(new NetBackgroundColourAction()));
        add(new YAWLMenuItem(new NetBackgroundImageAction()));

        addSeparator();
        add(new YAWLMenuItem(new ExportNetToPngAction()));
        add(new YAWLMenuItem(new PrintNetAction()));

        addSeparator();
        add(new YAWLMenuItem(new RepositoryAddAction(null, Repo.NetDecomposition, null)));
        add(new YAWLMenuItem(new RepositoryGetAction(null, Repo.NetDecomposition, null)));
        add(new YAWLMenuItem(new RepositoryRemoveAction(null, Repo.NetDecomposition, null)));
    }
}
