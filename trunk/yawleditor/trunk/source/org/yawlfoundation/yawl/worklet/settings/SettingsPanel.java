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

import org.yawlfoundation.yawl.editor.ui.preferences.ConnectionPanel;
import org.yawlfoundation.yawl.worklet.client.WorkletClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.MalformedURLException;

/**
 * @author Michael Adams
 * @date 13/03/15
 */
public class SettingsPanel extends JPanel {

    private JTextField _userField;
    private JPasswordField _passwordField;
    private JLabel _testReply;
    private ConnectionPanel _hostPanel;


    public SettingsPanel() {
        super();
        addContent();
        loadValues();
    }


    public void saveValues() {
        SettingsStore.setServiceHost(_hostPanel.getHost());
        SettingsStore.setServicePort(_hostPanel.getPort());
        SettingsStore.setServiceUserId(getUser());
        SettingsStore.setServicePassword(getPassword());
    }


    public void testConnection() {
        String user = getUser();
        String password = getPassword();
          _testReply.setForeground(Color.RED);
        try {
            WorkletClient testClient = new WorkletClient(_hostPanel.getHost(),
                    _hostPanel.getPort(), user, password);
            if (testClient.isConnected()) {
                _testReply.setForeground(Color.BLACK);
                _testReply.setText("Successfully connected to Worklet Service with the " +
                                        "parameters provided");
                testClient.disconnect();
            }
            else {
                _testReply.setText("Failed to connect to YAWL Engine with the " +
                        "parameters provided");
            }

        }
        catch (MalformedURLException mue) {
            _testReply.setText("Invalid host");
        }
    }


    private void addContent() {
        _hostPanel = new ConnectionPanel("Worklet Service");
        add(_hostPanel);
        add(getCredentialsPanel());
        add(getFeedbackPanel());
    }


    private JPanel getCredentialsPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 0, 10, 0));
        content.add(getCredentialsFieldsPanel());
        return content;
    }


    private JPanel getCredentialsFieldsPanel() {
        _userField = new JTextField();
        _userField.setPreferredSize(new Dimension(170, 25));

        _passwordField = new JPasswordField();
        _passwordField.setPreferredSize(new Dimension(170, 25));

        JPanel content = new JPanel();
        content.add(new JLabel("User"));
        content.add(_userField);
        content.add(new JLabel("Password"));
        content.add(_passwordField);

        return content;
    }


    private JPanel getFeedbackPanel() {
        JPanel content = new JPanel(new GridLayout(0,1,10,10));
        _testReply = new JLabel();
        content.add(_testReply);
        return content;
    }


    private void loadValues() {
        _hostPanel.setHost(SettingsStore.getServiceHost());
        _hostPanel.setPort(SettingsStore.getServicePort());
        _userField.setText(SettingsStore.getServiceUserId());
        _passwordField.setText(SettingsStore.getServicePassword());
    }


    private String getUser() { return _userField.getText(); }

    private String getPassword() { return new String(_passwordField.getPassword()); }

}
