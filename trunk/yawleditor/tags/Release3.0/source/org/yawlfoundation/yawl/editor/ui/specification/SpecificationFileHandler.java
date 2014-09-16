/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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
package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.specification.OpenRecentSubMenu;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.editor.ui.specification.io.SpecificationReader;
import org.yawlfoundation.yawl.editor.ui.specification.io.SpecificationWriter;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.ui.swing.YStatusBar;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class SpecificationFileHandler {

    private final YStatusBar _statusBar;
    private final YSpecificationHandler _handler;

    private static final String EXTENSION = ".yawl";


    public SpecificationFileHandler() {
        _statusBar = YAWLEditor.getStatusBar();
        _handler = SpecificationModel.getHandler();
    }


    /**
     * Prompts the user for and opens a specification file
     */
    public void openFile() {
        openFile(promptForLoadFileName());
    }


    /**
     * Opens a specification file
     * @param fileName the name of the file to open
     */
    public void openFile(String fileName) {
        loadFromFile(fileName);
    }


    /**
     *  Saves the currently open specification. This might include prompting for a file
     *  name if one has not yet been set for the specification.
     *  @return true if the specification was saved, false if the user cancelled the save.
     */
    public boolean saveFile() {
        String fileName = getFileName();
        if (StringUtil.isNullOrEmpty(fileName) || ! fileName.endsWith(EXTENSION)) {
            fileName = promptForSaveFileName();
            if (fileName == null) {
                return false;
            }
        }
        saveSpecification(fileName);
        return true;
    }


    /**
     *  Saves the currently open specification to a new file.
     */
    public void saveFileAs() {
        String fileName = promptForSaveFileName();
        if (fileName != null) {
            saveSpecification(fileName);
        }
    }


    /**
     *  Processes a user's request to close an open specification.
     *  This might include prompting for a file name and saving
     *  the specification before closing it.
     */
    public void closeFile() {
        _statusBar.setText("Closing Specification...");
        handleCloseResponse();
    }


    /**
     *  Processes a user's request to exit the application.
     *  This might include prompting for a file name and saving
     *  the specification before closing it.
     */
    public boolean closeFileOnExit() {
        _statusBar.setText("Exiting YAWL Editor...");
        YConnector.disconnect();
        return Publisher.getInstance().isFileStateClosed() || handleCloseResponse();
    }


    /****************************************************************************/

    private String getFileName() { return _handler.getFileName(); }


    /**
     * Asks user if they want to save the open specification before exiting
     * @return true if ok to close, false if user cancelled exiting
     */
    private boolean handleCloseResponse() {

        // only prompt to save if there have been changes
        if (SpecificationUndoManager.getInstance().isDirty()) {
            int response = getSaveOnCloseConfirmation();
            if (response == JOptionPane.CANCEL_OPTION) {         // user cancelled exit
                _statusBar.setTextToPrevious();
                return false;
            }
            if (response == JOptionPane.YES_OPTION) {           // save then exit
                return saveWhilstClosing();
            }
        }
        closeWithoutSaving();        // NO_OPTION               // exit and don't save
        return true;
    }


    /**
     * Constructs a suggested name for a new file, from the last used path and the
     * specification URI
     * @return the suggested file name to save the specification to
     */
    private File getSuggestedFileName() {
        String fileName = _handler.getFileName();
        if (fileName != null) {
            return new File(fileName);
        }

        // no existing filename, so build one from last known path and spec uri
        String path = UserSettings.getLastSaveOrLoadPath();
        if (path == null) {
            path = System.getProperty("user.home");
        }
        else if (! path.endsWith(File.separator)) {
            path = path.substring(0, path.lastIndexOf(File.separator));
        }
        return new File(path, _handler.getID().getUri() + EXTENSION);
    }


    private String promptForSaveFileName() {
        JFileChooser dialog = FileChooserFactory.build(EXTENSION, "YAWL Specification",
                        "Save specification");

        dialog.setSelectedFile(getSuggestedFileName());
        int response = dialog.showDialog(YAWLEditor.getInstance(), "Save");
        if (response == JFileChooser.CANCEL_OPTION) {
            return null;
        }

        // make sure the selected file name has the correct '.yawl' extension
        File file = dialog.getSelectedFile();
        if (! file.getName().endsWith(EXTENSION)) {
            file = new File(file.getName() + EXTENSION);
        }

        if (matchesExistingFile(file)) {
            response = JOptionPane.showConfirmDialog(YAWLEditor.getInstance(),
                    "You have selected an existing specification file.\n" +
                    "Are you sure you want to overwrite the existing file?",
                    "Existing File Selected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.NO_OPTION) return null;
        }
        return file.getAbsolutePath();
    }


    private boolean matchesExistingFile(File file) {
        return file.exists() && ! file.getAbsolutePath().equals(getFileName());
    }


    private void saveSpecification(String fileName) {
        if (StringUtil.isNullOrEmpty(fileName)) return;

        YPluginHandler.getInstance().preSaveFile();
        saveToFile(fileName);
        YPluginHandler.getInstance().postSaveFile();
    }


    private int getSaveOnCloseConfirmation() {
        return JOptionPane.showConfirmDialog(
                YAWLEditor.getInstance(),
                "Do you wish to save your changes before closing?   \n\n"
                        + "\t\t\t'Yes' to save the specification,\n"
                        + "\t\t\t'No' to discard unsaved changes,\n"
                        + "\t\t\t'Cancel' to continue editing.\n\n",
                "Save changes?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

    private void doPreSaveClosingWork() {
        YAWLEditor.getNetsPane().setVisible(false);
        YAWLEditor.getPropertySheet().setIgnoreRepaint(true);
        Publisher.getInstance().publishCloseFileEvent();
    }

    private void doPostSaveClosingWork() {
        YAWLEditor.getNetsPane().closeAllNets();
        SpecificationModel.reset();
        YPluginHandler.getInstance().specificationClosed();
        SpecificationUndoManager.getInstance().discardAllEdits();
        YAWLEditor.getNetsPane().setVisible(true);
    }

    private boolean saveWhilstClosing() {
        String fileName = getFileName();
        if (StringUtil.isNullOrEmpty(fileName) || ! fileName.endsWith(EXTENSION)) {
            fileName = promptForSaveFileName();
            if (fileName == null) {
                return false;
            }
        }

        doPreSaveClosingWork();
        saveSpecification(fileName);
        doPostSaveClosingWork();

        return true;
    }

    private void closeWithoutSaving() {
        doPreSaveClosingWork();
        doPostSaveClosingWork();
    }


    private void saveToFile(String fileName) {
        if (StringUtil.isNullOrEmpty(fileName)) {

            // rollback version number if auto-incrementing
            if (UserSettings.getAutoIncrementVersionOnSave()) {
                _handler.getVersion().minorRollback();
            }
            return;     // user-cancelled save or no file name selected
        }

        _statusBar.setText("Saving Specification...");
        _statusBar.progressOverSeconds(2);

        // SpecificationWriter is a SwingWorker
        SpecificationWriter writer = new SpecificationWriter(fileName);
        writer.addPropertyChangeListener(new SaveCompletionListener(fileName));
        writer.execute();
    }


    private void loadFromFile(String fullFileName) {
        if (fullFileName == null) return;
        _statusBar.setText("Opening Specification...");
        _statusBar.progressOverSeconds(4);
        YAWLEditor.getNetsPane().setVisible(false);

        // SpecificationReader is a SwingWorker
        SpecificationReader reader = new SpecificationReader(fullFileName);
        reader.addPropertyChangeListener(new LoadCompletionListener(fullFileName));
        reader.execute();
    }


    private String promptForLoadFileName() {
        JFileChooser chooser = FileChooserFactory.build(EXTENSION,
                "YAWL Specification", "Open specification");
        int response = chooser.showDialog(YAWLEditor.getInstance(), "Open");
        if (response == JFileChooser.CANCEL_OPTION) return null;

        File file = chooser.getSelectedFile();

        // check for odd dirs on non dos os's
        return file.isFile() ? file.getAbsolutePath() : null;
    }

    /***************************************************************************/

    // listens for reader swing worker completion & does final cleanups
    class LoadCompletionListener implements PropertyChangeListener {

        String fullFileName;

        LoadCompletionListener(String fileName) { fullFileName = fileName; }

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getNewValue() == SwingWorker.StateValue.DONE) {
                YAWLEditor.getNetsPane().setVisible(true);
                _statusBar.resetProgress();
                OpenRecentSubMenu.getInstance().addRecentFile(fullFileName);
                YPluginHandler.getInstance().specificationLoaded();
            }
        }
    }


    // listens for writer swing worker completion & does final cleanups
    class SaveCompletionListener implements PropertyChangeListener {

        String fullFileName;

        SaveCompletionListener(String fileName) { fullFileName = fileName; }

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getNewValue() == SwingWorker.StateValue.DONE) {
                SpecificationWriter writer = (SpecificationWriter) event.getSource();
                if (writer.successful()) {
                    SpecificationUndoManager.getInstance().setDirty(false);
                    _statusBar.setText("Saved to file: " + fullFileName);
                    OpenRecentSubMenu.getInstance().addRecentFile(fullFileName);
                    YAWLEditor.getInstance().setTitle(fullFileName);
                 }
                 else _statusBar.setTextToPrevious();

                 _statusBar.resetProgress();
            }
        }
    }


}
