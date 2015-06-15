package org.yawlfoundation.yawl.controlpanel;

import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.PostUpdateTasks;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class YControlPanelBootstrap {

    public static void main(final String[] args) {
        if (FileUtil.isWindows()) {
            UserPreferences prefs = new UserPreferences();
            if (!prefs.getPostUpdatesCompleted()) {
                prefs.setPostUpdatesCompleted(new PostUpdateTasks().go());
            }
        }

        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                "YAWL " + YControlPanel.VERSION + " Control Panel");
        YControlPanel.main(args);
    }
}
