/*
 * Created on 23/09/2004
 * YAWLEditor v1.01
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

package org.yawlfoundation.yawl.editor.swing;


import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.yawlfoundation.yawl.editor.YAWLEditor;

public class FileChooserFactory {
  
  public static final int SAVING_AND_LOADING = 0;
  public static final int IMPORTING_AND_EXPORTING = 1;
  
  private static final String SAVING_AND_LOADING_LABEL      = "lastUsedSaveLoadDirectory";
  private static final String IMPORTING_AND_EXPORTING_LABEL = "lastUsedImportExportDirectory";
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  public static JFileChooser buildFileChooser(final String fileType, 
                                              final String description,
                                              final String titlePrefix,
                                              final String titleSuffix,
                                              final int    usage) {
    
    JFileChooser fileChooser = new JFileChooser(){
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public int showDialog(Component parent, 
                            String approveButtonText) throws HeadlessException {

        // just before showing the dialog, point the dialog at the
        // last directory used.
        
        switch(usage) {
          case SAVING_AND_LOADING: {
            setCurrentDirectory(
                new File(prefs.get(SAVING_AND_LOADING_LABEL, 
                         System.getProperty("user.dir"))
                )
            );
            break;
            
          }
          case IMPORTING_AND_EXPORTING: {
            setCurrentDirectory(
                new File(prefs.get(IMPORTING_AND_EXPORTING_LABEL, 
                         System.getProperty("user.dir"))
                )
            );
            break;
          }
        }

        return super.showDialog(parent, approveButtonText);
      }      

      public File getSelectedFile() {
        
        // When the user retrieves the file, remember 
        // the directory used for next time.
        
        File selectedFile = super.getSelectedFile();
        if (selectedFile != null) {
          switch(usage) {
            case SAVING_AND_LOADING: {
              prefs.put(
                  SAVING_AND_LOADING_LABEL, 
                  getCurrentDirectory().getAbsolutePath()
              );
              break;
            }
            case IMPORTING_AND_EXPORTING: {
              prefs.put(
                  IMPORTING_AND_EXPORTING_LABEL, 
                  getCurrentDirectory().getAbsolutePath()
              );
              break;
            }
          }
        }
        return selectedFile;
      }
    };
    
    fileChooser.setDialogTitle(
      titlePrefix + 
      fileType.toUpperCase() + 
      titleSuffix
    );
    
    fileChooser.setFileFilter(new FileFilter() {
      public String getDescription() {
        return  description + " (*." + fileType + ")";
      }
      public boolean accept(File file) {
        return file.isDirectory()
          || file.getName().toLowerCase().endsWith("." + fileType);
      }
    });
    
    return fileChooser;
  }
}
