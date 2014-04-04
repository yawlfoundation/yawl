/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.gui;

import org.yawlfoundation.yawl.swingWorklist.YWorklistGUI;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;

/**
 * 
 * @author Lachlan Aldred
 * Date: 17/01/2005
 * Time: 19:05:50
 */

public class TabbedEngineGUI extends JPanel {
    static JFrame _frame;
    private static YSplash _splash;
    private JTabbedPane _tabbedPane;
    private static boolean journalising = false;
    private static boolean generateUIMetaData = false;
    private static final Logger logger = Logger.getLogger(TabbedEngineGUI.class);

    public TabbedEngineGUI() {
        super(new BorderLayout());
        logger.debug("Init");

        ImageIcon logo = new ImageIcon(
                getToolkit().getImage(
                        getClass().getResource("YAWL_Splash2.jpg")));

        JLabel l = new JLabel(logo);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(l, BorderLayout.NORTH);

        _tabbedPane = new JTabbedPane();

        JComponent panel1 = makeAdminPanel();
        _tabbedPane.addTab("Administration", panel1);

        YAdminGUI adminPanel = (YAdminGUI) panel1;
        adminPanel.loadWorklists();

        panel1.setPreferredSize(new Dimension(800, 600));
        _tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        add(leftPanel, BorderLayout.WEST);
        //Add the tabbed pane to this panel.
        add(_tabbedPane, BorderLayout.EAST);
        //Uncomment the following line to use scrolling tabs.
        //_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    private JComponent makeAdminPanel() {
        JPanel panel = new YAdminGUI(_splash, _frame, this, journalising, generateUIMetaData);

        return panel;
    }

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {

        java.net.URL imgURL = TabbedEngineGUI.class.getResource("YAWLIcon.jpg");
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * * Create the GUI and show it. For thread safety,
     * * this method should be invoked from the * event-dispatching thread.
     * */
    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        //JFrame.setDefaultLookAndFeelDecorated(true);
        //Create and set up the window.
        _frame = new JFrame("YAWL Engine : stand-alone version");

        if (journalising) {
            _frame.setTitle(_frame.getTitle() + " [Persistent mode]");
        }

        _splash = new YSplash("YAWL_Splash.gif", _frame, 300);
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create and set up the content pane.
        JComponent newContentPane = new TabbedEngineGUI();
        newContentPane.setOpaque(true);
        //content panes must be opaque
        _frame.getContentPane().add(new TabbedEngineGUI(), BorderLayout.CENTER);
        //Display the window.
        URL iconURL = YAdminGUI.class.getResource("YAWLIcon.jpg");
        _frame.setIconImage(Toolkit.getDefaultToolkit().createImage(iconURL));

        _frame.pack();

/**
 * AJH: Changed to support dual head X environments better
 */
        Dimension labelSize = _frame.getSize();
        Double screenWidth = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth());
        Double screenHeight = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());

        _frame.setLocation(screenWidth.intValue() / 2 - (labelSize.width / 2),
                    screenHeight.intValue() / 2 - (labelSize.height / 2));
        _frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.

        // If we have a -p flag, assuming a journalising engine
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-p")) {
                journalising = true;
            }
            if (args[i].equalsIgnoreCase("-uim")) {
                generateUIMetaData = true;
            }
        }

        createAndShowGUI();
    }


    public void addWorklistPanel(String userName, YWorklistGUI worklist) {
        _tabbedPane.addTab(userName + "'s Worklist", worklist);
        _tabbedPane.repaint();
    }
}
