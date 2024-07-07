/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel;

import org.yawlfoundation.yawl.controlpanel.components.ComponentsPane;
import org.yawlfoundation.yawl.controlpanel.components.MacIcon;
import org.yawlfoundation.yawl.controlpanel.components.OutputPanel;
import org.yawlfoundation.yawl.controlpanel.components.ToolBar;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.update.ChecksumsReader;
import org.yawlfoundation.yawl.controlpanel.util.CursorUtil;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class YControlPanel extends JFrame {

    private final EngineMonitor _engineMonitor;
    private JTabbedPane _tabbedPane;
    private ComponentsPane _componentsPane;

    public static final String VERSION = "5.1";


    public YControlPanel() {
        super();
        _engineMonitor = new EngineMonitor();
        buildUI();

        // TablePanel#setBounds adjustment needed since ComponentsPane is a JLayeredPane
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Rectangle r = getBounds();
                getComponentsPane().getTablePanel().setBounds(
                        0, 0, r.width-21, r.height-140);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                doOnExit();
            }
        });
    }


    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                YControlPanel cp = new YControlPanel();
                cp.setVisible(true);
                if (args != null && args.length == 1 && args[0].equals("-updateCompleted")) {
                    cp.showUpdateSuccess();
               }
            }
        });
    }


    public ComponentsPane getComponentsPane() { return _componentsPane; }


    public void showComponentsPane() { _tabbedPane.setSelectedIndex(1); }


    private String getAppTitle() { return "YAWL Control Panel " + getVersion(); }


    private void buildUI() {
        setLayout(new BorderLayout());
        setResizable(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(getAppTitle());
        setIconImage(getFrameIcon());

        JPanel content = new JPanel(new BorderLayout());

        _tabbedPane = new JTabbedPane();
        _tabbedPane.add("Output Log", new OutputPanel());
        _tabbedPane.add("Components", createComponentsPanel());
        content.add(_tabbedPane, BorderLayout.CENTER);
        _tabbedPane.setToolTipTextAt(1, "View, install, remove and update components");

        ToolBar toolBar = new ToolBar(this);
        content.add(toolBar, BorderLayout.NORTH);
        add(content);
        pack();
        setMinimumSize();
        setLocationRelativeTo(null);            // centre on screen
        setMacIcon();
        toolBar.performUserPreferencesOnStart();

        CursorUtil.setContainer(this);
    }


    private JPanel createComponentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        _componentsPane = new ComponentsPane();
        panel.add(_componentsPane, BorderLayout.CENTER);
        return panel;
    }


    private void doOnExit() {
        if (TomcatUtil.isEngineRunning() && new UserPreferences().stopEngineOnExit()) {
            shutdownTomcat();
        }
        else {
            System.exit(0);
        }
    }


    private Image getFrameIcon() {
        ImageIcon icon = IconLoader.get("YawlRunning");
        return icon != null ? icon.getImage() : null;
    }


    private void setMacIcon() {
        if (FileUtil.isMac()) {
            new MacIcon(_engineMonitor);
        }
    }


    private void setMinimumSize() {
        Dimension size = getSize();
        setMinimumSize(new Dimension(size.width, size.height / 2));
    }


    private String getVersion() {
        ChecksumsReader props = new ChecksumsReader(FileUtil.getLocalCheckSumFile());
        XNode node = props.getControlPanelNode();
        return VERSION + (node != null ? "." + node.getChildText("build") : "");
    }


    private void showUpdateSuccess() {
        JOptionPane.showMessageDialog(this,
                "Updated successfully to version " + getVersion(),
                "Update Completed", JOptionPane.INFORMATION_MESSAGE);
    }


    private void shutdownTomcat() {
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.add(new JLabel(IconLoader.get("wait.gif")));
        panel.add(new JLabel("Shutting down engine, please wait..."));
        panel.setBackground(new Color(253, 235, 14));
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // wait until shutdown completes before exiting
        TomcatUtil.stop(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("state") &&
                        event.getNewValue() == SwingWorker.StateValue.DONE) {
                    System.exit(0);
                }
            }
        });

        dialog.setVisible(true);
    }

}
