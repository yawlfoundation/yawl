package org.yawlfoundation.yawl.controlpanel.update;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.update.table.UpdateTable;
import org.yawlfoundation.yawl.controlpanel.util.WindowUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
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
    private Updater _updater;


    public UpdateDialog(JFrame mainWindow, Differ differ) {
        super(mainWindow);
        setResizable(false);
        setModal(false);
        setSize(new Dimension(600, 455));
        setTitle("YAWL " + YControlPanel.VERSION + " Updates");
        addOnCloseHandler(this);
        buildUI(differ);
        Publisher.addEngineStatusListener(this);
        Point p = WindowUtil.calcLocation(mainWindow, this);
        setLocation(p.x + 30, p.y + 30);
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Update")) {
            _updater = new Updater(this);
            _updater.start();
        }
    }


    public void propertyChange(PropertyChangeEvent event) { enableButton(); }

    public void statusChanged(EngineStatus status) { enableButton(); }

    public void refresh(Differ differ) {
        _table.refresh(differ);
    }

    protected UpdateTable getTable() { return _table; }


    private void addOnCloseHandler(final EngineStatusListener listener) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                Publisher.removeEngineStatusListener(listener);
                dispose();
            }
        });
    }


    private void addOnMoveHandler() {
        addComponentListener( new ComponentListener() {
            public void componentResized(ComponentEvent e) {}

            public void componentMoved(ComponentEvent e) {
                if (_updater != null) _updater.dialogMoved();
            }

            public void componentShown(ComponentEvent e) {}

            public void componentHidden(ComponentEvent e) {}
        });
    }

    private void buildUI(Differ differ) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        _table = new UpdateTable(differ);
        _table.addPropertyChangeListener(this);
        content.add(new JScrollPane(_table), BorderLayout.CENTER);
        content.add(getButtonBar(), BorderLayout.SOUTH);
        add(content);
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
        button.setPreferredSize(new Dimension(100, 30));
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private void enableButton() {
        _btnUpdate.setEnabled(_table.hasUpdates() && ! Publisher.isTransientStatus());
    }

}
