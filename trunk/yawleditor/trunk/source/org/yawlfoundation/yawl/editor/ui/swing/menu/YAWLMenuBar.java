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

import org.yawlfoundation.yawl.editor.ui.swing.YSplashScreen;

import javax.swing.*;

public class YAWLMenuBar extends JMenuBar {

    public YAWLMenuBar(YSplashScreen splashScreen) {
        super();
        int progress = 0;

        add(new SpecificationMenu());
        splashScreen.updateProgress(progress+=15);

        add(new EditMenu());
        splashScreen.updateProgress(progress+=15);

        add(new NetMenu());
        splashScreen.updateProgress(progress+=15);

        add(new ElementsMenu());
        splashScreen.updateProgress(progress+=15);

        add(new PluginsMenu());
        add(new HelpMenu());
    }

    public JMenu getMenu(String name) {
        for (int i=0; i < getMenuCount(); i++) {
            JMenu menu = getMenu(i);
            if (menu.getText().equals(name)) return menu;
        }
        return null;
    }

}