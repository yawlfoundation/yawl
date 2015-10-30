package org.yawlfoundation.yawl.controlpanel.util;

import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 16/06/15
 */
public class StopMonitor extends SwingWorker<Void, Void> {

    private int _totalSecs;
    private TomcatProcess _tomcatProcess;


    public StopMonitor(TomcatProcess tomcatProcess, int secs) {
        _tomcatProcess = tomcatProcess;
        _totalSecs = secs;
    }


    @Override
    protected Void doInBackground() throws Exception {
        long now = System.currentTimeMillis();
        long expiry = now + (_totalSecs * 1000);
        Publisher.announceStoppingStatus();
        while (now < expiry) {
            sleep(500);
            if (! TomcatUtil.isPortActive()) {
                sleep(2000);
                break;
            };
            now = System.currentTimeMillis();
        }
        return null;
    }


    @Override
    protected void done() {
        super.done();
        try {

            // finally kill tomcat if it has not completed shutdown after specified time
            _tomcatProcess.kill();
        }
        catch (IOException ioe) {
            //
        }

        // ... and destroy the bash or cmd process that started it
        _tomcatProcess.destroy();

        Publisher.announceStoppedStatus();
        System.out.println("INFO: Shutdown successfully completed.");
    }


    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException ignore) { }
    }


}
