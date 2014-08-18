package org.yawlfoundation.yawl.launch.update;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.yawlfoundation.yawl.launch.YControlPanel;
import org.yawlfoundation.yawl.launch.update.table.UpdateTable;
import org.yawlfoundation.yawl.launch.update.table.UpdateTableModel;
import org.yawlfoundation.yawl.launch.util.TomcatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class UpdateDialog extends JDialog implements ActionListener {

    private JTable _table;


    public UpdateDialog(JFrame mainWindow, Differ differ) {
        super(mainWindow);
        setResizable(false);
        setModal(false);
        setSize(new Dimension(600, 425));
        setTitle("YAWL " + YControlPanel.VERSION + " Updates Window");
        setLocationByPlatform(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI(differ);
    }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("Update")) {
            //update
        }
        else if (cmd.equals("(Un)Install")) {
            // do that
        }
    }


    private void buildUI(Differ differ) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(8, 8, 8, 8));
        _table = new UpdateTable(differ);
        content.add(new JScrollPane(_table), BorderLayout.CENTER);
        content.add(getButtonBar(), BorderLayout.SOUTH);
        add(content);
    }


    private JPanel getButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(createButton("Update", "Update to latest versions (orange rows)"));
        panel.add(createButton("(Un)Install",
                "Install new (green rows) or uninstall current (red rows) selections"));
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
