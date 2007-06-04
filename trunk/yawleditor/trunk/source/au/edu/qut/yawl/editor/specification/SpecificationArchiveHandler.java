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
package au.edu.qut.yawl.editor.specification;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.util.HashSet;
import java.util.Iterator;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;

import au.edu.qut.yawl.editor.foundations.ArchivableNetState;
import au.edu.qut.yawl.editor.foundations.ArchivableSpecificationState;
import au.edu.qut.yawl.editor.foundations.FileUtilities;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import au.edu.qut.yawl.editor.swing.JStatusBar;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;
import au.edu.qut.yawl.editor.swing.FileChooserFactory;

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

  public void save() {
    if (SpecificationModel.getInstance().getFileName().equals("")) {
      promptForAndSetSaveFileName();
    }
    saveSpecificationToFile(SpecificationModel.getInstance().getFileName());
  }
  
  private void promptForAndSetSaveFileName() {

    if (JFileChooser.CANCEL_OPTION == SAVE_FILE_CHOOSER.showSaveDialog(YAWLEditor.getInstance())) {
	  return;
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
        return;   
      }
    }
    SpecificationModel.getInstance().setFileName(getFullNameFromFile(file));
  }
  
  public void saveAs() {
    promptForAndSetSaveFileName();
    saveSpecificationToFile(SpecificationModel.getInstance().getFileName());  
  }
  
  public void saveSpecificationToFile(String fullFileName) {
    if (fullFileName.equals("")) {
      return;
    }

    JStatusBar.getInstance().setStatusText("Saving Specification...");
    JStatusBar.getInstance().updateProgressOverSeconds(2);
    
    try {
      
      // We write to a temporary file and then copy to the final file JIC
      // something goes wrong resulting in a crash. Only the temporary copy will
      // be in a corrupt state. 
      
      File temporarySpec = File.createTempFile("tempYAWLSpecification",null);
      
      ZipOutputStream outputStream = 
        new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temporarySpec.getName())));
      outputStream.putNextEntry(new ZipEntry("specification.xml"));
      XMLEncoder encoder = new XMLEncoder(outputStream);
      encoder.setExceptionListener(new ExceptionListener() {
        public void exceptionThrown(Exception exception) {
          exception.printStackTrace();
        }
      });
      
      writeSpecification(encoder);
      encoder.close();
      outputStream.close();
      
      FileUtilities.move(temporarySpec.getName(), fullFileName);
      
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Error discovered whilst writing YAWL Editor save file.\n Save has not been performed.\n",
          "Editor File Saving Error",
          JOptionPane.ERROR_MESSAGE
      );
      e.printStackTrace();
    }

    JStatusBar.getInstance().resetProgress();
    JStatusBar.getInstance().setStatusTextToPrevious();

  }
  
  private void writeSpecification(XMLEncoder encoder) {
    encoder.writeObject(new ArchivableSpecificationState(SpecificationModel.getInstance()));
  }

  public void close() {
    JStatusBar.getInstance().setStatusText("Closing Specification...");     
    if (SpecificationFileModel.getInstance().getFileCount() == 0) {
     return; 
    }
    int response = getSaveOnCloseResponse();
    if (response == JOptionPane.CANCEL_OPTION) {
      JStatusBar.getInstance().setStatusTextToPrevious();
      return;
    }
    if (response == JOptionPane.YES_OPTION) {
      saveWhilstClosing();
    } else {
      closeWithoutSaving();
    }
  }
  
  private int getSaveOnCloseResponse() {
    return JOptionPane.showConfirmDialog(
        YAWLEditor.getInstance(),
        "You have chosen to close this specification.\n"
            + "Do you wish to save your changes before closing?\n\n"
            + "Choose 'yes' to save the specification as-is, 'no' to lose all unsaved changes.",
        "Save changes before closing?", 
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
  
  private void saveWhilstClosing() {
    doPreSaveClosingWork();
    save();
    doPostSaveClosingWork();
  }
  
  private void closeWithoutSaving() {
    doPreSaveClosingWork();
    doPostSaveClosingWork();
  }
  
  public void exit() {
    JStatusBar.getInstance().setStatusText("Exiting YAWLEditor...");
    if (SpecificationFileModel.getInstance().getFileCount() > 0) {
      int response = getSaveOnCloseResponse();
      if (response == JOptionPane.CANCEL_OPTION) {
        JStatusBar.getInstance().setStatusTextToPrevious();
        return;
      }

      YAWLEditor.getInstance().setVisible(false);
      
      if (response == JOptionPane.YES_OPTION) {
        saveWhilstClosing();
      } else {
        closeWithoutSaving();
      }
    }

    System.exit(0);
  }
  
  public void open(String fileName) {
    File file;

    if (fileName == null) { // prompt user for the file
      if (JFileChooser.CANCEL_OPTION == 
        OPEN_FILE_CHOOSER.showOpenDialog(YAWLEditor.getInstance())) {
        JStatusBar.getInstance().setStatusTextToPrevious();
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
        JStatusBar.getInstance().setStatusTextToPrevious();
        return;        
      }
    }

    JStatusBar.getInstance().updateProgressOverSeconds(2);
    YAWLEditorDesktop.getInstance().setVisible(false);
    
    try {
      JStatusBar.getInstance().setStatusText("Opening Specification...");

      openSpecificationFromFile(
          getFullNameFromFile(file)
      );  
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          YAWLEditor.getInstance(), 
          "Error discovered reading YAWL Editor save file.\nDiscarding this load file.\n",
          "Editor File Loading Error",
          JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }

    YAWLEditorDesktop.getInstance().setVisible(true);
    JStatusBar.getInstance().resetProgress();
    JStatusBar.getInstance().setStatusTextToPrevious();
  }
  
  public void open() {
    open(null);
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

      readSpecification(encoder);
      encoder.close();
      inputStream.close();

      SpecificationModel.getInstance().setFileName(fullFileName);
      
      SpecificationFileModel.getInstance().incrementFileCount();

      SpecificationUndoManager.getInstance().discardAllEdits();
      
      /* self-correction of old editor files with buggy, redundant ports 
      SpecificationUtilities.removeAllRedundantPorts(
          SpecificationModel.getInstance()
      );  */

    } catch (Exception e) {
      close(); 
      throw e;
    }
  }
  
  private void readSpecification(XMLDecoder encoder) {
    ArchivableSpecificationState state = 
      (ArchivableSpecificationState) encoder.readObject();
    if (state.getSize() != null) {
			YAWLEditorDesktop.getInstance().setPreferredSize(state.getSize());
    }
    if (state.getDataTypeDefinition() != null) {
      SpecificationModel.getInstance().setDataTypeDefinition(
        XMLUtilities.unquoteXML(state.getDataTypeDefinition())
      );
    }
    SpecificationModel.getInstance().setWebServiceDecompositions(state.getDecompositions());
    readNets(state.getNets());
    SpecificationModel.getInstance().undoableSetFontSize(0,state.getFontSize());

    try {
      SpecificationModel.getInstance().setDefaultNetBackgroundColor(
          state.getDefaultNetBackgroundColor()
      );
    } catch(Exception e) {}
    
    SpecificationModel.getInstance().setName(state.getName());
    SpecificationModel.getInstance().setDescription(state.getDescription());
    SpecificationModel.getInstance().setId(state.getId());
    SpecificationModel.getInstance().setAuthor(state.getAuthor());
    SpecificationModel.getInstance().setVersionNumber(state.getVersionNumber());
    SpecificationModel.getInstance().setValidFromTimestamp(state.getValidFromTimestamp());
    SpecificationModel.getInstance().setValidUntilTimestamp(state.getValidUntilTimestamp());
    
    Iterator netIterator = SpecificationModel.getInstance().getNets().iterator();
    
    long largestIdSoFar = 0;
    while(netIterator.hasNext()) {
      NetGraphModel currentNet = (NetGraphModel) netIterator.next();
      long largestNetId = NetUtilities.getLargestEngineIdNumberWithin(currentNet);
      if (largestIdSoFar < largestNetId) {
        largestIdSoFar = largestNetId;
      }
    }
    SpecificationModel.getInstance().setUniqueElementNumber(largestIdSoFar);
    
    if (state.getBounds() != null) {
      YAWLEditor.getInstance().setBounds(state.getBounds());
    }
  }
  
  private void readNets(HashSet nets) {
    Object[] netArray = nets.toArray();
    for(int i = 0; i < netArray.length; i ++) {
      readNet((ArchivableNetState) netArray[i]);
    }
  }
  
  private void readNet(ArchivableNetState archivedNet) {
    NetGraph net = new NetGraph(archivedNet.getDecomposition());
    YAWLEditorDesktop.getInstance().openNet(archivedNet.getBounds(), 
                                            archivedNet.getIconified(),
                                            archivedNet.getIconBounds(),
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
    
    net.getNetModel().setIsStartingNet(archivedNet.getStartingNetFlag());
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
