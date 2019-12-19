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

package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.util.HttpUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class TomcatUtil {

    private static int SERVER_PORT = -1;
    private static URL ENGINE_URL;
    private static final String TOMCAT_VERSION = "7.0.65";
    private static final String CATALINA_HOME = deriveCatalinaHome();

    private static final TomcatProcess _process = new TomcatProcess(CATALINA_HOME);

    public static boolean start() throws IOException {
        if (! isEngineRunning()) {                       // yawl isn't running
            if (isPortActive()) {                        // but localhost:port is responsive
                throw new IOException("Tomcat port is already in use by another service.\n" +
                     "Please check/change the port in Preferences and try again.");
            }
            checkSizeOfLog();
            removePidFile();                            // if not already removed
            _process.start();
            return true;
        }
        return false;                                   // already started
    }


    public static boolean stop() { return stop(null); }


    public static boolean stop(PropertyChangeListener listener) {
        if (isTomcatRunning()) {
            System.out.println("INFO: Shutting down the server, please wait... ");
            try {
                return _process.stop(listener);
            }
            catch (IOException ioe) {
                return false;
            }
        }
        else {
            System.out.println("WARN: Server is already shutdown.");
        }
        return true;
    }



    public static boolean isPortActive() {
        return HttpUtil.isPortActive("localhost", getTomcatServerPort());
    }


    public static boolean isPortActive(int port) {
        return HttpUtil.isPortActive("localhost", port);
    }


    public static boolean isTomcatRunning() {
        return _process.isAlive() || isPortActive();
    }


    public static String getCatalinaHome() {
        return CATALINA_HOME;
    }


    public static int getTomcatServerPort() {
        if (SERVER_PORT < 0) SERVER_PORT = loadTomcatServerPort();
        return SERVER_PORT;
    }


    public static boolean setTomcatServerPort(int port) {
        XNode root = loadTomcatConfigFile("server.xml");
        if (root != null) {
            XNode service = root.getChild("Service");
            if (service != null) {
                XNode connector = service.getChild("Connector");
                if (connector != null) {
                    connector.addAttribute("port", port);
                    if (writeTomcatConfigFile("server.xml", root.toPrettyString(true))) {
                        updateServiceConfigs(getTomcatServerPort(), port);
                        SERVER_PORT = port;
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static boolean isEngineRunning() {
        if (ENGINE_URL == null) {
            try {
                ENGINE_URL = new URL("http", "localhost", getTomcatServerPort(),
                        "/yawl/ib");
            }
            catch (MalformedURLException mue) {
                return false;
            }
        }
        return HttpUtil.isResponsive(ENGINE_URL);
    }


    public static boolean killTomcatProcess() throws IOException {
        _process.kill();
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException ignore) {}
        return ! isPortActive();
    }


    public static void monitorShutdown(PropertyChangeListener listener) throws IOException {
        if (! _process.isMonitoringShutdown()) {
           _process.monitorShutdown(listener);
        }
    }

    private static int loadTomcatServerPort() {
        XNode root = loadTomcatConfigFile("server.xml");
        if (root != null) {
            XNode service = root.getChild("Service");
            if (service != null) {
                XNode connector = service.getChild("Connector");
                if (connector != null) {
                    return StringUtil.strToInt(connector.getAttributeValue("port"), -1);
                }
            }
        }
        return -1;       // default
    }


    private static XNode loadTomcatConfigFile(String filename) {
        File configFile = getTomcatConfigFile(filename);
        return (configFile.exists()) ?
                new XNodeParser().parse(StringUtil.fileToString(configFile)) : null;
    }


    private static boolean writeTomcatConfigFile(String filename, String content) {
        File configFile = getTomcatConfigFile(filename);
        if (configFile.exists()) {
            configFile = StringUtil.stringToFile(configFile, content);
        }
        return configFile != null;
    }


    private static File getTomcatConfigFile(String filename) {
        if (!filename.startsWith("conf")) {
            filename = "conf" + File.separator + filename;
        }
        File configFile = new File(filename);
        if (!configFile.isAbsolute()) {
            configFile = new File(getCatalinaHome(), filename);
        }
        return configFile;
    }


    // rename catalina.out if its too big - tomcat will create a new one on startup
    private static void checkSizeOfLog() {
        File log = new File(FileUtil.buildPath(getCatalinaHome(), "logs", "catalina.out"));
        if (log.exists() && log.length() > (1024 * 1024 * 5)) {              // 5mb
            String suffix = "." + new SimpleDateFormat("yyyyMMdd").format(new Date());
            log.renameTo(new File(log.getAbsolutePath() + suffix));
        }
    }


    private static void removePidFile() {
        File pidTxt = new File(getCatalinaHome(), "pid.txt");
        if (pidTxt.exists()) pidTxt.delete();
    }


    private static String deriveCatalinaHome() {
        try {
            File thisJar = FileUtil.getJarFile();
            if (thisJar.getAbsolutePath().endsWith(".jar")) {
                return FileUtil.buildPath(thisJar.getParentFile().getParent(),
                        "engine", "apache-tomcat-" + TOMCAT_VERSION);
            }
        }
        catch (URISyntaxException use) {
            //
        }
        return System.getenv("CATALINA_HOME");         // fallback
    }


    private static boolean updateServiceConfigs(int oldPort, int newPort) {
        if (oldPort == newPort) return true;
        String oldChars = ":" + oldPort;
        String newChars = ":" + newPort;
        File appsBase = new File(getCatalinaHome(), "webapps");
        for (File appDir : FileUtil.getDirList(appsBase)) {
            File webxml = new File(appDir, "WEB-INF" + File.separator + "web.xml");
            if (webxml.exists()) {
                StringUtil.replaceInFile(webxml, oldChars, newChars);
            }
        }
        return true;
    }

}
