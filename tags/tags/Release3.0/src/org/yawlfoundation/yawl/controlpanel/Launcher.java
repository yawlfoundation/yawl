package org.yawlfoundation.yawl.controlpanel;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class Launcher {

    public static void main(final String[] args) {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                "YAWL " + YControlPanel.VERSION + " Control Panel");
        YControlPanel.main(args);
    }
}
