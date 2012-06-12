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
 */

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.specification.OpenRecentSubMenu;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;

import javax.swing.*;
import java.io.File;
import java.util.prefs.Preferences;

public class EngineSpecificationHandler {
  
  
  private static final String SPECIFICATION_FILE_TYPE = "yawl,xml";
  
  private static final JFileChooser EXPORT_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        "YAWL Engine Specification",
        "Export specification to engine ",
        " format",
        FileChooserFactory.IMPORTING_AND_EXPORTING
    );

  private static final JFileChooser OPEN_FILE_CHOOSER =
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        "YAWL Engine Specification",
        "Open specification from ",
        " file",
        FileChooserFactory.IMPORTING_AND_EXPORTING
    );

  private transient static final EngineSpecificationHandler INSTANCE 
    = new EngineSpecificationHandler();

  public static EngineSpecificationHandler getInstance() {
    return INSTANCE; 
  }

  private EngineSpecificationHandler() {}
  
  
  public void validate(SpecificationModel editorSpec) {
    YAWLEditor.getInstance().showProblemList("Specification Validation Problems",
        EngineSpecificationValidator.getValidationResults(editorSpec)
    );
  }

  
  // As "import" is a java keyword, I've had to call the import method something
  // a little more verbose than necessary. As the export is the mirror method to
  // the import, that too has been renamed to match.

  public void engineFormatFileExport(SpecificationModel editorSpec) {
    String fileName = editorSpec.getFileName();
    if (fileName == null) fileName = promptForSaveFileName();
    saveSpecificationToFile(editorSpec, fileName);
  }

  public void engineFormatFileImport() {
    importEngineSpecificationFile(promptForLoadFileName());
  }

  public void engineFormatFileImport(String fileName) {
      importEngineSpecificationFile(fileName);
  }

  private void importEngineSpecificationFile(String fullFileName) {
    if (fullFileName == null) return;  
    YAWLEditor.setStatusBarText("Opening Specification...");
    YAWLEditor.progressStatusBarOverSeconds(4);
    YAWLEditorDesktop.getInstance().setVisible(false);

    SpecificationImporter.importSpecificationFromFile(
            SpecificationModel.getInstance(),
            fullFileName
    );

    YAWLEditorDesktop.getInstance().setVisible(true);
    YAWLEditor.resetStatusBarProgress();
    OpenRecentSubMenu.getInstance().addRecentFile(fullFileName);              
  }
  
  private String promptForLoadFileName() {

    if (JFileChooser.CANCEL_OPTION == 
        OPEN_FILE_CHOOSER.showOpenDialog(YAWLEditor.getInstance())) {
      return null;
    }

    File file = OPEN_FILE_CHOOSER.getSelectedFile();
    if (file.isFile())                              // check for odd dirs on non dos os's
        return getFullNameFromFile(file);
    else
        return null;
  }

  
  private String promptForSaveFileName() {

    if (JFileChooser.CANCEL_OPTION == 
        EXPORT_FILE_CHOOSER.showSaveDialog(YAWLEditor.getInstance())) {
      return null;
    }

    File file = EXPORT_FILE_CHOOSER.getSelectedFile();

    if (file.exists() && 
        !getFullNameFromFile(file).equals(SpecificationModel.getInstance().getEngineFileName())) {
      if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(YAWLEditor.getInstance(),
              "You have chosen an existing engine specification file.\n" +
              "If you save to this file, you will overwrite the file's contents.\n\n" +
              "Are you absolutely certain you want to export your specification to this file?\n",
              "Existing Specification File Selected",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE)) {
        return null;   
      }
    }
	return getFullNameFromFile(file);
  }

  public void saveSpecificationToFile(SpecificationModel editorSpec, String fullFileName) {
    if (fullFileName == null || fullFileName.equals("")) {

      // rollback version number if auto-incrementing
      boolean autoinc = Preferences.userNodeForPackage(YAWLEditor.class).getBoolean(
              EngineSpecificationExporter.AUTO_INCREMENT_VERSION_WITH_EXPORT_PREFERENCE,
              false);
      if (autoinc) {
          editorSpec.getVersionNumber().minorRollback();
      }

      return;     // user-cancelled save or no file name selected
    }

    YAWLEditor.setStatusBarText("Saving Specification...");
    YAWLEditor.progressStatusBarOverSeconds(2);

    if (EngineSpecificationExporter.checkAndExportEngineSpecToFile(
            editorSpec, fullFileName)) {
        SpecificationUndoManager.getInstance().setDirty(false);
    }

    YAWLEditor.setStatusBarTextToPrevious();
    YAWLEditor.resetStatusBarProgress();
  }
  
  private String getFullNameFromFile(File file) {
    if (file == null) {
      return "";
    }
    String fullFileName = file.getAbsolutePath();
    String [] extns = SPECIFICATION_FILE_TYPE.split(",");
    for (String extn : extns) {
      if (fullFileName.toLowerCase().endsWith("." + extn)) {
         return fullFileName;                                   // ok
      }
    }
    return fullFileName += ".yawl";
    }

}

