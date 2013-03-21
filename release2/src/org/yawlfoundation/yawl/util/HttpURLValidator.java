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

package org.yawlfoundation.yawl.util;

import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.net.*;

/**
 * A simple static checker that (1) checks that the url string passed in is a valid
 * URL, and then (2) that the server at the URL is responsive.
 * <p/>
 * Author: Michael Adams
 * Creation Date: 7/05/2009
 */
public class HttpURLValidator {

    private static final int CONNECT_TIMEOUT = 1000;
    private static boolean _cancelled = false;

    /**
     * validates a url passed as a String
     *
     * @param urlStr the url to validate
     * @return a message describing the success of failure of the validation
     */
    public static String validate(String urlStr) {
        try {
            return validate(createURL(urlStr));
        } catch (MalformedURLException mue) {
            return getErrorMessage(mue.getMessage());
        }
    }


    private static String validate(URL url) {
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("HEAD");
            httpConnection.setConnectTimeout(CONNECT_TIMEOUT);
            int response = httpConnection.getResponseCode();
            if ((response < 0) || (response >= 300))             // indicates some error
                return getErrorMessage(response + " " + httpConnection.getResponseMessage());
        } catch (SocketTimeoutException ste) {
            return getErrorMessage("Error attempting to validate URL: " + ste.getMessage());
        } catch (IOException ioe) {
            return getErrorMessage("Error attempting to validate URL: " + ioe.getMessage());
        }

        return "<success/>";                             // no errors and responded 'OK'
    }


    private static URL createURL(String urlStr) throws MalformedURLException {

        // escape early if urlStr is obviously bad
        if (urlStr == null) throw new MalformedURLException("URL is null");
        if (!urlStr.startsWith("http://"))
            throw new MalformedURLException("URL does not begin with 'http://'");

        // this will throw an exception if the URL is invalid
        return new URL(urlStr);
    }


    private static String getErrorMessage(String msg) {
        return StringUtil.wrap(msg, "failure");
    }


    public static void cancelAll() {
        _cancelled = true;            // called on shutdown to stop any current pings
    }


    public static boolean pingUntilAvailable(String urlStr, int timeoutSeconds)
            throws MalformedURLException {
        return new OnlineChecker().pingUntilAvailable(urlStr, timeoutSeconds);
    }


    public static boolean simplePing(String host, int port) {
        if ((host == null) || (port < 0)) return false;
        try {
            InetAddress address = InetAddress.getByName(host);
            Socket socket = new Socket(address, port);
            socket.close();
            return true;
        } catch (UnknownHostException uhe) {
            return false;
        } catch (IOException ioe) {
            return false;
        }
    }


    public static boolean isTomcatRunning(String urlStr) {
        try {
            return simplePing(new URL(urlStr).getHost(), getTomcatServerPort());
        } catch (MalformedURLException mue) {
            return false;
        }
    }


    private static int getTomcatServerPort() {
        Document serverConfigDoc = loadTomcatConfigFile("server.xml");
        if (serverConfigDoc != null) {
            Element e = serverConfigDoc.getRootElement();
            if (e != null) {
                return StringUtil.strToInt(e.getAttributeValue("port"), -1);
            }
        }
        return -1;
    }


    private static Document loadTomcatConfigFile(String filename) {
        if (!filename.startsWith("conf")) {
            filename = "conf" + File.separator + filename;
        }
        File configFile = new File(filename);
        if (!configFile.isAbsolute()) {
            configFile = new File(System.getProperty("catalina.base"), filename);
        }
        return (configFile.exists()) ? JDOMUtil.fileToDocument(configFile) : null;
    }


    static class OnlineChecker {

        boolean pingUntilAvailable(String urlStr, int timeoutSeconds)
                throws MalformedURLException {
            URL url = createURL(urlStr);                         // exception if URL is bad
            long now = System.currentTimeMillis();
            long timeoutMoment = now + (timeoutSeconds * 1000);
            while (now <= timeoutMoment) {
                if (validate(url).equals("<success/>")) return true;
                try {
                    if (_cancelled) throw new InterruptedException();
                    Thread.sleep(CONNECT_TIMEOUT);
                    now = System.currentTimeMillis();
                } catch (InterruptedException ie) {
                    return false;
                }
            }
            return false;
        }
    }


}
