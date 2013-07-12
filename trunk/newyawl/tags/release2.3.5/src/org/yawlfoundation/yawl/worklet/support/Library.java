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

package org.yawlfoundation.yawl.worklet.support;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * The support library class of static methods
 * for the worklet service
 *
 * @author Michael Adams
 *         v0.8, 04-09/2006
 */

public class Library {

    // various file paths to the service installation & repository files
    public static String wsHomeDir;
    public static String wsRepositoryDir;
    public static String wsLogsDir;
    public static String wsWorkletsDir;
    public static String wsRulesDir;
    public static String wsSelectedDir;
    public static String resourceServiceURL;     // read from context-param
    public static boolean wsPersistOn;
    public static boolean wsInitialised;

    public static final String newline = System.getProperty("line.separator");

    private static Logger _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.support.Library");


    //===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the persistence value
     * read in from web.xml
     *
     * @param setting - true or false as specified in web.xml
     */
    public static void setPersist(boolean setting) {
        wsPersistOn = setting;
    }

    //===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the path to the worklet
     * repository as read in from web.xml
     *
     * @param dir - the path value specified in web.xml
     */
    public static void setRepositoryDir(String dir) {
        dir = dir.replace('\\', '/');             // switch slashes
        if (!dir.endsWith("/")) dir += "/";       // make sure it has ending slash

        // set the repository dir and the sub-dirs
        wsRepositoryDir = dir;
        wsLogsDir = wsRepositoryDir + "logs/";
        wsWorkletsDir = wsRepositoryDir + "worklets/";
        wsRulesDir = wsRepositoryDir + "rules/";
        wsSelectedDir = wsRepositoryDir + "selected/";

        if (!fileExists(wsWorkletsDir)) {
            _log.warn("The path set to the worklet repository may be incorrrect.");
            _log.warn("Please check that the repository path in the WorkletService's " +
                    "'web.xml' is valid and points to the repository files. ");
        }
    }

    //===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the actual local file path to
     * the worklet service (as read from the servlet context)
     *
     * @param dir - the local path value to the root of the worklet service
     */
    public static void setHomeDir(String dir) {
        wsHomeDir = dir;
    }


    //===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set the root url of the resource service.
     * Used by the Exception Service to interact with the resource service's admin pages.
     *
     * @param url the base URL of the resource service (as read from the servlet context)
     */
    public static void setResourceServiceURL(String url) {
        resourceServiceURL = url;
    }

    //===========================================================================//

    /**
     * Called by the WorkletGateway servlet to set a flag when the service has
     * completed initialisation (to prevent multi-initialisations)
     */
    public static void setServicetInitialised() {
        wsInitialised = true;
    }


    //===========================================================================//

    /**
     * removes the ddd_ part from the front or rear of a taskid
     */
    public static String getTaskNameFromId(String tid) {
        if (tid.length() == 0) return null;            // no string passed
        if (tid.indexOf('_') == -1) return tid;        // no change required

        String[] split = tid.split("_");

        // find out which side has the decomp'd taskid
        char c = tid.charAt(0);

        if (Character.isDigit(c))                      // if tid starts with a digit
            return split[1];                           // return name after the '_'
        else
            return split[0];                           // return name before the '_'
    }

    //===========================================================================//

    /**
     * returns a string of characters of length 'len'
     */
    public static String getSepChars(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append('/');
        return sb.toString();
    }

    //===========================================================================//

    /**
     * converts the contents of a file to a String
     *
     * @param fName the name of the file
     * @return the String representing the file's contents
     */
    public static String FileToString(String fName) {
        String fLine;
        StringBuilder result = new StringBuilder();

        try {
            if (!fileExists(fName)) return null;     // don't go further if no file

            FileReader fread = new FileReader(fName);
            BufferedReader bufread = new BufferedReader(fread);

            fLine = bufread.readLine();        // read first line
            while (fLine != null) {
                result.append(fLine);
                fLine = bufread.readLine();     // read next line
            }
            bufread.close();
            fread.close();
            return result.toString();
        } catch (FileNotFoundException fnfe) {
            _log.error("File not found! - " + fName, fnfe);
            return null;
        } catch (IOException ioe) {
            _log.error("IO Exception when reading file - " + fName, ioe);
            return null;
        }
    }

    //===========================================================================//

    /**
     * returns true if the file is found
     */
    public static boolean fileExists(String fName) {
        File f = new File(fName);
        return f.exists();
    }

    //===========================================================================//

    /**
     * returns a list of objects as a String of csv's
     */
    public static String listItems(List l) {
        String s = "";
        Iterator itr = l.iterator();
        while (itr.hasNext()) {
            s += itr.next() + ", ";
        }
        return s;
    }

    //===========================================================================//

    /**
     * appends a formatted line with the passed title and value to the StringBuilder
     */
    public static StringBuilder appendLine(StringBuilder s, String title, String item) {
        if (title == null) title = "null";
        if (item == null) item = "null";
        s.append(title);
        s.append(": ");
        s.append(item);
        s.append(newline);
        return s;
    }

    //===========================================================================//

    /**
     * appends an XML formatted line with the passed tag and value to the StringBuilder
     */
    public static StringBuilder appendXML(StringBuilder s, String tag, String value) {
        String open = '<' + tag + '>';
        String close = "</" + tag + '>';

        // replace all <'s and &'s with unmarkedup equivalents
        if (value.indexOf('&') > -1) value = value.replaceAll("&", "&amp;");
        if (value.indexOf('<') > -1) value = value.replaceAll("<", "&lt;");

        s.append(open);
        s.append(value);
        s.append(close);
        return s;
    }


    //===========================================================================//
    //===========================================================================//

}  // ends

