/*
 * Created on 19/12/2003
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

import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.Palette;

import javax.swing.Action;

public class NetDragAction extends PaletteAction implements TooltipTogglingWidget {

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Drag Net Window");
        putValue(Action.LONG_DESCRIPTION, "Net Window Drag Mode");
        putValue(Action.SMALL_ICON, getPaletteIconByName("PaletteDrag"));
    }

    public NetDragAction() { super(); }

    public String getEnabledTooltipText() { return " Net Drag Mode "; }


    public String getButtonStatusText() {
        return "Drag the visible window to another area of this net.";
    }

    public Palette.SelectionState getSelectionID() {
        return Palette.SelectionState.DRAG;
    }
}
