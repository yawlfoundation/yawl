package org.yawlfoundation.yawl.editor.swing;

/**
 * Author: Michael Adams
 * Creation Date: 22/02/2009
 */
public class SplashContent {

    public static String getCopyright() {
        return "YAWLEditor v" +
        getVersionNumber() + " - (c) " + getBuildYear() + " The YAWL Foundation";
    }


    private static String getVersionNumber() {
      return "@EditorReleaseNumber@";
    }

    private static String getBuildYear() {
      String buildDate = "@BuildDate@";
      return buildDate.substring(0, 4);
    }

    
}
