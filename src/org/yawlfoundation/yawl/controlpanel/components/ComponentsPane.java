package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.Differ;
import org.yawlfoundation.yawl.controlpanel.update.ProgressPanel;
import org.yawlfoundation.yawl.controlpanel.update.Updater;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTable;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 23/11/2015
 */
public class ComponentsPane extends JLayeredPane
        implements ActionListener, PropertyChangeListener, EngineStatusListener {

    private UpdateTable _table;
    private JButton _btnUpdate;
    private ProgressPanel _progressPanel;
    private JPanel _tablePanel;


    public ComponentsPane() {
        super();

        try {
            buildUI(getDiffer());
            setPreferredSize(new Dimension(600,470));
        }
        catch (IOException ioe) {
            add(new JLabel("ERROR: Unable to locate installed component information"));
        }
        Publisher.addEngineStatusListener(this);
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Update")) {
            _btnUpdate.setEnabled(false);
            new Updater(this).start();
        }
    }


    public void propertyChange(PropertyChangeEvent event) { enableButton(); }

    public void statusChanged(EngineStatus status) { enableButton(); }

    public void refresh(Differ differ, boolean afterUpdate) {
        _progressPanel.setVisible(false);
        _table.refresh(differ);
        if (afterUpdate) {
            showUpdateDoneMessage();
        }
        else if (! differ.hasUpdates()) {             // pre-update check
            showNoUpdatesMessage();
        }
        enableButton();               // will disable due to no pending/selected updates
    }


    public UpdateTable getTable() { return _table; }

    public ProgressPanel getProgressPanel() { return _progressPanel; }

    public JPanel getTablePanel() { return _tablePanel; }

    private Differ getDiffer() throws IOException {
        return new Differ(null, getCurrentCheckSumFile());
    }


    protected File getCurrentCheckSumFile() throws IOException {
        File current = FileUtil.getLocalCheckSumFile();
        if (! current.exists()) {
            throw new IOException("Unable to determine current build version");
        }
        return current;
    }


    private void buildUI(Differ differ) {
        _table = new UpdateTable(differ);
        _table.addPropertyChangeListener(this);

        _tablePanel = new JPanel(new BorderLayout());
        _tablePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scrollPane = new JScrollPane(_table);
        _tablePanel.add(scrollPane, BorderLayout.CENTER);
        _tablePanel.add(getButtonBar(), BorderLayout.SOUTH);
        _tablePanel.setBounds(0, 0, 600, 470);

        _progressPanel = new ProgressPanel();
        _progressPanel.setBounds(150, 190, 300, 70);
        _progressPanel.setVisible(false);

        add(_tablePanel, new Integer(0));
        add(_progressPanel, new Integer(1));
        moveToFront(_progressPanel);
    }


    private JPanel getButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        _btnUpdate = createButton("Update",
                "Update to latest versions (blue rows)\n" +
                        "and install (green rows) and uninstall (red rows) selections");
        panel.add(_btnUpdate);
        return panel;
    }


    private JButton createButton(String label, String tip) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 30));
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private void enableButton() {
        boolean isRunningOrStopped = ! Publisher.isTransientStatus();
        _btnUpdate.setEnabled(_table.hasUpdates() && isRunningOrStopped);
        setButtonTip();
    }


    private void setButtonTip() {
        _btnUpdate.setToolTipText(_btnUpdate.isEnabled() ?
                "Update to latest versions (blue rows)\n" +
                "and install (green rows) and uninstall (red rows) selections" :
                "No updates available");
    }


    private void showUpdateDoneMessage() {
        showMessage("Update completed successfully.");
    }


    private void showNoUpdatesMessage() {
        showMessage("No updates available - you have the latest versions.");
    }


    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Check For Updates",
                JOptionPane.INFORMATION_MESSAGE);
    }

}
