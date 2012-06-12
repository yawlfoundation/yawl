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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.ui.engine.EngineSpecificationHandler;
import org.yawlfoundation.yawl.editor.ui.actions.net.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.editor.ui.actions.specification.OpenRecentSubMenu;

import javax.swing.*;
import java.io.File;
import java.util.UUID;

public class SpecificationArchiveHandler {

    private static final String SAVE_SPECIFICATION_FILE_TYPE = "yawl";
    private static final String DESCRIPTION = "YAWL Specification";

    private static final JFileChooser SAVE_FILE_CHOOSER =
            FileChooserFactory.buildFileChooser(
                    SAVE_SPECIFICATION_FILE_TYPE,
                    DESCRIPTION,
                    "Save specification to ",
                    " file",
                    FileChooserFactory.SAVING_AND_LOADING
            );


    private transient static final SpecificationArchiveHandler INSTANCE
            = new SpecificationArchiveHandler();

    public static SpecificationArchiveHandler getInstance() {
        return INSTANCE;
    }

    private SpecificationArchiveHandler() {}

    /**
     *  Processes a user's request to save an open specification.
     *  This might include prompting for a file name if one has not yet beem
     *  set for the specification.
     *  @return true if the specification was saved, false if the user cancelled the save.
     */

    public boolean processSaveRequest() {
        String fileName = SpecificationModel.getInstance().getFileName();
        if (fileName.equals("") || ! fileName.endsWith(".yawl")) {

            if (!promptForAndSetSaveFileName()) {
                return false;
            }
        }
        saveUpdatingGUI();
        return true;
    }

    private File getSuggestedFileName() {
        String fileName = SpecificationModel.getInstance().getFileName();
        if (fileName.equals("")) {
            fileName = SpecificationModel.getInstance().getId() + ".yawl";
        }
        return new File(fileName.substring(0, fileName.lastIndexOf(".")) + ".yawl");
    }

    private boolean promptForAndSetSaveFileName() {

        SAVE_FILE_CHOOSER.setSelectedFile(getSuggestedFileName());

        if (JFileChooser.CANCEL_OPTION == SAVE_FILE_CHOOSER.showSaveDialog(YAWLEditor.getInstance())) {
            return false;
        }

        File file = SAVE_FILE_CHOOSER.getSelectedFile();
        if (! file.getName().endsWith(".yawl")) {
            file = new File(file.getName() + ".yawl");
        }

        if (file.exists() &&
                ! getFullNameFromFile(file, SAVE_SPECIFICATION_FILE_TYPE).equals(
                        SpecificationModel.getInstance().getFileName())) {
            if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(YAWLEditor.getInstance(),
                    "You have chosen an existing specification file.\n" +
                            "If you save to this file, you will overwrite the file's contents.\n\n" +
                            "Are you absolutely certain you want to save your specification to this file?\n",
                    "Existing Specification File Selected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE)) {
                return false;
            }
        }
        SpecificationModel.getInstance().setFileName(
                getFullNameFromFile(file, SAVE_SPECIFICATION_FILE_TYPE)
        );

        SpecificationModel.getInstance().setUniqueID("UID_" + UUID.randomUUID().toString());
        SpecificationModel.getInstance().rationaliseUniqueIdentifiers();
        return true;
    }

    /**
     *  Processes a user's request to save an open specification.
     *  This might include prompting for a file name if one has not yet beem
     *  set for the specification.
     */

    public void processSaveAsRequest() {
        if (promptForAndSetSaveFileName()) {
            saveUpdatingGUI();
        }
    }

    public void saveUpdatingGUI() {
        saveUpdatingGUI(
                SpecificationModel.getInstance()
        );
    }

    public void saveUpdatingGUI(SpecificationModel specification) {

        // if the net has configuration preview on, turn it off temporarily
        ProcessConfigurationModel.PreviewState previewState =
                ProcessConfigurationModel.getInstance().getPreviewState();
        if (previewState != ProcessConfigurationModel.PreviewState.OFF) {
            PreviewConfigurationProcessAction.getInstance().actionPerformed(null);
        }

        String fullFileName = specification.getFileName();
        if (fullFileName.trim().equals("")) {
            return;
        }

        try {
            EngineSpecificationHandler.getInstance().engineFormatFileExport(specification);
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


    /**
     *  Processes a user's request to close an open specification.
     *  This might include prompting for a file name and saving
     *  the specification before closing it.
     */

    public void processCloseRequest() {
        YAWLEditor.setStatusBarText("Closing Specification...");
        if (SpecificationFileModel.getInstance().getFileCount() == 0) {
            return;
        }
        if (SpecificationUndoManager.getInstance().isDirty()) {
            int response = getSaveOnCloseConfirmation();
            if (response == JOptionPane.CANCEL_OPTION) {
                YAWLEditor.setStatusBarTextToPrevious();
                return;
            }
            if (response == JOptionPane.YES_OPTION) {
                saveWhilstClosing();
            }
            else {
                closeWithoutSaving();
            }
        }
        else closeWithoutSaving();
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
        YAWLEditorDesktop.getInstance().setVisible(false);
        SpecificationFileModel.getInstance().decrementFileCount();
        SpecificationModel.getInstance().nothingSelected();
    }

    private void doPostSaveClosingWork() {
        YAWLEditorDesktop.getInstance().closeAllNets();
        SpecificationModel.getInstance().reset();
        ProcessConfigurationModel.getInstance().reset();
        SpecificationUndoManager.getInstance().discardAllEdits();
        YAWLEditorDesktop.getInstance().setVisible(true);
    }

    private boolean saveWhilstClosing() {
        if (SpecificationModel.getInstance().getFileName().equals("") ||
                ! SpecificationModel.getInstance().getFileName().endsWith(".ywl")) {
            if (!promptForAndSetSaveFileName()) {
                return false;
            }
        }

        doPreSaveClosingWork();
        saveUpdatingGUI();
        doPostSaveClosingWork();

        return true;
    }

    public void closeWithoutSaving() {
        doPreSaveClosingWork();
        doPostSaveClosingWork();
    }


    /**
     *  Processes a user's request to exit the application.
     *  This might include prompting for a file name and saving
     *  the specification before closing it.
     */

    public void processExitRequest() {
        YAWLEditor.setStatusBarText("Exiting YAWLEditor...");

        boolean saveNotCancelled = true;

        if (SpecificationFileModel.getInstance().getFileCount() > 0) {
            if (SpecificationUndoManager.getInstance().isDirty()) {
                int response = getSaveOnCloseConfirmation();
                if (response == JOptionPane.CANCEL_OPTION) {
                    YAWLEditor.setStatusBarTextToPrevious();
                    return;
                }

                if (response == JOptionPane.YES_OPTION) {
                    saveNotCancelled = saveWhilstClosing();
                } else {
                    closeWithoutSaving();
                }
            }
            else closeWithoutSaving();
        }

        if (saveNotCancelled) {
            System.exit(0);
        }
    }



    private String getFullNameFromFile(File file, String type) {
        if (file == null) {
            return "";
        }
        String fullFileName = file.getAbsolutePath();
        if (!fullFileName.toLowerCase().endsWith(type)) {
            fullFileName += "." + type;
        }
        return fullFileName;
    }
}
