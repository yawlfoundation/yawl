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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 12/06/2014
 */
public class MiniToolBar extends JToolBar {

    private ActionListener _btnListener;


    public MiniToolBar(ActionListener listener) {
        super();
        init();
        _btnListener = listener;
    }


    public JButton addButton(String iconName, String action, String tip) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.addActionListener(_btnListener);
        add(button);
        return button;
    }


    public void enableComponents(boolean enable) {
        for (Component c : getComponents()) c.setEnabled(enable);
    }


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getMiniToolIcon(iconName);
    }


    private void init() {
        setBorder(null);
        setFloatable(false);
        setRollover(true);
    }

}
