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

package au.edu.qut.yawl.editor.thirdparty.engine;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.FileChooserFactory;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;
import au.edu.qut.yawl.editor.swing.specification.ProblemMessagePanel;

import java.io.File;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import au.edu.qut.yawl.editor.YAWLEditor;

public class EngineSpecificationHandler {
  
  private EngineSpecificationExporter exporter = new EngineSpecificationExporter();
  private EngineSpecificationImporter importer = new EngineSpecificationImporter();
  
  private static final String SPECIFICATION_FILE_TYPE = "xml";
  
  private static final JFileChooser EXPORT_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        "YAWL Engine Specification",
        "Export specification to engine ",
        " format",
        FileChooserFactory.IMPORTING_AND_EXPORTING
    );

  private static final JFileChooser IMPORT_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        "YAWL Engine Specification",
        "Import specification from engine ",
        " format",
        FileChooserFactory.IMPORTING_AND_EXPORTING
    );

  private transient static final EngineSpecificationHandler INSTANCE 
    = new EngineSpecificationHandler();

  public static EngineSpecificationHandler getInstance() {
    return INSTANCE; 
  }

  private EngineSpecificationHandler() {}
  
  
  public void validate() {

    YAWLEditor.setStatusBarText("Validating Specification...");
    YAWLEditor.progressStatusBarOverSeconds(2);
    
    try {
      ProblemMessagePanel.getInstance().setProblemList(
          "Specification Validation Problems",
          EngineSpecificationValidator.getValidationResults()
      );
      YAWLEditor.setStatusBarTextToPrevious();
      YAWLEditor.resetStatusBarProgress();

    } catch (Exception e) {
      
      LinkedList<String> stackMessageList = new LinkedList<String>();
      stackMessageList.add(e.getMessage());
      
      ProblemMessagePanel.getInstance().setProblemList(
          "Programming Exception with Specification Validation",
          stackMessageList
      );
      
//      e.printStackTrace();
      YAWLEditor.setStatusBarTextToPrevious();
      YAWLEditor.resetStatusBarProgress();
    }
  }

  
  // As "import" is a java keyword, I've had to call the import method something
  // a little more verbose than necessary. As the export is the mirror method to
  // the import, that too has been renamed to match.

  public void engineFormatFileExport() {
    promptForAndSetSaveFileName();
    saveSpecificationToFile(SpecificationModel.getInstance().getEngineFileName());
  }

  public void engineFormatFileImport() {
    importEngineSpecificationFile(promptForLoadFileName());
  }
  
  private void importEngineSpecificationFile(String fullFileName) {
    YAWLEditor.setStatusBarText("Importing Engine Specification...");
    YAWLEditor.progressStatusBarOverSeconds(2);
    YAWLEditorDesktop.getInstance().setVisible(false);

    importer.importEngineSpecificationFromFile(fullFileName);

    YAWLEditorDesktop.getInstance().setVisible(true);
    YAWLEditor.resetStatusBarProgress();
  }
  
  private String promptForLoadFileName() {

    if (JFileChooser.CANCEL_OPTION == 
        IMPORT_FILE_CHOOSER.showOpenDialog(YAWLEditor.getInstance())) {
      return null;
    }

    File file = IMPORT_FILE_CHOOSER.getSelectedFile();

    return getFullNameFromFile(file);
  }

  
  private void promptForAndSetSaveFileName() {

    if (JFileChooser.CANCEL_OPTION == 
        EXPORT_FILE_CHOOSER.showSaveDialog(YAWLEditor.getInstance())) {
      return;
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
        return;   
      }
    }
	SpecificationModel.getInstance().setEngineFileName(getFullNameFromFile(file));
  }

  public void saveSpecificationToFile(String fullFileName) {
    if (fullFileName.equals("")) {
      return;
    }

    YAWLEditor.setStatusBarText("Exporting Engine Specification...");
    YAWLEditor.progressStatusBarOverSeconds(2);

    exporter.exportEngineSpecificationToFile(fullFileName);

    YAWLEditor.setStatusBarTextToPrevious();
    YAWLEditor.resetStatusBarProgress();
  }
  
  private String getFullNameFromFile(File file) {
    if (file == null) {
      return "";
    }
    String fullFileName = file.getAbsolutePath();
    if (!fullFileName.toLowerCase().endsWith(SPECIFICATION_FILE_TYPE)) {
      fullFileName += "." + SPECIFICATION_FILE_TYPE;
    }
    return fullFileName;
  }
}

