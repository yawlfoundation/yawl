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

package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel;

import org.yawlfoundation.yawl.editor.core.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResourceSet;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YNet;

import java.awt.*;

/**
 * @author Michael Adams
 * @date 24/06/13
 */
public class TaskPrivilegesPanel extends AbstractResourceTabContent {

    private static final String[] _captions = {
            "Allow work item suspension",
            "Allow work item reallocation with reset state",
            "Allow work item reallocation with retained state",
            "Allow work item deallocation",
            "Allow work item delegation",
            "Allow work item to be skipped",
            "Allow work item to be piled"
    };

    public TaskPrivilegesPanel(YNet net, YAtomicTask task, ResourceDialog owner) {
        super(net, task);
        setLayout(new GridLayout(0,1));
        getContent(owner);
        load();
    }


    private void getContent(ResourceDialog owner) {
        for (int i= 0; i <= TaskPrivileges.PRIVILEGE_NAMES.size() -1; i++) {
             add(new TaskPrivilegeSubPanel(_captions[i],
                     TaskPrivileges.PRIVILEGE_NAMES.get(i), owner));
        }
    }


    public void load() {
        TaskResourceSet resources = getTaskResources();
        for (Component component : this.getComponents()) {
            ((TaskPrivilegeSubPanel) component).load(resources);
        }
    }

    public void save() {
        TaskResourceSet resources = getTaskResources();
        for (Component component : this.getComponents()) {
            ((TaskPrivilegeSubPanel) component).save(resources);
        }
    }
}
