package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.DownloadWorker;
import org.yawlfoundation.yawl.controlpanel.update.Downloader;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * @author Joerg Evermann
 * @author Michael Adams
 * @date 3/11/2015
 */
public class CliDownloader extends Downloader {

    private CliProgressPanel _progressPanel;


    public CliDownloader(String urlBase, String urlSuffix, List<String> fileNames,
                                long totalBytes, File targetDir) {
        super(urlBase, urlSuffix, fileNames, totalBytes, targetDir);
        _workerMap = new HashMap<DownloadWorker, Integer>();
    }


    public void setProgressPanel(CliProgressPanel panel) {
        _progressPanel = panel;
    }


    public void download() {
        _progressPanel.setText("Downloading " + _totalBytes + " bytes...");
        boolean cancelled = false;
        for (String fileName : _fileNames) {
            CliDownloadWorker worker = new CliDownloadWorker(_urlBase, _urlSuffix,
                     fileName, _totalBytes, _targetDir);
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


    public boolean cancel() {
        for (DownloadWorker worker : _workerMap.keySet()) {
            CliDownloadWorker cliWorker = (CliDownloadWorker) worker;
            if (cliWorker.isAlive()) cliWorker.setCancelled();
        }
        return true;
    }


    private boolean isComplete() {
        for (DownloadWorker worker : _workerMap.keySet()) {
            if (((CliDownloadWorker) worker).isAlive()) return false;
        }
        return true;
    }


    private int getTotalProgress() {
        int progress = 0;
        for (DownloadWorker worker : _workerMap.keySet()) {
            progress += ((CliDownloadWorker) worker).getProgressValue();
        }
        return progress;
    }

}
