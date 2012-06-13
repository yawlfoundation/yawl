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

package org.yawlfoundation.yawl.editor.ui.swing;


import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class FileChooserFactory {
  
  public static final int SAVING_AND_LOADING = 0;
  public static final int IMPORTING_AND_EXPORTING = 1;

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
          String lastPath = UserSettings.getLastSaveOrLoadPath();
          setCurrentDirectory(
                  new File(lastPath != null ? lastPath : System.getProperty("user.dir")));

        return super.showDialog(parent, approveButtonText);
      }      

      public File getSelectedFile() {
        
        // When the user retrieves the file, remember 
        // the directory used for next time.
        File selectedFile = super.getSelectedFile();
        if (selectedFile != null) {
            UserSettings.setLastSaveOrLoadPath(getCurrentDirectory().getAbsolutePath());
        }
        return selectedFile;
      }

  };


      class YAWLFileFilter extends FileFilter {

          private String[] makeExtensions() {
              return fileType.split(",");
          }

          private boolean isValidExtension(File file) {   
              String[] extns = makeExtensions();
              for (String extn : extns) {
                  if (file.getName().toLowerCase().endsWith("." + extn))
                      return true;
              }
              return false;
          }

          public String getDescription() {
              StringBuilder result = new StringBuilder(description);
              result.append(" (");
              String[] extns = makeExtensions();
              for (int i=0; i<extns.length; i++) {
                  if (i>0) result.append(" and ") ;
                  result.append(extns[i].toUpperCase());
              }
              result.append(" files)");
              return result.toString();
          }

          public boolean accept(File file) {
            return file.isDirectory() || isValidExtension(file);
          }

      }  // class


    fileChooser.setDialogTitle(
      titlePrefix + 
      fileType.toUpperCase().replaceAll(",", " or ") +
      titleSuffix
    );
    
    fileChooser.setAcceptAllFileFilterUsed(false);      // don't show 'all files' choice
    fileChooser.setFileFilter(new YAWLFileFilter());

    return fileChooser;
  }

}

