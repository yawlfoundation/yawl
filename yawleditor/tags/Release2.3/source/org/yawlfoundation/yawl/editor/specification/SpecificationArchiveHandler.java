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
package org.yawlfoundation.yawl.editor.specification;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.actions.net.PreviewConfigurationProcessAction;
import org.yawlfoundation.yawl.editor.actions.specification.OpenRecentSubMenu;
import org.yawlfoundation.yawl.editor.foundations.ArchivableNetState;
import org.yawlfoundation.yawl.editor.foundations.ArchivableSpecificationState;
import org.yawlfoundation.yawl.editor.foundations.LogWriter;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import java.util.zip.ZipInputStream;

public class SpecificationArchiveHandler {

  private static final String SAVE_SPECIFICATION_FILE_TYPE = "yawl";
  private static final String OPEN_SPECIFICATION_FILE_TYPE = "ywl";
  private static final String DESCRIPTION = "YAWL Specification";

  private static boolean _conversionAttempted = false;
  
  private static final JFileChooser SAVE_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SAVE_SPECIFICATION_FILE_TYPE,
        DESCRIPTION,
        "Save specification to ",
        " file",
        FileChooserFactory.SAVING_AND_LOADING
    );
  
  private static final JFileChooser OPEN_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        OPEN_SPECIFICATION_FILE_TYPE,
        DESCRIPTION,
        "Import specification from ",
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
        YAWLEngineProxy.getInstance().engineFormatFileExport(specification);
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
  
  
  /**
   *  Processes a user's request to open a specification file.
   */
  
  public void processOpenRequest() {
    processOpenRequest(null);
  }

  /**
   *  Processes a user's request to open a specification file
   *  @param fileName the specification file to open
   */
  
  public void processOpenRequest(String fileName) {
    File file;

    if (fileName == null) { // prompt user for the file
      if (JFileChooser.CANCEL_OPTION == 
            OPEN_FILE_CHOOSER.showOpenDialog(YAWLEditor.getInstance())) {
        YAWLEditor.setStatusBarTextToPrevious();
        return;
      }
      file = OPEN_FILE_CHOOSER.getSelectedFile();
    }
    else {
      file = new File(fileName);
      if (!file.exists()) { // create a specification with this name
        YAWLEditorDesktop.getInstance().newNet();
        SpecificationModel.getInstance().setFileName(fileName);
        SpecificationFileModel.getInstance().incrementFileCount();
        SpecificationUndoManager.getInstance().discardAllEdits();
        return;
      }
      else if (!file.canRead()) { // file exists, but can't be read
        YAWLEditor.setStatusBarTextToPrevious();
        return;        
      }
    }

    YAWLEditor.setStatusBarText("Importing 'YWL' Specification file...");    
    try {
      openSpecificationFromFile(
          getFullNameFromFile(file, OPEN_SPECIFICATION_FILE_TYPE)
      );  
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Error discovered reading YWL save file.\nDiscarding this load file.\n",
          "Editor File Loading Error",
          JOptionPane.ERROR_MESSAGE);
      SpecificationArchiveHandler.getInstance().closeWithoutSaving();
      LogWriter.error("Error discovered reading YWL file", e);
    }

    YAWLEditorDesktop.getInstance().setVisible(true);
    YAWLEditor.resetStatusBarProgress();
    YAWLEditor.setStatusBarTextToPrevious();
  }

  
  public boolean openSpecificationFromFile(String fullFileName) throws Exception {
    YAWLEditor.progressStatusBarOverSeconds(2);
    YAWLEditorDesktop.getInstance().setVisible(false);
    if (fullFileName.equals("")) {
      return false;
    }

    try{
      ZipInputStream inputStream = 
        new ZipInputStream(new BufferedInputStream(new FileInputStream(fullFileName)));
      inputStream.getNextEntry();

      XMLDecoder encoder = new XMLDecoder(inputStream);
      encoder.setExceptionListener(new ExceptionListener() {
        public void exceptionThrown(Exception e) {
          LogWriter.error("Error decoding YWL file.", e);
        }
      });
      SpecificationModel.getInstance().reset();

      boolean ok = readSpecification(encoder);
      encoder.close();
      inputStream.close();

      if (ok) {
          SpecificationModel.getInstance().setFileName(fullFileName);
          SpecificationFileModel.getInstance().incrementFileCount();
          SpecificationUndoManager.getInstance().discardAllEdits();
          return true;
      }
      else {
          if (! _conversionAttempted) {
             YAWLEditor.resetStatusBarProgress();
             YAWLEditor.setStatusBarText(
                     "Attempting to convert old format 'YWL' Specification file...");
              String fileName = new SpecificationConverter().convert(fullFileName);
              boolean converted = openSpecificationFromFile(fileName);
              if (! converted) {
                 JOptionPane.showMessageDialog(
                    YAWLEditor.getInstance(),
                    "Attempt to convert YWL file to v2.0 failed.\n",
                    "YWL File Loading Error",
                    JOptionPane.ERROR_MESSAGE);
              }
              _conversionAttempted = true;
          }
      }

    } catch (Exception e) {
      processCloseRequest(); 
      throw e;
    }
    _conversionAttempted = false;
    return false;
  }
  
  private boolean readSpecification(XMLDecoder encoder) {
    SpecificationModel specModel = SpecificationModel.getInstance();
    ArchivableSpecificationState state = 
      (ArchivableSpecificationState) encoder.readObject();
    if (state != null) {
        if (state.getSize() != null) {
      			YAWLEditorDesktop.getInstance().setPreferredSize(state.getSize());
        }
        if (state.getDataTypeDefinition() != null) {
          specModel.setDataTypeDefinition(XMLUtilities.unquoteXML(
                  state.getDataTypeDefinition())
          );
        }
        specModel.setWebServiceDecompositions(state.getDecompositions());
        specModel.setFontSize(state.getFontSize());

        readNets(state.getNets());
    
        try {
          specModel.setDefaultNetBackgroundColor(state.getDefaultNetBackgroundColor());
        } catch(Exception e) {}
    
        specModel.setName(state.getName());
        specModel.setDescription(state.getDescription());
        specModel.setId(state.getId());
        specModel.setAuthor(state.getAuthor());
        specModel.setVersionNumber(new YSpecVersion(String.valueOf(state.getVersionNumber())));
        specModel.setValidFromTimestamp(state.getValidFromTimestamp());
        specModel.setValidUntilTimestamp(state.getValidUntilTimestamp());
    
        Iterator netIterator = specModel.getNets().iterator();

        specModel.checkResourcingObjects();
    
        if (state.getBounds() != null) {
           YAWLEditor.getInstance().setBounds(state.getBounds());
        }
    }
    return (state != null);
  }


  private void readNets(HashSet nets) {
    for (Object o : nets) {
        readNet((ArchivableNetState) o);
    }
 //   YAWLEditorDesktop.getInstance().

  }
  
  private NetGraph readNet(ArchivableNetState archivedNet) {
    NetGraph net = new NetGraph(archivedNet.getDecomposition());
    
    YAWLEditorDesktop.getInstance().openNet(net);
                                            
    net.getNetModel().insert(archivedNet.getCells(), 
                             archivedNet.getCellViewAttributes(),
                             archivedNet.toConnectionSet(archivedNet.getConnectionHashMap()), 
                             archivedNet.toParentMap(archivedNet.getParentMap()),
                             null);

    SpecificationModel.getInstance().addNetNotUndoable(net.getNetModel());
    net.changeCancellationSet(archivedNet.getTriggeringTaskOfVisibleCancellationSet());
   
    net.setBackground(archivedNet.getBackgroundColor());
    
    if (archivedNet.getScale() != 0) {
      net.setScale(archivedNet.getScale());
    }
    
    if (archivedNet.getVisibleRectangle() != null) {
      net.scrollRectToVisible(
          archivedNet.getVisibleRectangle()
      );
    }
    
    if (archivedNet.getStartingNetFlag()) {
      SpecificationModel.getInstance().setStartingNet(
          net.getNetModel()
      );
    }

    return net;
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
