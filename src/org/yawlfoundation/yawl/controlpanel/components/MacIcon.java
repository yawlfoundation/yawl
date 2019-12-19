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

package org.yawlfoundation.yawl.controlpanel.components;

import org.simplericity.macify.eawt.DefaultApplication;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class MacIcon implements ActionListener, EngineStatusListener {

    private final EngineMonitor _engineMonitor;
    private static final DefaultApplication _macApp = new DefaultApplication();

    private int _waitIndex;


    public MacIcon(EngineMonitor monitor) {
        _engineMonitor = monitor;
        Publisher.addEngineStatusListener(this);
    }


    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped:  { setWaiting(false); setIcon("Stopped"); break; }
            case Stopping:
            case Starting: { setWaiting(true); break; }
            case Running:  { setWaiting(false); setIcon("Running"); break; }
        }
    }


    // triggered by engine monitor timer
    public void actionPerformed(ActionEvent event) {
        if (_waitIndex == 5) _waitIndex = 1;
        setIcon("Waiting" + _waitIndex++);
    }


    private void setIcon(String name) {
        ImageIcon icon = IconLoader.get("Yawl" + name);
        _macApp.setApplicationIconImage((BufferedImage) icon.getImage());
    }


    private void setWaiting(boolean start) {
        if (start) {
            _waitIndex = 1;
            _engineMonitor.addListener(this);
        }
        else _engineMonitor.removeListener(this);
    }

}
