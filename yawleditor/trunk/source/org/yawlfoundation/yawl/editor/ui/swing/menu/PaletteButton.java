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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.palette.PaletteAction;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PaletteButton extends JToggleButton {

    private static final Insets margin = new Insets(0,0,0,0);

    private Palette palette;

    public PaletteButton(Palette palette,
                         PaletteAction action, int mnemonic) {
        super(action);
        setPalette(palette);
        setText(null);
        setMnemonic(mnemonic);
        setMargin(margin);
        setMaximumSize(getPreferredSize());
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
    }

    public Palette getPalette() {
        return this.palette;
    }

    public PaletteAction getPaletteAction() {
        return (PaletteAction) this.getAction();
    }

    public Point getToolTipLocation(MouseEvent e) {
        return new Point(0,getSize().height);
    }

    private String getButtonStatusText() {
        return getPaletteAction().getButtonStatusText();
    }

    public Palette.SelectionState getSelectionID() {
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
            YAWLEditor.getStatusBar().setText(getButtonStatusText());
        }
    }

}
