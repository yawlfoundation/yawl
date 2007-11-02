/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.gui;

import au.edu.qut.yawl.swingWorklist.YWorklistGUI;
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
    private static Logger logger;

    public TabbedEngineGUI() {
        super(new BorderLayout());

        logger = Logger.getLogger(this.getClass());

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

        Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = _frame.getSize();
        _frame.setLocation(screenSize.width / 2 - (labelSize.width / 2),
                screenSize.height / 2 - (labelSize.height / 2));
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
