package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateRow;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.controlpanel.util.WebXmlReader;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 19/08/2014
 */
public class Updater implements PropertyChangeListener, EngineStatusListener {

    private final UpdateDialog _updateDialog;
    private final ProgressPanel _progressPanel;
    private final Differ _differ;
    private final List<AppUpdate> _installs;
    private Downloader _downloader;
    private List<String> _downloads;
    private List<String> _deletions;
    private boolean _updatingThis;
    private boolean _cycled;


    public Updater(UpdateDialog updateDialog) {
        _updateDialog = updateDialog;
        _progressPanel = updateDialog.getProgressPanel();
        UpdateTableModel model = (UpdateTableModel) updateDialog.getTable().getModel();
        _differ = model.getDiffer();
        _installs = getInstallList(model.getRows());
    }


    public void start() {
        List<AppUpdate> updates = getUpdatesList();
        updates.addAll(_installs);
        if (! updates.isEmpty()) {
            _progressPanel.setVisible(true);
            _downloads = getDownloadList(updates);
            _deletions = getDeletionList(updates);
            if (! _downloads.isEmpty()) {
                download(updates);            // download and verify updated/new files
            }
            else if (! _deletions.isEmpty()) {
                stopEngine();                 // stop engine then do deletions
            }
        }
    }


    // events from Downloader process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.STARTED) {
                _progressPanel.setIndeterminate(false);
            }
            else if (stateValue == SwingWorker.StateValue.DONE) {
                if (event.getSource() instanceof Verifier) {
                    verifyCompleted(((Verifier) event.getSource()));
                }
                else {
                    processDownloadCompleted();
                }
             }
        }
        else if (event.getPropertyName().equals("progress")) {
            int progress = (Integer) event.getNewValue();
            _progressPanel.update(progress);
        }
    }


    // 4. Wait for STOP -> copy,  or START -> complete
    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped : doUpdates(); break;
            case Running : complete(true); break;
        }
    }


    // 1. Start the downloads -> wait for finish at propertyChange
    private void download(List<AppUpdate> updates) {
        long downloadSize = getDownloadSize(updates);
        _progressPanel.setDownloadSize(downloadSize);
        _downloader = new Downloader(UpdateChecker.SOURCE_URL,
                UpdateChecker.SF_DOWNLOAD_SUFFIX, _downloads,
                downloadSize, FileUtil.getTmpDir());
        _downloader.addPropertyChangeListener(this);
        _downloader.execute();
    }


    private List<String> getDownloadList(List<AppUpdate> updates) {
        consolidateLibs(updates);
        List<String> fileNames = new ArrayList<String>();
        for (AppUpdate list : updates) {
            if (list.isControlPanelApp()) {
                _updatingThis = true;
            }
            fileNames.addAll(list.getDownloadNames());
        }
        return fileNames;
    }


    private List<String> getDeletionList(List<AppUpdate> updates) {
        List<String> fileNames = new ArrayList<String>();
        for (AppUpdate list : updates) {
             fileNames.addAll(list.getDeletionNames());
        }
        return fileNames;
    }


    private List<AppUpdate> getInstallList(List<UpdateRow> updateRows) {
        List<AppUpdate> upList = new ArrayList<AppUpdate>();
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
        List<AppUpdate> updatesList = getUpdatesList();
        if (updatesList == null) return null;
        Map<String,String> map = new HashMap<String, String>();
        for (AppUpdate list : updatesList) {
             map.putAll(list.getMd5Map());
        }
        return map;
    }


    private List<AppUpdate> getUpdatesList() {
        try {
            return _differ.getUpdatesList();
        }
        catch (IllegalStateException ise) {
            showError(ise.getMessage());
            return Collections.emptyList();
        }
    }

    private long getDownloadSize(List<AppUpdate> updates) {
       long fileSizes = 0;
        for (AppUpdate list : updates) {
             fileSizes += list.getTotalUpdateSize();
        }
        return fileSizes;
    }


    private List<AppUpdate> consolidateLibs(List<AppUpdate> updates) {
        List<AppUpdate> appList = new ArrayList<AppUpdate>();
        AppUpdate libAppUpdate = new AppUpdate(null);
        for (AppUpdate upList : updates) {
            if (upList.isAppList()) {
                appList.add(upList);
            }
            else {
                libAppUpdate.merge(upList);
            }
        }
        if (! libAppUpdate.isEmpty()) appList.add(libAppUpdate);
        return appList;
    }


    // 2. VERIFY, complete if fails, -> stopEngine
    private void processDownloadCompleted() {
        if (_downloader.hasErrors()) {
            StringBuilder s = new StringBuilder();
            s.append("Failed to download updates.\n");
            for (String error : _downloader.getErrors()) {
                s.append('\t').append(error).append('\n');
            }
            showError(s.toString());
            complete(false);
        }
        else {
            verify();
        }
    }


    private void complete(boolean success) {
        if (success) {
            File checkSum = getLocalCheckSumFile();
            _updateDialog.refresh(new Differ(checkSum, checkSum));
            updateServiceRegistration();
            if (_updatingThis) restartApp();
        }
        Publisher.removeEngineStatusListener(this);
        _progressPanel.setVisible(false);
    }


    private void verify() {
        _progressPanel.setIndeterminate(true);
        _progressPanel.setText("Verifying downloads...");
        Verifier verifier = new Verifier(getMd5Map());
        verifier.addPropertyChangeListener(this);
        verifier.execute();
    }


    private void verifyCompleted(Verifier verifier) {
        try {
            if (verifier.get()) {
                if (onlyUpdatingControlPanel()) {
                    doUpdates();
                }
                else {
                    stopEngine();
                }
                return;
            }
        }
        catch (Exception fallThrough) {
            //
        }
        showError("Downloaded files failed verification.");
        complete(false);
    }


    // 3. STOP ENGINE -> wait until stopped at statusChanged event
    private void stopEngine() {
        Publisher.addEngineStatusListener(this);
        if (Publisher.getCurrentStatus() == EngineStatus.Running) {
            _cycled = true;
            try {
                _progressPanel.setIndeterminate(true);
                _progressPanel.setText("Stopping Engine...");
                if (TomcatUtil.stop()) Publisher.announceStoppingStatus();
                else {
                    showError("Failed to stop Engine.");
                    complete(false);
                }
            }
            catch (IOException ioe) {
                showError("Failed to stop Engine.");
                complete(false);
            }
        }
        else doUpdates();
    }


    // 6. START -> wait at statusChange
    private void startEngine() {
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException ignore) {
           //
        }
        try {
            _progressPanel.setText((_cycled ? "Res" : "S") + "tarting Engine...");
            if (TomcatUtil.start()) Publisher.announceStartingStatus();
            else showError("Failed to restart Engine.");
        }
        catch (Exception whatever) {
            showError("Problem starting Engine.");
        }
    }


    private void showError(String message) {
        _progressPanel.setVisible(false);
        JOptionPane.showMessageDialog(_progressPanel.getParent(),
                "Update error: " + message,
                "Update Error", JOptionPane.ERROR_MESSAGE);
    }


    private void doUpdates() {
        _progressPanel.setIndeterminate(true);
        _progressPanel.setText("Updating files...");
        File tomcatDir = new File(TomcatUtil.getCatalinaHome());
        doUpdates(tomcatDir);
        doDeletions(tomcatDir);
        doUninstalls(tomcatDir);
        consolidateLibDir(tomcatDir);
        if (onlyUpdatingControlPanel()) {
            complete(true);
        }
        else startEngine();
    }


    // 5. COPY in new files -> startEngine
    private File doUpdates(File tomcatDir)  {
        File tmpDir = FileUtil.getTmpDir();
        for (String fileName : _downloads) {
            if (! isControlPanelFileName(fileName)) {            // do CP last
                File source = FileUtil.makeFile(tmpDir.getAbsolutePath(), fileName);
                File target = getCopyTarget(tomcatDir, fileName);
                copy(source, target);
            }
        }

        // copy downloaded checksums.xml to lib
        File source = new File(tmpDir, UpdateChecker.CHECKSUM_FILE);
        File target = getLocalCheckSumFile();
        copy(source, target);

        return tomcatDir;
    }


    private void copy(File source, File target) {
        try {
            FileUtil.copy(source, target);
        }
        catch (IOException ioe) {
            showError("Failed to copy file: " + target.getName() +
                    "\n" + ioe.getMessage());
            complete(false);
        }
    }


    private File getCopyTarget(File tomcatDir, String fileName) {
        String targetName = fileName;
        File targetRoot = tomcatDir;
        if (fileName.startsWith("lib")) {
            targetName = "yawl" + fileName;
        }
        else if (isControlPanelFileName(fileName)) {
            targetRoot = tomcatDir.getParentFile().getParentFile();    // up two dirs
        }
        return FileUtil.makeFile(targetRoot.getAbsolutePath(), targetName);
    }


    private boolean isControlPanelFileName(String fileName) {
        return fileName.startsWith("controlpanel");
    }


    private boolean onlyUpdatingControlPanel() {
        return _downloads.size() == 1 && isControlPanelFileName(_downloads.get(0));
    }


    private boolean updateThis() {
        File tmpDir = FileUtil.getTmpDir();
        File tomcatDir = new File(TomcatUtil.getCatalinaHome());
        for (String fileName : _downloads) {
            if (isControlPanelFileName(fileName)) {
                File source = FileUtil.makeFile(tmpDir.getAbsolutePath(), fileName);
                File target = getCopyTarget(tomcatDir, fileName);
                copy(source, target);
                return true;
            }
        }
        return false;
    }


    private File getLocalCheckSumFile() {
        return new File(FileUtil.buildPath(TomcatUtil.getCatalinaHome(), "yawllib",
                UpdateChecker.CHECKSUM_FILE));
    }


    private void doDeletions(File tomcatRoot) {
        for (String fileName : _deletions) {
            FileUtil.delete(tomcatRoot, fileName);
        }
    }

    private void doUninstalls(File tomcatRoot) {
        List<String> webappsToRemove = new ArrayList<String>();
        for (AppUpdate appUpdate : _installs) {
            if (appUpdate.hasDeletions() && appUpdate.isAppList()) {
                webappsToRemove.add(appUpdate.getAppName());
            }
        }
        for (String name : webappsToRemove) {
            String path = FileUtil.buildPath(tomcatRoot.getAbsolutePath(), "webapps", name);
            FileUtil.purgeDir(new File(path));
        }
    }


    private void updateServiceRegistration() {
        Registrar registrar = new Registrar();
        for (AppUpdate appUpdate : _installs) {
            if (appUpdate.isServiceApp()) {
                boolean success;
                String appName = appUpdate.getAppName();
                if (appUpdate.hasDeletions()) {
                    try {
                        success = registrar.remove(appName);
                    }
                    catch (IOException ioe) {
                        success = false;
                    }
                    if (! success) {
                        showError("Service '" + appName + "' has been successfully " +
                                "removed, but\n" +
                                "failed to deregister from the YAWL Engine.");
                    }
                }
                else if (appUpdate.hasDownloads()) {
                    WebXmlReader webXml = new WebXmlReader(appName);
                    String password = webXml.getContextParam("EngineLogonPassword");
                    String mapping = webXml.getIBServletMapping();
                    if (password != null) {
                        String url = mapping == null ? null :
                                "http://localhost:8080/" + appName + mapping;
                        try {
                            success = registrar.add(appName, password, url);
                        } catch (IOException ioe) {
                            success = false;
                        }
                    }
                    else success = false;

                    if (! success) {
                        showError("Service '" + appName + "' has been successfully " +
                                "added, but\n" +
                                "failed to register with the YAWL Engine.");
                    }
                }
            }
        }
        registrar.disconnect();
    }


    private void consolidateLibDir(File tomcatRoot) {
        Set<String> requiredLibs = _differ.getRequiredLibNames();
        for (String lib : _differ.getInstalledLibNames()) {
            if (lib.startsWith("h2-")) continue;          // h2 db driver is also needed
            if (!requiredLibs.contains(lib)) {
                FileUtil.delete(tomcatRoot, FileUtil.buildPath("yawllib", lib));
            }
        }
    }


    private void restartApp() {
        if (! updateThis()) {
            JOptionPane.showMessageDialog(_progressPanel.getParent(),
                    "The update has completed successfully, but the control panel " +
                    "was unable to restart itself automatically. Please close then " +
                    "restart the control panel manually to use the updated version.",
                    "Update Completed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String javaBin = System.getProperty("java.home") + File.separator +
                "bin" + File.separator + "java";
        try {
            String appPath = FileUtil.getJarFile().getPath();
            if (! new File(appPath).exists()) {
                appPath = getPathForNewAppName(appPath);
                if (appPath == null) {
                    throw new IllegalArgumentException();
                }
            }

            java.util.List<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(FileUtil.getJarFile().getPath());

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        }
        catch (Exception e) {
            showError("Failed to restart the Control Panel. Please restart manually.");
        }
    }


    private String getPathForNewAppName(String appPath) {
        return appPath.substring(0, appPath.lastIndexOf(File.separatorChar) + 1) +
                _differ.getNewJarName();
    }


}
