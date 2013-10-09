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

import org.yawlfoundation.yawl.editor.ui.actions.palette.*;

import javax.swing.*;
import java.awt.*;

public class PalettePopupMenu extends JPopupMenu {

    public PalettePopupMenu() {
        super();
        addMenuItems();
        for (Component component : getComponents()) {
            ((JMenuItem) component).setToolTipText(null);
        }
        pack();
    }

    private void addMenuItems() {
        add(new AtomicTaskAction());
        add(new CompositeTaskAction());
        add(new MultipleAtomicTaskAction());
        add(new MultipleCompositeTaskAction());
        add(new ConditionAction());
        add(new MarqueeAction());
        add(new NetDragAction());
    }
}
