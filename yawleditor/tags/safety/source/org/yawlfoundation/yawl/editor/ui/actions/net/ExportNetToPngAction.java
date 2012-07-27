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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetPrintUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.FileChooserFactory;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;


public class ExportNetToPngAction extends YAWLSelectedNetAction {

    private static final String PNG_FILE_TYPE = "png";
    private static final int IMAGE_BUFFER = 10;


    {
        putValue(Action.SHORT_DESCRIPTION, " Export the currently active net to a PNG image");
        putValue(Action.NAME, "Export to PNG Image...");
        putValue(Action.LONG_DESCRIPTION, "Export the currently active net to a PNG image");
        putValue(Action.SMALL_ICON, getPNGIcon("photo"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("E"));
    }


    public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = FileChooserFactory.build(
                PNG_FILE_TYPE, "Portable Network Graphics Image",
                "Export net to ", " image");

        chooser.showSaveDialog(YAWLEditor.getInstance());
        File file = chooser.getSelectedFile();
        if (file != null) {
            NetPrintUtilities.toPNGfile(getGraph(), IMAGE_BUFFER, file.getAbsolutePath());
        }
    }
}
