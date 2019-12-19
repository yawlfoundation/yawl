/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel;

import org.yawlfoundation.yawl.controlpanel.cli.CliUpdateSwitcher;
import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.PostUpdateTasks;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class YControlPanelBootstrap {

    public static void main(final String[] args) {
        if (FileUtil.isMac()) {
        //    System.setProperty("apple.awt.application.name", "Your App Name");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                        "YAWL " + YControlPanel.VERSION + " Control Panel");

        }
        else if (FileUtil.isWindows()) {
            UserPreferences prefs = new UserPreferences();
            if (!prefs.getPostUpdatesCompleted()) {
                prefs.setPostUpdatesCompleted(new PostUpdateTasks().go());
            }
        }

        // if there are args, it is probably a call to the CLI interface
        if (args.length > 0 && new CliUpdateSwitcher().handle(args)) {
            System.exit(0);
        }

        // otherwise proceed to UI
        YControlPanel.main(args);
    }
}
