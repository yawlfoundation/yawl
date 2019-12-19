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

package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.DownloadWorker;
import org.yawlfoundation.yawl.controlpanel.update.Downloader;
import org.yawlfoundation.yawl.controlpanel.update.FileNode;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Override of GUI-based Downloader for CLI
 *
 * @author Michael Adams
 * @author Joerg Evermann
 * @date 3/11/2015
 */
public class CliDownloader extends Downloader {

    private CliProgressPanel _progressPanel;


    public CliDownloader(List<FileNode> fileNodes, long totalBytes, File targetDir) {
        super(fileNodes, totalBytes, targetDir);
        _workerMap = new HashMap<DownloadWorker, Integer>();
    }


    public void setProgressPanel(CliProgressPanel panel) {
        _progressPanel = panel;
    }


    /**
     * Performs the downloading of the required files (each on a separate thread)
     */
    public void download() {
        _progressPanel.setText("Downloading " + _totalBytes + " bytes...");
        boolean cancelled = false;
        for (FileNode fileNode : _fileNodes) {
            CliDownloadWorker worker = new CliDownloadWorker(fileNode, _totalBytes, _targetDir);
            _workerMap.put(worker, 0);
            worker.start();
        }
        while (! isComplete()) {
            pause(500);
            _progressPanel.update(getTotalProgress());
            if (hasErrors()) {
                cancelled = cancel();
                break;
            }
        }
        _progressPanel.complete(cancelled);
    }


    /**
     * Cancels all workers if one reports an error
     * @return true
     */
    public boolean cancel() {
        for (DownloadWorker worker : _workerMap.keySet()) {
            CliDownloadWorker cliWorker = (CliDownloadWorker) worker;
            if (cliWorker.isAlive()) cliWorker.setCancelled();
        }
        return true;
    }


    /**
     * @return true if all workers have completed
     */
    private boolean isComplete() {
        for (DownloadWorker worker : _workerMap.keySet()) {
            if (((CliDownloadWorker) worker).isAlive()) return false;
        }
        return true;
    }


    /**
     * @return the summed progress of all download workers
     */
    private int getTotalProgress() {
        int progress = 0;
        for (DownloadWorker worker : _workerMap.keySet()) {
            progress += ((CliDownloadWorker) worker).getProgressValue();
        }
        return progress;
    }

}
