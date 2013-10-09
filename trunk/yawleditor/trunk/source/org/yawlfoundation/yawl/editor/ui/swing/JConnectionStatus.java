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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.preferences.PreferencesDialog;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Michael Adams
 * Creation Date: 16/03/2009
 */
public class JConnectionStatus extends JPanel {

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/";
    private static final ImageIcon onlineIcon = getIconByName("online") ;
    private static final ImageIcon offlineIcon = getIconByName("offline") ;
    private JIndicator engineIndicator;
    private JIndicator resourceIndicator;

    public JConnectionStatus() {
        super();
        setLayout(new BorderLayout());
        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(0, 2, 0, 2)
            )
        );
        addIndicators();
        addMouseListener(new PreferencesLauncher());
        startHeartbeat();
    }


    private static ImageIcon getIconByName(String iconName) {
      return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    private void addIndicators() {
        engineIndicator = new JIndicator("Engine");
        resourceIndicator = new JIndicator("Resource Service");
        add(engineIndicator, BorderLayout.WEST);
        add(resourceIndicator, BorderLayout.EAST);
    }


    private void startHeartbeat() {
        ScheduledExecutorService _heartbeat = Executors.newScheduledThreadPool(1);
        _heartbeat.scheduleAtFixedRate(new Runnable() {
            public void run() {
                checkConnectionStatus();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    
    public void setStatusMode(String component, boolean online) {
        if (component.equals("engine")) {
            engineIndicator.setOnline(online);
        }
        else if (component.equals("resource")) {
            resourceIndicator.setOnline(online);
        }
    }

    public void checkConnectionStatus() {
        setStatusMode("engine", YConnector.isEngineConnected());
        setStatusMode("resource", YConnector.isResourceConnected());
    }

    /*******************************************/

    class JIndicator extends JPanel {

        private JLabel _indicator ;
        private boolean _online ;
        private String _indicatorFor;

        public JIndicator() {
            super();
            _indicator = new JLabel();
            _indicator.addMouseListener(new PreferencesLauncher());
            setOnline(false);
            this.add(_indicator);
        }

        public JIndicator(String iFor) {
            this();
            setIndicatorFor(iFor) ;
        }


        public void setOnline(boolean online) {
            _online = online;
            _indicator.setIcon(online ? onlineIcon : offlineIcon) ;
            setTip();
        }

        public void setIndicatorFor(String iFor) {
            _indicatorFor = iFor;
            setTip();
        }

        private void setTip() {
            _indicator.setToolTipText(_indicatorFor + " is " + getStatus());
        }

        private String getStatus() {
            return _online ? "online" : "offline" ;
        }
    }

    /*******************************************/

    // Opens the preferences dialog when an icon or surrounding panel is clicked
    class PreferencesLauncher extends MouseAdapter {
        public void mouseClicked(MouseEvent mouseEvent) {
            new PreferencesDialog().setVisible(true);
            super.mouseClicked(mouseEvent);
        }
    }
}
