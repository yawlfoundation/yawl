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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Michael Adams
 * @date 7/09/2010
 */
public class PluginLoader {

    public static <T> T loadInstance(Class<T> c, String localPkg, String className)
            throws PluginLoaderException {
        if ((localPkg != null) && (! className.contains(".")))
            className = localPkg + className;
        boolean external = (localPkg == null) || (! className.startsWith(localPkg));

        try {
            return (external) ? loadInstance(c, className)
                              : (T) Class.forName(className).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            throw new PluginLoaderException("ClassNotFoundException: class '" +
                    className + "' could not be found - class ignored.");
        }
        catch (IllegalAccessException iae) {
            throw new PluginLoaderException("IllegalAccessException: class '" +
                    className + "' could not be accessed - class ignored.");
	    	}
        catch (InstantiationException ie) {
            throw new PluginLoaderException("InstantiationException: class '" +
                    className + "' could not be instantiated - class ignored.");
		    }
        catch (ClassCastException cce) {
            throw new PluginLoaderException("ClassCastException: class '" +
                    className + "' does not extend its Abstract Class - class ignored.");
        }
        catch (MalformedURLException mue) {
            throw new PluginLoaderException("MalformedURLException: class '" +
                    className + "' has a malformed classpath - class ignored.");
        }
    }


    public static <T> Set<T> loadInstances(Class<T> c, String pkg, String[] excludes) {
        String categoryName = getCategoryName(c);
        String pkgPath = Docket.getPackageFileDir(categoryName) ;
        String[] classes = new File(pkgPath).list(new ClassFileFilter(excludes));
        Set<T> instances = getInstances(c, pkg, classes);
        instances.addAll(getExternalInstances(c, categoryName));
        return instances;
    }


    /**************************************************************************/

    private static <T> T loadInstance(Class<T> c, String className)
            throws MalformedURLException, ClassNotFoundException, ClassCastException,
                   InstantiationException, IllegalAccessException {
        String basePath = Docket.getExternalPluginsDir();
        if (basePath != null) {
            if (! basePath.endsWith(File.separator)) basePath += File.separator;
            ClassLoader primaryLoader = Thread.currentThread().getContextClassLoader();
            URL[] pluginsDir = new URL[] { new URL("file://" + basePath) };
            ClassLoader loader = new URLClassLoader(pluginsDir, primaryLoader);
            return (T) loader.loadClass(className).newInstance();
        }
        else throw new ClassNotFoundException();
    }


    private static <T> Set<T> getInstances(Class<T> c, String pkg, String[] classNames) {
        Set<T> instances = new HashSet<T>();
        for (String className : classNames) {

            // strip off the file extension
            String sansExtn = className.substring(0, className.lastIndexOf('.'));
            try {
                instances.add(loadInstance(c, null, pkg + sansExtn));
            }
            catch (PluginLoaderException ple) {
                //
            }
        }
        return instances;
    }


    private static <T> Set<T> getExternalInstances(Class<T> c, String categoryName) {
        Set<T> instances = new HashSet<T>();
        List<String> plugins = PluginLoader.getPluginNames(categoryName);
        if (plugins != null) {
            String pkg = plugins.remove(0) + ".";
            instances = getInstances(c, pkg, plugins.toArray(new String[0]));
        }
        return instances;
    }


    private static List<String> getPluginNames(String pluginCategory) {
        List<String> pluginNames = null;
        String basePath = Docket.getExternalPluginsDir();
        if ((basePath != null) && (basePath.length() > 0)) { // don't bother if no ext dir set
            File dir = locateDir(basePath, pluginCategory);
            if (dir != null) {
                pluginNames = new ArrayList<String>();
                pluginNames.add(getPackageName(basePath, dir.getAbsolutePath()));
                pluginNames.addAll(Arrays.asList(dir.list(new ClassFileFilter())));
            }
        }
        return pluginNames;
    }


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


    private static String getCategoryName(Class c) {
        String pkg = c.getPackage().getName();
        return pkg.substring(pkg.lastIndexOf('.') + 1);
    }


    /**
     * This class is used by the File.list call in 'getPluginNames'
     * so that only valid class files are included
     */
    private static class ClassFileFilter implements FilenameFilter {

        private String[] excludes;

        public ClassFileFilter() { }

        public ClassFileFilter(String[] excludeArray) {
            excludes = excludeArray;
        }


        private boolean isExcluded(String name) {
            if (excludes != null) {
                for (String s : excludes) {
                    if (name.startsWith(s)) {
                        return true;
                    }
                }
            }    
            return false;
        }

        public boolean accept(File dir, String name) {
            return (! new File(dir, name).isDirectory()) &&    // ignore dirs
                   (! isExcluded(name)) &&                     // ignore excludes
                    name.toLowerCase().endsWith(".class");     // only want .class files
        }
    }

}
