package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.AppUpdate;
import org.yawlfoundation.yawl.controlpanel.update.UpdateChecker;
import org.yawlfoundation.yawl.controlpanel.update.Updater;
import org.yawlfoundation.yawl.controlpanel.update.Verifier;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Joerg Evermann
 * @author Michael Adams
 * @date 3/11/2015
 */
public class CliUpdater extends Updater {

    private final CliProgressPanel _progressPanel;
    private CliDownloader _downloader;


    public CliUpdater(UpdateTableModel model) {
        _progressPanel = new CliProgressPanel();
        _differ = model.getDiffer();
        _installs = getInstallList(model.getRows());
    }

    public void update() {
        List<AppUpdate> updates = getUpdatesList();
        updates.addAll(_installs);
        if (! updates.isEmpty()) {
            _downloads = getDownloadList(updates);
            _deletions = getDeletionList(updates);
            if (! _downloads.isEmpty()) {
                download(updates);            // download and verify updated/new files
                if (processDownloadCompleted()) {
                    stopEngine();
                } else {
                    showError("Downloaded files failed verification.");
                }
            }
            else if (! _deletions.isEmpty()) {
                stopEngine();                 // stop engine then do deletions
            }
        }
    }


    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped : {
                doUpdates();
                Publisher.removeEngineStatusListener(this);
                break;
            }
        }
    }


    private void doUpdates() {
        _progressPanel.setText("Updating files...");
        File tomcatDir = new File(TomcatUtil.getCatalinaHome());
        doUpdates(tomcatDir);
        doDeletions(tomcatDir);
        doUninstalls(tomcatDir);
        consolidateLibDir(tomcatDir);
    }


    private void download(List<AppUpdate> updates) {
        long downloadSize = getDownloadSize(updates);
        _progressPanel.setDownloadSize(downloadSize);
        _downloader = new CliDownloader(UpdateChecker.SOURCE_URL,
                UpdateChecker.SF_DOWNLOAD_SUFFIX, _downloads,
                downloadSize, FileUtil.getTmpDir());
        _downloader.setProgressPanel(_progressPanel);
        _downloader.download();
    }


    private boolean processDownloadCompleted() {
        if (_downloader.hasErrors()) {
            StringBuilder s = new StringBuilder();
            s.append("Failed to download updates.\n");
            for (String error : _downloader.getErrors()) {
                s.append('\t').append(error).append('\n');
            }
            showError(s.toString());
            return false;
        }
        else {
            return verify();
        }
    }


    private boolean verify() {
        _progressPanel.setText("Verifying downloads...");
        Verifier verifier = new Verifier(getMd5Map());
        return verifier.verify();
    }


    private void stopEngine() {
        Publisher.addEngineStatusListener(this);
        if (Publisher.getCurrentStatus() == EngineStatus.Running) {
            _progressPanel.setText("Stopping Engine...");
            if ( ! TomcatUtil.stop()) {
                showError("Failed to stop Engine.");
                Publisher.removeEngineStatusListener(this);
            }
        }
    }


    protected void showError(String message) {
        _progressPanel.setText("Update error: " + message);
    }


    protected void copy(File source, File target) {
        try {
            FileUtil.copy(source, target);
        }
        catch (IOException ioe) {
            showError("Failed to copy file: " + target.getName() +
                    "\n" + ioe.getMessage());
            Publisher.removeEngineStatusListener(this);
        }
    }

}
