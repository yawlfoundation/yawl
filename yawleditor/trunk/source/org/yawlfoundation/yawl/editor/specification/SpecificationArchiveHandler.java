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
import org.yawlfoundation.yawl.editor.foundations.ArchivableNetState;
import org.yawlfoundation.yawl.editor.foundations.ArchivableSpecificationState;
import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SpecificationArchiveHandler {

  private static final String SPECIFICATION_FILE_TYPE = "ywl";
  private static final String DESCRIPTION = "YAWL Specification";
  
  private static final JFileChooser SAVE_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        DESCRIPTION,
        "Save specification to ",
        " file",
        FileChooserFactory.SAVING_AND_LOADING
    );
  
  private static final JFileChooser OPEN_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        DESCRIPTION,
        "Open specification from ",
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
   *  @returns true if the specification was saved, false if the user cancelled the save.
   */

  public boolean processSaveRequest() {
    if (SpecificationModel.getInstance().getFileName().equals("") ||
      ! SpecificationModel.getInstance().getFileName().endsWith(".ywl")) {

      if (!promptForAndSetSaveFileName()) {
        return false;
      }
    }
    saveUpdatingGUI();
    return true;
  }
  
  private boolean promptForAndSetSaveFileName() {

    if (JFileChooser.CANCEL_OPTION == SAVE_FILE_CHOOSER.showSaveDialog(YAWLEditor.getInstance())) {
	  return false;
    }

    File file = SAVE_FILE_CHOOSER.getSelectedFile();

    if (file.exists() && 
        !getFullNameFromFile(file).equals(SpecificationModel.getInstance().getFileName())) {
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
        getFullNameFromFile(file)
    );
      
    SpecificationModel.getInstance().setUniqueID("UID_" + UUID.randomUUID().toString());

    return true;
  }
  
  /**
   *  Processes a user's request to save an open specification.
   *  This might include prompting for a file name if one has not yet beem
   *  set for the specification.
   *  @returns true if the specification was saved, false if the user cancelled the save.
   */
  
  public void processSaveAsRequest() {
    if (promptForAndSetSaveFileName()) {
      saveUpdatingGUI();  
    }
  }
  
  public void save() throws Exception {
    save(SpecificationModel.getInstance().getFileName());
  }
  
  public void save(String fullFileName) throws Exception {
    // We write to a temporary file and then copy to the final file JIC
    // something goes wrong resulting in a crash. Only the temporary copy will
    // be in a corrupt state. 
    
    File temporarySpec = File.createTempFile("tempYAWLSpecification",null);
    
    ZipOutputStream outputStream = 
      new ZipOutputStream(
          new BufferedOutputStream(
              new FileOutputStream(
                  temporarySpec.getName()
             )
          )
      );
    
    outputStream.putNextEntry(
        new ZipEntry(
            "specification.xml"
        )
    );

    XMLEncoder encoder = new XMLEncoder(outputStream);

    encoder.setExceptionListener(
        new ExceptionListener() {
          public void exceptionThrown(Exception exception) {
            exception.printStackTrace();
          }
        }
    );
    
    writeSpecification(encoder);
    encoder.close();
    outputStream.close();
    
    FileUtilities.move(
        temporarySpec.getName(), 
        fullFileName
    );
    SpecificationUndoManager.getInstance().setDirty(false);
  }
  
  public void saveUpdatingGUI() {
    saveUpdatingGUI(
        SpecificationModel.getInstance().getFileName()
    );
  }
  
  public void saveUpdatingGUI(String fullFileName) {
    if (fullFileName.trim().equals("")) {
      return;
    }

    YAWLEditor.setStatusBarText("Saving Specification...");
    YAWLEditor.progressStatusBarOverSeconds(2);
    
    try {
      save(fullFileName);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Error discovered whilst writing YAWL Editor save file.\n Save has not been performed.\n",
          "Editor File Saving Error",
          JOptionPane.ERROR_MESSAGE
      );
      e.printStackTrace();
    }

    YAWLEditor.resetStatusBarProgress();
    YAWLEditor.setStatusBarTextToPrevious();
  }
  
  private void writeSpecification(XMLEncoder encoder) {
    encoder.writeObject(
        new ArchivableSpecificationState(
            SpecificationModel.getInstance()
        )
    );
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
    try {
      saveUpdatingGUI();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    } else {
      file = new File(fileName);
      if (!file.exists()) { // create a specification with this name

        YAWLEditorDesktop.getInstance().newNet();

        SpecificationModel.getInstance().setFileName(fileName);
        
        SpecificationFileModel.getInstance().incrementFileCount();

        SpecificationUndoManager.getInstance().discardAllEdits();

        return;

      } else if (!file.canRead()) { // file exists, but can't be read
        YAWLEditor.setStatusBarTextToPrevious();
        return;        
      }
    }

    YAWLEditor.progressStatusBarOverSeconds(2);
    YAWLEditorDesktop.getInstance().setVisible(false);
    
    try {
      YAWLEditor.setStatusBarText("Opening Specification...");

      openSpecificationFromFile(
          getFullNameFromFile(file)
      );  
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Error discovered reading YAWL Editor save file.\nDiscarding this load file.\n",
          "Editor File Loading Error",
          JOptionPane.ERROR_MESSAGE);
      SpecificationArchiveHandler.getInstance().closeWithoutSaving();
      e.printStackTrace();
    }

    YAWLEditorDesktop.getInstance().setVisible(true);
    YAWLEditor.resetStatusBarProgress();
    YAWLEditor.setStatusBarTextToPrevious();
  }

  
  public void openSpecificationFromFile(String fullFileName) throws Exception {
    if (fullFileName.equals("")) {
      return;
    }

    try{
      ZipInputStream inputStream = 
        new ZipInputStream(new BufferedInputStream(new FileInputStream(fullFileName)));
      inputStream.getNextEntry();

      XMLDecoder encoder = new XMLDecoder(inputStream);
      encoder.setExceptionListener(new ExceptionListener() {
        public void exceptionThrown(Exception exception) {
          exception.printStackTrace();
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
      }
      else {
          JOptionPane.showMessageDialog(
              YAWLEditor.getInstance(),
              "Error discovered reading YAWL Editor save file.\n" +
              "It appears to be a pre-2.0 file. Please convert it using\n" +
              "the YAWLSaveFileConverter utility and try again.\n",
              "Editor File Loading Error",
              JOptionPane.ERROR_MESSAGE);
      }

    } catch (Exception e) {
      processCloseRequest(); 
      throw e;
    }
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
    
        long largestIdSoFar = 0;
        while(netIterator.hasNext()) {
          NetGraphModel currentNet = (NetGraphModel) netIterator.next();
          long largestNetId = NetUtilities.getLargestEngineIdNumberWithin(currentNet);
          if (largestIdSoFar < largestNetId) {
            largestIdSoFar = largestNetId;
          }
        }
        specModel.setUniqueElementNumber(largestIdSoFar);

        specModel.checkResourcingObjects();
    
        if (state.getBounds() != null) {
           YAWLEditor.getInstance().setBounds(state.getBounds());
        }
    }
    return (state != null);
  }


  private void readNets(HashSet nets) {
    Object[] netArray = nets.toArray();
    LinkedList<NetGraph> rebuiltNets = new LinkedList<NetGraph>();
    
    for(int i = 0; i < netArray.length; i ++) {
      rebuiltNets.add(
          readNet((ArchivableNetState) netArray[i])
      );
    }

    // now the full set of nets are built and have their internal
    // framees built, we can specify the net internal frame z-order
    // without odd z-order reshuffling that would happen with missing nets.

    try {
      for(int i = 0; i < netArray.length; i ++) {
        YAWLEditorDesktop.getInstance().setComponentZOrder(
            rebuiltNets.get(i).getFrame(), 
            ((ArchivableNetState) netArray[i]).getZOrder()
        );
      }
    } catch (Exception e) {
      // Older spec save files did not save zOrder, leave as-is if this is the case.
    }
  }
  
  private NetGraph readNet(ArchivableNetState archivedNet) {
    NetGraph net = new NetGraph(archivedNet.getDecomposition());
    
    YAWLEditorDesktop.getInstance().openNet(archivedNet.getBounds(), 
                                            archivedNet.getMaximised(),
                                            net);
                                            
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
