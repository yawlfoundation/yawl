package org.yawlfoundation.yawl.editor.swing.menu;

import javax.swing.*;

/**
 * Author: Michael Adams
 * Creation Date: 7/05/2010
 */
public class MenuUtilities {

    private static String os = System.getProperty("os.name");

    public MenuUtilities() {  }

    public static boolean isMacOS() {
        return (os != null) && os.toLowerCase().startsWith("mac");
    }

    public static String getAcceleratorModifier() {
        return isMacOS() ? "meta" : "control" ;
    }

    public static KeyStroke getAcceleratorKeyStroke(String keys) {
        return KeyStroke.getKeyStroke(getAcceleratorModifier() + " " + keys);
    }

}
