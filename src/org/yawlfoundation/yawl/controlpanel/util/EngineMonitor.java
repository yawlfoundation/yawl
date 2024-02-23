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

package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Periodically checks that tomcat is running, and that the engine is available, and
 * announces any changes in those states.
 *
 * @author Michael Adams
 * @date 7/08/2014
 */
public class EngineMonitor implements ActionListener, EngineStatusListener {

    private Timer _timer;
    private static boolean _suspended;
    private EngineStatus _currentStatus = EngineStatus.Stopped;

    private static final int STARTUP_SHUTDOWN_PERIOD = 1000;
    private static final int MONITOR_PERIOD = 5000;


    public EngineMonitor() {
        _timer = new Timer(STARTUP_SHUTDOWN_PERIOD, this);
        _suspended = false;
        Publisher.addEngineStatusListener(this);
        ping();
    }


    public void addListener(ActionListener listener) {
        _timer.addActionListener(listener);
    }


    public void removeListener(ActionListener listener) {
        _timer.removeActionListener(listener);
    }


    public static void setSuspended(boolean suspend) { _suspended = suspend; }


    // from timer
    public void actionPerformed(ActionEvent event) {
        if (! (_suspended || _currentStatus == EngineStatus.Stopped)) {
            ping();
        }
    }


    // from Publisher
    public void statusChanged(EngineStatus status) {
        if (_currentStatus != status) {
            _currentStatus = status;
            switch (status) {
                case Stopping:
                    monitorStopping();             // deliberate fallthrough
                case Starting: {
                    monitorStartingOrStopping();
                    break;
                }
                case Running: {
                    monitorRunning();
                    break;
                }
                case Stopped: {
                    handleStop();
                    break;
                }
            }
        }
    }


    private void monitorStartingOrStopping() {
        _timer.setDelay(STARTUP_SHUTDOWN_PERIOD);
        _timer.restart();
    }


    private void monitorRunning() {
        _timer.setDelay(MONITOR_PERIOD);
        _timer.restart();
    }


    private void monitorStopping() {
        try {
            TomcatUtil.monitorShutdown(null);
        }
        catch (IOException ioe) {
            System.out.println("ERROR: The YAWL Engine has been shutdown externally and" +
                    " is now in an inconsistent state. Please restart the Control Panel.");
        }
    }


    private void handleStop() {
        _timer.stop();
    }


    private void ping() { new Pinger().execute(); }


    /***************************************************************************/

    class Pinger extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            if (TomcatUtil.isTomcatRunning()) {
                if (TomcatUtil.isEngineRunning()) {
                    announce(EngineStatus.Running);                     // T & E
                }
                else {                                                  // T & !E
                    if (_currentStatus == EngineStatus.Running) {
                        announce(EngineStatus.Stopping);
                    }
                    else if (_currentStatus == EngineStatus.Stopped) {
                        announce(EngineStatus.Starting);
                    }
                }
            }
            else {
                announce(EngineStatus.Stopped);
            }

            return null;
        }


        protected void announce(EngineStatus status) {
            if (_currentStatus != status) {
                switch (status) {
                    case Stopped:
                        Publisher.announceStoppedStatus();
                        break;
                    case Stopping:
                        Publisher.announceStoppingStatus();
                        break;
                    case Starting:
                        Publisher.announceStartingStatus();
                        break;
                    case Running:
                        Publisher.announceRunningStatus();
                        break;
                }
            }
        }

    }
}
