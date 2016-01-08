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
public class ComponentsPanel extends JLayeredPane
        implements ActionListener, PropertyChangeListener, EngineStatusListener {

    private UpdateTable _table;
    private JButton _btnUpdate;
    private ProgressPanel _progressPanel;
    private JPanel _content;


    public ComponentsPanel() {
        super();

        try {
            buildUI(getDiffer());
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

    public JPanel getTablePanel() { return _content; }

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

        _content = new JPanel(new BorderLayout());
        _content.setLayout(new BorderLayout());
        _content.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scrollPane = new JScrollPane(_table);
        scrollPane.setSize(_table.getPreferredSize());
        _content.add(scrollPane, BorderLayout.CENTER);
        _content.add(getButtonBar(), BorderLayout.SOUTH);
        _content.setBounds(0, 0, 600, 455);

        _progressPanel = new ProgressPanel();
        _progressPanel.setBounds(150, 190, 300, 70);
        _progressPanel.setVisible(false);

        add(_content, new Integer(0));
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
        button.setPreferredSize(new Dimension(75, 30));
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
