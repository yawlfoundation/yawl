package org.yawlfoundation.yawl.launch;

import org.yawlfoundation.yawl.launch.components.ButtonPanel;
import org.yawlfoundation.yawl.launch.components.LogoPanel;
import org.yawlfoundation.yawl.launch.components.StatusPanel;
import org.yawlfoundation.yawl.launch.pubsub.Publisher;
import org.yawlfoundation.yawl.launch.util.EngineMonitor;
import org.yawlfoundation.yawl.launch.util.TomcatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class YControlPanel extends JFrame {

    private ButtonPanel _buttonPanel;
    private EngineMonitor _engineMonitor;

    public static final String VERSION = "3.0";

    public YControlPanel() {
        super();
        _engineMonitor = new EngineMonitor();
        buildUI();
        initStatus();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new YControlPanel().setVisible(true);
            }
        });
    }




    private void buildUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setTitle("YAWL " + VERSION + " Control Panel");

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10,10,10,10));
        content.add(buildLeftPanel(), BorderLayout.CENTER);
        content.add(new ButtonPanel(this), BorderLayout.EAST);
        getContentPane().add(content);
        pack();
    }


    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new LogoPanel(_engineMonitor), BorderLayout.CENTER);
        panel.add(new StatusPanel(), BorderLayout.SOUTH);
        return panel;
    }


    private void initStatus() {
        if (TomcatUtil.isRunning()) {
            Publisher.announceRunningStatus();
        }
    }

}
