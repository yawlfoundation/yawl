package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 7/08/2014
 */
public class EngineMonitor implements ActionListener, EngineStatusListener {

    private Timer _timer;
    private EngineStatus _currentStatus;

    // Tomcat stops accepting pings before it finishes shutting down.
    // These vars allow a few extra timer ticks before we're satisfied its done.
    private boolean _shuttingDown;
    private int _shutdownCounter = 0;

    private static final int STARTUP_SHUTDOWN_PERIOD = 1000;
    private static final int MONITOR_PERIOD = 5000;
    private static final int SHUTDOWN_TICKS = 4;


    public EngineMonitor() {
        _timer = new Timer(STARTUP_SHUTDOWN_PERIOD, this);
        Publisher.addEngineStatusListener(this);
    }


    public void addListener(ActionListener listener) {
        _timer.addActionListener(listener);
    }


    public void removeListener(ActionListener listener) {
        _timer.removeActionListener(listener);
    }


    public void actionPerformed(ActionEvent event) {
        if (_currentStatus != EngineStatus.Stopped) {
            if (_shuttingDown && _shutdownCounter++ > SHUTDOWN_TICKS) {
                _shuttingDown = false;
                _shutdownCounter = 0;
                Publisher.announceStoppedStatus();
                TomcatUtil.removePidFile();
            }
            else {
                ping();
            }
        }
    }


    public void statusChanged(EngineStatus status) {
        if (_currentStatus != status) {
            _currentStatus = status;
            switch (status) {
                case Starting:
                case Stopping: {
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


    private void handleStop() {
        _timer.stop();
        if (FileUtil.isWindows()) {
            try {
                TomcatUtil.killTomcatProcess();
            }
            catch (IOException ignore) {
                //
            }
        }
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
                _shuttingDown = true;                                    // !T
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
