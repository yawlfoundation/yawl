/*
 * Copyright (c) 2004-2015 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.settings;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 13/03/15
 */
public class SettingsDialog extends JDialog implements ActionListener {

    private SettingsPanel _settingsPanel;

    public SettingsDialog() {
        super(YAWLEditor.getInstance());
        setTitle("Worklet Service Connection Settings");
        setModal(true);
        setResizable(false);
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(getContent());
        setPreferredSize(new Dimension(520, 255));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("Test Connection")) {
            _settingsPanel.testConnection();
        }
        else {
            if (cmd.equals("OK")) _settingsPanel.saveValues();
            setVisible(false);
        }
    }


    private JPanel getContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        _settingsPanel = new SettingsPanel();
        panel.add(_settingsPanel, BorderLayout.CENTER);
        panel.add(getButtonBar(), BorderLayout.SOUTH);
        return panel;

    }


    private JPanel getButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,10,5));
        panel.add(makeButton("Test Connection"));
        panel.add(makeButton("Cancel"));
        panel.add(makeButton("OK"));
        return panel;
    }


    private JButton makeButton(String caption) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(125, 25));
        btn.addActionListener(this);
        return btn;
    }

}
