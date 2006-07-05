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

package au.edu.qut.yawl.editor.swing;


import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import au.edu.qut.yawl.editor.YAWLEditor;

public class FileChooserFactory {
  
  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  public static JFileChooser buildFileChooser(final String fileType, 
                                              final String description,
                                              final String titlePrefix,
                                              final String titleSuffix) {
    
    JFileChooser fileChooser = new JFileChooser(){
      public int showDialog(Component parent, 
                            String approveButtonText) throws HeadlessException {

        // just before showing the dialog, point the dialog at the
        // last directory used.
        
        setCurrentDirectory(
          new File(prefs.get("lastUsedDirectory", 
                   System.getProperty("user.dir"))
          )
        );

        return super.showDialog(parent, approveButtonText);
      }      

      public File getSelectedFile() {
        
        // When the user retrieves the file, remember 
        // the directory used for next time.
        
        File selectedFile = super.getSelectedFile();
        if (selectedFile != null) {
          prefs.put(
            "lastUsedDirectory", 
            getCurrentDirectory().getAbsolutePath()
          );
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
