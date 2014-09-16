/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
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
        putValue(Action.NAME, "Save PNG Image...");
        putValue(Action.LONG_DESCRIPTION, "Export the currently active net to a PNG image");
        putValue(Action.SMALL_ICON, getMenuIcon("photo"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("E"));
    }


    public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = FileChooserFactory.build(
                PNG_FILE_TYPE, "Portable Network Graphics Image",
                "Save net image");

        chooser.showDialog(YAWLEditor.getInstance(), "Save");
        File file = chooser.getSelectedFile();
        if (file != null) {
            NetUtilities.toPNGfile(getGraph(), IMAGE_BUFFER, file.getAbsolutePath());
        }
    }
}
