/*
 * Created on 18/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.palette.ControlFlowPaletteAction;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ControlFlowPaletteButton extends JToggleButton {

    private static final Insets margin = new Insets(0,0,0,0);

    private ControlFlowPalette palette;

    public ControlFlowPaletteButton(ControlFlowPalette palette,
                                    ControlFlowPaletteAction action, int mnemonic) {
        super(action);
        setPalette(palette);
        setText(null);
        setMnemonic(mnemonic);
        setMargin(margin);
        setMaximumSize(getPreferredSize());
    }

    public void setPalette(ControlFlowPalette palette) {
        this.palette = palette;
    }

    public ControlFlowPalette getPalette() {
        return this.palette;
    }

    public ControlFlowPaletteAction getPaletteAction() {
        return (ControlFlowPaletteAction) this.getAction();
    }

    public Point getToolTipLocation(MouseEvent e) {
        return new Point(0,getSize().height);
    }

    private String getButtonStatusText() {
        return getPaletteAction().getButtonStatusText();
    }

    public ControlFlowPalette.SelectionState getSelectionID() {
        return getPaletteAction().getSelectionID();
    }

    public void setEnabled(boolean enabled) {
        TooltipTogglingWidget action = (TooltipTogglingWidget) this.getAction();
        if (enabled) {
            setToolTipText(action.getEnabledTooltipText());
        }
        else {
            setToolTipText(action.getDisabledTooltipText());
        }
        super.setEnabled(enabled);
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected && isEnabled()) {
            YAWLEditor.setStatusBarText(getButtonStatusText());
        }
    }

}
