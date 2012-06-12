package org.yawlfoundation.yawl.editor.ui.plugin;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.foundations.LogWriter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
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

    private static YPluginLoader INSTANCE;
    private static Set<YEditorPlugin> plugins;

    private static final String PLUGIN_INTERFACE_NAME = YEditorPlugin.class.getName();


    private YPluginLoader() {
	    super(new URL[0], YAWLEditor.class.getClassLoader());
        plugins = new HashSet<YEditorPlugin>();
	    init(getPluginsPath());
    }


    public static YPluginLoader getInstance() {
        if (INSTANCE == null) INSTANCE = new YPluginLoader();
        return INSTANCE;
    }


    public Set<YEditorPlugin> getPlugins() {
        return plugins;
    }


    private void init(String path) {
        File f = new File(path);
        try {
            addURL(f.toURI().toURL());             // add plugin dir to search path
            String[] jarList = f.list(new JarFileFilter());
            if (jarList != null) {
                for (String jarName : jarList) {
                    addJAR(new File(path, jarName));
                }
            }
        }
        catch (IOException e) {
            LogWriter.error("Error loading plugins: " + e);
        }
    }


    private void addJAR(File f) throws IOException {
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
            for (Class<?> iface : c.getInterfaces()) {
                if (iface.getName().equals(PLUGIN_INTERFACE_NAME)) {
                    return (YEditorPlugin) c.newInstance();
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
