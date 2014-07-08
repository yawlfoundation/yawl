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

import org.yawlfoundation.yawl.editor.core.util.FileUtil;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;

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
    private VersionDiffer _differ;
    private boolean _restarting;
    private long _fileSize;
    private String _fileSizeString;


    private UpdateDialog() { super(YAWLEditor.getInstance()); }

    public UpdateDialog(VersionDiffer differ) {
        this();
        setTitle("Update Information");
        setModal(true);
        _differ = differ;
        setContentPane(addContent());
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
                            "Latest version downloaded to " + _downloader.getTmpDir());
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

    private JPanel addContent() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.add(new JLabel("A new version of the YAWL Editor is available:"),
                BorderLayout.NORTH);
        panel.add(getInfoPanel(), BorderLayout.CENTER);
        panel.add(getButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }


    private JPanel getInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(0,1,5,5));
        panel.setBorder(new EmptyBorder(0,10,0,10));
        panel.add(new JLabel(_differ.getCurrentInfo()));
        panel.add(new JLabel(_differ.getLatestInfo()));
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


    private void enableButtons(boolean enable) {
        _btnDownload.setEnabled(enable);
        _btnDownloadAndRestart.setEnabled(enable);
    }


    private void download(File saveToDir) {
        enableButtons(false);
        _progressBar.setVisible(true);
        _progressBar.setIndeterminate(true);

        if (saveToDir == null) saveToDir = getTmpDir();
        _downloader = new UpdateDownloader(UpdateChecker.SOURCE_URL,
                UpdateChecker.SF_DOWNLOAD_SUFFIX, _differ.getDownloadList(),
                _differ.getDownloadSize(), saveToDir);
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
            _fileSize = _differ.getDownloadSize();
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
        File tmpDir = getTmpDir();
        for (String fileName : _differ.getDownloadList()) {
            File source = makeFile(tmpDir.getAbsolutePath(), fileName);
            File target = makeFile(editorDir.getAbsolutePath(), fileName);
            FileUtil.copy(source, target);
        }
        for (String fileName : _differ.getDeleteList()) {
            File toDelete = makeFile(editorDir.getAbsolutePath(), fileName);
            if (toDelete.exists()) toDelete.delete();
        }

        // copy downloaded checksums.xml to lib
        File source = new File(tmpDir, UpdateChecker.CHECKSUM_FILE);
        File target = makeFile(editorDir.getAbsolutePath() + "/lib",
                UpdateChecker.CHECKSUM_FILE);
        FileUtil.copy(source, target);
    }


    private File makeFile(String path, String fileName) {
        if (fileName.contains(File.separator)) {
            int sepPos = fileName.lastIndexOf(File.separator);
            String dirPart = fileName.substring(0, sepPos);
            String filePart = fileName.substring(sepPos + 1);
            File dir = new File(path + File.separator + dirPart);
            dir.mkdirs();
            return new File(dir, filePart);
        }
        return new File(path, fileName);
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

}
