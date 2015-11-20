package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateRow;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.controlpanel.util.WebXmlReader;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 19/11/2015
 */
public class Updater implements PropertyChangeListener, EngineStatusListener {

    protected enum State {
        Download, Verify, StopEngine, Update, StartEngine, Complete, Finalise
    }

    private UpdateDialog _updateDialog;
    private ProgressPanel _progressPanel;
    private Downloader _downloader;
    private boolean _cycled;                            // was engine running at init?

    protected Differ _differ;
    protected List<AppUpdate> _installs;
    protected List<String> _downloads;
    protected List<String> _deletions;
    protected State _state;
    protected boolean _updatingThis;                      // updating control panel?


    protected Updater() { }


    protected Updater(UpdateTableModel model) {
        _differ = model.getDiffer();
        _installs = getInstallList(model.getRows());
    }


    public Updater(UpdateDialog updateDialog) {
        this((UpdateTableModel) updateDialog.getTable().getModel());
        _updateDialog = updateDialog;
        _progressPanel = updateDialog.getProgressPanel();
    }


    // entry point - start the update process
    public void start() {
        Publisher.addEngineStatusListener(this);
        setState(State.Download);
    }


    // events from Downloader & Verifier processes
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            Object stateValue = event.getNewValue();
            if (stateValue == SwingWorker.StateValue.STARTED) {
                getProgressPanel().setIndeterminate(false);
            }
            else if (stateValue == SwingWorker.StateValue.DONE) {
                if (event.getSource() instanceof Verifier) {
                    verifyCompleted(((Verifier) event.getSource()));
                }
                else {
                    downloadCompleted();
                }
             }
        }
        else if (event.getPropertyName().equals("progress")) {
            int progress = (Integer) event.getNewValue();
            getProgressPanel().update(progress);
        }
    }


    // events from Publisher on engine status
    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped : if (_state == State.StopEngine) {
                setState(State.Update);
            } break;
            case Running : if (_state == State.StartEngine) {
                setState(State.Complete);
            } break;
        }
    }


    // coordinating method - sets current state and takes action on state change
    protected void setState(State state) {
        _state = state;
        switch (_state) {
            case Download: download(); break;
            case Verify: verify(); break;
            case StopEngine: stopEngine(); break;
            case Update: update(); break;
            case StartEngine: startEngine(); break;
            case Complete: complete(); break;
            case Finalise: finalise(); break;
        }
    }


    protected void download() {
        List<AppUpdate> updates = getUpdatesList();
        updates.addAll(_installs);
        if (! updates.isEmpty()) {
            _downloads = getDownloadList(updates);
            _deletions = getDeletionList(updates);
            if (! _downloads.isEmpty()) {
                getProgressPanel().setText("Downloading files...");
                download(updates);            // download & verify updated/new files
            }
            else if (! _deletions.isEmpty()) {
                setState(State.StopEngine);    // skip download & verify
            }
        }
        else setState(State.Finalise);
    }


    // Starts the verify swing worker
    protected void verify() {
        getProgressPanel().setIndeterminate(true);
        getProgressPanel().setText("Verifying downloads...");
        Verifier verifier = new Verifier(getMd5Map());
        verifier.addPropertyChangeListener(this);
        verifier.execute();
    }


    private void stopEngine() {
        if (Publisher.getCurrentStatus() == EngineStatus.Running) {
            _cycled = true;
            getProgressPanel().setIndeterminate(true);
            getProgressPanel().setText("Stopping Engine...");
            if (! TomcatUtil.stop()) {
                showError("Failed to stop Engine. Update could not complete.");
                setState(State.Finalise);
            }
        }
        else setState(State.Update);
    }


    private void update() {
        getProgressPanel().setIndeterminate(true);
        getProgressPanel().setText("Updating files...");
        File tomcatDir = new File(TomcatUtil.getCatalinaHome());
        doUpdates(tomcatDir);
        doDeletions(tomcatDir);
        doUninstalls(tomcatDir);
        consolidateLibDir(tomcatDir);
        setState(onlyUpdatingControlPanel() ? State.Complete : State.StartEngine);
    }


    private void startEngine() {
        getProgressPanel().setText((_cycled ? "Res" : "S") + "tarting Engine...");
        try {
            TomcatUtil.start();
        }
        catch (Exception e) {
            showError("Problem starting Engine: " + e.getMessage());
            setState(State.Finalise);
        }
    }


    // Update process completed - do cleanup
    private void complete() {
        File checkSum = getLocalCheckSumFile();
        if (_updateDialog != null) {
            _updateDialog.refresh(new Differ(checkSum, checkSum));
        }
        updateServiceRegistration();
        new UserPreferences().setPostUpdatesCompleted(false);
        if (_updatingThis) {
            restartApp();
        }
        else setState(State.Finalise);
    }


    protected void finalise() {
        Publisher.removeEngineStatusListener(this);
        getProgressPanel().setVisible(false);
    }


    protected List<String> getDownloadList(List<AppUpdate> updates) {
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


    protected List<String> getDeletionList(List<AppUpdate> updates) {
        List<String> fileNames = new ArrayList<String>();
        for (AppUpdate list : updates) {
             fileNames.addAll(list.getDeletionNames());
        }
        return fileNames;
    }


    protected List<AppUpdate> getInstallList(List<UpdateRow> updateRows) {
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


    protected Map<String,String> getMd5Map() {
        List<AppUpdate> updatesList = getUpdatesList();
        if (updatesList == null) return null;
        Map<String,String> map = new HashMap<String, String>();
        for (AppUpdate list : updatesList) {
             map.putAll(list.getMd5Map());
        }
        return map;
    }


    protected List<AppUpdate> getUpdatesList() {
        try {
            return _differ.getUpdatesList();
        }
        catch (IllegalStateException ise) {
            showError(ise.getMessage());
            return Collections.emptyList();
        }
    }


    protected long getDownloadSize(List<AppUpdate> updates) {
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


    // Starts the download swing worker
    protected void download(List<AppUpdate> updates) {
        long downloadSize = getDownloadSize(updates);
        getProgressPanel().setDownloadSize(downloadSize);
        _downloader = new Downloader(UpdateChecker.SOURCE_URL,
                UpdateChecker.SF_DOWNLOAD_SUFFIX, _downloads,
                downloadSize, FileUtil.getTmpDir());
        _downloader.addPropertyChangeListener(this);
        _downloader.execute();
    }


    // Downloads have completed - move to next state
    protected void downloadCompleted() {
        if (getDownloader().hasErrors()) {
            StringBuilder s = new StringBuilder();
            s.append("Failed to download updates.\n");
            for (String error : getDownloader().getErrors()) {
                s.append('\t').append(error).append('\n');
            }
            showError(s.toString());
            setState(State.Finalise);
        }
        else {
            setState(State.Verify);
        }
    }


    // Verification has completed - move to next state
    private void verifyCompleted(Verifier verifier) {
        boolean success = false;
        try {
            success = verifier.get();           // return true if verify successful
        }
        catch (Exception fallThrough) {
            //
        }

        if (success) {

            // can update CP only without cycling the engine
            setState(onlyUpdatingControlPanel() ? State.Update : State.StopEngine);
        }
        else {
            showError("Downloaded files failed verification. Update could not complete.");
            setState(State.Finalise);
        }
    }


    // for overriders
    protected ProgressPanel getProgressPanel() { return _progressPanel; }

    protected Downloader getDownloader() { return _downloader; }


    protected void showError(String message) {
        getProgressPanel().setVisible(false);
        JOptionPane.showMessageDialog(getProgressPanel().getParent(),
                "Update error: " + message,
                "Update Error", JOptionPane.ERROR_MESSAGE);
    }



    // 5. COPY in new files -> startEngine
    protected File doUpdates(File tomcatDir)  {
        File tmpDir = FileUtil.getTmpDir();
        int port = TomcatUtil.getTomcatServerPort();
        for (String fileName : _downloads) {
            if (! isControlPanelFileName(fileName)) {            // do CP last
                File source = FileUtil.makeFile(tmpDir.getAbsolutePath(), fileName);
                File target = getCopyTarget(tomcatDir, fileName);
                copy(source, target);
                if (port != 8080 && fileName.endsWith("web.xml")) {
                    updatePort(target, port);
                }
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
            setState(State.Finalise);
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


    protected boolean onlyUpdatingControlPanel() {
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


    protected void doDeletions(File tomcatRoot) {
        for (String fileName : _deletions) {
            FileUtil.delete(tomcatRoot, fileName);
        }
    }

    protected void doUninstalls(File tomcatRoot) {
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


    protected void consolidateLibDir(File tomcatRoot) {
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
            JOptionPane.showMessageDialog(getProgressPanel().getParent(),
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
            String newJarName = _differ.getNewJarName();
            if (! (newJarName == null || appPath.endsWith(newJarName))) {
                appPath = getPathForNewAppName(appPath);
                if (appPath == null) {
                    throw new IllegalArgumentException();
                }
            }

            java.util.List<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(FileUtil.getJarFile().getPath());
            command.add("-updateCompleted");

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        }
        catch (Exception e) {
            showError("Failed to restart the Control Panel. Please restart manually.");
        }
    }


    private String getPathForNewAppName(String appPath) {
        return appPath == null ? null : getPathPart(appPath) + _differ.getNewJarName();
    }


    private String getPathPart(String appPath) {
        return appPath == null ? null :
                appPath.substring(0, appPath.lastIndexOf(File.separatorChar) + 1);
    }


    // update tomcat port in a newly downloaded web.xml if current port is not the default
    private void updatePort(File webxml, int port) {
        if (port != 8080) {
            StringUtil.replaceInFile(webxml, ":8080", ":" + port);
        }
    }

}
