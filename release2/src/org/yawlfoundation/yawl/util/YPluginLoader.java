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

package org.yawlfoundation.yawl.util;

import org.apache.log4j.Logger;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads classes from specified paths that implement or extend a particular class
 *
 * @author Michael Adams
 * @date 16/04/14
 */
public class YPluginLoader extends URLClassLoader {

    private List<String> _pathList;
    private Logger _log = Logger.getLogger(YPluginLoader.class);


    /**
     * Constructor
     * @param searchPath the path(s) to search for jar and class files. Multiple paths
     *                   can be specified, separated by semi-colons (;). Any
     *                   sub-directories are also searched
     */
    public YPluginLoader(String searchPath) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        _pathList = setPath(searchPath);
    }


    /**
     * Loads the set of classes found in the search path that implement or subclass a
     * specified class
     * @param mask the interface or super class to use as the basis of the search
     * @param <T>
     * @return the set of matching classes
     */
    public <T> Set<Class<T>> load(Class<T> mask) {
        try {
            Set<Class<T>> plugins = new HashSet<Class<T>>();
            loadPackageMatches(mask, plugins);   // load internal package classes

            // now add external matching classes, if any
            for (String path : _pathList) {
                if (! path.endsWith(File.separator)) path += File.separator;
                File f = new File(path);
                addURL(f.toURI().toURL());         // add dir to search path

                for (File file : getFileSet(f)) {
                    if (file.getName().endsWith(".jar")) {
                        processJAR(mask, plugins, file);
                    }
                    else {
                        String fileName = file.getAbsolutePath().replace(path, "");
                        addIfMatch(mask, plugins, fileName);
                    }
                }
            }
            return plugins;
        }
        catch (IOException e) {
            _log.error("Error loading classes: " + e);
            return Collections.emptySet();
        }
    }


    public <T> Map<String, Class<T>> loadAsMap(Class<T> mask) {
        Map<String, Class<T>> map = new HashMap<String, Class<T>>();
        for (Class<T> clazz : load(mask)) {
            map.put(clazz.getName(), clazz);
        }
        return map;
    }


    /**
     * Loads the set of classes that implement or subclass a specified class that are
     * members of the same package as that class
     * @param mask the interface or super class to use as the basis of the load
     * @param <T>
     * @return the set of matching classes
     */
    public <T> Set<Class<T>> loadInternal(Class<T> mask) {
        try {
            Set<Class<T>> plugins = new HashSet<Class<T>>();
            loadPackageMatches(mask, plugins);
            return plugins;
        }
        catch (IOException e) {
            _log.error("Error loading classes: " + e);
            return Collections.emptySet();
        }
    }


    /**
     * Loads a class that implements or subclasses a specified class and matches a
     * specific name
     * @param mask the interface or super class to use as the basis of the load
     * @param instanceName the name of the class to load an instance of
     * @param <T>
     * @return the loaded instance, or null if a matching class isn't found
     */
    public <T> T getInstance(Class<T> mask, String instanceName) {
        try {
            for (Class<T> clazz : load(mask)) {
                if (clazz.getName().equals(instanceName)) {
                    return clazz.newInstance();
                }
            }
        }
        catch (Throwable t) {
            // fall through
        }
        return null ;
    }


    /******************************************************************************/

    private <T> void processJAR(Class<T> mask, Set<Class<T>> plugins, File f)
            throws IOException {
        addURL(f.toURI().toURL());
        processJAR(mask, plugins, new JarFile(f));
    }


    private <T> void processJAR(Class<T> mask, Set<Class<T>> plugins,
                                JarURLConnection connection) throws IOException {
        JarFile jar = connection.getJarFile();
        JarEntry baseEntry = connection.getJarEntry();
        String base = baseEntry.getName();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (! entry.isDirectory() && entry.getName().startsWith(base)) {
                addIfMatch(mask, plugins, entry.getName());
            }
        }

    }


    // walks a jar file and checks each internal file for a match
    private <T> void processJAR(Class<T> mask, Set<Class<T>> plugins, JarFile jar)
            throws IOException {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (! entry.isDirectory()) {
                addIfMatch(mask, plugins, entry.getName());
            }
        }
    }


    private <T> void addIfMatch(Class<T> mask, Set<Class<T>> plugins, String fileName) {
        if (fileName.endsWith(".class")) {
            Class<T> plugin = loadIfMatch(mask, fileName);
            if (plugin != null) {
                plugins.add(plugin);
            }
        }
    }


    private <T> Class<T> loadIfMatch(Class<T> mask, String name) {
        try {
            Class<?> c = loadClass(pathToPackage(name));      // may throw NoClassDef
            if (mask.isInterface())  {
                return (Class<T>) loadIfImplementer(c, mask);
            }
            else {
                return (Class<T>) loadIfSubclass(c, mask);
            }
        }
        catch (Throwable e) {
            _log.error(e.getMessage());
        }
        return null;
    }

    // returns an instance of c if c implements the specified interface
    private <T> Class<T> loadIfImplementer(Class<T> c, Class<?> interfaceToMatch)
            throws Throwable {
        for (Class<?> iface : c.getInterfaces()) {
            if (iface.getName().equals(interfaceToMatch.getName())) {
                return (Class<T>) c;
            }
        }
        return null;
    }


    // returns an instance of c if c (or its ancestors) extend from the specified superclass
    private <T> Class<T> loadIfSubclass(Class<?> c, Class<T> superclassToMatch)
            throws Throwable {
        Class<?> superClass = c.getSuperclass();
        if (superClass == null || superClass == Object.class) return null;
        if (superClass.getName().equals(superclassToMatch.getName())) {
            return (Class<T>) c;
        }
        return loadIfSubclass(superClass, superclassToMatch);
    }


    // transforms a path string to a package name
    private String pathToPackage(String path) {
        return path.replace('/', '.').substring(0, path.lastIndexOf('.'));
    }


    // splits a path string on ';' to a list of paths (also fixes file separators)
    private List<String> setPath(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(pathStr.replace('\\', '/').split(";"));
    }


    // walks the tree from 'dir' to build a set of files found (no dirs included)
    private Set<File> getFileSet(File dir) {
        Set<File> fileTree = new HashSet<File>();
        File[] entries = dir.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isFile()) fileTree.add(entry);
                else fileTree.addAll(getFileSet(entry));
            }
        }
        return fileTree;
    }


    // loads the matching classes in the same package as 'mask'
    private <T> void loadPackageMatches(Class<T> mask, Set<Class<T>> plugins)
            throws IOException {
        String pkg = mask.getPackage().getName();
        String pkgPath = pkg.replace('.', '/');
        Enumeration<URL> e = getResources(pkgPath);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                processJAR(mask, plugins, (JarURLConnection) connection);
            }
            else if (connection instanceof FileURLConnection) {
                String[] fileList = new File(url.getPath()).list();
                if (fileList != null) {
                    for (String file : fileList) {
                        String fileName = new File(pkgPath, file).getPath();
                        addIfMatch(mask, plugins, fileName);
                    }
                }
            }
        }
    }

}
