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
        _downloader = new CliDownloader(UpdateChecker.SOURCE_URL,
                UpdateChecker.SF_DOWNLOAD_SUFFIX, _downloads,
                downloadSize, FileUtil.getTmpDir());
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


}
