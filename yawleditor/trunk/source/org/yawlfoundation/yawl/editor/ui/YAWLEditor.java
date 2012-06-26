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

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.specification.ArchivingThread;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.FileState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationFileModelListener;
import org.yawlfoundation.yawl.editor.ui.swing.*;
import org.yawlfoundation.yawl.editor.ui.swing.menu.Palette;
import org.yawlfoundation.yawl.editor.ui.swing.menu.ToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLMenuBar;
import org.yawlfoundation.yawl.editor.ui.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.editor.ui.swing.specification.SpecificationBottomPanel;
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
import java.util.LinkedList;
import java.util.List;

/**
 * The core executable class of the YAWL Editor, responsible for  bootstrapping the editor
 * This class is a singleton extension of JFrame.
 */

public class YAWLEditor extends JFrame implements SpecificationFileModelListener {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static String loadFileName;

  private final Palette paletteBar = Palette.getInstance();

  private static JSplitPane splitPane;
  private static SpecificationBottomPanel specificationBottomPanel;
  private static YAWLEditorDesktop editDesktop;
  private static YAWLEditor INSTANCE;

  private static final JSplashScreen splashScreen = new JSplashScreen();
  private static final JStatusBar statusBar = new JStatusBar();



  public static YAWLEditor getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new YAWLEditor();
    }
    return INSTANCE;
  }

  public static void main(String[] args) {

    setLookAndFeel();

    startLoading();

    validateParameter(args);

    getInstance().setVisible(true);
    hideBottomOfSplitPane();  // Yes, I can only move the split pane when the editor is visible.

    processParametersAsNecessary();

    finishLoading();
  }

  private YAWLEditor() {
    super();
    updateLoadProgress(5);
      establishConnections();
    buildInterface();
    Publisher.getInstance().subscribe(this);
  }

  private static void setLookAndFeel() {

    // move menu to screen top - only affects mac installs
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "YAWL Editor");
    System.setProperty("apple.laf.useScreenMenuBar", "true");

//      try {
//          for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//              if ("Nimbus".equals(info.getName())) {
//                  UIManager.setLookAndFeel(info.getClassName());
//                  break;
//              }
//          }
//      } catch (Exception e) {
//          // stick with default laf
//      }
  }

  private static JSplashScreen getSplashScreen() {
    return splashScreen;
  }

  public static void updateLoadProgress(int completionValue) {
    getSplashScreen().updateProgressBar(completionValue);
  }

  private static void startLoading() {
    LogWriter.init(FileUtilities.getHomeDir());
    getSplashScreen().setContent(
            "/org/yawlfoundation/yawl/editor/ui/resources/yawlSplashScreen.jpg",
            SplashContent.getCopyright());

    getSplashScreen().show();
  }

  private static void finishLoading() {
    getSplashScreen().finish();
  }

  private static void validateParameter(String[] args) {
    if (args.length > 1) {
      LogWriter.warn("Usage: " + System.getProperty("java.class.path") + " [<EditorSaveFile>]");
      System.exit(1);
    }

    if (args.length == 1) {
      loadFileName = args[0];
    }
  }

  private static void processParametersAsNecessary() {
    if (loadFileName != null) {
      ArchivingThread.getInstance().open(loadFileName);
    }
  }

  private static JStatusBar getStatusBar() {
    return statusBar;
  }

  public static void setStatusBarText(String statusString) {
    getStatusBar().setStatusBarText(statusString);
  }

  public static void setStatusBarTextToPrevious(){
    getStatusBar().setStatusBarTextToPrevious();
  }

  public static void resetStatusBarProgress() {
    getStatusBar().resetStatusBarProgress();
  }

  public static void finishStatusBarProgress() {
    getStatusBar().finishStatusBarProgress();
  }

  public static void progressStatusBarOverSeconds(int seconds) {
    getStatusBar().progressStatusBarOverSeconds(seconds);
  }

  public static void setStatusMode(String component, boolean on) {
      getStatusBar().setStatusMode(component, on);
  }


  private void buildInterface() {

    setJMenuBar(new YAWLMenuBar());
    Container pane = this.getContentPane();

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

    ArchivingThread.getInstance().start();
    processPreferences();
    installEventListeners();

    updateLoadProgress(95);
  }


  public void setTitle(String title) {
    String titleSeparator = "";
    if (!title.equals("")) {
      titleSeparator = " - ";
    }
    super.setTitle("YAWLEditor" + titleSeparator + title);
  }

  private void installEventListeners() {
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      private boolean closing = false;
      public void windowClosing(WindowEvent we) {
        synchronized(this) {
          if (!closing) {
            closing = true;
            ArchivingThread.getInstance().exit();
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

    splitPane.setDividerSize(10);
    splitPane.setResizeWeight(0);
    splitPane.setOneTouchExpandable(true);

    return splitPane;
  }

  public void indicateSplitPaneActivity() {

    // We choose an animation here because I have
    // no control over the colour of the divider
    // via the Swing interface.

    final int originalDividerLocation = splitPane.getDividerLocation();

    splitPane.setDividerLocation(
      originalDividerLocation - 20
    );

    pause(200); 

    splitPane.setDividerLocation(
        originalDividerLocation
    );

    pause(200);

    splitPane.setDividerLocation(
        originalDividerLocation - 20
    );

    pause(200);

    splitPane.setDividerLocation(
        originalDividerLocation
    );
  }

  public void selectNotesTab() {
    specificationBottomPanel.selectNotesTab();
  }

  public void indicateProblemsTabActivity() {
    specificationBottomPanel.selectProblemsTab();
  }

  public void showProblemList(String title, List problemList) {
    try {
      ProblemMessagePanel.getInstance().setProblemList(
          title,
          problemList
      );
      splitPane.setDividerLocation(0.8);

    } catch (Exception e) {

      LinkedList<String> stackMessageList = new LinkedList<String>();
      stackMessageList.add(e.getMessage());

      ProblemMessagePanel.getInstance().setProblemList(
          "Programming Exception with problem list generation",
          stackMessageList
      );
    }
  }


  private JSplitPane getTopPanel() {
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    splitPane.setLeftComponent(getPalette());

    updateLoadProgress(70);

    splitPane.setRightComponent(getEditPanel());

    updateLoadProgress(80);

    splitPane.setDividerSize(10);
    splitPane.setResizeWeight(0);
    splitPane.setOneTouchExpandable(true);

    return splitPane;
  }

  private JPanel getToolbarMenuPanel() {
    JPanel toolbarMenuPanel = new JPanel();
    toolbarMenuPanel.setLayout(new BoxLayout(toolbarMenuPanel, BoxLayout.X_AXIS));
    toolbarMenuPanel.add(new ToolBarMenu());
    toolbarMenuPanel.add(Box.createVerticalGlue());
    return toolbarMenuPanel;
  }

  private Palette getPalette() {
    return this.paletteBar;
  }

  private JPanel getBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());

    specificationBottomPanel = new SpecificationBottomPanel();

    bottomPanel.add(
        specificationBottomPanel,
        BorderLayout.CENTER
    );

    return bottomPanel;
  }

  private JPanel getEditPanel() {
    JPanel editPanel = new JPanel(new BorderLayout());
    editDesktop = YAWLEditorDesktop.getInstance();
    editPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    editPanel.add(editDesktop, BorderLayout.CENTER);
    return editPanel;
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


  public static void hideBottomOfSplitPane() {
    splitPane.setDividerLocation((double)1);
  }

  public void specificationFileStateChange(FileState state) {
    switch(state) {
      case Ready: {
        String title = SpecificationModel.getInstance().getFileName();
        if (title != null) setTitle(title);
        break;
      }
      case Idle: {
        setTitle("");
        splitPane.setDividerLocation((double)1);
        break;
      }
    }
  }


    private void establishConnections() {
        YConnector.setEngineUserID(UserSettings.getEngineUserid());
        YConnector.setEnginePassword(UserSettings.getEnginePassword());
        YConnector.setEngineURL(UserSettings.getEngineUri());

        YConnector.setResourceUserID(UserSettings.getResourceUserid());
        YConnector.setResourcePassword(UserSettings.getResourcePassword());
        YConnector.setResourceURL(UserSettings.getResourceUri());
    }


  public static void pause(long milliseconds) {
    Object lock = new Object();
    long now = System.currentTimeMillis();
    long finishTime = now + milliseconds;
    while (now < finishTime) {
      long timeToWait = finishTime - now;
      synchronized (lock) {
         try {
           lock.wait(timeToWait);
         }
         catch (InterruptedException ex) {
         }
      }
      now = System.currentTimeMillis();
    }
  }

}
