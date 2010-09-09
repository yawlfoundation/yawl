/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 7/09/2010
 */
public class PluginLoader {

    public static List<String> getPluginNames(String pluginType) {
        List<String> pluginNames = null;
        String basePath = Docket.getExternalPluginsDir();  
        if (basePath != null) {            // don't bother if no ext dir set
            File dir = locateDir(basePath, pluginType);
            if (dir != null) {
                pluginNames = new ArrayList<String>();
                pluginNames.add(getPackageName(basePath, dir.getAbsolutePath()));
                pluginNames.addAll(Arrays.asList(dir.list(new ClassFileFilter())));
            }
        }
        return pluginNames;   
    }


    /**************************************************************************/

    private static File locateDir(String basePath, String dir) {
       return locateDir(new File(basePath), dir);
    }


    private static File locateDir(File f, String dir) {
        if (f.getAbsolutePath().endsWith(File.separator + dir)) {
            return f;
        }
        File[] fileList = f.listFiles();
        if (fileList != null) {
            for (File sub : fileList) {
                if (sub.isDirectory()) {
                    File located = locateDir(sub, dir);               // recurse
                    if (located != null) return located;
                }
            }
        }
        return null;
    }


    private static String getPackageName(String basePath, String packagePath) {
        String subPath = packagePath.substring(basePath.length() + 1);
        return subPath.replaceAll(File.separator, ".");
    }


    /**
     * This class is used by the File.list call in 'getPluginNames'
     * so that only valid class files are included
     */
    private static class ClassFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return (! new File(dir, name).isDirectory()) &&    // ignore dirs
                    name.toLowerCase().endsWith(".class");     // only want .class files
        }
    }
}
