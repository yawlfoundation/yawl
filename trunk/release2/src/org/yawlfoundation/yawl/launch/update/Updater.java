package org.yawlfoundation.yawl.launch.update;

import org.yawlfoundation.yawl.launch.pubsub.EngineStatus;
import org.yawlfoundation.yawl.launch.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.launch.pubsub.Publisher;
import org.yawlfoundation.yawl.launch.update.table.UpdateRow;
import org.yawlfoundation.yawl.launch.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.launch.util.TomcatUtil;
import org.yawlfoundation.yawl.util.CheckSummer;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * @author Michael Adams
 * @date 19/08/2014
 */
public class Updater implements PropertyChangeListener, EngineStatusListener {

    private ProgressDialog _progressDialog;
    private Differ _differ;
    private Downloader _downloader;
    private List<String> _downloads;
    private List<String> _deletions;
    private List<UpdateList> _installs;


    public Updater(UpdateDialog updateDialog) {
        UpdateTableModel model = (UpdateTableModel) updateDialog.getTable().getModel();
        _differ = model.getDiffer();
        _installs = getInstallList(model.getRows());
        _progressDialog = new ProgressDialog(updateDialog);
    }


    public void start() {
        _progressDialog.setVisible(true);
        download();
    }


    // events from Downloader process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.STARTED) {
                _progressDialog.setIndeterminate(false);
            }
            else if (stateValue == SwingWorker.StateValue.DONE) {
                processDownloadCompleted();
             }
        }
        else if (event.getPropertyName().equals("progress")) {
            int progress = (Integer) event.getNewValue();
            _progressDialog.update(progress);
        }
    }


    // 4. Wait for STOP -> copy,  or START -> complete
    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped : copy(); break;
            case Running : complete(true); break;
        }
    }


    // 1. Start the downloads -> wait for finish at propertyChange
    private void download() {
        List<UpdateList> updates = _differ.getUpdatesList();
        _downloads = getDownloadList(updates);
        _deletions = getDeletionList(updates);
        _downloader = new Downloader(UpdateChecker.SOURCE_URL,
                UpdateChecker.SF_DOWNLOAD_SUFFIX, _downloads,
                getDownloadSize(updates), getTmpDir());
        _downloader.addPropertyChangeListener(this);
        _downloader.execute();
    }


    private List<String> getDownloadList(List<UpdateList> updates) {
        List<String> fileNames = new ArrayList<String>();
        for (UpdateList list : updates) {
             fileNames.addAll(list.getDownloadNames());
        }
        return fileNames;
    }


    private List<String> getDeletionList(List<UpdateList> updates) {
        List<String> fileNames = new ArrayList<String>();
        for (UpdateList list : updates) {
             fileNames.addAll(list.getDeletionNames());
        }
        return fileNames;
    }


    private List<UpdateList> getInstallList(List<UpdateRow> updateRows) {
        List<UpdateList> upList = new ArrayList<UpdateList>();
        for (UpdateRow row : updateRows) {
            String appName = row.getName();
            if (row.isAdding()) {
                upList.add(_differ.getAppFiles(appName, true));
                upList.add(_differ.getAppLibs(appName));
            }
            else if (row.isRemoving()) {
                upList.add(_differ.getAppFiles(appName, false));
            }
        }
        return upList;
    }


    private Map<String,String> getMd5Map() {
        Map<String,String> map = new HashMap<String, String>();
        for (UpdateList list : _differ.getUpdatesList()) {
             map.putAll(list.getMd5Map());
        }
        return map;
    }


    private long getDownloadSize(List<UpdateList> updates) {
       long fileSizes = 0;
        for (UpdateList list : updates) {
             fileSizes += list.getTotalUpdateSize();
        }
        return fileSizes;
    }


    // 2. VERIFY, complete if fails, -> stopEngine
    private void processDownloadCompleted() {
        if (_downloader.hasErrors()) {
            showError("Failed to download updates.");
            complete(false);
            return;
        }
        if (!verify()) {
            showError("Downloaded files failed verification.");
            complete(false);
            return;
        }
        Publisher.addEngineStatusListener(this);
        stopEngine();
    }


    private void complete(boolean success) {
        if (success) {
            File checkSum = getLocalCheckSumFile();
            ((UpdateDialog) _progressDialog.getParent()).refresh(
                    new Differ(checkSum, checkSum));
        }
        Publisher.removeEngineStatusListener(this);
        _progressDialog.setVisible(false);
    }


    private boolean verify() {
        _progressDialog.setText("Verifying downloads...");
        File targetDir = _downloader.getTargetDir();
        CheckSummer summer = new CheckSummer();
        Map<String, String> md5Map = getMd5Map();
        for (String file : md5Map.keySet()) {
            String fileName = targetDir.getAbsolutePath() + File.separator + file;
            try {
                if (! summer.compare(fileName, md5Map.get(file))) {
                    return false;
                }
            }
            catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }


    private File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


    // 3. STOP ENGINE -> wait until stopped at statusChanged event
    private void stopEngine() {
        if (Publisher.getCurrentStatus() == EngineStatus.Running) {
            try {
                _progressDialog.setIndeterminate(true);
                _progressDialog.setText("Stopping Engine...");
                TomcatUtil.stop();
            }
            catch (IOException ioe) {
                showError("Falied to stop Engine.");
                complete(false);
            }
        }
        else statusChanged(EngineStatus.Stopped);
    }


    // 6. START -> wait at statusChange
    private void startEngine() {
        try {
            _progressDialog.setText("Starting Engine...");
            TomcatUtil.start();
        }
        catch (IOException ignore) {
            showError("Failed to restart Engine.");
        }
    }


    private void showError(String message) {
        JOptionPane.showMessageDialog(_progressDialog.getParent(),
                "Update error: " + message,
                "Update Error", JOptionPane.ERROR_MESSAGE);
    }


    // 5. COPY in new files -> startEngine
    private void copy()  {
        _progressDialog.setText("Updating files...");
        File tomcatDir = new File(TomcatUtil.getCatalinaHome());
        File tmpDir = getTmpDir();
        for (String fileName : _downloads) {
            File source = makeFile(tmpDir.getAbsolutePath(), fileName);
            File target = makeFile(tomcatDir.getAbsolutePath(), fileName);
            copy(source, target);
        }
        for (String fileName : _deletions) {
            delete(tomcatDir, fileName);
        }

        // copy downloaded checksums.xml to lib
        File source = new File(tmpDir, UpdateChecker.CHECKSUM_FILE);
        File target = makeFile(tomcatDir.getAbsolutePath() + "/yawllib",
                UpdateChecker.CHECKSUM_FILE);
        copy(source, target);

        doUninstalls(tomcatDir);
        startEngine();
    }


    private File getLocalCheckSumFile() {
        return new File(TomcatUtil.getCatalinaHome() + "/yawllib/" +
                UpdateChecker.CHECKSUM_FILE);
    }


    private File makeFile(String path, String fileName) {
        if (fileName.contains("/")) {
            int sepPos = fileName.lastIndexOf('/');
            String dirPart = fileName.substring(0, sepPos);
            String filePart = fileName.substring(sepPos + 1);
            File dir = new File(path + File.separator + dirPart);
            dir.mkdirs();
            return new File(dir, filePart);
        }
        return new File(path, fileName);
    }


    private void copy(File sourceFile, File targetFile) {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            targetChannel = new FileOutputStream(targetFile).getChannel();
            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
        catch (IOException ioe) {
            showError("Failed to copy file: " + sourceFile.getName());
            complete(false);
        }
        finally {
            try {
                if (sourceChannel != null) sourceChannel.close();
                if (targetChannel != null) targetChannel.close();
            }
            catch (IOException ignore) {
                //
            }
        }
    }


    private void doUninstalls(File tomcatRoot) {
        List<String> webappsToRemove = new ArrayList<String>();
        for (UpdateList updateList : _installs) {
            if (updateList.hasDeletions()) {
                webappsToRemove.add(updateList.getAppName());
            }
        }
        for (String name : webappsToRemove) {
            purgeDir(makeFile(tomcatRoot.getAbsolutePath(), "webapps/" + name));
        }
        consolidateLibDir(tomcatRoot);
    }


    private void consolidateLibDir(File tomcatRoot) {
        Set<String> requiredLibs = _differ.getRequiredLibNames();
        for (String lib : _differ.getInstalledLibNames()) {
            if (requiredLibs.contains(lib)) {
                delete(tomcatRoot, lib);
            }
        }
    }


    private void delete(File root, String fileName) {
        File toDelete = makeFile(root.getAbsolutePath(), fileName);
        if (toDelete.exists()) toDelete.delete();
    }


    private void purgeDir(File dir) {
        if (dir != null) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) purgeDir(file);
                    file.delete();
                }
            }
        }
    }





}
