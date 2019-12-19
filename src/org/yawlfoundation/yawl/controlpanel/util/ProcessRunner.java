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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 29/10/2015
 */
public class ProcessRunner {

    private Process _process;


    public void run(List<String> cmdList, Map<String, String> envParams) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        if (envParams != null) pb.environment().putAll(envParams);
        pb.redirectErrorStream(true);
        _process = pb.start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    InputStreamReader isr = new InputStreamReader(_process.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String line;

                    while ((line = br.readLine()) != null) {
                        handleOutput(line);
                    }
                }
                catch (IOException ioe) {
                    //
                }
                notifyCompletion();
            }
        }).start();
    }


    public boolean stop() {
        if (isAlive()) _process.destroy();
        return true;
    }


    public boolean isAlive() {
        if (_process == null) return false;

        try {
            _process.exitValue();
            return false;
        }
        catch(IllegalThreadStateException e) {
            return true;
        }
    }


    public int waitFor() {
        if (_process != null) {
            while (isAlive()) {
                try {
                    return _process.waitFor();
                }
                catch (InterruptedException ignore) {

                }
            }
        }
        return -1;
    }


    protected void handleOutput(String line) {
        System.out.println(line);
    }


    protected void notifyCompletion() { }


    protected Process getProcess() { return _process; }

}
