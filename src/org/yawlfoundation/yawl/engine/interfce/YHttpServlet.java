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

package org.yawlfoundation.yawl.engine.interfce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.jar.Manifest;

/**
 * An override of HttpServlet to include a few useful generic methods
 *
 * @author Michael Adams
 * @date 6/10/13
 */
public class YHttpServlet extends HttpServlet {

    protected static Logger _log;


    public YHttpServlet() {
        super();
        _log = LogManager.getLogger(this.getClass());
    }


    // should be called by any sub-class that uses hibernate or sets timers
    public void destroy() {
        deregisterDbDrivers();
        interruptTimerThreads();
    }


    protected boolean getBooleanFromContext(String param) {
        return getBooleanFromContext(param, false);
    }


    protected boolean getBooleanFromContext(String param, boolean defValue) {
        String s = getServletContext().getInitParameter(param);
        return s != null ? s.equalsIgnoreCase("true") : defValue;
    }


    protected int getIntFromContext(String param) {
        String s = getServletContext().getInitParameter(param);
        return StringUtil.strToInt(s, -1);
    }


    protected double getDoubleFromContext(String param) {
        String s = getServletContext().getInitParameter(param);
        return StringUtil.strToDouble(s, -1);
    }


    protected String encryptPassword(String s) {
        try {
            return PasswordEncryptor.encrypt(s);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException nsae) {
            // nothing to do - call will return 'incorrect password'
            return s;
        }
    }


    protected String fail(String msg) {
        return StringUtil.wrap(msg, "failure");
    }


    protected String response(String result) {
        return StringUtil.wrap(result, "response");
    }


    protected Manifest getManifest() throws IOException {
        ServletContext application = getServletConfig().getServletContext();
        InputStream inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");
        return new Manifest(inputStream);
    }


    protected boolean isAllowedRedundantAction(String action) {
        return action == null ||
                action.contains("onnect") ||  // 'connect' 'checkConnection', 'disconnect'
                action.equals("promote") || action.equals("demote");
    }

    private void deregisterDbDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                _log.info("Deregistered JDBC driver: {}", driver);
            } catch (SQLException e) {
                _log.warn("Unable to deregister JDBC driver {}: {}", driver, e.getMessage());
            }
        }
    }

    private void interruptTimerThreads() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("Timer")) {
                thread.interrupt();
            }
        }
    }


}
