package org.yawlfoundation.yawl.editor.swing;

import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

/**
 * Author: Michael Adams
 * Creation Date: 22/02/2009
 */
public class SplashContent {

    public static String getCopyright() {
        return "YAWLEditor" + getSizeDistinction() + " v " +
        getVersionNumber() + " - (c) " + getBuildYear() + " The YAWL Foundation";
    }

    private static String getSizeDistinction() {
      return (YAWLEngineProxy.engineLibrariesAvailable() ? "" : "Lite");
    }

    private static String getVersionNumber() {
      return "@EditorReleaseNumber@";
    }

    private static String getBuildYear() {
      String buildDate = "@BuildDate@";
      return buildDate.substring(0, 4);
    }

    
}
