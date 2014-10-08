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

package org.yawlfoundation.yawl.worklet;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.plugin.YEditorPluginAdapter;
import org.yawlfoundation.yawl.worklet.menu.RulesMenu;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 29/09/2014
 */
public class WorkletRulesPlugin extends YEditorPluginAdapter {

    public WorkletRulesPlugin() { }


    @Override
    public String getName() { return "Worklet Rules Plugin"; }

    @Override
    public String getDescription() {
        return "Allows rules to be added to Worklet Service";
    }

    @Override
    public YAWLSelectedNetAction getPluginMenuAction() {
        return null;   //?
    }

    @Override
    public JMenu getPluginMenu() {
        return new RulesMenu();
    }
}
