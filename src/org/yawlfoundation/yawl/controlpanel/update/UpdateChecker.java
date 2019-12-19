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

package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.util.HttpUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class UpdateChecker extends SwingWorker<Void, Void> {

    protected Differ _differ;
    protected String _error;


    public UpdateChecker() { super(); }


    public Void doInBackground() {
        check();
        return null;
    }


    public void check() {
        try {
            UpdateConstants.init();
            _differ = new Differ(loadLatestCheckSums(), loadCurrentCheckSums());
        }
        catch (IOException ioe) {
            _error = "Error: " + ioe.getMessage();
        }
    }


    public Differ getDiffer() { return _differ; }

    public String getError() { return _error; }


    public boolean isNewVersion() {
        return _differ != null && _differ.isNewVersion();
    }


    public boolean hasUpdates() {
        return _differ != null && _differ.hasUpdates();
    }


    public boolean hasError() {
        return _error != null || getCurrentVersion() == null ||
                getLatestVersion() == null;
    }


    public String getErrorMessage() {
        if (_error != null) return _error;
        if (getCurrentVersion() == null) {
            return "Error: Unable to determine current version";
        }
        if (getLatestVersion() == null) {
            return "Error: Unable to determine latest version";
        }
        return null;
    }


    protected File loadCurrentCheckSums() throws IOException {
        File current = FileUtil.getLocalCheckSumFile();
        if (! current.exists()) {
            throw new IOException("Unable to determine current build version");
        }
        return current;
    }

    protected File loadLatestCheckSums() throws IOException {
        File latest = new File(getTmpDir(), UpdateConstants.CHECK_FILE);
        URL url = UpdateConstants.getCheckUrl();
        HttpUtil.download(url, latest);
        if (! latest.exists()) {
            throw new IOException("Unable to determine latest build version");
        }
        return latest;
    }


    private File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


    private String getCurrentVersion() {
        return _differ != null ? _differ.getCurrentVersion() : null;
    }

    private String getLatestVersion() {
        return _differ != null ? _differ.getLatestVersion() : null;
    }

}

