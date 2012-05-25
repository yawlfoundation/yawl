package org.yawlfoundation.yawl.editor.api.plugin;

import java.util.*;

/**
 * @author Michael Adams
 * @date 25/05/12
 */
public class YPluginLoader {

    private static Set<YEditorPlugin> plugins;

    public YPluginLoader() {}


    public static Set<YEditorPlugin> load() {
        plugins = new HashSet<YEditorPlugin>();
        ServiceLoader<YEditorPlugin> loader = ServiceLoader.load(YEditorPlugin.class);
        for (YEditorPlugin plugin : loader) {
             plugins.add(plugin);
        }
        return plugins;
    }


    public static Set<YEditorPlugin> getPlugins() {
        if (plugins == null) return load();
        return plugins;
    }



}
