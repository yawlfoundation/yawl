/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.update;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 24/07/2014
 */
public class InstallInstructions {

    public void show() {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), getInstructions(),
                "Installation Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    public String getInstructions() {
        return  "To install the new version of the YAWL Editor:\n\n" +
                " - unzip the downloaded file to a folder of your choice\n" +
                "   (Windows users - ensure you have full admin rights to the folder).\n" +
                " - Copy any plugins, specifications, repository data, task icons,\n" +
                "   and extended attributes files from the old version's location to\n" +
                "   the new. Repository files can be found in the 'lib/repository'\n" +
                "   folder.\n" +
                " - when you are satisfied you have all local data copied from the old\n" +
                "   version, you may delete it.";
    }
}
