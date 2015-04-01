package org.yawlfoundation.yawl.controlpanel.preferences;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

/**
 * @author Michael Adams
 * @date 5/08/2014
 */
public class PreferencesDialog extends JDialog implements ActionListener, EngineStatusListener {

    private JCheckBox _cbxStart;
    private JCheckBox _cbxUpdates;
    private JCheckBox _cbxOutput;
    private JCheckBox _cbxLogon;
    private JCheckBox _cbxStop;
    private JPanel _portPanel;
    private JTextField _portField;
    private UserPreferences _prefs;
    private int _origPortValue;

    public PreferencesDialog(JFrame mainWindow) {
        super(mainWindow);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("YAWL Control Panel Options");
        _prefs = new UserPreferences();
        buildUI();
        load();
        pack();
        setLocationByPlatform(true);
        Publisher.addEngineStatusListener(this);
    }


    public void actionPerformed(ActionEvent event) {
        boolean canClose = true;
        if (event.getActionCommand().equals("OK")) {
            canClose = save();
        }
        if (canClose) setVisible(false);
    }


    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped:  {
                enablePanel(_portPanel, true);
                _portField.setToolTipText("");
                break;
            }
            case Stopping:
            case Starting:
            case Running:  {
                enablePanel(_portPanel, false);
                _portField.setToolTipText("Port cannot be edited while engine is running");
                break;
            }
        }
    }



    private void load() {
        _cbxStart.setSelected(_prefs.startEngineOnStartup());
        _cbxUpdates.setSelected(_prefs.checkForUpdatesOnStartup());
        _cbxLogon.setSelected(_prefs.showLogonPageOnEngineStart());
        _cbxStop.setSelected(_prefs.stopEngineOnExit());
        if (_cbxOutput != null) {
            _cbxOutput.setSelected((_prefs.openOutputWindowOnStartup()));
        }
        _origPortValue = TomcatUtil.getTomcatServerPort();
        _portField.setText(String.valueOf(_origPortValue));
    }


    private boolean save() {
        _prefs.setStartEngineOnStartup(_cbxStart.isSelected());
        _prefs.setCheckForUpdatesOnStartup(_cbxUpdates.isSelected());
        _prefs.setShowLogonPageOnEngineStart(_cbxLogon.isSelected());
        _prefs.setStopEngineOnExit(_cbxStop.isSelected());
        if (_cbxOutput != null) {
            _prefs.setOpenOutputWindowOnStartup(_cbxOutput.isSelected());
        }
        return savePort();
    }


    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5, 5, 5, 5));
        content.add(buildOptionsPanel(), BorderLayout.CENTER);
        content.add(buildButtonPanel(), BorderLayout.SOUTH);
        add(content);
    }


    private JPanel buildOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.add(getTopPanel(), BorderLayout.NORTH);
        panel.add(getCentrePanel(), BorderLayout.CENTER);
        panel.add(getBottomPanel(), BorderLayout.SOUTH);
        return panel;
    }


    private JPanel getTopPanel() {
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.setBorder(new CompoundBorder(
                new TitledBorder("When Control Panel Starts:"),
                new EmptyBorder(5, 5, 5, 5)));
        _cbxStart = makeCheckBox("Start Engine if not already running", KeyEvent.VK_S);
        _cbxUpdates = makeCheckBox("Check for updates", KeyEvent.VK_C);
        panel.add(_cbxStart);
        panel.add(_cbxUpdates);

        // no output dialog needed for windows users
        if (! FileUtil.isWindows()) {
            _cbxOutput = makeCheckBox("Open Output Log window", KeyEvent.VK_O);
            panel.add(_cbxOutput);
        }

        return panel;
    }


    private JPanel getCentrePanel() {
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.setBorder(new CompoundBorder(
                new TitledBorder("When Engine Starts:"),
                new EmptyBorder(5, 5, 5, 5)));
        _cbxLogon = makeCheckBox("Go to Logon page in browser", KeyEvent.VK_G);
        panel.add(_cbxLogon);
        return panel;
    }


    private JPanel getBottomPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(getExitPanel());
        panel.add(getPortPanel());
        return panel;
    }


    private JPanel getExitPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBorder(new CompoundBorder(
                new TitledBorder("When Control Panel Exits:"),
                new EmptyBorder(5, 5, 5, 5)));
        _cbxStop = makeCheckBox("Stop Engine if running", KeyEvent.VK_P);
        panel.add(_cbxStop);
        return panel;
    }


    private JPanel getPortPanel() {
        _portPanel = new JPanel();
        _portPanel.setLayout(new BoxLayout(_portPanel, BoxLayout.LINE_AXIS));
        _portPanel.setBorder(new CompoundBorder(
                new TitledBorder("Tomcat Port:"),
                new EmptyBorder(5, 5, 5, 5)));
        _portField = new JFormattedTextField(getPortValueFormatter());
        _portField.setPreferredSize(new Dimension(75, 25));
        _portPanel.add(new JLabel("Port"));
        _portPanel.add(_portField);
        enablePanel(_portPanel, !TomcatUtil.isEngineRunning());
        return _portPanel;
    }


    private JCheckBox makeCheckBox(String caption, int mnemonic) {
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setBorder(new EmptyBorder(3, 0, 2, 0));
        checkBox.setMnemonic(mnemonic);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        return checkBox;
    }


    private NumberFormatter getPortValueFormatter() {
        NumberFormat plainIntegerFormat = NumberFormat.getInstance();
        plainIntegerFormat.setGroupingUsed(false);                      // no commas

        NumberFormatter portFormatter = new NumberFormatter(plainIntegerFormat);
        portFormatter.setValueClass(Integer.class);
        portFormatter.setAllowsInvalid(false);
        portFormatter.setMinimum(0);
        portFormatter.setMaximum(65535);
        return portFormatter;
    }



    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 0, 5));
        panel.add(createButton("Cancel"));
        panel.add(createButton("OK"));
        return panel;
    }


    private JButton createButton(String caption) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(85, 25));
        btn.addActionListener(this);
        return btn;
    }


    private void enablePanel(JPanel panel, boolean enable) {
        for (Component c : panel.getComponents()) c.setEnabled(enable);
        panel.setEnabled(enable);
    }


    private boolean savePort() {
        int port = Integer.parseInt(_portField.getText());
        if (port != _origPortValue) {
            if (TomcatUtil.isPortActive(port)) {
                JOptionPane.showMessageDialog(this,
                        "Port " + port + " is already in use.\n" +
                        "Please try another",
                        "Port in use",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else {
                TomcatUtil.setTomcatServerPort(port);
            }
        }
        return true;
    }

}
