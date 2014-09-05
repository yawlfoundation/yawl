package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatusListener;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class StatusPanel extends JPanel implements EngineStatusListener {

    private JLabel _statusLabel;

    private static final Color STOPPED = new Color(255, 54, 53);
    private static final Color WAITING = new Color(253, 235, 14);
    private static final Color RUNNING = new Color(25, 192, 37);

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
            case Stopped  : setColors(STOPPED); setText(STOPPED_TEXT); break;
            case Stopping : setColors(WAITING); setText(STOPPING_TEXT); break;
            case Starting : setColors(WAITING); setText(STARTING_TEXT); break;
            case Running  : setColors(RUNNING); setText(RUNNING_TEXT); break;
        }
    }


    private void setColors(Color backColor) {
        setBackground(backColor);
        _statusLabel.setForeground(backColor == WAITING ? Color.DARK_GRAY : Color.WHITE);
    }


    private void setText(String text) {
        _statusLabel.setText("Engine is " + text);
    }


    private void buildUI() {
        setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(10, 0, 10, 10)));
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
        _statusLabel.setForeground(Color.WHITE);
        _statusLabel.setFont(_statusLabel.getFont().deriveFont(Font.BOLD));
        return _statusLabel;
    }
}
