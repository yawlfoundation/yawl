package org.yawlfoundation.yawl.editor.ui;

/**
 * Sets a couple of system properties for mac users, then calls the main editor class.
 * Note: the setProperty calls won't 'take' unless there are in a separate class to the
 * main GUI
 * @author Michael Adams
 * @date 21/11/2013
 */
public class YLauncher {

    public static void main(final String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "YAWL Editor");
        YAWLEditor.main(args);
    }

}
