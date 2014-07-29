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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Author: Michael Adams
 * Creation Date: 16/03/2009
 */
public class ConnectionStatus extends JPanel implements ActionListener {

    private static final ImageIcon onlineIcon = ResourceLoader.getIcon("online.png");
    private static final ImageIcon offlineIcon = ResourceLoader.getIcon("offline.png");
    private final PreferencesLauncher preferencesLauncher = new PreferencesLauncher();

    private ConnectionIndicator engineIndicator;
    private ConnectionIndicator resourceIndicator;


    public ConnectionStatus() {
        super();
        setLayout(new BorderLayout());
        setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,0,0,1,Color.GRAY),
                BorderFactory.createEmptyBorder(0, 2, 0, 2)
            )
        );
        addIndicators();
        addMouseListener(preferencesLauncher);
        startHeartbeat();
    }


    public boolean refresh() {
        engineIndicator.setStatus(YConnector.isEngineConnected());
        return resourceIndicator.setStatus(YConnector.isResourceConnected());
    }


    // triggered by the 'heartbeat' timer
    public void actionPerformed(ActionEvent event) {
        refresh();
    }


    private void addIndicators() {
        engineIndicator = new ConnectionIndicator("Engine");
        resourceIndicator = new ConnectionIndicator("Resource Service");
        add(engineIndicator, BorderLayout.WEST);
        add(resourceIndicator, BorderLayout.EAST);
    }


    private void startHeartbeat() {
        Timer timer = new Timer(10000, this);     // 10 seconds
        timer.setInitialDelay(500);
        timer.start();
    }


    /*******************************************/

    class ConnectionIndicator extends JPanel {

        private final JLabel _indicator ;
        private boolean _online ;
        private String _indicatorFor;


        public ConnectionIndicator() {
            super();
            _indicator = new JLabel();
            _indicator.addMouseListener(preferencesLauncher);
            this.add(_indicator);
        }


        public ConnectionIndicator(String iFor) {
            this();
            setIndicatorFor(iFor) ;
        }


        public boolean setStatus(boolean isOnline) {
            if (_online != isOnline) {             // if there's a status change
                _online = isOnline;
                _indicator.setIcon(isOnline ? onlineIcon : offlineIcon);
                setTip();
            }
            return _online;
        }


        public void setIndicatorFor(String iFor) {
            _indicatorFor = iFor;
            setTip();
        }


        private void setTip() {
            _indicator.setToolTipText(_indicatorFor + " is " +
                    (_online ? "online" : "offline"));
        }

    }

    /*******************************************/

    // Opens the preferences dialog when an icon or surrounding panel is clicked
    private class PreferencesLauncher extends MouseAdapter {
        public void mouseClicked(MouseEvent mouseEvent) {
            new PreferencesDialog().setVisible(true);
            super.mouseClicked(mouseEvent);
        }
    }
}
