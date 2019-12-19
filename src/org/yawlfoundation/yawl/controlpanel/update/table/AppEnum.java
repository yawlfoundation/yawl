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
    orderfulfillment("Custom forms for orderfulfillment example", 7, true),
    procletService("Handles inter-process communications", 8, true),
    schedulingService("Schedules resources for future tasks", 9, true),
    twitterService("Sends Twitter status messages from tasks", 10, true),
    workletService("Handles flexibility and exception handling", 11, true),
    yawlSMSInvoker("Sends text messages from tasks", 12, true),
    yawlWSInvoker("Invokes external web services from tasks", 13, true);


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
