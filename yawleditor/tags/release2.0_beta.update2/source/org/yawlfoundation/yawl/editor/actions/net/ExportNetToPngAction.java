/*
 * Created on 09/10/2003
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

package org.yawlfoundation.yawl.editor.actions.net;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.swing.Action;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.net.utilities.NetPrintUtilities;
import org.yawlfoundation.yawl.editor.swing.FileChooserFactory;


public class ExportNetToPngAction extends YAWLSelectedNetAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final String PNG_FILE_TYPE = "png";
  
  private static final int IMAGE_BUFFER = 10;
  
  private static JFileChooser pngFileChooser;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Export the currently active net to a PNG image");
    putValue(Action.NAME, "Export to PNG Image...");
    putValue(Action.LONG_DESCRIPTION, "Export the currently active net to a PNG image");
    putValue(Action.SMALL_ICON, getIconByName("ExportPNG"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));

    if (pngFileChooser == null) {
      
      // For reasons that SHOULD NOT BE, the creation of THIS file chooser under the
      // Windows 2K platform takes several seconds. I have regulated its creation
      // to a low-priority thread so I don't hold the editor boot sequence up waiting on it.
      
      Thread chooserCreationThread = new Thread() {
        public void run() {
          pngFileChooser = FileChooserFactory.buildFileChooser(
              PNG_FILE_TYPE,
              "Portable Network Graphics Image",
              "Export net to ",
              " image",
              FileChooserFactory.IMPORTING_AND_EXPORTING
          );
        }
      };
      chooserCreationThread.setPriority(Thread.MIN_PRIORITY);
      chooserCreationThread.start();
    }
  }
  
  public void actionPerformed(ActionEvent event) {
    if (pngFileChooser != null) {
      pngFileChooser.showSaveDialog(YAWLEditor.getInstance());
      File file = pngFileChooser.getSelectedFile();
      if (file != null) {
        NetPrintUtilities.toPNGfile(
            getGraph(), 
            IMAGE_BUFFER, 
            file.getAbsolutePath()
        );
      }
    } else {
      JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
          "The export to PNG action is not yet ready to execute.\n\nPlease try again soon.",
          "Export to PNG action unavailable",
          JOptionPane.INFORMATION_MESSAGE);
    }
  }
}
