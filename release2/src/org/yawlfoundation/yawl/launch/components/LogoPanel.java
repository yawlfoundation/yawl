package org.yawlfoundation.yawl.launch.components;

import org.yawlfoundation.yawl.launch.pubsub.EngineStatus;
import org.yawlfoundation.yawl.launch.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.launch.pubsub.Publisher;
import org.yawlfoundation.yawl.launch.util.EngineMonitor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class LogoPanel extends JPanel implements ActionListener, EngineStatusListener {

    private Map<String, ImageIcon> _iconMap;
    private EngineMonitor _engineMonitor;
    private JLabel _lblIcon;
    private int _waitIndex;


    public LogoPanel(EngineMonitor monitor) {
        super();
        _engineMonitor = monitor;
        setBorder(new EmptyBorder(0,0,10,0));
        setLayout(new BorderLayout());
        buildIconMap();
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


    private void buildUI() {
        _lblIcon = new JLabel(_iconMap.get("Stopped"));
        add(_lblIcon, BorderLayout.CENTER);
    }


    private void setLogo(String name) {
        _lblIcon.setIcon(_iconMap.get(name));
    }


    private void setWaiting(boolean start) {
        if (start) {
            _waitIndex = 1;
            _engineMonitor.addListener(this);
        }
        else _engineMonitor.removeListener(this);;
    }


    private void buildIconMap() {
        _iconMap = new HashMap<String, ImageIcon>();
        loadIcon("Running");
        loadIcon("Waiting1");
        loadIcon("Waiting2");
        loadIcon("Waiting3");
        loadIcon("Waiting4");
        loadIcon("Stopped");
    }


    private void loadIcon(String fileName) {
        String file = "../icons/Yawl" + fileName + ".png";
        try {
            InputStream stream = getClass().getResourceAsStream(file);
            _iconMap.put(fileName, new ImageIcon(ImageIO.read(stream)));
        }
        catch (IOException ioe) {
            // ignore this file
        }
    }

}
