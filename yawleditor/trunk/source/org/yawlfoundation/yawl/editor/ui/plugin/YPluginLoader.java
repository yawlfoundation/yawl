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

package org.yawlfoundation.yawl.editor.ui.plugin;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Michael Adams
 * @date 25/05/12
 */
public class YPluginLoader extends URLClassLoader {

    private static final Class PLUGIN_INTERFACE_CLASS = YEditorPlugin.class;


    protected YPluginLoader() {
	    super(new URL[0], YAWLEditor.class.getClassLoader());
    }


    public Set<YEditorPlugin> getPlugins() {
        String path = getPluginsPath();
        File f = new File(path);
        try {
            Set<YEditorPlugin> plugins = new HashSet<YEditorPlugin>();
            addURL(f.toURI().toURL());             // add plugin dir to search path
            String[] jarList = f.list(new JarFileFilter());
            if (jarList != null) {
                for (String jarName : jarList) {
                    addJAR(plugins, new File(path, jarName));
                }
            }
            return plugins;
        }
        catch (IOException e) {
            LogWriter.error("Error loading plugins: " + e);
            return Collections.emptySet();
        }
    }


    private void addJAR(Set<YEditorPlugin> plugins, File f) throws IOException {
        addURL(f.toURI().toURL());
        JarFile jar = new JarFile(f);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                YEditorPlugin plugin = loadIfPlugin(entry.getName());
                if (plugin != null) {
                    plugins.add(plugin);
                }
            }
        }
    }


    private YEditorPlugin loadIfPlugin(String name) {
        try {
            Class<?> c = loadClass(pathToPackage(name));
            if (implementsPluginInterface(c)) {
                return (YEditorPlugin) c.newInstance();
            }
        }
        catch (Exception e) {
            // fall through to null
        }
        return null;
    }


    private boolean implementsPluginInterface(Class<?> c) {
        if (c == null) return false;
        for (Class<?> iface : c.getInterfaces()) {
            if (iface.getName().equals(PLUGIN_INTERFACE_CLASS.getName())) {
                return true;
            }
        }
        return implementsPluginInterface(c.getSuperclass());
    }


    private String pathToPackage(String path) {
        return path.replaceAll("/", ".").substring(0, path.lastIndexOf('.'));
    }


    private static String getPluginsPath() {
        return FileUtilities.getHomeDir() + "plugins";
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
