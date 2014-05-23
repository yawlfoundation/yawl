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

import org.yawlfoundation.yawl.editor.ui.actions.view.TogglePluginToolbarViewAction;
import org.yawlfoundation.yawl.editor.ui.plugin.YEditorPlugin;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class PluginsMenu extends JMenu {

    private JMenu _toolBarMenu;

    public PluginsMenu() {
        super("Plugins");
        buildInterface();
    }


    public JMenuItem addToolBarMenuItem(JToolBar toolBar) {
        TogglePluginToolbarViewAction action = new TogglePluginToolbarViewAction(toolBar);
        YAWLCheckBoxMenuItem item = new YAWLCheckBoxMenuItem(action);
        item.setSelected(action.isSelected());
        return _toolBarMenu.add(item);
    }

    public void setToolBarSelected(String name, boolean selected) {
        for (int i=0; i < _toolBarMenu.getItemCount(); i++) {
            JMenuItem item = _toolBarMenu.getItem(i);
            if (item.getText().equals(name)) {
                item.setSelected(selected);
                ((TogglePluginToolbarViewAction) item.getAction()).setSelected(selected);
            }
        }
    }


    protected void buildInterface() {
        setMnemonic(KeyEvent.VK_P);
        _toolBarMenu = new JMenu("Toolbars");
        add(_toolBarMenu);
        addSeparator();
        addPlugins();
        setEnabled(getItemCount() > 2);    // plugins found
    }


    private void addPlugins() {
        for (YEditorPlugin plugin : YPluginHandler.getInstance().getPlugins()) {
            JMenu subMenu = plugin.getPluginMenu();
            if (subMenu != null) {
                add(subMenu);
            }
            AbstractAction action = plugin.getPluginMenuAction();
            if (action != null) {
                add(new YAWLMenuItem(action));
            }
        }
    }
}
