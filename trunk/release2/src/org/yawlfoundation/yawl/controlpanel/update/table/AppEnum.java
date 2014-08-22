package org.yawlfoundation.yawl.controlpanel.update.table;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public enum AppEnum {

    controlpanel("This YAWL Control Panel application", -1, false),
    yawl("The core YAWL Engine", 0, false),
    resourceService("Handles the resource perspective", 1, false),
    documentStore("Stores files-as-data for processes", 2, false),
    costService("Adds cost-awareness to processes and tasks", 3, true),
    digitalSignature("Adds secure digital signatures to tasks", 4, true),
    mailService("Send emails from tasks", 5, true),
    monitorService("Provides basic process monitoring", 6, true),
    procletService("Handles inter-process communications", 7, true),
    schedulingService("Schedules resources for future tasks", 8, true),
    twitterService("Sends Twitter status messages from tasks", 9, true),
    workletService("Handles flexibility and exception handling", 10, true),
    yawlSMSInvoker("Sends text messages from tasks", 11, true),
    yawlWSInvoker("Invokes external web services from tasks", 12, true);


    private final String _description;
    private final int _sortOrder;
    private final boolean _installable;

    private static final Map<String, AppEnum> _fromStringMap =
            new HashMap<String, AppEnum>();


    static { for (AppEnum app : values()) _fromStringMap.put(app.name(), app); }


    AppEnum(String desc, int order, boolean installable) {
        _description = desc;
        _sortOrder = order;
        _installable = installable;
    }


    public String getDescription() { return _description; }

    public int getSortOrder() { return _sortOrder; }

    public boolean isInstallable() { return _installable; }


    public static AppEnum fromString(String s) {
        return (s != null) ? _fromStringMap.get(s) : null;
    }

}
