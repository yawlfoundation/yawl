/*
 * Created on 18/10/2003
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
        return " You must have an open specification, and selected net to use the palette ";
    }


    public abstract String getButtonStatusText();

    public abstract Palette.SelectionState getSelectionID();
}
