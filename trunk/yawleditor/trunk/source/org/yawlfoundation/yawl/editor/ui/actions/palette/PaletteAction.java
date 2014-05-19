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

package org.yawlfoundation.yawl.editor.ui.actions.palette;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.menu.Palette;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class PaletteAction extends AbstractAction {

    public PaletteAction() { }


    public void actionPerformed(ActionEvent event) {
        YAWLEditor.getPalette().getPalette().setSelectedState(getSelectionID());
    }

    protected ImageIcon getPaletteIconByName(String iconName) {
        return ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/"
                + iconName + "24.gif");
    }

    protected String getClickAnywhereText() {
        return "Left-click on the selected net to create a new ";
    }

    public String getDisabledTooltipText() {
        return " You must have an open specification to use the palette ";
    }


    public abstract String getButtonStatusText();

    public abstract Palette.SelectionState getSelectionID();
}
