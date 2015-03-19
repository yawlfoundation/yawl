package org.yawlfoundation.yawl.controlpanel;

import org.yawlfoundation.yawl.controlpanel.components.ButtonPanel;
import org.yawlfoundation.yawl.controlpanel.components.LogoPanel;
import org.yawlfoundation.yawl.controlpanel.components.StatusPanel;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.preferences.UserPreferences;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class YControlPanel extends JFrame {

    private EngineMonitor _engineMonitor;

    public static final String VERSION = "3.0.1";

    public YControlPanel() {
        super();
        _engineMonitor = new EngineMonitor();
        buildUI();
        initStatus();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                doOnExit();
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


    private static String getAppTitle() { return "YAWL " + VERSION + " Control Panel"; }

    private void buildUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
        setTitle(getAppTitle());
        setIconImage(getFrameIcon());

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10,10,10,10));
        content.add(buildLeftPanel(), BorderLayout.CENTER);
        ButtonPanel bp = new ButtonPanel(this);
        content.add(bp, BorderLayout.EAST);
        getContentPane().add(content);
        pack();
        bp.performUserPreferencesOnStart();
    }


    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new LogoPanel(_engineMonitor), BorderLayout.CENTER);
        panel.add(new StatusPanel(), BorderLayout.SOUTH);
        return panel;
    }


    private void initStatus() {
        if (TomcatUtil.isRunning()) Publisher.announceRunningStatus();
    }


    private void doOnExit() {
        UserPreferences prefs = new UserPreferences();
        if (prefs.stopEngineOnExit() &&
                Publisher.getCurrentStatus() == EngineStatus.Running) {
            try {
                TomcatUtil.stop();
            }
            catch (IOException ignore) {
                //
            }
        }
    }


    private Image getFrameIcon() {
        ImageIcon icon = IconLoader.get("YawlRunning");
        return icon != null ? icon.getImage() : null;
    }

}
