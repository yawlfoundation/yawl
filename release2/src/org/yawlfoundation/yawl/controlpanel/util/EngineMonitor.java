package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
            if (_shuttingDown && _shutdownCounter++ > 5) {
                _shuttingDown = false;
                _shutdownCounter = 0;
                Publisher.announceStoppedStatus();
            }
            else {
                new Pinger().execute();
            }
        }
    }


    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Starting: { monitorStarting(); break; }
            case Running: { monitorRunning(); break; }
            case Stopping: { monitorStopping(); break; }
            case Stopped: { handleStop(); break; }
        }
        _currentStatus = status;
    }


    private void monitorStarting() {
        _timer.setDelay(STARTUP_SHUTDOWN_PERIOD);
        _timer.restart();
    }


    private void monitorRunning() {
        _timer.setDelay(MONITOR_PERIOD);
        _timer.restart();
    }


    private void monitorStopping() {
        _timer.setDelay(STARTUP_SHUTDOWN_PERIOD);
        _timer.restart();
    }


    private void handleStop() {
        _timer.stop();
    }


    /***************************************************************************/

    class Pinger extends SwingWorker<Boolean, Void> {

        @Override
        protected Boolean doInBackground() throws Exception {
            switch (_currentStatus) {
                case Starting:
                case Running:  return TomcatUtil.isEngineRunning();
                case Stopping: return TomcatUtil.isRunning();
            }
            return false;
        }

        protected void done() {
            boolean result;
            try {
                result = get();
            }
            catch (Exception e) {
                result = false;
            }
            switch (_currentStatus) {
                case Starting: { if (result) Publisher.announceRunningStatus(); break; }
                case Running:  { if (!result) Publisher.announceStoppingStatus(); break; }
                case Stopping: { if (!result) { _shuttingDown = true; }
                    break;
                }
            }
        }
    }

}
