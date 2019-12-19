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

package org.yawlfoundation.yawl.worklet.support;

/**
 * The support library class of static methods
 * for the worklet service
 *
 * @author Michael Adams
 *         v0.8, 04-09/2006
 */

public class WorkletConstants {

    // various file paths to the service installation & repository files
    public static String wsHomeDir;
    public static String resourceServiceURL;     // read from context-param
    public static boolean wsPersistOn;
    public static boolean wsInitialised;


    /**
     * Called by the WorkletGateway servlet to set the persistence value
     * read in from web.xml
     *
     * @param setting - true or false as specified in web.xml
     */
    public static void setPersist(boolean setting) {
        wsPersistOn = setting;
    }


    /**
     * Called by the WorkletGateway servlet to set the actual local file path to
     * the worklet service (as read from the servlet context)
     *
     * @param dir - the local path value to the root of the worklet service
     */
    public static void setHomeDir(String dir) {
        wsHomeDir = dir;
    }


    /**
     * Called by the WorkletGateway servlet to set the root url of the resource service.
     * Used by the Exception Service to interact with the resource service's admin pages.
     *
     * @param url the base URL of the resource service (as read from the servlet context)
     */
    public static void setResourceServiceURL(String url) {
        resourceServiceURL = url;
    }


    /**
     * Called by the WorkletGateway servlet to set a flag when the service has
     * completed initialisation (to prevent multi-initialisations)
     */
    public static void setServicetInitialised() {
        wsInitialised = true;
    }

}  // ends

