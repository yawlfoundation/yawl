/*
 * Created on 18/03/2004
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.yawlfoundation.yawl.editor.ui.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.net.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.editor.ui.actions.specification.OpenRecentSubMenu;
import org.yawlfoundation.yawl.editor.ui.engine.SpecificationReader;
import org.yawlfoundation.yawl.editor.ui.engine.SpecificationWriter;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.ui.swing.YStatusBar;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.io.File;

public class SpecificationFileHandler {

    private static final String EXTENSION = ".yawl";
    private YStatusBar statusBar;

    public SpecificationFileHandler() {
        statusBar = YAWLEditor.getStatusBar();
    }


    public void processOpenRequest() {
        loadFromFile(promptForLoadFileName());
    }


    public void processOpenRequest(String fileName) {
        loadFromFile(fileName);
    }


    /**
     *  Processes a user's request to save an open specification.
     *  This might include prompting for a file name if one has not yet been
     *  set for the specification.
     *  @return true if the specification was saved, false if the user cancelled the save.
     */

    public boolean processSaveRequest() {
        String fileName = SpecificationModel.getHandler().getFileName();
        if (StringUtil.isNullOrEmpty(fileName) || ! fileName.endsWith(".yawl")) {
            if (! promptForAndSetSaveFileName()) {
                return false;
            }
        }
        saveSpecification();
        return true;
    }


    /**
     *  Processes a user's request to save an open specification.
     *  This might include prompting for a file name if one has not yet been
     *  set for the specification.
     */
    public void processSaveAsRequest() {
        if (promptForAndSetSaveFileName()) {
            saveSpecification();
        }
    }


    /**
     *  Processes a user's request to close an open specification.
     *  This might include prompting for a file name and saving
     *  the specification before closing it.
     */
    public void processCloseRequest() {
        statusBar.setText("Closing Specification...");
        handleUserResponse();
    }


    /**
     *  Processes a user's request to exit the application.
     *  This might include prompting for a file name and saving
     *  the specification before closing it.
     */
    public void processExitRequest() {
        statusBar.setText("Exiting YAWLEditor...");
        boolean okToExit = true;
        if (Publisher.getInstance().getFileState() != FileState.Closed) {
            okToExit = handleUserResponse();
        }
        if (okToExit) {
            System.exit(0);
        }
    }


    /****************************************************************************/

    private boolean handleUserResponse() {
        if (SpecificationUndoManager.getInstance().isDirty()) {
            int response = getSaveOnCloseConfirmation();
            if (response == JOptionPane.CANCEL_OPTION) {
                statusBar.setTextToPrevious();
                return false;
            }
            if (response == JOptionPane.YES_OPTION) {
                return saveWhilstClosing();
            }
        }
        closeWithoutSaving();     // NO_OPTION
        return true;
    }


    private File getSuggestedFileName() {
        String fileName = SpecificationModel.getHandler().getFileName();
        if (StringUtil.isNullOrEmpty(fileName)) {
            fileName = SpecificationModel.getHandler().getID().getUri() + ".yawl";
        }
        return new File(fileName.substring(0, fileName.lastIndexOf(".")) + ".yawl");
    }


    private boolean promptForAndSetSaveFileName() {
        JFileChooser chooser = FileChooserFactory.build(EXTENSION, "YAWL Specification",
                        "Save specification to ", " file");
        chooser.setSelectedFile(getSuggestedFileName());

        if (JFileChooser.CANCEL_OPTION == chooser.showDialog(YAWLEditor.getInstance(), "Save")) {
            return false;
        }

        File file = chooser.getSelectedFile();
        if (! file.getName().endsWith(".yawl")) {
            file = new File(file.getName() + ".yawl");
        }

        String fileName = SpecificationModel.getHandler().getFileName();
        if (file.exists() &&  (! StringUtil.isNullOrEmpty(fileName)) &&
                ! getFullFileName(file).equals(fileName)) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(YAWLEditor.getInstance(),
                    "You have chosen an existing specification file.\n" +
                    "If you save to this file, you will overwrite the file's contents.\n\n" +
                    "Are you sure you want to save your specification to this file?\n",
                    "Existing Specification File Selected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)) {
                return false;
            }
        }
        SpecificationModel.getHandler().setFileName(getFullFileName(file));
 //       SpecificationModel.getHandler().setUniqueID();
        return true;
    }



    private void saveSpecification() {
        saveSpecification(SpecificationModel.getInstance());
    }

    private void saveSpecification(SpecificationModel specification) {

        // if the net has configuration preview on, turn it off temporarily
        ProcessConfigurationModel.PreviewState previewState =
                ProcessConfigurationModel.getInstance().getPreviewState();
        if (previewState != ProcessConfigurationModel.PreviewState.OFF) {
            PreviewConfigurationProcessAction.getInstance().actionPerformed(null);
        }

        String fullFileName = SpecificationModel.getHandler().getFileName();
        if (StringUtil.isNullOrEmpty(fullFileName)) {
            return;
        }

        try {
            saveToFile(specification);
            OpenRecentSubMenu.getInstance().addRecentFile(fullFileName);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    YAWLEditor.getInstance(),
                    "Error discovered whilst writing YAWL Editor save file.\n Save has not been performed.\n",
                    "Editor File Saving Error",
                    JOptionPane.ERROR_MESSAGE
            );
            LogWriter.error("Error discovered whilst saving specification", e);
        }

        // put preview state back if necessary
        if (previewState != ProcessConfigurationModel.PreviewState.OFF) {
            PreviewConfigurationProcessAction.getInstance().actionPerformed(null);
        }
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
        Publisher publisher = Publisher.getInstance();
        publisher.publishCloseFileEvent();
        if (publisher.getSpecificationState() != SpecificationState.NoNetsExist) {
            publisher.publishState(SpecificationState.NoNetSelected);
        }
    }

    private void doPostSaveClosingWork() {
        YAWLEditor.getNetsPane().closeAllNets();
        SpecificationModel.getInstance().reset();
        ProcessConfigurationModel.getInstance().reset();
        SpecificationUndoManager.getInstance().discardAllEdits();
        YAWLEditor.getNetsPane().setVisible(true);
    }

    private boolean saveWhilstClosing() {
        String fileName = SpecificationModel.getHandler().getFileName();
        if (StringUtil.isNullOrEmpty(fileName) || ! fileName.endsWith(".yawl")) {
            if (! promptForAndSetSaveFileName()) {
                return false;
            }
        }

        doPreSaveClosingWork();
        saveSpecification();
        doPostSaveClosingWork();

        return true;
    }

    private void closeWithoutSaving() {
        doPreSaveClosingWork();
        doPostSaveClosingWork();
    }


    private String getFullFileName(File file) {
        if (file == null) return "";
        String fullFileName = file.getAbsolutePath();
        if (! fullFileName.toLowerCase().endsWith(EXTENSION)) {
            fullFileName += EXTENSION;
        }
        return fullFileName;
    }


    private void saveToFile(SpecificationModel specificationModel) {
        String fileName = SpecificationModel.getHandler().getFileName();
        if (StringUtil.isNullOrEmpty(fileName)) {

            // rollback version number if auto-incrementing
            if (UserSettings.getAutoIncrementVersionOnSave()) {
                SpecificationModel.getHandler().getVersion().minorRollback();
            }
            return;     // user-cancelled save or no file name selected
        }

        statusBar.setText("Saving Specification...");
        statusBar.progressOverSeconds(2);

        if (SpecificationWriter.checkAndExportEngineSpecToFile(
                specificationModel, fileName)) {
            SpecificationUndoManager.getInstance().setDirty(false);
            statusBar.setText("Saved to file: " + fileName);
        }
        else statusBar.setTextToPrevious();

        statusBar.resetProgress();
    }


    private void loadFromFile(String fullFileName) {
        if (fullFileName == null) return;
        statusBar.setText("Opening Specification...");
        statusBar.progressOverSeconds(4);
        YAWLEditor.getNetsPane().setVisible(false);

        SpecificationReader importer = new SpecificationReader();
        importer.load(fullFileName);

        YAWLEditor.getNetsPane().setVisible(true);
        statusBar.resetProgress();
        OpenRecentSubMenu.getInstance().addRecentFile(fullFileName);
    }


    private String promptForLoadFileName() {
        JFileChooser chooser = FileChooserFactory.build(EXTENSION,
                "YAWL Engine Specification", "Open specification from ", " file");

        if (JFileChooser.CANCEL_OPTION == chooser.showDialog(YAWLEditor.getInstance(), "Open")) {
            return null;
        }

        File file = chooser.getSelectedFile();

        // check for odd dirs on non dos os's
        return file.isFile() ? getFullFileName(file) : null;
    }


}
