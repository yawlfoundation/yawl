package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.preferences.PreferencesDialog;
import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.BackgroundChecker;
import org.yawlfoundation.yawl.controlpanel.update.UpdateDialogLoader;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class ButtonPanel extends JPanel implements ActionListener, EngineStatusListener {

    private JButton btnStartStop;
    private JButton btnLogon;
    private JButton btnOutput;
    private JButton btnUpdates;
    private JButton btnPreferences;
    private JFrame _mainWindow;
    private OutputDialog _outputDialog;
    private UpdateDialogLoader _updateDialogLoader;


    public ButtonPanel(JFrame mainWindow) {
        super();
        _mainWindow = mainWindow;
        setBorder(new EmptyBorder(0, 10, 0, 0));
        configLayout();
        addButtons();
        Publisher.addEngineStatusListener(this);
    }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("Start")) {
            doStartOrStop();
        }
        else if (cmd.equals("Logon")) {
            showLogonPage();
        }
        else if (cmd.equals("Updates")) {
            showUpdateDialog();
        }
        else if (cmd.equals("Output Log")) {
            if (_outputDialog == null || ! _outputDialog.isVisible()) {
                _outputDialog = new OutputDialog(_mainWindow);
            }
            _outputDialog.toFront();
        }
        else if (cmd.equals("Preferences")) {
           new PreferencesDialog(_mainWindow).setVisible(true);
        }
    }


    public void statusChanged(EngineStatus status) {
        if (status == EngineStatus.Running) {
            btnStartStop.setText("Stop");
            btnStartStop.setToolTipText("Stop the YAWL Engine");
            if (new UserPreferences().showLogonPageOnEngineStart()) {
                ActionEvent event = new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED, "Logon");
                actionPerformed(event);
            }
        }
        else if (status == EngineStatus.Stopped) {
            btnStartStop.setText("Start");
            btnStartStop.setToolTipText("Start the YAWL Engine");
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
            new BackgroundChecker(this);
        }
    }


    private void addButtons() {
        btnStartStop = createButton("Start", "Start the YAWL Engine");
        add(btnStartStop);
        btnLogon = createButton("Logon", "Go to the YAWL Logon Page");
        add(btnLogon);
        btnUpdates = createButton("Updates", "Check for Updates");
        add(btnUpdates);

        // windows doesn't need the output window
        if (! FileUtil.isWindows()) {
            btnOutput = createButton("Output Log", "Show Output Log Window");
            add(btnOutput);
        }

        btnPreferences = createButton("Preferences", "Set or Check Preferences");
        add(btnPreferences);
        enableButtons(EngineStatus.Stopped);   // to begin with
    }


    private JButton createButton(String label, String tip) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(120, 25));
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private void showLogonPage() {
        try {
            openWebPage(getLogonURL());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(), "Error browsing to page",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void openWebPage(String url) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
                return;
            }
        }
        throw new IOException("Cannot launch browser page from java on this platform");
    }


    private String getLogonURL() {
        int port = TomcatUtil.getTomcatServerPort();
        return "http://localhost:" + port + "/resourceService";
    }


    private void doStartOrStop() {
        if (Publisher.getCurrentStatus() == EngineStatus.Stopped) {
            try {
                if (TomcatUtil.start()) {
                    Publisher.announceStartingStatus();
                }
                else {
                    showError("General failure: unable to start Engine");
                }
            }
            catch (IOException ioe) {
                showError("Error when starting Engine: " + ioe.getMessage());
            }
        }
        else {
            try {
                if (TomcatUtil.stop()) {
                    Publisher.announceStoppingStatus();
                }
                else {
                    showError("General failure: unable to stop Engine");
                }
            }
            catch (IOException ioe) {
                showError("Error when stopping Engine: " + ioe.getMessage());
            }
        }
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


    private void showError(String msg) {
            JOptionPane.showMessageDialog(null, msg, "Engine Execution Error",
                    JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(EngineStatus status) {
        boolean waiting = (status == EngineStatus.Starting ||
                status == EngineStatus.Stopping);
        btnStartStop.setEnabled(! waiting);
        btnUpdates.setEnabled(! waiting);
        btnPreferences.setEnabled(! waiting);
        if (btnOutput != null) btnOutput.setEnabled(! waiting);
        btnLogon.setEnabled(status == EngineStatus.Running);
    }


    private void configLayout() {
        GridLayout layout = new GridLayout(0, 1);
        if (FileUtil.isWindows()) layout.setVgap(10);
        setLayout(layout);
    }

}
