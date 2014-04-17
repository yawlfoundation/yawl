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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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
    public <T> Set<T> load(Class<T> mask) {
        try {
            Set<T> plugins = new HashSet<T>();
            for (String path : _pathList) {
                if (! path.endsWith(File.separator)) path += File.separator;
                File f = new File(path);
                addURL(f.toURI().toURL());         // add dir to search path

                for (File file : getFileSet(f)) {
                    if (file.getName().endsWith(".jar")) {
                        processJAR(mask, plugins, file);
                    }
                    else {
                        String fileName = file.getAbsolutePath().replaceFirst(path, "");
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


    /******************************************************************************/

    // walks a jar file and checks each internal file for a match
    private <T> void processJAR(Class<T> mask, Set<T> plugins, File f) throws IOException {
        addURL(f.toURI().toURL());
        JarFile jar = new JarFile(f);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (! entry.isDirectory()) {
                addIfMatch(mask, plugins, entry.getName());
            }
        }
    }


    private <T> void addIfMatch(Class<T> mask, Set<T> plugins, String fileName) {
        if (fileName.endsWith(".class")) {
            T plugin = loadIfMatch(mask, fileName);
            if (plugin != null) {
                plugins.add(plugin);
            }
        }
    }


    private <T> T loadIfMatch(Class<T> mask, String name) {
        try {
            Class<?> c = loadClass(pathToPackage(name));      // may throw NoClassDef
            if (mask.isInterface())  {
                return (T) loadIfImplementer(c, mask);
            }
            else {
                return (T) loadIfSubclass(c, mask);
            }
        }
        catch (Throwable e) {
            _log.error(e.getMessage());
        }
        return null;
    }

    // returns an instance of c if c implements the specified interface
    private <T> T loadIfImplementer(Class<T> c, Class<?> interfaceToMatch) throws Throwable {
        for (Class<?> iface : c.getInterfaces()) {
            if (iface.getName().equals(interfaceToMatch.getName())) {
                return (T) c.newInstance();
            }
        }
        return null;
    }


    // returns an instance of c if c (or its ancestors) extend from the specified superclass
    private <T> T loadIfSubclass(Class<?> c, Class<T> superclassToMatch) throws Throwable {
        Class<?> superClass = c.getSuperclass();
        if (superClass == Object.class) return null;
        if (superClass.getName().equals(superclassToMatch.getName())) {
            return (T) c.newInstance();
        }
        return loadIfSubclass(superClass, superclassToMatch);
    }


    // transforms a path string to a package name
    private String pathToPackage(String path) {
        return path.replaceAll(File.separator, ".").substring(0, path.lastIndexOf('.'));
    }


    // splits a path string on ';' to a list of paths
    private List<String> setPath(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(pathStr.split(";"));
    }


    // walks the tree from 'dir' to build a set of files found (no dirs included)
    private Set<File> getFileSet(File dir) {
        Set<File> fileTree = new HashSet<File>();
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(getFileSet(entry));
        }
        return fileTree;
    }

}
