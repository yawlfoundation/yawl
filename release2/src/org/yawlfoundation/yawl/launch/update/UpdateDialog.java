package org.yawlfoundation.yawl.launch.update;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.yawlfoundation.yawl.launch.YControlPanel;
import org.yawlfoundation.yawl.launch.update.table.UpdateTable;
import org.yawlfoundation.yawl.launch.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.launch.util.TomcatUtil;
import org.yawlfoundation.yawl.launch.util.WindowUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class UpdateDialog extends JDialog
        implements ActionListener, PropertyChangeListener {

    private UpdateTable _table;
    private JButton _btnUpdate;


    public UpdateDialog(JFrame mainWindow, Differ differ) {
        super(mainWindow);
        setResizable(false);
        setModal(false);
        setSize(new Dimension(600, 425));
        setTitle("YAWL " + YControlPanel.VERSION + " Updates");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI(differ);
        Point p = WindowUtil.calcLocation(mainWindow, this);
        setLocation(p.x + 30, p.y + 30);
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Update")) {
            new Updater(this).start();
        }
    }


    public void propertyChange(PropertyChangeEvent event) {
        _btnUpdate.setEnabled(_table.hasUpdates());
    }


    public void refresh(Differ differ) {
        _table.refresh(differ);
    }

    protected UpdateTable getTable() { return _table; }


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

}
