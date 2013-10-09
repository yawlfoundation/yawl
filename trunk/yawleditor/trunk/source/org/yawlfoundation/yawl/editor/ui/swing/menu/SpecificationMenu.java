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

import org.yawlfoundation.yawl.editor.ui.actions.ExitAction;
import org.yawlfoundation.yawl.editor.ui.actions.specification.*;
import org.yawlfoundation.yawl.editor.ui.plugin.YEditorPlugin;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginLoader;

import javax.swing.*;
import java.awt.event.KeyEvent;

class SpecificationMenu extends JMenu {


    public SpecificationMenu() {
        super("File");
        setMnemonic(KeyEvent.VK_F);
        buildInterface();
    }

    protected void buildInterface() {
        addMenuItemAction(new CreateSpecificationAction());
        addMenuItemAction(new OpenSpecificationAction());
        add(OpenRecentSubMenu.getInstance());

        addSeparator();
        addMenuItemAction(new SaveSpecificationAction());
        addMenuItemAction(new SaveSpecificationAsAction());
        addMenuItemAction(new CloseSpecificationAction());

        addSeparator();
        addMenuItemAction(new ValidateSpecificationAction());
        addMenuItemAction(new AnalyseSpecificationAction());

        addSeparator();
        addMenuItemAction(new PrintSpecificationAction());

        addSeparator();
        addMenuItemAction(new DataTypeDefinitionsAction());
        addMenuItemAction(new DeleteOrphanDecompositionAction());
        addSeparator();

        if (addPlugins() > 0) addSeparator();

        addMenuItemAction(new PreferencesAction());
        addMenuItemAction(new ExitAction(this));
    }


    private int addPlugins() {
        int addedItemCount = 0;
        for (YEditorPlugin plugin : YPluginLoader.getInstance().getPlugins()) {
            AbstractAction action = plugin.getSpecificationMenuAction();
            if (action != null) {
                addMenuItemAction(action);
                addedItemCount++;
            }
        }
        return addedItemCount;
    }


    private void addMenuItemAction(AbstractAction action) {
        add(new YAWLMenuItem(action));
    }

}
