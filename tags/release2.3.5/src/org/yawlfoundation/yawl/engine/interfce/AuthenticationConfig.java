/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce;


/**
 * 
 * @author Lachlan Aldred
 * Date: 15/03/2004
 * Time: 12:11:08
 * 
 */
public class AuthenticationConfig {
    private static String userName;
    private String _password;
    private String _proxyHost;
    private String _proxyPort;


    private static AuthenticationConfig _myInstance;


    private AuthenticationConfig() {
    }


    public static boolean isSetForAuthentication() {
        return userName != null;
    }


    public static AuthenticationConfig getInstance() {
        if (_myInstance == null) {
            _myInstance = new AuthenticationConfig();
        }
        return _myInstance;
    }


    public void setProxyAuthentication(String userName, String password,
                                       String proxyHost, String proxyPort) {
        this.userName = userName;
        this._password = password;
        this._proxyHost = proxyHost;
        this._proxyPort = proxyPort;
    }


    public String getPassword() {
        return _password;
    }


    public String getProxyHost() {
        return _proxyHost;
    }


    public String getUserName() {
        return userName;
    }


    public String getProxyPort() {
        return _proxyPort;
    }
}
