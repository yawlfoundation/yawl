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

package org.yawlfoundation.yawl.editor.ui.actions.view;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 6/05/2014
 */
public class TogglePluginToolbarViewAction extends YAWLBaseAction {

    private boolean selected;
    private JToolBar toolbar;

    public TogglePluginToolbarViewAction(JToolBar bar) {
        toolbar = bar;
        putValue(Action.NAME, bar.getName());
        selected = UserSettings.getViewPluginToolbar(bar.getName());
        if (selected) {
            YAWLEditor.getToolBar().getParent().add(bar);
        }
    }

    public void actionPerformed(ActionEvent event) {
        selected = !selected;
        UserSettings.setViewPluginToolbar(toolbar.getName(), selected);
        YAWLEditor.getInstance().setPluginToolBarVisible(toolbar, selected);
    }

    public void setSelected(boolean select) { selected = select; }

    public boolean isSelected() {
        return selected;
    }
}
