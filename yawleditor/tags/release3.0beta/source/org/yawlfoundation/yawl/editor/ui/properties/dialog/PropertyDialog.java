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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 26/08/13
 */
public abstract class PropertyDialog extends JDialog {

    private JButton btnOK;
    private JButton btnCancel;

    protected static final String MENU_ICON_PATH =
            "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/";


    public PropertyDialog(Window parent) {
        this(parent, true);
    }

    public PropertyDialog(Window parent, boolean createContent) {
        super(parent);
        setModal(true);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        if (createContent) add(getContent());
    }


    public JButton getOKButton() { return  btnOK; }

    public JButton getCancelButton() { return  btnCancel; }


    protected abstract JPanel getContent();



    protected JPanel getButtonBar(ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,10,5));
        btnCancel = createButton("Cancel", listener);
        panel.add(btnCancel);
        btnOK = createButton("OK", listener);
        btnOK.setEnabled(false);
        panel.add(btnOK);
        return panel;
    }


    protected JButton createButton(String caption, ActionListener listener) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(75,25));
        btn.addActionListener(listener);
        return btn;
    }


    protected ImageIcon getMenuIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(MENU_ICON_PATH + iconName + ".png");
    }

}
