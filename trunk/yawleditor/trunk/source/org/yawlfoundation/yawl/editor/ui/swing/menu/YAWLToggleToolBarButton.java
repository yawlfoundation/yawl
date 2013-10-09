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

import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Author: Michael Adams
 * Creation Date: 11/05/2010
 */
public class YAWLToggleToolBarButton extends JToggleButton {

    private static final Insets margin = new Insets(4,4,4,4);

    public YAWLToggleToolBarButton(Action a) {
        super(a);
        setText(null);
        setMnemonic(0);
        setMargin(margin);
        setMaximumSize(getPreferredSize());
    }

    public Point getToolTipLocation(MouseEvent e) {
        return new Point(0,getSize().height);
    }

    public void setEnabled(boolean enabled) {
        if (getAction() instanceof TooltipTogglingWidget) {
            TooltipTogglingWidget action = (TooltipTogglingWidget) this.getAction();
            if (enabled) {
                setToolTipText(action.getEnabledTooltipText());
            } else {
                setToolTipText(action.getDisabledTooltipText());
            }
        }
        super.setEnabled(enabled);
    }
}



