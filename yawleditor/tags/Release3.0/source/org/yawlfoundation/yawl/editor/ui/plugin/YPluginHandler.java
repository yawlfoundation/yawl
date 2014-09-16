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

import org.apache.log4j.Logger;
import org.jgraph.graph.VertexView;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLPort;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.swing.menu.PluginsMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;


/**
 * @author Michael Adams
 * @date 18/12/2013
 */
public class YPluginHandler {

    private Set<YEditorPlugin> plugins;
    private boolean loaded;
    private Logger _log = Logger.getLogger(YPluginHandler.class);

    private static final YPluginHandler INSTANCE = new YPluginHandler();


    public static YPluginHandler getInstance() {
        return INSTANCE;
    }


    public Set<YEditorPlugin> getPlugins() {
        if (! loaded) {
            plugins = new YPluginLoader().getPlugins();
            loaded = true;
        }
        return plugins;
    }


    public void preOpenFile() {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.performPreFileOpenTasks();
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }


    public void postOpenFile() {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.performPostFileOpenTasks();
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }


    public void preSaveFile() {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.performPreFileSaveTasks();
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }


    public void postSaveFile() {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.performPostFileSaveTasks();
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }


    public void preCellRender(Graphics2D g2, VertexView cell) {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.performPreCellRenderingTasks(g2, cell);
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void elementAdded(NetGraphModel model, YAWLVertex vertex) {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.netElementAdded(model, vertex);
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void elementsRemoved(NetGraphModel model, Set<Object> cellsAndTheirEdges) {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.netElementsRemoved(model, cellsAndTheirEdges);
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void portsConnected(NetGraphModel model, YAWLPort source, YAWLPort target) {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.portsConnected(model, source, target);
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void specificationClosed() {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.closeSpecification();
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void specificationLoaded() {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.openSpecification();
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void netAdded(NetGraphModel model) {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.netAdded(model);
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }

    public void netRemoved(NetGraphModel model) {
        for (YEditorPlugin plugin : plugins) {
            try {
                plugin.netRemoved(model);
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
    }


    public java.util.List<JToolBar> getToolBars() {
        java.util.List<JToolBar> barSet = new ArrayList<JToolBar>();
        for (YEditorPlugin plugin : getPlugins()) {
            try {
                JToolBar bar = plugin.getToolbar();
                if (bar != null) {
                    if (bar.getName() == null) {
                        throw new IllegalArgumentException(
                                "Cannot import unnamed toolbar - please provide a name.");
                    }
                    bar.setOrientation(JToolBar.HORIZONTAL);
                    bar.setComponentPopupMenu(new PluginToolBarPopupMenu(bar));
                    barSet.add(bar);
                }
            }
            catch (Exception e) {
                warn(plugin, e);
            }
        }
        Collections.sort(barSet, new Comparator<JToolBar>() {
            public int compare(JToolBar bar1, JToolBar bar2) {
                return bar1.getName().compareTo(bar2.getName());
            }
        });
        return barSet;
    }

    private void warn(YEditorPlugin plugin, Exception e) {
        _log.warn("Plugin " + plugin.getName() + " threw an Exception", e);
    }


    /**************************************************************************/

    class PluginToolBarPopupMenu extends JPopupMenu {

        JToolBar bar;

        PluginToolBarPopupMenu(final JToolBar bar) {
            this.bar = bar;
            add(new YAWLBaseAction() {

                { putValue(Action.NAME, "Hide"); }

                public void actionPerformed(ActionEvent e) {

                    // remove toolbar
                    YAWLEditor.getInstance().setPluginToolBarVisible(bar, false);

                    // deselect menu item for toolbar
                    YAWLMenuBar menuBar = (YAWLMenuBar) YAWLEditor.getInstance().getJMenuBar();
                    PluginsMenu pluginsMenu = (PluginsMenu) menuBar.getMenu("Plugins");
                    if (pluginsMenu != null) {
                        pluginsMenu.setToolBarSelected(bar.getName(), false);
                    }
                }
            });
        }
    }

}
