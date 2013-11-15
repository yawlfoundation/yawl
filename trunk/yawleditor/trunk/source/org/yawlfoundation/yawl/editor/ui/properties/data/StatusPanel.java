/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.swing.MoreDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 15/08/13
 */
public class StatusPanel extends JPanel {

    private JLabel _statusLabel;
    private JButton _btnMore;
    private java.util.List<String> _moreText;
    private Window _owner;

    public static final Color ERROR = Color.RED;
    public static final Color WARNING = Color.YELLOW;
    public static final Color INFO = Color.BLUE;
    public static final Color OK = Color.GREEN;


    public StatusPanel(Window owner) {
        super(new BorderLayout());
        _owner = owner;
        _statusLabel = new JLabel();
        add(_statusLabel, BorderLayout.WEST);
        add(createMoreButton(), BorderLayout.CENTER);
    }


    public void clear() {
        _statusLabel.setText(null);
        _moreText = null;
        _statusLabel.setVisible(false);
        _btnMore.setVisible(false);
    }

    public void set(String text) {
        set(text, ERROR);
    }

    public void set(String text, Color foreColor) {
        set(text, foreColor, null);
    }

    public void set(String text, java.util.List<String> moreText) {
        set(text, ERROR, moreText);
    }

    public void set(String text, Color foreColor, java.util.List<String> moreText) {
        _statusLabel.setForeground(foreColor);
        _statusLabel.setText(text);
        _statusLabel.setVisible(true);
        _moreText = moreText;
        _btnMore.setVisible(moreText != null);
    }


    private JButton createMoreButton() {
        _btnMore = new JButton();
        _btnMore.setText("<HTML><FONT color=\"#112DCD\"><U><I>more...</I></U></FONT></HTML >");
        _btnMore.setHorizontalAlignment(SwingConstants.LEFT);
        _btnMore.setBorderPainted(false);
        _btnMore.setOpaque(false);
        _btnMore.setFocusPainted(false);
        _btnMore.setActionCommand("more");
        _btnMore.setVisible(false);
        _btnMore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                MoreDialog dialog = new MoreDialog(_owner, _moreText);
                dialog.setLocationAdjacentTo(_btnMore, _btnMore.getVisibleRect());
                dialog.setVisible(true);
            }
        });

        return _btnMore;
    }

}
