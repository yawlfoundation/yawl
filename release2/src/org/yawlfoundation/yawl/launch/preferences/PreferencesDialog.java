package org.yawlfoundation.yawl.launch.preferences;

import org.yawlfoundation.yawl.launch.YControlPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 5/08/2014
 */
public class PreferencesDialog extends JDialog implements ActionListener {

    private JCheckBox _cbxStart;
    private JCheckBox _cbxUpdates;
    private JCheckBox _cbxOutput;
    private JCheckBox _cbxLogon;
    private JCheckBox _cbxStop;
    private UserPreferences _prefs;

    public PreferencesDialog(JFrame mainWindow) {
        super(mainWindow);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setTitle("YAWL Control Panel Options");
        _prefs = new UserPreferences();
        buildUI();
        load();
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            save();
        }
        setVisible(false);
    }


    private void load() {
        _cbxStart.setSelected(_prefs.startEngineOnStartup());
        _cbxUpdates.setSelected(_prefs.checkForUpdatesOnStartup());
        _cbxOutput.setSelected((_prefs.openOutputWindowOnStartup()));
        _cbxLogon.setSelected(_prefs.showLogonPageOnEngineStart());
        _cbxStop.setSelected(_prefs.stopEngineOnExit());
    }


    private void save() {
        _prefs.setStartEngineOnStartup(_cbxStart.isSelected());
        _prefs.setCheckForUpdatesOnStartup(_cbxUpdates.isSelected());
        _prefs.setOpenOutputWindowOnStartup(_cbxOutput.isSelected());
        _prefs.setShowLogonPageOnEngineStart(_cbxLogon.isSelected());
        _prefs.setStopEngineOnExit(_cbxStop.isSelected());
    }


    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
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
                new EmptyBorder(5,5,5,5)));
        _cbxStart = makeCheckBox("Start Engine if not already running", KeyEvent.VK_S);
        _cbxUpdates = makeCheckBox("Check for updates", KeyEvent.VK_C);
        _cbxOutput = makeCheckBox("Open Output Log window", KeyEvent.VK_O);
        panel.add(_cbxStart);
        panel.add(_cbxUpdates);
        panel.add(_cbxOutput);
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
        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.setBorder(new CompoundBorder(
                new TitledBorder("When Control Panel Exits:"),
                new EmptyBorder(5, 5, 5, 5)));
        _cbxStop = makeCheckBox("Stop Engine if running", KeyEvent.VK_P);
        panel.add(_cbxStop);
        return panel;
    }


    private JCheckBox makeCheckBox(String caption, int mnemonic) {
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setBorder(new EmptyBorder(3, 0, 2, 0));
        checkBox.setMnemonic(mnemonic);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        return checkBox;
    }


    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,0,5));
        panel.add(createButton("Cancel"));
        panel.add(createButton("OK"));
        return panel;
    }


    private JButton createButton(String caption) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(75,25));
        btn.addActionListener(this);
        return btn;
    }

}
