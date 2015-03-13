/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.settings;

import java.util.prefs.Preferences;

/**
 * @author Michael Adams
 * @date 13/03/15
 */
public class SettingsStore {

    private static final String NODE_PATH = "/org/yawlfoundation/yawl/editor/plugins/worklet";
    private static final Preferences _prefs = Preferences.userRoot().node(NODE_PATH);

    // worklet service URI, userid & password
    private static final String SERVICE_USERID = "serviceUserID";
    private static final String SERVICE_PASSWORD = "serviceUserPassword";
    private static final String SERVICE_HOST = "serviceHost";
    private static final String SERVICE_PORT = "servicePort";

    private static final String DEFAULT_SERVICE_USERID = "editor";
    private static final String DEFAULT_SERVICE_PASSWORD = "yEditor";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;


    public static void setServiceUserId(String id) {
        _prefs.put(SERVICE_USERID, id);
    }

    public static String getServiceUserId() {
        return _prefs.get(SERVICE_USERID, DEFAULT_SERVICE_USERID);
    }


    public static void setServicePassword(String password) {
        _prefs.put(SERVICE_PASSWORD, password);
    }

    public static String getServicePassword() {
        return _prefs.get(SERVICE_PASSWORD, DEFAULT_SERVICE_PASSWORD);
    }


    public static String getServiceHost() {
        return _prefs.get(SERVICE_HOST, DEFAULT_HOST);
    }

    public static void setServiceHost(String host) {
        _prefs.put(SERVICE_HOST, host);
    }


    public static int getServicePort() {
        return _prefs.getInt(SERVICE_PORT, DEFAULT_PORT);
    }

    public static void setServicePort(int port) {
        _prefs.putInt(SERVICE_PORT, port);
    }

}
