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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.beans.editor.FilePropertyEditor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 13/07/12
 */
public class ImageFilePropertyEditor extends FilePropertyEditor {

    public ImageFilePropertyEditor() {
        super();
        removeCancelButton();
    }


    protected void customizeFileChooser(JFileChooser chooser) {
        FileFilter filter = new FileNameExtensionFilter("Image files",
                "gif", "jpg", "jpeg", "png");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(filter);
    }


    private void removeCancelButton() {
        for (Component component : ((JPanel) editor).getComponents()) {
            if (component instanceof JButton && ((JButton) component).getText().equals("X")) {
                ((JPanel) editor).remove(component);
            }
        }
    }
}
