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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Loads and instantiates classes that use the pluggable interface.
 *
 * @author Michael Adams
 * @date 7/09/2010
 */
public class PluginLoader {

    private static URL[] _pluginDirs;

    private static final Logger _log = Logger.getLogger(PluginLoader.class);


    /**
     * Instantiates a single pluggable class.
     * @param c the type of class to instantiate
     * @param localPkg the class's package (may be null if class is located externally
     * to the resource service) 
     * @param className the canonical name of the required class (no extension)
     * @param external true if the clas is stored in an external directory
     * @return the instantiated class, or null if there was a problem
     * @throws PluginLoaderException a wrapper for the various exceptions that may occur
     */
    public static <T> T loadInstance(Class<T> c, String localPkg, String className, boolean external)
            throws PluginLoaderException {
        if ((localPkg != null) && (! className.contains(".")))
            className = localPkg + className;

        try {
            return (external) ? loadInstance(c, className)
                              : (T) Class.forName(className).newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            throw new PluginLoaderException("ClassNotFoundException: class '" +
                    className + "' could not be found - class ignored.", cnfe);
        }
        catch (IllegalAccessException iae) {
            throw new PluginLoaderException("IllegalAccessException: class '" +
                    className + "' could not be accessed - class ignored.", iae);
	    	}
        catch (InstantiationException ie) {
            throw new PluginLoaderException("InstantiationException: class '" +
                    className + "' could not be instantiated - class ignored.", ie);
		    }
        catch (ClassCastException cce) {
            throw new PluginLoaderException("ClassCastException: class '" +
                    className + "' does not extend its Abstract Class - class ignored.", cce);
        }
        catch (MalformedURLException mue) {
            throw new PluginLoaderException("MalformedURLException: class '" +
                    className + "' has a malformed classpath - class ignored.", mue);
        }
    }


    /**
     * Instantiates a single pluggable class.
     * @param c the type of class to instantiate
     * @param localPkg the class's package (may be null if class is located externally
     * to the resource service)
     * @param className the canonical name of the required class (no extension)
     * @return the instantiated class, or null if there was a problem
     * @throws PluginLoaderException a wrapper for the various exceptions that may occur
     */
    public static <T> T loadInstance(Class<T> c, String localPkg, String className)
            throws PluginLoaderException {
        boolean external = (localPkg == null) ||
                (className.contains(".") && (! className.startsWith(localPkg)));
        return loadInstance(c, localPkg, className, external);
    }


    /**
     * Instantiates all the classes matching the parameters passed.
     * @param c the type of class to instantiate
     * @param pkg the classes package (may be null if class is located externally
     * to the resource service)
     * @param excludes an array of the class names to not instantiate from those found
     * @return a Set of instantiated classes, or null if there was a problem
     */
    public static <T> Set<T> loadInstances(Class<T> c, String pkg, String[] excludes) {
        String category = getCategoryName(c);
        Set<T> instances = getInternalInstances(c, pkg, category, excludes);
        instances.addAll(getExternalInstances(c, category));
        return instances;
    }


    /**************************************************************************/

    /**
     * Instantiates a single pluggable class, located externally.
     * @param c the type of class to instantiate
     * @param className the fully qualified name of the required class (no extension)
     * @return the instantiated class, or null if there was a problem
     * @throws MalformedURLException 
     * @throws ClassNotFoundException
     * @throws ClassCastException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private static <T> T loadInstance(Class<T> c, String className)
            throws MalformedURLException, ClassNotFoundException, ClassCastException,
                   InstantiationException, IllegalAccessException {
        ClassLoader primaryLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = new URLClassLoader(getPluginsDirs(), primaryLoader);
        return (T) loader.loadClass(className).newInstance();
    }


    /**
     * Instantiates all the internal pluggable classes matching the parameters passed.
     * @param c the type of class to instantiate
     * @param pkg the classes package
     * @parma category the type of class to load (eg. codelets, filters, allocators etc)
     * @param excludes an array of the class names to not instantiate from those found
     * @return a Set of instantiated classes, or null if there was a problem
     */
    private static <T> Set<T> getInternalInstances(Class<T> c, String pkg,
                                                   String category, String[] excludes) {
        String pkgPath = Docket.getPackageFileDir(category) ;
        String[] classes = new File(pkgPath).list(new ClassFileFilter(excludes));
        return getInstances(c, pkg, classes, false);
    }

    
    /**
     * Instantiates all the external pluggable classes matching the parameters passed.
     * @param c the type of class to instantiate
     * @parma category the type of class to load (eg. codelets, filters, allocators etc)
     * @return a Set of instantiated classes, or null if there was a problem
     */
    private static <T> Set<T> getExternalInstances(Class<T> c, String category) {
        Set<T> instances = new HashSet<T>();
        for (TaggedStringList plugins : getPluginNames(category)) {
            String pkg = plugins.getTag() + ".";
            String[] names = plugins.toArray(new String[plugins.size()]);
            instances.addAll(getInstances(c, pkg, names, true));
        }
        return instances;
    }


    /**
     * Instantiates all the classes matching the parameters passed.
     * @param c the (base) type of class to instantiate
     * @param pkg the classes package
     * @param classNames an array of the class names to instantiate
     * @return a Set of instantiated classes, or an empty set if there was a problem
     */
    private static <T> Set<T> getInstances(Class<T> c, String pkg,
                                           String[] classNames, boolean external) {
        Set<T> instances = new HashSet<T>();
        for (String className : classNames) {

            // strip off the file extension
            String sansExtn = className.substring(0, className.lastIndexOf('.'));
            try {
                instances.add(loadInstance(c, null, pkg + sansExtn, external));
            }
            catch (PluginLoaderException ple) {
                _log.warn(ple.getMessage());
            }
        }
        return instances;
    }


    /**
     * Gets the array of external locations loaded from web.xml on startup
     * @return the array of external file locations
     * @throws MalformedURLException if a loaded path is invalid
     * @throws ClassNotFoundException if the path string wasn't loaded at startup
     */
    private static URL[] getPluginsDirs()
            throws MalformedURLException, ClassNotFoundException {
        if (_pluginDirs == null) {
            _pluginDirs = initPluginsDirs();
        }
        return _pluginDirs;
    }


    /**
     * Inits the array of external locations loaded from web.xml on startup
     * @return the array of external file locations
     * @throws MalformedURLException if a loaded path is invalid
     * @throws ClassNotFoundException if the path string wasn't loaded at startup
     */
    private static URL[] initPluginsDirs()
            throws MalformedURLException, ClassNotFoundException {
        List<String> baseDirs = getBaseDirs();
        if (baseDirs.size() > 0) {
            URL[] urls = new URL[baseDirs.size()];
            int i = 0;
            for (String path : baseDirs) {
                path = path.trim();
                if (! path.endsWith(File.separator)) path += File.separator;
                urls[i++] = new URL("file://" + path);
            }
            return urls;
        }
        throw new ClassNotFoundException();            // no base path loaded at startup
    }


    /**
     * Processes the external plugin path string from web.xml into a list of paths
     * @return the list of loaded dirs
     */
    private static List<String> getBaseDirs() {
        List<String> baseDirs = new ArrayList<String>();
        String basePath = Docket.getExternalPluginsDir();
        if ((basePath != null) && (basePath.length() > 0)) {
            String[] paths = basePath.split(";");
            for (String path : paths) baseDirs.add(path.trim());
        }
        return baseDirs;
    }


    /**
     * Gets a list of external classNames
     * @param pluginCategory the type of class to get the names for
     * @return a list of classNames
     */
    private static List<TaggedStringList> getPluginNames(String pluginCategory) {
        List<TaggedStringList> pluginNames = new ArrayList<TaggedStringList>();
        List<String> baseDirs = getBaseDirs();
        for (String path : baseDirs) {
            File dir = locateDir(path, pluginCategory);
            if (dir != null) {

                // tag = package; list = contained classes
                TaggedStringList tsList = new TaggedStringList(
                        getPackageName(path, dir.getAbsolutePath()));
                tsList.addAll(Arrays.asList(dir.list(new ClassFileFilter())));
                pluginNames.add(tsList);
            }
        }
        return pluginNames;
    }


    /**
     * Locates a subdir of a specified directory
     * @param basePath the name of the starting directory
     * @param dir the name of the subdir to find
     * @return the subdir if found, or null if not found
     */
    private static File locateDir(String basePath, String dir) {
        return locateDir(new File(basePath), dir);
    }


    /**
     * Locates a subdir of a specified directory
     * @param f the starting directory
     * @param dir the name of the subdir to find
     * @return the subdir if found, or null if not found
     */
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


    /**
     * Subtracts basePath from packagePath, then converts it into a package name
     * @param basePath the root path
     * @param packagePath the root path + the package path
     * @return the package name
     */
    private static String getPackageName(String basePath, String packagePath) {
        String subPath = packagePath.substring(basePath.length() + 1);
        return subPath.replaceAll(File.separator, ".");
    }


    /**
     * Gets the category of a class from its package name (ie. codelets, filters, etc.)
     * @param c
     * @return
     */
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
