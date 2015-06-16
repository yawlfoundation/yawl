package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 16/06/15
 */
public class StartMonitor extends SwingWorker<Void, Void> {

    private int _totalSecs;

    public StartMonitor(int secs) {
        _totalSecs = secs;
    }


    @Override
    protected Void doInBackground() throws Exception {
        boolean started = false;
        for (int i=0; i < _totalSecs * 2; i++) {
            pause(500);
            if (TomcatUtil.isPortActive()) {
                started = true;
                Publisher.announceStartingStatus();
                break;
            };
        }
        if (! started) showError("Unable to start: Unknown Error");
        return null;
    }


    protected static void pause(long milliseconds) {
        Object lock = new Object();
        long now = System.currentTimeMillis();
        long finishTime = now + milliseconds;
        while (now < finishTime) {
            long timeToWait = finishTime - now;
            synchronized (lock) {
                try {
                    lock.wait(timeToWait);
                }
                catch (InterruptedException ex) {
                }
            }
            now = System.currentTimeMillis();
        }
    }


    private void showError(String msg) {
            JOptionPane.showMessageDialog(null, msg, "Engine Execution Error",
                    JOptionPane.ERROR_MESSAGE);
    }
}
