/*
 * Created on 18/03/2004
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import java.io.IOException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.foundations.ArchivableNetState;
import au.edu.qut.yawl.editor.foundations.ArchivableSpecificationState;
import au.edu.qut.yawl.editor.net.NetGraph;

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
        " file"
    );
  
  private static final JFileChooser OPEN_FILE_CHOOSER = 
    FileChooserFactory.buildFileChooser(
        SPECIFICATION_FILE_TYPE,
        DESCRIPTION,
        "Open specification from ",
        " file"
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
  
  private void saveSpecificationToFile(String fullFileName) {
    if (fullFileName.equals("")) {
      return;
    }
    
    JStatusBar.getInstance().updateProgressOverSeconds(2);

    try {
      ZipOutputStream outputStream = 
        new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fullFileName)));
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
    } catch (IOException e) {
      e.printStackTrace();
    }

    JStatusBar.getInstance().resetProgress();
  }
  
  private void writeSpecification(XMLEncoder encoder) {
    encoder.writeObject(new ArchivableSpecificationState(SpecificationModel.getInstance()));
  }

  public void close() {
    if (SpecificationFileModel.getInstance().getFileCount() > 0) {
      YAWLEditorDesktop.getInstance().setVisible(false);
      SpecificationFileModel.getInstance().decrementFileCount();
      save();
      YAWLEditorDesktop.getInstance().closeAllNets();
			SpecificationModel.getInstance().reset();
      SpecificationUndoManager.getInstance().discardAllEdits();
      YAWLEditorDesktop.getInstance().setVisible(true);
    }
  }
  
  public void exit() {
    YAWLEditor.getInstance().setVisible(false);
    close();
    System.exit(0);
  }
  
  public void open() {
    if (JFileChooser.CANCEL_OPTION == 
        OPEN_FILE_CHOOSER.showOpenDialog(YAWLEditor.getInstance())) {
    	return;
    }
    File file = OPEN_FILE_CHOOSER.getSelectedFile();

    JStatusBar.getInstance().updateProgressOverSeconds(2);
    YAWLEditorDesktop.getInstance().setVisible(false);
    
    openSpecificationFromFile(getFullNameFromFile(file));  

    YAWLEditorDesktop.getInstance().setVisible(true);
    JStatusBar.getInstance().resetProgress();
  }
  
  public void openSpecificationFromFile(String fullFileName) {
    if (fullFileName.equals("")) {
      return;
    }

    try {

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

    } catch (IOException e) {
      e.printStackTrace();
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
    SpecificationModel.getInstance().setDecompositions(state.getDecompositions());
    readNets(state.getNets());
    SpecificationModel.getInstance().undoableSetFontSize(0,state.getFontSize());
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
                                            
    net.getGraphLayoutCache().insert(archivedNet.getCells(), 
                                     archivedNet.getCellViewAttributes(),
                                     archivedNet.toConnectionSet(archivedNet.getConnectionHashMap()), 
                                     archivedNet.toParentMap(archivedNet.getParentMaps()),
                                     null);

		SpecificationModel.getInstance().addNetNotUndoable(net.getNetModel());
    net.changeCancellationSet(archivedNet.getTriggeringTaskOfVisibleCancellationSet());
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
