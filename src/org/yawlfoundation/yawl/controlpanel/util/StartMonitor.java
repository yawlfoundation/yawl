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
        long now = System.currentTimeMillis();
        long expiry = now + (_totalSecs * 1000);
        while (now < expiry) {
            sleep(500);
            if (TomcatUtil.isPortActive()) {
                started = true;
                Publisher.announceStartingStatus();
                break;
            };
            now = System.currentTimeMillis();
        }

        if (! started) showError("Unable to start: Unknown Error");
        return null;
    }



    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException ignore) { }
    }


    private void showError(String msg) {
            JOptionPane.showMessageDialog(null, msg, "Engine Execution Error",
                    JOptionPane.ERROR_MESSAGE);
    }
}
