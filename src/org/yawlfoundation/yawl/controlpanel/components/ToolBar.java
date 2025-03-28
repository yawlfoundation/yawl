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

package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.editor.EditorLauncher;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.preferences.PreferencesDialog;
import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.BackgroundChecker;
import org.yawlfoundation.yawl.controlpanel.update.UpdateLoader;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.controlpanel.util.WebPageLauncher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 13/10/2015
 */
public class ToolBar extends JToolBar implements ActionListener, EngineStatusListener {

    private static final String WEBPAGE_URL = "https://yawlfoundation.github.io";
    private static final String MANUAL_URL = "https://yawlfoundation.github.io/assets/files/YAWLUserManual5.2.pdf";

    private static final Dimension spacer = new Dimension(11,16);

    private final YControlPanel _mainWindow;

    private JButton _btnStart;
    private JButton _btnStop;
    private JButton _btnLogon;
    private JButton _btnEditor;
    private JButton _btnUpdates;
    private JButton _btnPreferences;


    public ToolBar(YControlPanel mainWindow) {
        super();
        _mainWindow = mainWindow;
        setRollover(true);
        setFloatable(false);
        setBorder(new EmptyBorder(10, 5, 10, 5));
        addButtons();
        addStatusPanel();
        Publisher.addEngineStatusListener(this);
    }

    public void addSeparator() {
        super.addSeparator(spacer);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("start")) {
            startEngine();
        }
        else if (cmd.equals("stop")) {
            stopEngine();
        }
        else if (cmd.equals("logon")) {
            browseTo(getLogonURL());
        }
        else if (cmd.equals("editor")) {
            startEditor();
        }
        else if (cmd.equals("updates")) {
            checkForUpdates();
        }
        else if (cmd.equals("preferences")) {
            showPreferencesDialog();
        }
        else if (cmd.equals("manual")) {
            browseTo(MANUAL_URL);
        }
        else if (cmd.equals("webpage")) {
            browseTo(WEBPAGE_URL);
        }
        else if (cmd.equals("about")) {
            showAboutDialog();
        }
    }


    public void statusChanged(EngineStatus status) {
        if (status == EngineStatus.Running) {
            if (new UserPreferences().showLogonPageOnEngineStart()) {
                browseTo(getLogonURL());
            }
        }
        enableButtons(status);
    }


    public void performUserPreferencesOnStart() {
        UserPreferences prefs = new UserPreferences();
        if (prefs.checkForUpdatesOnStartup()) {
            new BackgroundChecker(_mainWindow);
        }
        if (prefs.startEngineOnStartup()) {
            startEngine();
        }
    }


    // called from editor launcher
    public void enableEditorButton(boolean enable) { _btnEditor.setEnabled(enable); }


    private void addButtons() {
        _btnStart = createToolButton("start", " Start the YAWL Engine ");
        _btnStop = createToolButton("stop", " Stop the YAWL Engine ");
        _btnLogon = createToolButton("logon", " Open the YAWL Logon Page ");
        _btnEditor = createToolButton("editor", " Launch the YAWL Process Editor ");
        _btnUpdates = createToolButton("updates", " Add or remove services, or check for updates ");
        _btnPreferences = createToolButton("preferences", " Edit Preferences ");

        add(_btnStart);
        add(_btnStop);
        addSeparator();
        add(_btnLogon);
        add(_btnEditor);
        add(_btnUpdates);
        add(_btnPreferences);
        addSeparator();
        add(createToolButton("manual", " View the YAWL User Manual "));
        add(createToolButton("webpage", " Visit the YAWL home page "));
        addSeparator();
        add(createToolButton("about", " About... "));
    }


    private JButton createToolButton(String action, String tip) {
        JButton button = new JButton();
        button.setIcon(IconLoader.get(action));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.setText(null);
        button.setMnemonic(0);
        button.setMargin(new Insets(2,2,2,2));
        button.setMaximumSize(button.getPreferredSize());
        button.addActionListener(this);
        return button;
    }


    private void enableButtons(EngineStatus status) {
        boolean waiting = (status == EngineStatus.Starting ||
                status == EngineStatus.Stopping);
        _btnStart.setEnabled(status == EngineStatus.Stopped);
        _btnStop.setEnabled(status == EngineStatus.Running);
        _btnUpdates.setEnabled(! waiting);
        _btnPreferences.setEnabled(! waiting);
        _btnLogon.setEnabled(status == EngineStatus.Running);
    }


    private void addStatusPanel() {
        addSeparator();
        addSeparator();
        add(new StatusPanel());
    }

    private void startEngine() {
        FileUtil.updateYawlLibInUI();        // ensure UI has latest lib before starting
        boolean running = TomcatUtil.isEngineRunning();
        if (! running) {
            try {
                _mainWindow.clearOutputPane();
                running = TomcatUtil.start();
                if (! running) {
                    offerToKillProcess();
                }
            }
            catch (IOException ioe) {
                System.out.println("ERROR starting Engine: " + ioe.getMessage());
            }
        }
        else {

            // if the start button was enabled and the engine is running, it must have
            // been started outside the control panel
            Publisher.announceRunningStatus();
            System.out.println("INFO: System started (external to Control Panel)");
        }
    }


    private void stopEngine() {
        TomcatUtil.stop();
    }


    private void checkForUpdates() {
        _mainWindow.showComponentsPane();
        new UpdateLoader(_mainWindow).execute();
    }


    private void startEditor() {
        EditorLauncher editorLauncher = new EditorLauncher(this);
        editorLauncher.launch();
        enableEditorButton(false);
    }


    private void showPreferencesDialog() {
        new PreferencesDialog(_mainWindow).setVisible(true);
    }


    private void showAboutDialog() {
        new AboutDialog(_mainWindow).setVisible(true);
    }


    private void browseTo(String url) {
        try {
            new WebPageLauncher().openWebPage(url);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(), "Error browsing to page",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private String getLogonURL() {
        int port = TomcatUtil.getTomcatServerPort();
        return "http://localhost:" + port + "/yawlui";
    }


    private void offerToKillProcess() {
        String message =
                "The YAWL engine cannot be started because there is already\n" +
                "an process instance or remnant running on the specified port.\n" +
                "Press 'Yes' to attempt to remove the existing instance and\n" +
                "retry, or 'No' to retain the existing instance.";
        int choice = JOptionPane.showConfirmDialog(_mainWindow, message,
                "Cannot Start Engine", JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            showWaitCursor(true);
            try {
                if (TomcatUtil.killTomcatProcess()) {
                    message = "Successfully removed existing instance.\n" +
                            "Please retry starting the YAWL Engine.";
                }
                else {
                    message = "Unable to free the port required to start YAWL.\n" +
                            "Check the process using port " +
                            TomcatUtil.getTomcatServerPort() +
                            ",\nor set YAWL to use a different port in the\n" +
                            "Preferences dialog.";
                }
            }
            catch (IOException ioe) {
                message = "Unable to remove existing instance.";
            }
            showWaitCursor(false);
            JOptionPane.showMessageDialog(_mainWindow, message, "Remove Instance",
                                 JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void showWaitCursor(boolean show) {
        int cursor = show ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR;
        Component glassPane = ((RootPaneContainer) getTopLevelAncestor()).getGlassPane();
        glassPane.setCursor(Cursor.getPredefinedCursor(cursor));
        glassPane.setVisible(show);
    }

}
