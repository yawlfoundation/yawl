/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.update;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationFileHandler;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Michael Adams
 * @date 27/05/2014
 */
public class UpdateDialog extends JDialog
        implements ActionListener, PropertyChangeListener {

    private JProgressBar _progressBar;
    private JButton _btnDownload;
    private JButton _btnDownloadAndRestart;
    private UpdateDownloader _downloader;
    private boolean _restarting;
    private long _fileSize;
    private String _fileSizeString;

    protected static final String ZIP_FILE = "YAWLEditor3.0beta.zip";


    public UpdateDialog() { super(YAWLEditor.getInstance()); }

    public UpdateDialog(UpdateChecker checker) {
        this();
        setTitle("Update Information");
        setModal(true);
        setContentPane(addContent(checker));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(YAWLEditor.getInstance());
        pack();
    }


    // buttons on this dialog
    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Cancel")) {
            if (! (_downloader == null || _downloader.isDone())) {
                _downloader.cancel(true);
            }
            setVisible(false);
        }
        else if (action.equals("Download")) {
            _restarting = false;
            File path = getUserDownloadToPath();
            if (path != null) download(path);                    // null = user cancel
        }
        else if (action.equals("Download & Restart")) {
            _restarting = true;
            download(null);                                      // null = use tmp dir
        }
    }


    // events from UpdateDownloader process
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("state")) {
            SwingWorker.StateValue stateValue = (SwingWorker.StateValue) event.getNewValue();
            if (stateValue == SwingWorker.StateValue.STARTED) {
                _progressBar.setMaximum(100);
                _progressBar.setValue(0);
                _progressBar.setIndeterminate(false);
            }
            else if (stateValue == SwingWorker.StateValue.DONE) {
                setVisible(false);
                if (! _downloader.isCancelled()) {
                    YAWLEditor.getStatusBar().setText(
                            "Latest version downloaded to " + _downloader.getFileTo());
                    if (_restarting) restart();
                }
            }
        }
        else if (event.getPropertyName().equals("progress")) {
            int progress = (Integer) event.getNewValue();
            _progressBar.setValue(progress);
            _progressBar.setString(getProgressString(progress));
        }
    }

    private JPanel addContent(UpdateChecker checker) {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.add(new JLabel("A new version of the YAWL Editor is available:"),
                BorderLayout.NORTH);
        panel.add(getInfoPanel(checker), BorderLayout.CENTER);
        panel.add(getButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }


    private JPanel getInfoPanel(UpdateChecker checker) {
        JPanel panel = new JPanel(new GridLayout(0,1,5,5));
        panel.setBorder(new EmptyBorder(0,10,0,10));
        panel.add(new JLabel(getCurrentInfo(checker)));
        panel.add(new JLabel(getLatestInfo(checker)));
        panel.add(getProgressBar());
        return panel;
    }


    private JPanel getButtonPanel() {
        JPanel panel = new JPanel();
        panel.add(createButton("Cancel"));
        _btnDownload = createButton("Download");
        panel.add(_btnDownload);
        _btnDownloadAndRestart = createButton("Download & Restart");
        panel.add(_btnDownloadAndRestart);
        return panel;
    }


    protected JButton createButton(String caption) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(130,25));
        btn.addActionListener(this);
        return btn;
    }


    private JProgressBar getProgressBar() {
        _progressBar = new JProgressBar(0, 100);
        _progressBar.setValue(0);
        _progressBar.setStringPainted(true);
        _progressBar.setVisible(false);
        return _progressBar;
    }

    private String getCurrentInfo(UpdateChecker checker) {
        StringBuilder s = new StringBuilder();
        s.append("Current Version: ").append(checker.getCurrentVersion())
                .append(" (build ").append(checker.getCurrentBuildNumber()).append(')');
        return s.toString();
    }


    private String getLatestInfo(UpdateChecker checker) {
        StringBuilder s = new StringBuilder();
        s.append("New Version: ").append(checker.getLatestVersion())
                .append(" (build ").append(checker.getLatestBuildNumber()).append(')');
        return s.toString();
    }


    private void enableButtons(boolean enable) {
        _btnDownload.setEnabled(enable);
        _btnDownloadAndRestart.setEnabled(enable);
    }


    private void download(File saveToDir) {
        enableButtons(false);
        _progressBar.setVisible(true);
        _progressBar.setIndeterminate(true);

        if (saveToDir == null) saveToDir = getTmpDir();
        File toFile = new File(saveToDir, ZIP_FILE);
        _downloader = new UpdateDownloader(UpdateChecker.SOURCE_URL + ZIP_FILE, toFile);
        _downloader.addPropertyChangeListener(this);
        _downloader.execute();
    }


    private File getUserDownloadToPath() {
        String path = UserSettings.getLastDownloadedToPath();
        if (path == null) path = System.getProperty("user.home");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(path));
        chooser.setDialogTitle("Select Destination Folder for Download");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Download");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            UserSettings.setLastDownloadedToPath(selected.getAbsolutePath());
            return selected;
        }
        return null;
    }


    private File getTmpDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


    private String getProgressString(int progress) {
        StringBuilder s = new StringBuilder();
        s.append(getDownloadRatio(progress)).append(" (").append(progress).append("%)");
        return s.toString();
    }


    private String getDownloadRatio(int progress) {
        return shorten(_fileSize * progress / 100) + "/" + getTotalFileSize();
    }


    private String getTotalFileSize() {
        if (_fileSizeString == null) {
            _fileSize = _downloader.getFileSize();
            _fileSizeString = shorten(_fileSize);
        }
        return _fileSizeString;
    }


    private String shorten(long size) {
        if (size < 1024) return size + "b";
        else if (size < 1024 * 1024) return getScaledSize(size / 1024.0, 0) + "Kb";
        else return getScaledSize(size / (1024.0 * 1024.0), 2) + "Mb";
    }


    private BigDecimal getScaledSize(double size, int scale) {
        return BigDecimal.valueOf(size).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }


    private void restart() {
        try {
            if (new SpecificationFileHandler().closeFileOnExit()) {
                replaceApp();
                restartApp();
            }
        }
        catch (Exception e) {
            showError(e.getMessage());
            setVisible(false);
        }
    }


    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                "Update error: " + message,
                "Update Error", JOptionPane.ERROR_MESSAGE);
    }


    private void replaceApp() throws URISyntaxException, IOException {
        File editorDir = getJarFile().getParentFile();
        removeCurrentVersion(editorDir);
        unzip(_downloader.getFileTo(), editorDir);
    }


    private void restartApp() throws URISyntaxException, IOException {
        String javaBin = System.getProperty("java.home") + File.separator +
                "bin" + File.separator + "java";

        java.util.List<String> command = new ArrayList<String>();
        command.add(javaBin);
        command.add("-jar");
        command.add(getJarFile().getPath());
        command.add("-updated");

        System.out.println(command);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        System.exit(0);
    }


    private File getJarFile() throws URISyntaxException {
        return new File(this.getClass().getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
    }


    private void removeCurrentVersion(File dir) {
        if (dir != null) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) removeCurrentVersion(file);
                    file.delete();
                }
            }
        }
    }


    private void unzip(File zipFile, File targetDir) throws IOException {
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            if (! ze.isDirectory()) {
                File f = new File(targetDir, ze.getName());
                f.getParentFile().mkdirs();                // create subdirs as required
                FileOutputStream fos = new FileOutputStream(f);
                int len;
                byte buffer[] = new byte[1024];
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            ze = zis.getNextEntry();
        }
        zis.close();
    }

}
