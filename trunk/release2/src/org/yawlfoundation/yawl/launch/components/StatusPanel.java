package org.yawlfoundation.yawl.launch.components;

import org.yawlfoundation.yawl.launch.pubsub.EngineStatus;
import org.yawlfoundation.yawl.launch.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.launch.pubsub.Publisher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class StatusPanel extends JPanel implements EngineStatusListener {

    private JLabel _statusLabel;

    private static final Color STOPPED = new Color(255, 128, 128);
    private static final Color WAITING = new Color(230, 230, 64);
    private static final Color RUNNING = new Color(96, 192, 96);

    private static final String STOPPED_TEXT  = "Stopped";
    private static final String STARTING_TEXT = "Starting";
    private static final String STOPPING_TEXT = "Stopping";
    private static final String RUNNING_TEXT  = "Running";


    public StatusPanel() {
        super();
        buildUI();
        Publisher.addEngineStatusListener(this);
    }


    public void statusChanged(EngineStatus status) {
        switch (status) {
            case Stopped  : setBackground(STOPPED); setText(STOPPED_TEXT); break;
            case Stopping : setBackground(WAITING); setText(STOPPING_TEXT); break;
            case Starting : setBackground(WAITING); setText(STARTING_TEXT); break;
            case Running  : setBackground(RUNNING); setText(RUNNING_TEXT); break;
        }
    }


    private void setText(String text) {
        _statusLabel.setText("Engine is " + text);
    }


    private void buildUI() {
        setBorder(new EmptyBorder(10, 0, 10, 10));
        setPreferredSize(new Dimension(175,40));
        setMinimumSize(new Dimension(175,40));
        setMaximumSize(new Dimension(175,40));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());
        add(buildStatusLabel());
        add(Box.createHorizontalGlue());
        statusChanged(EngineStatus.Stopped);
    }


    private JLabel buildStatusLabel() {
        _statusLabel = new JLabel("", SwingConstants.CENTER);
        _statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _statusLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        _statusLabel.setForeground(new Color(96,96,96));
        _statusLabel.setFont(_statusLabel.getFont().deriveFont(Font.BOLD));
        return _statusLabel;
    }
}
