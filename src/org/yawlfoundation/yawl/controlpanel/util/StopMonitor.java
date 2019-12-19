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
        EngineMonitor.setSuspended(true);                      // suspend monitor pings
        long now = System.currentTimeMillis();
        long expiry = now + (_totalSecs * 1000);
        Publisher.announceStoppingStatus();
        while (now < expiry) {
            sleep(500);
            if (! TomcatUtil.isPortActive()) {
                sleep(2000);                         // port drops before stop completes
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

        EngineMonitor.setSuspended(false);
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
