package org.yawlfoundation.yawl.launch.preferences;

import java.util.prefs.Preferences;

/**
 * @author Michael Adams
 * @date 5/08/2014
 */
public class UserPreferences {

    private static final Preferences _prefs =
            Preferences.userRoot().node("/org/yawlfoundation/yawl/controlPanel");

    private static final String START_ENGINE_ON_STARTUP = "startEngineOnStartup";
    private static final String CHECK_FOR_UPDATES_ON_STARTUP = "checkForUpdatesOnStartup";
    private static final String OPEN_OUTPUT_WINDOW_ON_STARTUP = "openOutputWindowOnStartup";
    private static final String STOP_ENGINE_ON_EXIT = "stopEngineOnExit";
    private static final String SHOW_LOGON_PAGE_ON_ENGINE_START =
            "showLogonPageOnEngineStart";


    public boolean startEngineOnStartup() {
        return getBoolean(START_ENGINE_ON_STARTUP);
    }

    public void setStartEngineOnStartup(boolean b) {
        setBoolean(START_ENGINE_ON_STARTUP, b);
    }


    public boolean checkForUpdatesOnStartup() {
        return getBoolean(CHECK_FOR_UPDATES_ON_STARTUP);
    }

    public void setCheckForUpdatesOnStartup(boolean b) {
        setBoolean(CHECK_FOR_UPDATES_ON_STARTUP, b);
    }


    public boolean openOutputWindowOnStartup() {
        return getBoolean(OPEN_OUTPUT_WINDOW_ON_STARTUP);
    }

    public void setOpenOutputWindowOnStartup(boolean b) {
        setBoolean(OPEN_OUTPUT_WINDOW_ON_STARTUP, b);
    }


    public boolean stopEngineOnExit() {
        return getBoolean(STOP_ENGINE_ON_EXIT);
    }

    public void setStopEngineOnExit(boolean b) {
        setBoolean(STOP_ENGINE_ON_EXIT, b);
    }


    public boolean showLogonPageOnEngineStart() {
        return getBoolean(SHOW_LOGON_PAGE_ON_ENGINE_START);
    }

    public void setShowLogonPageOnEngineStart(boolean b) {
        setBoolean(SHOW_LOGON_PAGE_ON_ENGINE_START, b);
    }


    private boolean getBoolean(String key) {
        return _prefs.getBoolean(key, false);
    }

    private void setBoolean(String key, boolean value) {
        _prefs.putBoolean(key, value);
    }




}
