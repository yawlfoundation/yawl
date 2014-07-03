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

package org.yawlfoundation.yawl.editor.ui.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;

public class FileLocations {

    private static final String HOME_DIR = setHomeDir();

    private static final String ABSOLUTE_TASK_ICON_PATH =
            HOME_DIR + File.separator + "icons";
    private static final String ABSOLUTE_EXTENDED_ATTRIBUTE_PATH =
            HOME_DIR + File.separator + "attributes";
    private static final String DECOMPOSITION_EXTENDED_ATTRIBUTE_PROPERTIES =
            "decomposition.properties";
    private static final String VARIABLE_EXTENDED_ATTRIBUTE_PROPERTIES =
            "variable.properties";



    /**
     * @return the absolute path of the default task icon path
     */
    public static String getDefaultTaskIconPath() {
        String path = UserSettings.getTaskIconsFilePath();
        return path != null ? path : ABSOLUTE_TASK_ICON_PATH;
    }

    /**
     * @return the absolute path of the default decomposition extended attribute
     * properties file
     */
    public static String getDefaultDecompositionAttributePath() {
        return ABSOLUTE_EXTENDED_ATTRIBUTE_PATH + File.separator +
                DECOMPOSITION_EXTENDED_ATTRIBUTE_PROPERTIES;
    }

    /**
     * @return the absolute path of the default variable extended attribute
     * properties plugin file
     */
    public static String getDefaultVariableAttributePath() {
        return ABSOLUTE_EXTENDED_ATTRIBUTE_PATH + File.separator +
                VARIABLE_EXTENDED_ATTRIBUTE_PROPERTIES;
    }


    /**
     * @return the absolute path of the location the editor was loaded
     */
    public static String getHomeDir() { return HOME_DIR; }


    // returns the path from which the editor was loaded (ie. the location of the editor jar)
    private static String setHomeDir() {
        String result = "";
        try {
            Class editorClass = Class.forName("org.yawlfoundation.yawl.editor.ui.YAWLEditor");
            CodeSource source = editorClass.getProtectionDomain().getCodeSource();
            if (source != null) {
                URL location = source.getLocation();
                String path = URLDecoder.decode(location.getPath(), "UTF-8");
                if (path.charAt(2) == ':') path = path.substring(1);
                int lastSep = path.lastIndexOf('/') ;
                if (lastSep > -1) result = path.substring(0, lastSep + 1) ;
                if (File.separatorChar != '/')
                    result = result.replace('/', File.separatorChar);
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
            result = System.getProperty("user.dir");   // default to current working dir
        }
        return result;
    }

}
