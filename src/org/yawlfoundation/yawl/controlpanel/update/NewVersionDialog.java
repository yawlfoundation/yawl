/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.util.WebPageLauncher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 13/10/2015
 */
public class NewVersionDialog extends JDialog implements ActionListener {

    private JButton btnClose;

    private static final String YAWL_URL = "http://yawlfoundation.org/";


    public NewVersionDialog(Window parent, Differ differ) {
        super(parent);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("New YAWL Version");
        add(getContent(differ));
        pack();
        setLocationRelativeTo(parent);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Close")) {
            setVisible(false);
        }
    }


    private JPanel getContent(Differ differ) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(8,0,0,0));
        panel.add(getIconPanel(), BorderLayout.WEST);
        panel.add(getCentrePanel(differ), BorderLayout.CENTER);
        panel.add(getButtonBar(this), BorderLayout.SOUTH);
        return panel;
    }


    private JPanel getIconPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(15,15,0,0));
        JLabel label = new JLabel();
        label.setIcon(IconLoader.get("Yawl64"));
        panel.add(label);
        return panel;
    }


    private JPanel getCentrePanel(Differ differ) {
        JPanel panel = new JPanel(new GridLayout(0,1,1,1));
        panel.setBorder(new EmptyBorder(0,10,0,10));
        panel.add(new JLabel("A new version of YAWL is now available!"));
        panel.add(new JLabel(differ.getCurrentVersionInfo()));
        panel.add(new JLabel(differ.getLatestVersionInfo()));
        panel.add(new WebPageLauncher().createLink(
                "Download YAWL " + differ.getLatestVersion() + " from the YAWL website",
                YAWL_URL));
        return panel;
    }


    private JPanel getButtonBar(ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,10,5));
        btnClose = createButton("Close", listener);
        panel.add(btnClose);
        return panel;
    }


    private JButton createButton(String caption, ActionListener listener) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(75,25));
        btn.addActionListener(listener);
        return btn;
    }

}
