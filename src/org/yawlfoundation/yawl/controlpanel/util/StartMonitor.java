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
