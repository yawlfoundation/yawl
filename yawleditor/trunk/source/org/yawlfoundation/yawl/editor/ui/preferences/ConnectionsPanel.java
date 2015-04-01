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

package org.yawlfoundation.yawl.editor.ui.preferences;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;

/**
 * @author Michael Adams
 * @date 25/09/13
 */
public class ConnectionsPanel extends JPanel
        implements PreferencePanel, ActionListener, CaretListener {

    private JTextField _userField;
    private JPasswordField _passwordField;
    private JLabel _engineTestReply;
    private JLabel _resourceTestReply;
    private ConnectionPanel _enginePanel;
    private ConnectionPanel _resourcePanel;

    private boolean _hasChanges;


    public ConnectionsPanel(CaretListener listener) {
        super();
        addContent(listener);
        loadValues();
    }


    // test button clicked
    public void actionPerformed(ActionEvent event) {
        testConnections();
    }

    // field value changed
    public void caretUpdate(CaretEvent event) {
        _hasChanges = true;
    }

    public void applyChanges() {
        if (_hasChanges) {
            try {
                updateConnector();
                saveValues();
                _hasChanges = false;
            }
            catch (MalformedURLException mue) {
                JOptionPane.showMessageDialog(this, "Update failed: " + mue.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void addContent(CaretListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        _enginePanel = new ConnectionPanel("Engine");
        _enginePanel.addCaretListener(listener);
        _enginePanel.addCaretListener(this);
        add(_enginePanel);
        _resourcePanel = new ConnectionPanel("Resource Service");
        _resourcePanel.addCaretListener(listener);
        _resourcePanel.addCaretListener(this);
        add(_resourcePanel);
        add(getCredentialsPanel(listener));
        add(getFeedbackPanel());
    }

    private JPanel getCredentialsPanel(CaretListener listener) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10,0,10,0));
        content.add(getCredentialsFieldsPanel(listener));
        content.add(getButtonPanel(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel getCredentialsFieldsPanel(CaretListener listener) {
        _userField = new JTextField();
        _userField.setPreferredSize(new Dimension(170, 25));
        _userField.addCaretListener(listener);
        _userField.addCaretListener(this);

        _passwordField = new JPasswordField();
        _passwordField.setPreferredSize(new Dimension(170, 25));
        _passwordField.addCaretListener(listener);
        _passwordField.addCaretListener(this);

        JPanel content = new JPanel();
        content.add(new JLabel("User"));
        content.add(_userField);
        content.add(new JLabel("Password"));
        content.add(_passwordField);

        return content;
    }


    private JPanel getButtonPanel() {
        JButton testButton = new JButton("Test Connections");
        testButton.addActionListener(this);
        testButton.setPreferredSize(new Dimension(140, 25));
        JPanel panel = new JPanel();
        panel.add(testButton);
        return panel;
    }


    private JPanel getFeedbackPanel() {
        JPanel content = new JPanel(new GridLayout(0,1,10,10));
        content.setBorder(new EmptyBorder(5,5,150,5));
        _engineTestReply = new JLabel();
        content.add(_engineTestReply);
        _resourceTestReply = new JLabel();
        content.add(_resourceTestReply);
        return content;
    }


    private void loadValues() {
        _enginePanel.setHost(UserSettings.getEngineHost());
        _enginePanel.setPort(UserSettings.getEnginePort());
        _resourcePanel.setHost(UserSettings.getResourceHost());
        _resourcePanel.setPort(UserSettings.getResourcePort());
        _userField.setText(UserSettings.getEngineUserid());
        _passwordField.setText(UserSettings.getEnginePassword());
    }


    private void saveValues() {
        UserSettings.setEngineHost(_enginePanel.getHost());
        UserSettings.setEnginePort(_enginePanel.getPort());
        UserSettings.setResourceHost(_resourcePanel.getHost());
        UserSettings.setResourcePort(_resourcePanel.getPort());
        UserSettings.setEngineUserid(getUser());
        UserSettings.setEnginePassword(getPassword());
    }


    private void updateConnector() throws MalformedURLException {
        YConnector.setEngineURL(_enginePanel.getHost(), _enginePanel.getPort());
        YConnector.setResourceURL(_resourcePanel.getHost(), _resourcePanel.getPort());
        YConnector.setUserID(getUser());
        YConnector.setPassword(getPassword());
        boolean isConnected = YAWLEditor.getStatusBar().refreshConnectionStatus();
        if (isConnected) {
            SpecificationModel.getHandler().getResourceHandler().resetCache();
        }
    }


    private void testConnections() {
        String user = getUser();
        String password = getPassword();
        testEngineConnection(user, password);
        testResourceConnection(user, password);
    }


    private String getUser() { return _userField.getText(); }

    private String getPassword() { return new String(_passwordField.getPassword()); }


    private void testEngineConnection(String user, String password) {
        _engineTestReply.setForeground(Color.RED);
        try {
            if (YConnector.testEngineParameters(_enginePanel.getHost(),
                    _enginePanel.getPort(), user, password)) {
                _engineTestReply.setForeground(Color.BLACK);
                _engineTestReply.setText("Successfully connected to YAWL Engine with the " +
                                        "parameters provided");
            }
            else {
                _engineTestReply.setText("Failed to connect to YAWL Engine with the " +
                        "parameters provided");
            }

        }
        catch (MalformedURLException mue) {
            _engineTestReply.setText("Invalid host");
        }
    }

    private void testResourceConnection(String user, String password) {
        _resourceTestReply.setForeground(Color.RED);
        try {
            if (YConnector.testResourceServiceParameters(_resourcePanel.getHost(),
                    _resourcePanel.getPort(), user, password)) {
                _resourceTestReply.setForeground(Color.BLACK);
                _resourceTestReply.setText("Successfully connected to the Resource " +
                        "Service with the parameters provided");
            }
            else {
                _resourceTestReply.setText("Failed to connect to the Resource Service " +
                        "with the parameters provided");
            }
        }
        catch (MalformedURLException mue) {
            _resourceTestReply.setText("Invalid host");
        }
    }

}
