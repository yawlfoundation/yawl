package org.yawlfoundation.yawl.controlpanel;

import org.yawlfoundation.yawl.controlpanel.components.MacIcon;
import org.yawlfoundation.yawl.controlpanel.components.OutputPanel;
import org.yawlfoundation.yawl.controlpanel.components.ToolBar;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.update.ChecksumsReader;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class YControlPanel extends JFrame {

    private EngineMonitor _engineMonitor;

    public static final String VERSION = "3.1";


    public YControlPanel() {
        super();
        _engineMonitor = new EngineMonitor();
        buildUI();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                doOnExit();
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


    private String getAppTitle() { return "YAWL Control Panel " + getVersion(); }


    private void buildUI() {
        setLayout(new BorderLayout());
        setResizable(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(getAppTitle());
        setIconImage(getFrameIcon());

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10,7,7,7));
        content.add(new OutputPanel(), BorderLayout.CENTER);
        ToolBar tb = new ToolBar(this);
        content.add(tb, BorderLayout.NORTH);
        add(content);
        pack();
        setLocationByPlatform(true);
        tb.performUserPreferencesOnStart();
        setMacIcon();
    }


    private void doOnExit() {
        if (TomcatUtil.isEngineRunning()) {
            shutdownTomcat();
        }
        else {
            System.exit(0);
        }
    }


    private Image getFrameIcon() {
        ImageIcon icon = IconLoader.get("YawlRunning");
        return icon != null ? icon.getImage() : null;
    }


    private void setMacIcon() {
        if (FileUtil.isMac()) {
            new MacIcon(_engineMonitor);
        }
    }


    private String getVersion() {
        ChecksumsReader props = new ChecksumsReader(FileUtil.getLocalCheckSumFile());
        XNode node = props.getControlPanelNode();
        return VERSION + (node != null ? "." + node.getChildText("build") : "");
    }


    private void shutdownTomcat() {
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel();
        panel.add(new JLabel(IconLoader.get("wait.gif")));
        panel.add(new JLabel("Shutting down server, please wait..."));
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // wait until shutdown completes before exiting
        TomcatUtil.stop(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("state") &&
                        event.getNewValue() == SwingWorker.StateValue.DONE) {
                    System.exit(0);
                }
            }
        });

        dialog.setVisible(true);
    }

}
