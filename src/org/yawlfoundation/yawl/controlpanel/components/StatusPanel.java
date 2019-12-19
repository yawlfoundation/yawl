/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
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
            case Stopped  : setStatus(STOPPED, STOPPED_TEXT, false); break;
            case Stopping : setStatus(WAITING, STOPPING_TEXT, true); break;
            case Starting : setStatus(WAITING, STARTING_TEXT, true); break;
            case Running  : setStatus(RUNNING, RUNNING_TEXT, false); break;
        }
    }


    private void setStatus(Color backColor, String text, boolean showWaiting) {
        setColors(backColor);
        setText(text);
        enableWaitIcon(showWaiting);
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
        _statusLabel.setHorizontalTextPosition(JLabel.RIGHT);
        return _statusLabel;
    }


    private void enableWaitIcon(boolean show) {
        ImageIcon icon = IconLoader.get("wait.gif");
        _statusLabel.setIcon(show ? icon : null);
    }

}
