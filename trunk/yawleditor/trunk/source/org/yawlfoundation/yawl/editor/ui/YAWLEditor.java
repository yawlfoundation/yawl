/*
 * Created on 23/12/2003, 21:00:38
 * YAWLEditor v1.0
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.properties.YPropertySheet;
import org.yawlfoundation.yawl.editor.ui.specification.FileOperations;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.NetsPane;
import org.yawlfoundation.yawl.editor.ui.swing.YSplashScreen;
import org.yawlfoundation.yawl.editor.ui.swing.YStatusBar;
import org.yawlfoundation.yawl.editor.ui.swing.menu.PaletteBar;
import org.yawlfoundation.yawl.editor.ui.swing.menu.ToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLMenuBar;
import org.yawlfoundation.yawl.editor.ui.swing.specification.BottomPanel;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

/**
 * The core executable class of the YAWL Editor, responsible for bootstrapping the editor
 * This class is a singleton extension of JFrame.
 */

public class YAWLEditor extends JFrame implements FileStateListener {

    private BottomPanel bottomPanel;

    private static YPropertySheet sheet;
    private static PaletteBar paletteBar;
    private static NetsPane netsPane;
    private static YStatusBar statusBar;
    private static JSplitPane splitPane;
    private static YSplashScreen splashScreen;
    private static YAWLEditor INSTANCE;


    private YAWLEditor() {
        super();
        updateLoadProgress(5);
        establishConnections();
        buildInterface();
        Publisher.getInstance().subscribe(this);
    }


    public static YAWLEditor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new YAWLEditor();
        }
        return INSTANCE;
    }


    public static YStatusBar getStatusBar() { return statusBar; }

    public static YPropertySheet getPropertySheet() { return sheet; }

    public static NetsPane getNetsPane() { return netsPane; }

    public static PaletteBar getPalette() { return paletteBar; }


    public void setTitle(String title) {
        String titleSeparator = title.equals("") ? "" : " - ";
        super.setTitle("YAWLEditor" + titleSeparator + title);
    }


    public void showProblemList(String title, List<String> problemList) {
        try {
            bottomPanel.setProblemList(title, problemList);
            splitPane.setDividerLocation(0.8);
        }
        catch (Exception e) {
            List<String> stackMessageList = new LinkedList<String>();
            stackMessageList.add(e.getMessage());
            bottomPanel.setProblemList("Program Exception with problem list generation",
                    stackMessageList);
        }
    }


    public void specificationFileStateChange(FileState state) {
        switch(state) {
            case Open: {
                String title = SpecificationModel.getHandler().getFileName();
                if (title != null) setTitle(title);
                break;
            }
            case Closed: {
                setTitle("");
                hideBottomOfSplitPane();
                break;
            }
        }
    }


    public static void main(String[] args) {
        showGUI(validateParameter(args));
    }


    /****************************************************************************/

    private static void showGUI(final String fileName) {
        SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 setLookAndFeel();
                 startLoading();
                 getInstance().setVisible(true);
                 finishLoading();
                 hideBottomOfSplitPane();
                 if (fileName != null) FileOperations.open(fileName);
             }
        });
    }


    private static void hideBottomOfSplitPane() {
        splitPane.setDividerLocation((double)1);
    }


    private static String validateParameter(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: " + System.getProperty("java.class.path") +
                    " [yawlFile]");
            System.exit(1);
        }
        return args.length == 1 ? args[0] : null;
    }


    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        }
        catch (Exception e) {
            // accept default LaF
        }

        // move menu to screen top - only affects OSX installs
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "YAWL Editor");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }


    private void updateLoadProgress(int completionValue) {
        splashScreen.updateProgress(completionValue);
    }


    private static void startLoading() {
        splashScreen = new YSplashScreen();
        splashScreen.init();
        LogWriter.init(FileUtilities.getHomeDir());
    }

    private static void finishLoading() {
        splashScreen.close();
        splashScreen = null;
    }


    private void buildInterface() {
        statusBar = new YStatusBar();
        paletteBar = new PaletteBar();
        sheet = new YPropertySheet();
        setJMenuBar(new YAWLMenuBar(splashScreen));
        Container pane = getContentPane();

        pane.setLayout(new BorderLayout());
        pane.add(getToolbarMenuPanel(), BorderLayout.NORTH);
        pane.add(getVerticalSplitPane(), BorderLayout.CENTER);
        pane.add(getStatusBar(),BorderLayout.SOUTH);
        setTitle("");

        setIconImage(
                ResourceLoader.getImageAsIcon(
                        "/org/yawlfoundation/yawl/editor/ui/resources/applicationIcon.gif"
                ).getImage()
        );

        updateLoadProgress(90);
        processPreferences();
        installEventListeners();
        updateLoadProgress(95);
    }


    private void installEventListeners() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            private boolean closing = false;
            public void windowClosing(WindowEvent we) {
                synchronized(this) {
                    if (! closing) {
                        closing = true;
                        FileOperations.exit();
                        closing = false;
                    }
                }
            }
        });
        final JFrame frame = this;
        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent event) {
                savePosition();
            }

            public void componentResized(ComponentEvent event) {
                savePosition();
                UserSettings.setFrameWidth(frame.getWidth());
                UserSettings.setFrameHeight(frame.getHeight());
            }

            private void savePosition() {
                UserSettings.setFrameLocation(frame.getX(), frame.getY());
            }
        });
    }

    private JSplitPane getVerticalSplitPane() {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(getTopPanel());
        splitPane.setBottomComponent(getBottomPanel());
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0);
        splitPane.setOneTouchExpandable(true);
        return splitPane;
    }


    private JSplitPane getTopPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(getLeftPane());
        updateLoadProgress(70);
        splitPane.setRightComponent(getEditPanel());
        updateLoadProgress(80);
        splitPane.setDividerSize(8);
        splitPane.setResizeWeight(0);
        splitPane.setOneTouchExpandable(true);
        return splitPane;
    }


    private JPanel getToolbarMenuPanel() {
        JPanel toolbarMenuPanel = new JPanel();
        toolbarMenuPanel.setLayout(new GridLayout(1,0));
        ToolBarMenu menu = new ToolBarMenu();
        toolbarMenuPanel.add(menu);
        return toolbarMenuPanel;
    }


    private JPanel getLeftPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(paletteBar, BorderLayout.NORTH);
        panel.add(getPropertiesPane(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel getBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        bottomPanel = new BottomPanel();
        panel.add(bottomPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getEditPanel() {
        JPanel editPanel = new JPanel(new BorderLayout());
        editPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        netsPane = new NetsPane();
        editPanel.add(netsPane, BorderLayout.CENTER);
        return editPanel;
    }

    private JPanel getPropertiesPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Properties"));
        JScrollPane propertiesPane = new JScrollPane(sheet);
        panel.add(propertiesPane, BorderLayout.CENTER);
        Dimension size = sheet.getPreferredSize();
        sheet.getTable().setBackground(getBackground());   // init uncoloured
        panel.setMinimumSize(new Dimension((int) size.getWidth() + 20,
                (int) size.getHeight() + 50));
        return panel;
    }


    private void processPreferences() {
        setSize(UserSettings.getFrameWidth(), UserSettings.getFrameHeight());
        Point pos = UserSettings.getFrameLocation();
        if (pos.x == -1 || pos.y == -1) {
            JUtilities.centerWindow(this);
        }
        else {
            this.setLocation(pos.x, pos.y);
        }

        // initialise analysis 'off'
        UserSettings.setAnalyseOnSave(false);
        initResetNetAnalysisPreferences();
        initWofYAWLAnalysisPreferences();
    }

    private void initResetNetAnalysisPreferences() {
        UserSettings.setWeakSoundnessAnalysis(false);
        UserSettings.setCancellationAnalysis(false);
        UserSettings.setOrJoinAnalysis(false);
        UserSettings.setShowObservations(false);
    }

    private void initWofYAWLAnalysisPreferences() {
        UserSettings.setWofyawlAnalysis(false);
        UserSettings.setStructuralAnalysis(false);
        UserSettings.setBehaviouralAnalysis(false);
        UserSettings.setExtendedCoverability(false);
    }


    private void establishConnections() {
        YConnector.setUserID(UserSettings.getEngineUserid());
        YConnector.setPassword(UserSettings.getEnginePassword());
        try {
            YConnector.setEngineURL(UserSettings.getEngineHost(),
                    UserSettings.getEnginePort());
        }
        catch (MalformedURLException mue) {
            //
        }
        try {
            YConnector.setResourceURL(UserSettings.getResourceHost(),
                                UserSettings.getResourcePort());
        }
        catch (MalformedURLException mue) {
            //
        }
    }

}
