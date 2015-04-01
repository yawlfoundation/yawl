package org.yawlfoundation.yawl.editor.ui;

import org.yawlfoundation.yawl.editor.core.util.FileUtil;
import org.yawlfoundation.yawl.editor.ui.update.PostUpdateTasks;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

/**
 * Sets a couple of system properties for mac users, then calls the main editor class.
 * Note: the setProperty calls won't 'take' unless there are in a separate class to the
 * main GUI
 * @author Michael Adams
 * @date 21/11/2013
 */
public class YLauncher {

    public static void main(final String[] args) {

        // run any post upgrade tasks
        if (FileUtil.isWindows()) {
            if (!UserSettings.getPostUpdatesCompleted()) {
                if (new PostUpdateTasks().go()) {
                    UserSettings.setPostUpdatesCompleted(true);
                }
            }
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "YAWL Editor");

        YAWLEditor.main(args);
    }

}
