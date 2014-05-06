/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.configuration;

import java.util.prefs.Preferences;

/**
 * Manages the storage of all user settings between runs of the Editor
 * @author Michael Adams
 * @date 13/06/12
 */
public class ConfigurationSettings {

    private static final String NODE_PATH = "/org/yawlfoundation/yawl/editor/configuration";
    private static final Preferences _prefs = Preferences.userRoot().node(NODE_PATH);


    // process configuration settings
    private static final String CONFIGURABLE_NEW_ELEMENTS = "NewElementsConfigurable";
    private static final String CONFIGURABLE_AUTO_GREYOUT = "AutoGrayout";
    private static final String CONFIGURABLE_BLOCKING_INPUT_PORTS = "BlockingInputPorts";
    private static final String CONFIGURABLE_ALLOW_DEFAULT_CHANGES =
            "AllowDefaultConfigChanges";


    public static Preferences getSettings() { return _prefs; }


    public static void setConfigurableAllowDefaultChanges(boolean allow) {
        setBoolean(CONFIGURABLE_ALLOW_DEFAULT_CHANGES, allow);
    }

    public static boolean getConfigurableAllowDefaultChanges() {
        return getBoolean(CONFIGURABLE_ALLOW_DEFAULT_CHANGES);
    }

    public static void setConfigurableNewElements(boolean allow) {
        setBoolean(CONFIGURABLE_NEW_ELEMENTS, allow);
    }

    public static boolean getConfigurableNewElements() {
        return getBoolean(CONFIGURABLE_NEW_ELEMENTS);
    }

    public static void setConfigurableAutoGreyout(boolean allow) {
        setBoolean(CONFIGURABLE_AUTO_GREYOUT, allow);
    }

    public static boolean getConfigurableAutoGreyout() {
        return getBoolean(CONFIGURABLE_AUTO_GREYOUT);
    }

    public static void setConfigurableBlockingInputPorts(boolean allow) {
        setBoolean(CONFIGURABLE_BLOCKING_INPUT_PORTS, allow);
    }

    public static boolean getConfigurableBlockingInputPorts() {
        return getBoolean(CONFIGURABLE_BLOCKING_INPUT_PORTS);
    }



    private static boolean getBoolean(String key) {
        return _prefs.getBoolean(key, false);
    }

    private static void setBoolean(String key, boolean value) {
        _prefs.putBoolean(key, value);
    }

}
