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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Michael Adams
 * @date 14/08/13
 */
public class MoreDialog extends JDialog {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 80;

    private final AWTEventListener _appWideMouseListener = new AWTEventListener() {
        int count = 0;       // ignore the click that opened the dialog
        public void eventDispatched(AWTEvent event) {
            if (event instanceof MouseEvent) {
                MouseEvent evt = (MouseEvent) event;
                if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
                    if (count > 0) setVisible(false);
                    count++;
                }
            }
        }
    };


    public MoreDialog(Window owner, String text) {
        super(owner);
        init(owner, text);
    }

    public MoreDialog(Window owner, java.util.List<String> textList) {
        super(owner);
        init(owner, coalesceText(textList));
    }


    public MoreDialog(Component parent, String text) {
        super(YAWLEditor.getInstance());
        init(parent, text);
    }


    public void setLocationAdjacentTo(Component c, Rectangle rect) {
        Point screenLocation = c.getLocationOnScreen();
        Point relativeLocation = rect != null ? rect.getLocation() : new Point(0,0);
        Point offset = rect != null ?
                new Point((int) rect.getWidth() / 4,  (int) rect.getHeight()) :
                new Point(150, 10);
        Point adjacentLocation = new Point(
                (int) (screenLocation.getX() + relativeLocation.getX() + offset.getX()),
                (int) (screenLocation.getY() + relativeLocation.getY() + offset.getY()));
        setLocation(adjustLocationForScreenEdges(adjacentLocation, offset));
    }


    public void setVisible(boolean visible) {
        if (visible) {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                    _appWideMouseListener, AWTEvent.MOUSE_EVENT_MASK);
        }
        super.setVisible(visible);
    }


    private void init(Component c, String text) {
        setUndecorated(true);
        setModal(false);
        add(getContent(text));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(c);

        addWindowStateListener(new WindowAdapter() {
            public void windowDeactivated(WindowEvent windowEvent) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(_appWideMouseListener);
                super.windowDeactivated(windowEvent);
            }
        });

        pack();
    }


    private String coalesceText(java.util.List<String> textList) {
        StringBuilder s = new StringBuilder();
        if (textList != null) {
            for (String text : textList) {
                if (! text.contains("foo_bar")) {   // dummy task name for verification
                    s.append(text).append('\n');
                }
            }
        }
        return s.toString();
    }

    private JScrollPane getContent(String text) {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(255, 254, 226));
        textArea.setForeground(Color.DARK_GRAY);
        textArea.setMargin(new Insets(5, 7, 5, 7));
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);

        textArea.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setVisible(false);
            }
        });

        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                setVisible(false);
            }
        });

        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        return pane;
    }

    private Point adjustLocationForScreenEdges(Point p, Point offset) {
        GraphicsConfiguration gc = YAWLEditor.getInstance().getGraphicsConfiguration();
        DisplayMode mode = gc.getDevice().getDisplayMode();  // current screen
        double x = p.getX();
        double y = p.getY();
        if (mode.getHeight() < y + HEIGHT) {
            y = y - HEIGHT - offset.getY();            // flip it
        }
        if (mode.getWidth() < p.getX() + WIDTH) {
            x = mode.getWidth() - WIDTH;               // slide it left
        }
        return new Point((int) x, (int) y);
    }

}
