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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Michael Adams
 * @date 25/05/12
 */
public class YPluginLoader extends URLClassLoader {

    List<String> _pathList;

    public YPluginLoader(String searchPath) {
        super(new URL[0], Thread.currentThread().getContextClassLoader());
        _pathList = setPath(searchPath);
    }


    public <T> Set<T> getPlugins(Class<T> iface) {
        try {
            Set<T> plugins = new HashSet<T>();
            for (String path : _pathList) {
                File f = new File(path);
                addURL(f.toURI().toURL());         // add plugin dir to search path
                String[] jarList = f.list(new JarFileFilter());
                if (jarList != null) {
                    for (String jarName : jarList) {
                        addJAR(iface, plugins, new File(path, jarName));
                    }
                }
            }
            return plugins;
        }
        catch (IOException e) {
            Logger.getLogger(this.getClass()).error("Error loading plugins: " + e);
            return Collections.emptySet();
        }
    }


    private <T> void addJAR(Class<T> iface, Set<T> plugins, File f) throws IOException {
        addURL(f.toURI().toURL());
        JarFile jar = new JarFile(f);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                T plugin = loadIfPlugin(iface, entry.getName());
                if (plugin != null) {
                    plugins.add(plugin);
                }
            }
        }
    }


    public Set<File> getFileList(File dir) {
        Set<File> fileTree = new HashSet<File>();
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(getFileList(entry));
        }
        return fileTree;
    }


    private <T> T loadIfPlugin(Class<T> ifaceToMatch, String name) {
        try {
            Class<?> c = loadClass(pathToPackage(name));
            for (Class<?> iface : c.getInterfaces()) {
                if (iface.getName().equals(ifaceToMatch.getName())) {
                    return (T) c.newInstance();
                }
            }
        }
        catch (Exception e) {
            // fall through to null
        }
        return null;
    }


    private String pathToPackage(String path) {
        return path.replaceAll("/", ".").substring(0, path.lastIndexOf('.'));
    }


    private List<String> setPath(String pathStr) {
        if (pathStr == null || pathStr.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(pathStr.split(";"));
    }


    /****************************************************************************/

    private static class JarFileFilter implements FilenameFilter {

        public JarFileFilter() { super(); }

        public boolean accept(File dir, String name) {
            return (! new File(dir, name).isDirectory()) &&    // ignore dirs
                    name.toLowerCase().endsWith(".jar");       // only want .jar files
        }
    }

}
