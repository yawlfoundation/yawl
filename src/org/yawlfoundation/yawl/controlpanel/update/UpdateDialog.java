package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class UpdateDialog extends JDialog
        implements ActionListener, PropertyChangeListener, EngineStatusListener {

    private UpdateTable _table;
    private JButton _btnUpdate;
    private JButton _btnClose;
    private Updater _updater;
    private ProgressPanel _progressPanel;


    public UpdateDialog(JFrame mainWindow, Differ differ) {
        super(mainWindow);
        setResizable(false);
        setModal(false);
        setSize(new Dimension(600, 480));
        setTitle("YAWL " + YControlPanel.VERSION + " Updates");
        addOnCloseHandler(this);
        buildUI(differ);
        Publisher.addEngineStatusListener(this);
        setLocationRelativeTo(mainWindow);
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Update")) {
            _btnUpdate.setEnabled(false);
            _btnClose.setEnabled(false);
            _updater = new Updater(this);
            _updater.start();
        }
        else {
            setVisible(false);
        }
    }


    public void propertyChange(PropertyChangeEvent event) { enableButtons(); }

    public void statusChanged(EngineStatus status) { enableButtons(); }

    public void refresh(Differ differ) {
        _progressPanel.setVisible(false);
        _table.refresh(differ);
        enableButtons();               // will disable due to no pending/selected updates
    }


    protected UpdateTable getTable() { return _table; }

    protected ProgressPanel getProgressPanel() { return _progressPanel; }


    private void addOnCloseHandler(final EngineStatusListener listener) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                Publisher.removeEngineStatusListener(listener);
                dispose();
            }
        });
    }



    private void buildUI(Differ differ) {
        _table = new UpdateTable(differ);
        _table.addPropertyChangeListener(this);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        content.add(new JScrollPane(_table), BorderLayout.CENTER);
        content.add(getButtonBar(), BorderLayout.SOUTH);
        content.setBounds(0, 0, 600, 455);

        _progressPanel = new ProgressPanel();
        _progressPanel.setBounds(150, 0, 300, 70);
        _progressPanel.setVisible(false);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.add(content, new Integer(0));
        layeredPane.add(_progressPanel, new Integer(1));
        layeredPane.moveToFront(_progressPanel);

        add(layeredPane);
    }


    private JPanel getButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        _btnUpdate = createButton("Update",
                "Update to latest versions (blue rows)\n" +
                        "and install (green rows) and uninstall (red rows) selections");
        panel.add(_btnUpdate);
        _btnClose = createButton("Close", "");
        panel.add(_btnClose);
        return panel;
    }


    private JButton createButton(String label, String tip) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(100, 30));
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private void enableButtons() {
        _btnUpdate.setEnabled(_table.hasUpdates() && ! Publisher.isTransientStatus());
        setButtonTip();
        _btnClose.setEnabled(true);
    }


    private void setButtonTip() {
        _btnUpdate.setToolTipText(_btnUpdate.isEnabled() ?
                "Update to latest versions (blue rows)\n" +
                "and install (green rows) and uninstall (red rows) selections" :
                "No updates available");
    }

}
