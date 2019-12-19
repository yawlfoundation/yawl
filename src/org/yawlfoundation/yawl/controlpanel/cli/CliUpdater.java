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

import org.yawlfoundation.yawl.controlpanel.update.*;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;

import java.util.List;

/**
 * Overrides the SwingWorker operations in the UI Updater
 *
 * @author Michael Adams
 * @author Joerg Evermann
 * @date 3/11/2015
 */
public class CliUpdater extends Updater {

    private final CliProgressPanel _progressPanel;
    private CliDownloader _downloader;
    private boolean _done;


    public CliUpdater(UpdateTableModel model) {
        super(model);
        _progressPanel = new CliProgressPanel();
        new EngineMonitor();
        _done = false;
    }


    protected void download(List<AppUpdate> updates) {
        long downloadSize = getDownloadSize(updates);
        getProgressPanel().setDownloadSize(downloadSize);
        _downloader = new CliDownloader(_downloads, downloadSize, FileUtil.getTmpDir());
        _downloader.setProgressPanel(getProgressPanel());
        _downloader.download();
        downloadCompleted();
        _updatingThis = false;                              // prevent UI restart
    }


    protected void verify() {
        getProgressPanel().setText("Verifying downloads...");
        Verifier verifier = new Verifier(getMd5Map());
        if (verifier.verify()) {

            // can update CP only without cycling the engine
            setState(onlyUpdatingControlPanel() ? State.Update : State.StopEngine);
        }
        else {
            showError("Downloaded files failed verification. Update could not complete.");
            setState(State.Finalise);
        }
    }


    protected void finalise() {
        super.finalise();
        _done = true;
    }

    protected boolean isDone() { return _done; }

    protected boolean hasErrors() {
        return _downloader != null && _downloader.hasErrors();
    }


    protected void showError(String message) {
        getProgressPanel().setText("Update error: " + message);
    }


    protected CliProgressPanel getProgressPanel() { return _progressPanel; }

    protected Downloader getDownloader() { return _downloader; }

    protected void resetCursor() { }

    protected void setWaitCursor() { }

}
