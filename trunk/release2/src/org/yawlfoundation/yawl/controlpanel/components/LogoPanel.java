package org.yawlfoundation.yawl.controlpanel.components;

import org.simplericity.macify.eawt.DefaultApplication;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.util.EngineMonitor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class LogoPanel extends JPanel implements ActionListener, EngineStatusListener {

    private EngineMonitor _engineMonitor;
    private JLabel _lblIcon;
    private int _waitIndex;
    private DefaultApplication _macApp;     // OS X only


    public LogoPanel(EngineMonitor monitor) {
        super();
        _engineMonitor = monitor;
        setBorder(new EmptyBorder(0, 24, 10, 0));
        setLayout(new BorderLayout());
        setMacApp();
        buildUI();
        Publisher.addEngineStatusListener(this);
    }


    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped:  { setWaiting(false); setLogo("Stopped"); break; }
            case Stopping:
            case Starting: { setWaiting(true); break; }
            case Running:  { setWaiting(false); setLogo("Running"); break; }
        }
    }


    // triggered by timer
    public void actionPerformed(ActionEvent event) {
        if (_waitIndex == 5) _waitIndex = 1;
        setLogo("Waiting" + _waitIndex++);
    }


    private void setMacApp() {
        String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().startsWith("mac")) {
            _macApp = new DefaultApplication();
        }
    }


    private void buildUI() {
        _lblIcon = new JLabel();
        add(_lblIcon, BorderLayout.CENTER);
        setLogo("Stopped");
    }


    private void setLogo(String name) {
        ImageIcon icon = IconLoader.get("Yawl" + name);
        _lblIcon.setIcon(icon);
        if (_macApp != null) {
            _macApp.setApplicationIconImage((BufferedImage) icon.getImage());
        }
    }


    private void setWaiting(boolean start) {
        if (start) {
            _waitIndex = 1;
            _engineMonitor.addListener(this);
        }
        else _engineMonitor.removeListener(this);
    }

}
