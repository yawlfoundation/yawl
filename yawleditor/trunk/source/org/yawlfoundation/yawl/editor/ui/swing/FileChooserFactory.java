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

package org.yawlfoundation.yawl.editor.ui.swing;


import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class FileChooserFactory {

    public static JFileChooser build(final String fileType, final String description,
                                     final String title) {

        JFileChooser fileChooser = new JFileChooser() {

            public int showDialog(Component parent, String approveButtonText)
                    throws HeadlessException {
                setLastPath();
                return super.showDialog(parent, approveButtonText);
            }

            public File getSelectedFile() {

                // remember the last directory used for next time.
                File selectedFile = super.getSelectedFile();
                if (selectedFile != null) {
                    UserSettings.setLastSaveOrLoadPath(selectedFile.getAbsolutePath());
                }
                return selectedFile;
            }


            // point the dialog at the last directory used.
            private void setLastPath() {
                String lastPath = UserSettings.getLastSaveOrLoadPath();
                setCurrentDirectory(new File(lastPath != null ? lastPath :
                        System.getProperty("user.dir")));
            }

        };


        class YAWLFileFilter extends FileFilter {

            private String[] extensions = fileType.split(",");

            private boolean isValidExtension(File file) {
                for (String extn : extensions) {
                    if (file.getName().toLowerCase().endsWith(extn))
                        return true;
                }
                return false;
            }

            public String getDescription() {
                StringBuilder result = new StringBuilder(description);
                result.append(" (");
                for (int i=0; i < extensions.length; i++) {
                    if (i > 0) result.append(" and ") ;
                    result.append(extensions[i].toUpperCase());
                }
                result.append(" files)");
                return result.toString();
            }

            public boolean accept(File file) {
                return file.isDirectory() || isValidExtension(file);
            }

        }  // class


        fileChooser.setDialogTitle(title);
        fileChooser.setAcceptAllFileFilterUsed(false);   // don't show 'all files' choice
        fileChooser.setFileFilter(new YAWLFileFilter());

        return fileChooser;
    }

}

