/*
 * Created on 23/12/2003, 21:00:38
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import javax.swing.WindowConstants;

import au.edu.qut.yawl.editor.foundations.ResourceLoader;
import au.edu.qut.yawl.editor.foundations.ArchivingThread;

import au.edu.qut.yawl.editor.specification.SpecificationArchiveHandler;
import au.edu.qut.yawl.editor.specification.SpecificationFileModel;
import au.edu.qut.yawl.editor.specification.SpecificationFileModelListener;
import au.edu.qut.yawl.editor.swing.JSplashScreen;
import au.edu.qut.yawl.editor.swing.JStatusBar;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;
import au.edu.qut.yawl.editor.swing.menu.Palette;
import au.edu.qut.yawl.editor.swing.menu.ToolBarMenu;
import au.edu.qut.yawl.editor.swing.menu.YAWLMenuBar;
import au.edu.qut.yawl.editor.swing.specification.ProblemMessagePanel;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

/**
 * The core executable class of the YAWL Editor, responsible for  bootstrapping the editor
 * This class is a singleton extension of JFrame.
 */

public class YAWLEditor extends JFrame implements SpecificationFileModelListener {

  protected final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

  private final Palette paletteBar = Palette.getInstance();
  
  private static JSplitPane splitPane;

  private static YAWLEditor INSTANCE;
  
  public static YAWLEditor getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new YAWLEditor();
    }
    return INSTANCE;
  }  

  public static void main(String[] args) {
    getInstance().setVisible(true);
    setInitialSplitPanePosition();
  }
  
	private YAWLEditor() {
	  super();
    buildInterface();
    SpecificationFileModel.getInstance().subscribe(this);
	}
  
  private void buildInterface() {
    JSplashScreen.getInstance().setContent(
        "/au/edu/qut/yawl/editor/resources/yawlSplashScreen.jpg",
        "YAWLEditor" + getSizeDistinction() + " v " +
        getVersionNumber() + " - (c) 2004 Queensland University of Technology");

    JSplashScreen.getInstance().show();

    setJMenuBar(new YAWLMenuBar());  
    Container pane = this.getContentPane();

    pane.setLayout(new BorderLayout());
    pane.add(getToolbarMenuPanel(), BorderLayout.NORTH);
    
    pane.add(getSplitPane(), BorderLayout.CENTER);
    pane.add(JStatusBar.getInstance(),BorderLayout.SOUTH);

    setTitle("");

    setIconImage(
      ResourceLoader.getImageAsIcon(
        "/au/edu/qut/yawl/editor/resources/applicationIcon.gif"
      ).getImage()
    );

    JSplashScreen.getInstance().updateProgressBar(90);

    ArchivingThread.getInstance().start();
    processPreferences(); 
    installEventListeners();
    
    
    JSplashScreen.getInstance().finish();
  }
		
	public void setTitle(String title) {
    String titleSeparator = "";
    if (!title.equals("")) {
      titleSeparator = " - ";
    }
    super.setTitle("YAWLEditor" + titleSeparator + title);		
	}
	
	private void installEventListeners() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		  public void windowClosing(WindowEvent we) {
       SpecificationArchiveHandler.getInstance().exit();
		  }
		});
		final JFrame frame = this;
    addComponentListener(
      new ComponentAdapter() {
        public void componentMoved(ComponentEvent event) {
          rememberLocation();
        }
        
        public void componentResized(ComponentEvent event) {
          rememberLocation();

          prefs.putInt("width", frame.getWidth());
          prefs.putInt("height", frame.getHeight());
        } 
        
        private void rememberLocation() {
          prefs.putInt("posX",frame.getX());
          prefs.putInt("posY",frame.getY());
        }
    });
	}
  
  private JSplitPane getSplitPane() {
    splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    splitPane.setTopComponent(getTopPanel());
    splitPane.setBottomComponent(getBottomPanel());

    splitPane.setDividerSize(10);
    splitPane.setResizeWeight(1);
    splitPane.setOneTouchExpandable(true);
    
    return splitPane;
  }
  
  public void showVerificationDetail() {
    final int potentialNewHeight = 
      splitPane.getHeight() - 
      (int) splitPane.getBottomComponent().getPreferredSize().getHeight() - 
      splitPane.getDividerSize();
    
    if (splitPane.getDividerLocation() >= splitPane.getMaximumDividerLocation()) {
      splitPane.setDividerLocation(
          Math.max(
              potentialNewHeight, 
              splitPane.getMinimumDividerLocation()
          )
      );
      return;      
    }
    
    if (potentialNewHeight > splitPane.getDividerLocation() &&
        potentialNewHeight < splitPane.getMaximumDividerLocation()) {
      splitPane.setDividerLocation(potentialNewHeight);
    }
  }

  private JPanel getTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    
    JSplashScreen.getInstance().updateProgressBar(70);
    topPanel.add(getPalettePanel(), BorderLayout.WEST);
    JSplashScreen.getInstance().updateProgressBar(80);
    topPanel.add(getEditPanel(), BorderLayout.CENTER);

    return topPanel;
  }

  private JPanel getToolbarMenuPanel() {
    JPanel toolbarMenuPanel = new JPanel();
    toolbarMenuPanel.setLayout(new BoxLayout(toolbarMenuPanel, BoxLayout.X_AXIS));
    toolbarMenuPanel.add(new ToolBarMenu());
    toolbarMenuPanel.add(Box.createVerticalGlue());
    return toolbarMenuPanel;
  }
    
  private JPanel getPalettePanel() {
    JPanel palettePanel = new JPanel();
    palettePanel.setLayout(new BoxLayout(palettePanel, BoxLayout.Y_AXIS));
    palettePanel.add(this.paletteBar);
    palettePanel.add(Box.createHorizontalGlue());
    return palettePanel;
  }
  
  private JPanel getBottomPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());

    bottomPanel.add(ProblemMessagePanel.getInstance(), BorderLayout.CENTER);
    
    return bottomPanel;
  }
  
  private JPanel getEditPanel() {
    JPanel editPanel = new JPanel(new BorderLayout());
    YAWLEditorDesktop editDesktop = YAWLEditorDesktop.getInstance();
    editPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2)); 
    final JScrollPane scrollPane = new JScrollPane(editDesktop);
    editDesktop.setScrollPane(scrollPane);       
    editPanel.add(scrollPane, BorderLayout.CENTER);
    return editPanel;
  }
  
  private void processPreferences() {
    setSize(prefs.getInt("width", 500), 
            prefs.getInt("height", 300));

    final int posX = prefs.getInt("posX",-1);
    final int posY = prefs.getInt("posY",-1);

    if (posX == -1 || posY == -1) {
      JUtilities.centerWindow(this);
    } else {
      this.setLocation(posX, posY);
    }
  }
  
  private static void setInitialSplitPanePosition() {
    // modifying divider location only works once the split
    // pane is visible. The Swing javadocs tell me its true,
    // and it is... 8(
    
    if (!YAWLEngineProxy.engineLibrariesAvailable()) {
      // splitPane.setEnabled(false);   Swing bug?
      splitPane.getBottomComponent().setVisible(false);
      splitPane.setDividerSize(0);
    }
    splitPane.setDividerLocation((double)1);
  }
  
  private String getSizeDistinction() {
    if (YAWLEngineProxy.engineLibrariesAvailable()) {
      return "";   
    } 
    return "Lite";
  }
  
  private String getVersionNumber() {
    return "1.2";
  }

  public void specificationFileModelStateChanged(int state) {
    switch(state) {
      case SpecificationFileModel.EDITING: {
        String title = SpecificationFileModel.getInstance().getFileName(); 
        if (title != null) {
          setTitle(title);
        }
        break;
      }
      case SpecificationFileModel.IDLE: {
        setTitle("");
        splitPane.setDividerLocation((double)1);
        break;
      }
      default: {
        break;
      }
    }
  }
}
