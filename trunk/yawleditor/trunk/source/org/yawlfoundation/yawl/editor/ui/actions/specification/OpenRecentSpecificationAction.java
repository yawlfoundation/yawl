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

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class OpenRecentSpecificationAction extends YAWLSpecificationAction implements TooltipTogglingWidget {

    {
        putValue(Action.LONG_DESCRIPTION, "Open a recently loaded specification");
        putValue(Action.SMALL_ICON, getMenuIcon("folder_page"));
    }

    private String _fullFileName = null;

    public OpenRecentSpecificationAction() { }

    public OpenRecentSpecificationAction(String fullFileName) {
        setFileName(fullFileName);
    }


    public void setFileName(String fullFileName) {
        _fullFileName = fullFileName ;
        putValue(Action.NAME, getShortFileName());
    }


    public void actionPerformed(ActionEvent event) {
        FileOperations.open(_fullFileName) ;
    }

    public String getEnabledTooltipText() {
        return _fullFileName;
    }

    public String getDisabledTooltipText() {
        return " You must have no specification" +
                " open to in order to open another ";
    }

    private String getShortFileName() {
        File file = new File(_fullFileName);
        return file.getName();
    }
}