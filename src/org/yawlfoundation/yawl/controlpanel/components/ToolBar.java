package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.editor.EditorLauncher;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.preferences.PreferencesDialog;
import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.UpdateDialogLoader;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.controlpanel.util.WebPageLauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 13/10/2015
 */
public class ToolBar extends JToolBar implements ActionListener, EngineStatusListener {

    private static final String YAWL_URL = "http://www.yawlfoundation.org/";
    private static final String EXAMPLES_URL = YAWL_URL + "pages/resources/examples.html";
    private static final String MANUAL_URL = YAWL_URL + "manuals/YAWLUserManual3.0.pdf";

    private static final Dimension spacer = new Dimension(11,16);

    private UpdateDialogLoader _updateDialogLoader;
    private JFrame _mainWindow;

    private JButton _btnStart;
    private JButton _btnStop;
    private JButton _btnLogon;
    private JButton _btnEditor;
    private JButton _btnUpdates;
    private JButton _btnPreferences;
    private JButton _btnManual;
    private JButton _btnExamples;


    public ToolBar(JFrame mainWindow) {
        super();
        _mainWindow = mainWindow;
        setRollover(true);
        setFloatable(false);
        setMargin(new Insets(3, 2, 2, 0));
        addButtons();
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
            showUpdateDialog();
        }
        else if (cmd.equals("preferences")) {
            showPreferencesDialog();
        }
        else if (cmd.equals("manual")) {
            browseTo(MANUAL_URL);
        }
        else if (cmd.equals("examples")) {
            browseTo(EXAMPLES_URL);
        }
    }


    public void statusChanged(EngineStatus status) {
        if (status == EngineStatus.Running) {
            if (new UserPreferences().showLogonPageOnEngineStart()) {
                ActionEvent event = new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED, "Logon");
                actionPerformed(event);
            }
        }
        enableButtons(status);
    }


    public void performUserPreferencesOnStart() {
        UserPreferences prefs = new UserPreferences();
        ActionEvent event;
        boolean tomcatIsRunning = TomcatUtil.isPortActive();
        if (prefs.openOutputWindowOnStartup()) {
            event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Output Log");
            actionPerformed(event);
        }
        if (prefs.startEngineOnStartup() && ! tomcatIsRunning) {
            event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Start");
            actionPerformed(event);
        }
        else if (prefs.showLogonPageOnEngineStart() && tomcatIsRunning) {
            event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Logon");
            actionPerformed(event);
        }
        if (prefs.checkForUpdatesOnStartup()) {
     //       new BackgroundChecker(this);
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
        _btnManual = createToolButton("manual", " View the YAWL User Manual ");
        _btnExamples = createToolButton("examples", " Download some example processes ");

        add(_btnStart);
        add(_btnStop);
        add(_btnLogon);
        addSeparator();
        add(_btnEditor);
        add(_btnUpdates);
        add(_btnPreferences);
        addSeparator();
        add(_btnManual);
        add(_btnExamples);
    }


    private JButton createToolButton(String action, String tip) {
        JButton button = new JButton();
        button.setIcon(IconLoader.get(action));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.setText(null);
        button.setMnemonic(0);
        button.setMargin(new Insets(4,4,4,4));
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


    private void startEngine() {
        boolean success = false;
        try {
            success = TomcatUtil.start();
        }
        catch (IOException ioe) {
            showError("Error when starting Engine: " + ioe.getMessage());
        }
        _btnStart.setEnabled(! success);
    }


    private void stopEngine() {
        boolean success = false;
        try {
            success = TomcatUtil.stop();
            if (success) {
                Publisher.announceStoppingStatus();
            }
            else {
                showError("General failure: unable to stop Engine");
            }
        }
        catch (IOException ioe) {
            showError("Error when stopping Engine: " + ioe.getMessage());
        }
        _btnStop.setEnabled(! success);
    }


    private void showUpdateDialog() {
        if (_updateDialogLoader != null && _updateDialogLoader.isDialogActive()) {
            _updateDialogLoader.toFront();
        }
        else {
            _updateDialogLoader = new UpdateDialogLoader(_mainWindow);
            _updateDialogLoader.execute();
        }
    }


    private void startEditor() {
        EditorLauncher editorLauncher = new EditorLauncher(null);     // this
        editorLauncher.launch();
        enableEditorButton(false);
    }


    private void showPreferencesDialog() {
        new PreferencesDialog(_mainWindow).setVisible(true);
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
        return "http://localhost:" + port + "/resourceService";
    }


    private void showError(String msg) {
             JOptionPane.showMessageDialog(null, msg, "Engine Execution Error",
                     JOptionPane.ERROR_MESSAGE);
     }

}
