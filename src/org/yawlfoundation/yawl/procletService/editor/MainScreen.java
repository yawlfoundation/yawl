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

package org.yawlfoundation.yawl.procletService.editor;


import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.procletService.blockType.BlockCP;
import org.yawlfoundation.yawl.procletService.blockType.BlockPI;
import org.yawlfoundation.yawl.procletService.blockType.CompleteCaseDeleteCase;
import org.yawlfoundation.yawl.procletService.editor.block.BlockCoordinator;
import org.yawlfoundation.yawl.procletService.editor.blockchoiceexception.BlockExceptionChoiceCoordinator;
import org.yawlfoundation.yawl.procletService.editor.choiceexception.ExceptionChoiceCoordinator;
import org.yawlfoundation.yawl.procletService.editor.extra.GraphCoordinator;
import org.yawlfoundation.yawl.procletService.editor.model.ModelCoordinator;
import org.yawlfoundation.yawl.procletService.editor.pconns.PConnsCoordinator;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.models.procletModel.PortConnections;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModel;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.persistence.DBConnection;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class MainScreen {

  private static final int KEY_NEW_MODEL = KeyEvent.VK_N;
  private static final int KEY_QUIT = KeyEvent.VK_Q;
  private static final int KEY_OPEN_MODEL = KeyEvent.VK_O;
  private static final int KEY_SAVE_MODEL = KeyEvent.VK_S;
  private static final int KEY_DEL_MODEL = KeyEvent.VK_D;
  private static final int KEY_PCONN_MODEL = KeyEvent.VK_P;
  private static final int KEY_MODEL_NEW = KeyEvent.VK_P;
  private static final int KEY_MODEL_EXPORT_IMAGE = KeyEvent.VK_I;
  private static final int KEY_MODEL_VERIFY = KeyEvent.VK_V;
  private static final int KEY_BLOCK = KeyEvent.VK_R;
  private static final int KEY_DECISION = KeyEvent.VK_U;
  private static final int KEY_GRAPH = KeyEvent.VK_G;
  private static final int KEY_START_CASE = KeyEvent.VK_C;
  private static final int KEY_HANDLE_EXCEPTION = KeyEvent.VK_N;
  private static final int KEY_TEMPLATES = KeyEvent.VK_C;
  
  private static String YAWLlocation = "http://localhost:8080/yawl/ib";
  private static String ProcletServiceLocation = "http://localhost:8080/procletService/ib";
  private static String sUserId = "admin";
  private static String sPwd = "YAWL";


  /**
   * mainFrame is the main frame for the Designer.
   * Within mainFrame internal frames are open.
   */
  private MainFrame mainFrame = null;

  private JDesktopPane desktop;

  private JMenuItem newItem, save, delete, pconns;

    private Properties editorProps;


  private WindowMenu frames;

  public MainScreen() {
    mainFrame = new MainFrame();
    frames = new WindowMenu();
    frames.addListener(new WindowMenuListener() {
      public void itemSelected(JInternalFrame frame, boolean active) {
        if (active) {
          maximize(frame);
        }
      }
    });
    //assignmentCoordinator = new AssignmentCoordinator(mainFrame);
    //assignmentCoordinator.addListener(this);
    start();
    //assignmentCoordinator.init();

      loadProperties();
      setYawlProperties();
      DBConnection.init(getDbProperties());
  }

    private void loadProperties() {
        String fName = System.getProperty("user.dir") +
                            File.separator + "editor.properties";
        try {
            editorProps = new Properties();
            FileInputStream in = new FileInputStream(fName);
            editorProps.load(in);
            in.close();
        }
        catch (IOException ioe) {
            System.err.println("Error reading properties file: " + fName);
        }

    }

    private Properties getDbProperties() {
        Properties props = null;

        // try to find 'hibernate.properties' first
        String basePath = System.getenv("CATALINA_HOME");
        if (basePath != null) {
            String hibFile = File.separator + "hibernate.properties";
            String libPath = File.separator + "yawllib";
            File hibProp = new File(basePath + libPath + hibFile);   // 4Study
            if (! hibProp.exists()) {
                String procPath = File.separator + "webapps" + File.separator +
                           "procletService" + File.separator + "WEB-INF" +
                           File.separator + "classes";
                hibProp = new File(basePath + procPath + hibFile);   // service
            }
            if (hibProp.exists()) {
                try { 
                    props = new Properties() ;
                    props.load(new FileInputStream(hibProp));
                    String url = props.getProperty("hibernate.connection.url");
                    if (url != null && url.contains("catalina.base")) {
                        url = url.replace("${catalina.base}", basePath);
                        props.setProperty("hibernate.connection.url", url);
                    }
                    return props;
                }
                catch (Exception e) {
                    // fall through to below
                }
            }
        }
        if (! (editorProps == null || editorProps.isEmpty())) {
              props = DBConnection.configure(
                        editorProps.getProperty("dialect"),
                        editorProps.getProperty("driver"),
                        editorProps.getProperty("url"),
                        editorProps.getProperty("username"),
                        editorProps.getProperty("password"));
        }
        else {
            props = DBConnection.configure("org.hibernate.dialect.PostgreSQLDialect",
                        "org.postgresql.Driver", "jdbc:postgresql:yawl", "postgres", "yawl");
            System.err.println(
                    "   ... could not load database connection properties, using postgres defaults");
        }
        return props;
    }

    private void setYawlProperties() {
        if (! (editorProps == null || editorProps.isEmpty())) {
            YAWLlocation = editorProps.getProperty("yawluri", YAWLlocation);
            ProcletServiceLocation = editorProps.getProperty("serviceuri", ProcletServiceLocation);
            sUserId = editorProps.getProperty("serviceusername", sUserId);
            sPwd = editorProps.getProperty("servicepassword", sPwd);
        }
        else {
            System.err.println("   ... could not load YAWL connection properties, using defaults");
        }
    }

  /**
   * start
   */
  private void start() {

    boolean packFrame = false;

    //Set up the GUI.
    desktop = new JDesktopPane(); //a specialized layered pane
    mainFrame.setContentPane(desktop);
    mainFrame.setJMenuBar(createMenuBar());

    //Make dragging a little faster but perhaps uglier.
    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

    // Validate frames that have preset sizes
    // Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      mainFrame.pack();
    }
    else {
      mainFrame.validate();
    }

    // Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = mainFrame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    mainFrame.setLocation( (screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
    mainFrame.setVisible(true);
  }

  /**
   * maximize
   *
   * @param aFrame aFrame Maximize an internal frame aFrame.
   */
  private void maximize(JInternalFrame aFrame) {
    try {
      aFrame.setVisible(true);
      aFrame.setSelected(true);
      aFrame.setMaximum(true);
    }
    catch (Exception e) {}
  }
  
  /**
   * frameClosed / /* public void frameClosed() { internalFrame = null; }
    *
    * @param actionEvent ActionEvent
    */
   void childClose_actionPerformed(ActionEvent actionEvent) {}

  private void setMenuKey(JMenuItem menuItem, int key) {
    menuItem.setMnemonic(key);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
        key, ActionEvent.ALT_MASK));
  }

  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    //Set up the lone menu.
    JMenu jMenuAssignment = new JMenu("Model");
    jMenuAssignment.setMnemonic(KeyEvent.VK_P);
    menuBar.add(jMenuAssignment);

    //Set up the menu item for new model
    //jMenuAssignment.add(newItem());
    
    //Set up the menu item for newItem of the model
    this.newItem = new JMenuItem("New");
    setMenuKey(this.newItem, KEY_MODEL_NEW);
    this.newItem.setActionCommand("new");
    this.newItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  jModelNew_actionPerformed(e);
      }
    });

    jMenuAssignment.add(this.newItem);

    //Set up the menu item for open model
    JMenuItem menuItem = new JMenuItem("Open");
    setMenuKey(menuItem, KEY_OPEN_MODEL);
    menuItem.setActionCommand("open");
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  jModelOpen_actionPerformed(e);
      }
    });

    jMenuAssignment.add(menuItem);

    //Set up the menu item for save model
    this.save = new JMenuItem("Save");
    setMenuKey(this.save, KEY_SAVE_MODEL);
    this.save.setActionCommand("save");
    this.save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         jMenuSaveModel_actionPerformed(e);
        // do the save action
      }
    });

    jMenuAssignment.add(this.save);    
    
    // set up the menu for deleting a model
    this.delete = new JMenuItem("Delete");
    setMenuKey(this.delete, KEY_DEL_MODEL);
    this.delete.setActionCommand("delete");
    this.delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         jMenuDeleteModel_actionPerformed(e);
        // do the delete action
      }
    });
    
    jMenuAssignment.add(this.delete);   
    
    // set up the menu for the port connections
    this.pconns = new JMenuItem("External Interactions");
    setMenuKey(this.pconns, KEY_PCONN_MODEL);
    this.pconns.setActionCommand("pconns");
    this.pconns.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         jMenuPConnsModel_actionPerformed(e);
        // do the port connections action
      }
    });

    jMenuAssignment.add(this.pconns);   

    jMenuAssignment.addSeparator();

    //Set up the second menu item.
    menuItem = new JMenuItem("Quit");
    setMenuKey(menuItem, KEY_QUIT);
    menuItem.setActionCommand("quit");
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
    jMenuAssignment.add(menuItem);

    // setup the Design menu item
    JMenu jMenuDesign = new JMenu();
    jMenuDesign.setText("Interaction Point");
    menuBar.add(jMenuDesign);

    // setup the Organization menu item
    JMenuItem jMenuOrganization = new JMenuItem();
    setMenuKey(jMenuOrganization, KEY_BLOCK);
    jMenuOrganization.setText("Extend Interaction Graph CONFIGURATION Interaction Point");
    jMenuOrganization.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          java.util.List<InteractionNode> nodes = BlockCP.getInteractionNodesUser();
    	  if (!nodes.isEmpty()) { 
	    	  InteractionNode s = (InteractionNode)JOptionPane.showInputDialog(
	    	                      mainFrame,
	    	                      "select the workitem you want to work on",
	    	                      "Message",
	    	                      JOptionPane.PLAIN_MESSAGE,
	    	                      null,
	    	                      nodes.toArray(),
	    	                      nodes.get(0));
	    	  // register in DB that wi is selected
	    	  if (s != null) {
		    	  BlockCP.setWorkitemSelected(s.getClassID(),s.getProcletID(),s.getBlockID());
		    	  jMenuBlock_actionPerformed(e,"CP");
	    	  }
    	  }
    	  else {
    		  // no blocks
    		  JOptionPane.showMessageDialog(null,
					    "There are no blocks that need to be configured!",
					    "Information",
					    JOptionPane.INFORMATION_MESSAGE);
    	  }
      }
    });

    jMenuDesign.add(jMenuOrganization);
    
    // setup the choice under exception
    // setup the decision menu item
    JMenuItem jMenuDecisionBlock = new JMenuItem();
    setMenuKey(jMenuDecisionBlock, KEY_DECISION);
    jMenuDecisionBlock.setText("Handle Exception INBOX Interaction Point");
    jMenuDecisionBlock.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  jMenuDecisionBlock_actionPerformed(e);
      }
    });

    jMenuDesign.add(jMenuDecisionBlock);

 // setup the choice under exception
    // setup the decision menu item
    JMenuItem jMenuHandleExceptionBlock = new JMenuItem();
    setMenuKey(jMenuHandleExceptionBlock, KEY_HANDLE_EXCEPTION);
    jMenuHandleExceptionBlock.setText("Extend Interaction Graph Exception INBOX Interaction Point");
    jMenuHandleExceptionBlock.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  // check if we have exceptions that we need to handle with
          java.util.List<InteractionNode> nodes = BlockPI.getExceptionBlockSelected();
    	  if (!nodes.isEmpty()) { 
	    	  InteractionNode s = (InteractionNode)JOptionPane.showInputDialog(
	    	                      mainFrame,
	    	                      "select the exception you want to handle",
	    	                      "Message",
	    	                      JOptionPane.PLAIN_MESSAGE,
	    	                      null,
	    	                      nodes.toArray(),
	    	                      nodes.get(0));
	    	  // register in DB that wi is selected
	    	  BlockPI.setExceptionBlockSelected(s.getClassID(),s.getProcletID(),s.getBlockID());
	    	  //jMenuHandleException_actionPerformed(e);
	    	  jMenuBlock_actionPerformed(e,"BlockException");
    	  }
    	  else {
    		  // no exceptions
    		  JOptionPane.showMessageDialog(null,
					    "No exceptions that need to be handled for a block!",
					    "Information",
					    JOptionPane.INFORMATION_MESSAGE);
    	  }
      }
    });

    jMenuDesign.add(jMenuHandleExceptionBlock);

    // setup the ConstraintTemplate menu item
//    JMenuItem jMenuConstraintTemplate = new JMenuItem();
//    setMenuKey(jMenuConstraintTemplate, KEY_TEMPLATES);
//    jMenuConstraintTemplate.setText("Constraint templates");
//    jMenuConstraintTemplate.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        jMenuConstraintTemplate_actionPerformed(e);
//      }
//    });
//
//    jMenuDesign.add(jMenuConstraintTemplate);

    //setup window menu item
    menuBar.add(frames);
    
 // setup the Exception menu item
    JMenu jMenuException = new JMenu();
    jMenuException.setText("Proclet Instance");
    menuBar.add(jMenuException);
    
    // setup the choice under exception
    // setup the decision menu item
    JMenuItem jMenuDecision = new JMenuItem();
    setMenuKey(jMenuDecision, KEY_DECISION);
    jMenuDecision.setText("Handle Exception");
    jMenuDecision.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  jMenuDecision_actionPerformed(e);
      }
    });

    jMenuException.add(jMenuDecision);

 // setup the choice under exception
    // setup the decision menu item
    JMenuItem jMenuHandleException = new JMenuItem();
    setMenuKey(jMenuHandleException, KEY_HANDLE_EXCEPTION);
    jMenuHandleException.setText("Extend Interaction Graph");
    jMenuHandleException.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  // check if we have exceptions that we need to handle with
          java.util.List<InteractionNode> nodes = CompleteCaseDeleteCase.getExceptionCasesSelected();
    	  if (!nodes.isEmpty()) { 
	    	  InteractionNode s = (InteractionNode)JOptionPane.showInputDialog(
	    	                      mainFrame,
	    	                      "select the exception you want to handle",
	    	                      "Message",
	    	                      JOptionPane.PLAIN_MESSAGE,
	    	                      null,
	    	                      nodes.toArray(),
	    	                      nodes.get(0));
	    	  // register in DB that wi is selected
	    	  CompleteCaseDeleteCase.setExceptionCaseSelected(s.getClassID(),s.getProcletID(),s.getBlockID());
	    	  //jMenuHandleException_actionPerformed(e);
	    	  jMenuBlock_actionPerformed(e,"CaseException");
    	  }
    	  else {
    		  // no exceptions
    		  JOptionPane.showMessageDialog(null,
					    "There are no exceptions for a case that need to be handled!",
					    "Information",
					    JOptionPane.INFORMATION_MESSAGE);
    	  }
      }
    });

    jMenuException.add(jMenuHandleException);
    
    // setup the Extra menu
    JMenu jMenuExtra = new JMenu();
    jMenuExtra.setText("Extra");
    menuBar.add(jMenuExtra);
    
    // setup the choice under exception
    // setup the decision menu item
    JMenuItem jMenuGraph = new JMenuItem();
    setMenuKey(jMenuGraph, KEY_GRAPH);
    jMenuGraph.setText("Graph");
    jMenuGraph.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  // ask for which emid the graph should be shown
    	  // take from mem
          java.util.List<EntityMID> emids = new ArrayList<EntityMID>();
    	  InteractionGraphs igraphs = InteractionGraphs.getNewInstance();
    	  for (InteractionGraph graph : igraphs.getGraphs()) {
    		  EntityMID emid = graph.getEntityMID();
    		  emids.add(emid);
    	  }
    	  if (!emids.isEmpty()) {
    		  EntityMID emidSelected = (EntityMID) JOptionPane.showInputDialog(
	                  mainFrame,
	                  "select the emid for which you want to see the graph",
	                  "Message",
	                  JOptionPane.PLAIN_MESSAGE,
	                  null,
	                  emids.toArray(),
	                  emids.get(0));
    		  // show the stuff
    		  if (emidSelected != null) {
    			  jMenuGraph_actionPerformed(e,emidSelected);
    		  }
    	  }
    	  else {
    		  // emids is empty
    		  JOptionPane.showMessageDialog(null,
					    "There are no graphs that can be shown!",
					    "Information",
					    JOptionPane.INFORMATION_MESSAGE);
    	  }
      }
    });

    jMenuExtra.add(jMenuGraph);

    // add the start case
    JMenuItem jMenuStartCase = new JMenuItem();
    setMenuKey(jMenuStartCase, KEY_START_CASE);
    jMenuStartCase.setText("Initiate Instance of a Proclet Class");
    jMenuStartCase.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
    	  // get all the existing proclet models
    	  ProcletModels pmodelsInst = ProcletModels.getInstance();
          java.util.List<ProcletModel> pmodels = pmodelsInst.getProcletClasses();
    	  if (!pmodels.isEmpty()) {
	    	  ProcletModel pmodelSelected = (ProcletModel) JOptionPane.showInputDialog(
	    			  mainFrame,
	    			  "Select the Proclet Class for which you want to create an instance.",
	    			  "Message",
	    			  JOptionPane.PLAIN_MESSAGE,
	    			  null,
	    			  pmodels.toArray(),
	    			  pmodels.get(0));
	    	  // show the stuff
	    	  if (pmodelSelected != null) {
	    		  try {
	    			  InterfaceB_EnvironmentBasedClient client = new InterfaceB_EnvironmentBasedClient(YAWLlocation);
	    			  String sessionHandle = client.connect(sUserId, sPwd);
	    			  //first check whether this model is already loaded into the engine
	    			  YSpecificationID yidSelected = null;
                      java.util.List<SpecificationData> specList = client.getSpecificationList(sessionHandle);
	    			  for (SpecificationData specData : specList) {
	    				  String name = specData.getID().getUri();
	    				  if (name.equals(pmodelSelected.getClassID())) {
	    					  System.out.println(specData.getRootNetID());
	    					  yidSelected = specData.getID();
	    				  }
	    			  }
	    			  if (yidSelected != null) {
	    				  System.out.println(yidSelected.getUri());
		    			  String dataReturn = "<" + yidSelected.getUri() + ">" + "</" + yidSelected.getUri() + ">";
		    			  System.out.println("dataReturn is " + dataReturn);
		    			  // http://localhost:8080/yawl/ib
		    			  String yawlResponse = client.launchCase(yidSelected.getIdentifier(), "", sessionHandle,ProcletServiceLocation);
		    			  System.out.println("start case for " + yidSelected.getUri() + ", yawlResponse is " + yawlResponse);
		    			  JOptionPane.showMessageDialog(null,
	    						    "Response of YAWL for starting an instance for Proclet Class " 
		    					  + pmodelSelected.getClassID() + ":" + yawlResponse,
	    						    "Information",
	    						    JOptionPane.INFORMATION_MESSAGE);
	    			  }
	    			  else {
	    				  // model not available!
	    				  JOptionPane.showMessageDialog(null,
	    						    "There is no yawl model currently loaded for Proclet Class " + pmodelSelected.getClassID() ,
	    						    "Error",
	    						    JOptionPane.ERROR_MESSAGE);
	    			  }
	    		  }
	    		  catch (Exception exc) {
	    			  exc.printStackTrace();
	    		  }
	    	  }
    	  }
      }
    });

    jMenuExtra.add(jMenuStartCase);

    
    // setup the Help menu item
    JMenu jMenuHelp = new JMenu();
    jMenuHelp.setText("Help");
    menuBar.add(jMenuHelp);

    // setup the About menu item
    JMenuItem jMenuHelpAbout = new JMenuItem();
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMenuHelpAbout_actionPerformed(e);
      }
    });

    jMenuHelp.add(jMenuHelpAbout);

    return menuBar;
  }
  
  /**
   * jMenuBlock_actionPerformed
   *
   * @param actionEvent actionEvent actionEvent Execute this method when user
   *   selects to work with "Block" Create the form for Block and
   *   start the blockCoordinator
   */
  void jMenuBlock_actionPerformed(ActionEvent actionEvent, String option) {
    boolean first = !BlockCoordinator.exists();
    BlockCoordinator coordinator = BlockCoordinator.singleton(
        mainFrame,option);
//    if (option.equals("CP")) {
//    	coordinator.setCP();
//    }
//    else if (option.equals("CaseException")) {
//    	coordinator.setCaseException();
//    }
    JInternalFrame frame = coordinator.getInternalFrame();
    if (first) {
      frame.addInternalFrameListener(new InternalFrameListener() {
        public void internalFrameDeactivated(InternalFrameEvent e) {
          frames.activate(false, e.getInternalFrame());
        }

        public void internalFrameActivated(InternalFrameEvent e) {
          frames.activate(true, e.getInternalFrame());
        }

        public void internalFrameDeiconified(InternalFrameEvent e) {}

        public void internalFrameIconified(InternalFrameEvent e) {}

        public void internalFrameClosing(InternalFrameEvent e) {}

        public void internalFrameOpened(InternalFrameEvent e) {}

        public void internalFrameClosed(InternalFrameEvent e) {
          BlockCoordinator.finish();
          frames.remove(e.getInternalFrame());
        }
      });
      frames.add(true, frame);
      desktop.add(frame);
    }
    this.maximize(frame);
    coordinator.start();
  }
  
  // jMenuDecision_actionPerformed(e);
  void jMenuDecision_actionPerformed(ActionEvent actionEvent) {
	  // first check if we have exceptions
      java.util.List exc = CompleteCaseDeleteCase.getExceptions();
	  if (!exc.isEmpty()) {
	    boolean first = !ExceptionChoiceCoordinator.exists();
	    ExceptionChoiceCoordinator coordinator = ExceptionChoiceCoordinator.singleton(
	        mainFrame);
	    JInternalFrame frame = coordinator.getInternalFrame();
	    if (true) {
	      frame.addInternalFrameListener(new InternalFrameListener() {
	        public void internalFrameDeactivated(InternalFrameEvent e) {
	          frames.activate(false, e.getInternalFrame());
	        }

	        public void internalFrameActivated(InternalFrameEvent e) {
	          frames.activate(true, e.getInternalFrame());
	        }

	        public void internalFrameDeiconified(InternalFrameEvent e) {}

	        public void internalFrameIconified(InternalFrameEvent e) {}

	        public void internalFrameClosing(InternalFrameEvent e) {}

	        public void internalFrameOpened(InternalFrameEvent e) {}

	        public void internalFrameClosed(InternalFrameEvent e) {
	        	ExceptionChoiceCoordinator.finish();
	        	frames.remove(e.getInternalFrame());
	        }
	      });
	      frames.add(true, frame);
	      desktop.add(frame);
	    }
	    this.maximize(frame);
	    coordinator.start();
	  }
	  else {
		  // no decisions
		  JOptionPane.showMessageDialog(null,
				    "There are no decisions that need to be made!",
				    "Information",
				    JOptionPane.INFORMATION_MESSAGE);
	  }
	  }
  
  // jMenuDecisionBlock_actionPerformed
  void jMenuDecisionBlock_actionPerformed(ActionEvent actionEvent) {
	  // first check if we have exceptions
      java.util.List exc = BlockPI.getExceptions();
	  if (!exc.isEmpty()) {
	    boolean first = !BlockExceptionChoiceCoordinator.exists();
	    BlockExceptionChoiceCoordinator coordinator = BlockExceptionChoiceCoordinator.singleton(
	        mainFrame);
	    JInternalFrame frame = coordinator.getInternalFrame();
	    if (true) {
	      frame.addInternalFrameListener(new InternalFrameListener() {
	        public void internalFrameDeactivated(InternalFrameEvent e) {
	          frames.activate(false, e.getInternalFrame());
	        }

	        public void internalFrameActivated(InternalFrameEvent e) {
	          frames.activate(true, e.getInternalFrame());
	        }

	        public void internalFrameDeiconified(InternalFrameEvent e) {}

	        public void internalFrameIconified(InternalFrameEvent e) {}

	        public void internalFrameClosing(InternalFrameEvent e) {}

	        public void internalFrameOpened(InternalFrameEvent e) {}

	        public void internalFrameClosed(InternalFrameEvent e) {
	        	BlockExceptionChoiceCoordinator.finish();
	        	frames.remove(e.getInternalFrame());
	        }
	      });
	      frames.add(true, frame);
	      desktop.add(frame);
	    }
	    this.maximize(frame);
	    coordinator.start();
	  }
	  else {
		  // no decisions
		  JOptionPane.showMessageDialog(null,
				    "There are no decisions that need to be made for an exception!",
				    "Information",
				    JOptionPane.INFORMATION_MESSAGE);
	  }
	  }
  
  
  // jMenuHandleException_actionPerformed
  void jMenuHandleException_actionPerformed(ActionEvent actionEvent) {
	  // first check if we have exceptions
      java.util.List exc = CompleteCaseDeleteCase.getExceptions();
	  if (!exc.isEmpty()) {
	    boolean first = !ExceptionChoiceCoordinator.exists();
	    ExceptionChoiceCoordinator coordinator = ExceptionChoiceCoordinator.singleton(
	        mainFrame);
	    JInternalFrame frame = coordinator.getInternalFrame();
	    if (true) {
	      frame.addInternalFrameListener(new InternalFrameListener() {
	        public void internalFrameDeactivated(InternalFrameEvent e) {
	          frames.activate(false, e.getInternalFrame());
	        }

	        public void internalFrameActivated(InternalFrameEvent e) {
	          frames.activate(true, e.getInternalFrame());
	        }

	        public void internalFrameDeiconified(InternalFrameEvent e) {}

	        public void internalFrameIconified(InternalFrameEvent e) {}

	        public void internalFrameClosing(InternalFrameEvent e) {}

	        public void internalFrameOpened(InternalFrameEvent e) {}

	        public void internalFrameClosed(InternalFrameEvent e) {
	        	ExceptionChoiceCoordinator.finish();
	        	frames.remove(e.getInternalFrame());
	        }
	      });
	      frames.add(true, frame);
	      desktop.add(frame);
	    }
	    this.maximize(frame);
	    coordinator.start();
	  }
	  }
  
  // jModelNew_actionPerformed, open a new frame
  void jModelNew_actionPerformed(ActionEvent actionEvent) {
	    //boolean first = !ModelCoordinator.exists();
      ModelCoordinator coordinator = ModelCoordinator.singleton(
              mainFrame);
	    JInternalFrame frame = coordinator.getInternalFrame();
	    if (true) {
	      frame.addInternalFrameListener(new InternalFrameListener() {
	        public void internalFrameDeactivated(InternalFrameEvent e) {
	          frames.activate(false, e.getInternalFrame());
	        }

	        public void internalFrameActivated(InternalFrameEvent e) {
	          frames.activate(true, e.getInternalFrame());
	        }

	        public void internalFrameDeiconified(InternalFrameEvent e) {}

	        public void internalFrameIconified(InternalFrameEvent e) {}

	        public void internalFrameClosing(InternalFrameEvent e) {}

	        public void internalFrameOpened(InternalFrameEvent e) {}

	        public void internalFrameClosed(InternalFrameEvent e) {
	          BlockCoordinator.finish();
	          frames.remove(e.getInternalFrame());
	        }
	      });
	      frames.add(true, frame);
	      desktop.add(frame);
	    }
	    this.maximize(frame);
	    coordinator.start();
	    // already create a model with name new
	    ProcletModels pmodelsInst = ProcletModels.getInstance();
		ProcletModel pmodel = new ProcletModel("new");
		pmodelsInst.addProcletModel(pmodel);
	  }
  
  	void jModelOpen_actionPerformed(ActionEvent actionEvent) {
  	  // open an existing model
  	  ProcletModels pmodelsInst = ProcletModels.getInstance();
          java.util.List<ProcletModel> pmodels = pmodelsInst.getProcletClasses();
  	  if (!pmodels.isEmpty()) {
	    	  ProcletModel pmodel = (ProcletModel) JOptionPane.showInputDialog(
	                  mainFrame,
	                  "select the Proclet Class you want to work on",
	                  "Message",
	                  JOptionPane.PLAIN_MESSAGE,
	                  null,
	                  pmodels.toArray(),
	                  pmodels.get(0));
	    	  // open frame for the procletmodel
	    	  if (pmodel != null) {
	    		  // temporarily rename the graph to new
	    		  	String oldName = pmodel.getClassID();
		  		    pmodel.setClassID("new");
	    		  //JInternalFrame frame = null;
		  	    	ModelCoordinator coordinator = ModelCoordinator.singleton(
		  		        mainFrame);
		  		    JInternalFrame frame = coordinator.getInternalFrame();
		  		    if (true) {
		  		      frame.addInternalFrameListener(new InternalFrameListener() {
		  		        public void internalFrameDeactivated(InternalFrameEvent e) {
		  		          frames.activate(false, e.getInternalFrame());
		  		        }
	
		  		        public void internalFrameActivated(InternalFrameEvent e) {
		  		          frames.activate(true, e.getInternalFrame());
		  		        }
	
		  		        public void internalFrameDeiconified(InternalFrameEvent e) {}
	
		  		        public void internalFrameIconified(InternalFrameEvent e) {}
	
		  		        public void internalFrameClosing(InternalFrameEvent e) {}
	
		  		        public void internalFrameOpened(InternalFrameEvent e) {}
	
		  		        public void internalFrameClosed(InternalFrameEvent e) {
		  		          // rename new  to oldName
		  		        	ModelCoordinator coordinator = ModelCoordinator.getInstance();
		  		        	String oldName = coordinator.getNameTextField().getText();
		  		        	ProcletModels pmodelsInst = ProcletModels.getInstance();
		  		        	ProcletModel model = pmodelsInst.getProcletClass("new");
		  		        	model.setClassID(oldName);
		  		        	pmodelsInst.persistProcletModels();
		  		        	ModelCoordinator.finish();
		  		      	  	frames.remove(e.getInternalFrame());
		  		        }
		  		      });
		  		      frames.add(true, frame);
		  		      desktop.add(frame);
		  		    }
		  		    this.maximize(frame);
		  		    coordinator.start();
		  		    // temporarily rename the name to new
		  		    coordinator.getNameTextField().setText(oldName);
	    	  }
  	  }
  	  else {
  		  // no models existing
  		  JOptionPane.showMessageDialog(null,
					    "There are no Proclet Classes existing...",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
  	  }
  	}
  
  	void jMenuSaveModel_actionPerformed(ActionEvent actionEvent) {
  		System.out.println("before save");
  		if (ModelCoordinator.exists()) {
  			ModelCoordinator mCoord = ModelCoordinator.getInstance();
  			String nameModel = mCoord.getNameModel();
  			if (nameModel.equals("new")) {
  				// do not allow a model with name new
  				JOptionPane.showMessageDialog(null,
					    "It is not allowed to have a Proclet Class with name 'new'. " + 
					    "Please adjust the name!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
  			}
  			else {
	  			ProcletModels pmodelsInst = ProcletModels.getInstance();
	  			ProcletModel pmodel = pmodelsInst.getProcletClass("new");
	  			// change name
	  			pmodel.setClassID(nameModel);
	  			pmodel.persistProcletModel();
	  			// change name again
	  			pmodel.setClassID("new");
//	  			// save also the pconns
  			}
  		}
  		if (PConnsCoordinator.exists()) {
  			 PortConnections.getInstance().persistPConns();
  		}
  	}
  	
 	void jMenuDeleteModel_actionPerformed(ActionEvent actionEvent) {
 	  	  // open an existing model
 	  	  ProcletModels pmodelsInst = ProcletModels.getInstance();
         java.util.List<ProcletModel> pmodels = pmodelsInst.getProcletClasses();
 	  	  if (!pmodels.isEmpty()) {
 		    	  ProcletModel pmodel = (ProcletModel) JOptionPane.showInputDialog(
 		                  mainFrame,
 		                  "select the Proclet Class you want to delete",
 		                  "Message",
 		                  JOptionPane.PLAIN_MESSAGE,
 		                  null,
 		                  pmodels.toArray(),
 		                  pmodels.get(0));
 		    	  if (pmodel != null) {
 		    		  // delete the selected model
 		    		 pmodelsInst.deleteProcletModel(pmodel); 
 		    		 // persist this
 		    		 //pmodelsInst.persistProcletModels();
 		    		 // notify the user
 		    		JOptionPane.showMessageDialog(null,
 						    "Proclet Class " + pmodel.getClassID() + " has been successfully deleted.",
 						    "Deletion Successfull",
 						    JOptionPane.INFORMATION_MESSAGE);
 		    	  }
 	  	  }
 	  	  else {
 	  		  // no models existing
 	  		  JOptionPane.showMessageDialog(null,
 						    "There are no Proclet Classes existing...",
 						    "Error",
 						    JOptionPane.ERROR_MESSAGE);
 	  	  }
 	  	}
  	
  	// jMenuPConnsModel_actionPerformed(e);
 	void jMenuPConnsModel_actionPerformed(ActionEvent actionEvent) {
	    PConnsCoordinator coordinator = PConnsCoordinator.singleton(
		        mainFrame);
		    JInternalFrame frame = coordinator.getInternalFrame();
		    if (true) {
		      frame.addInternalFrameListener(new InternalFrameListener() {
		        public void internalFrameDeactivated(InternalFrameEvent e) {
		          frames.activate(false, e.getInternalFrame());
		        }

		        public void internalFrameActivated(InternalFrameEvent e) {
		          frames.activate(true, e.getInternalFrame());
		        }

		        public void internalFrameDeiconified(InternalFrameEvent e) {}

		        public void internalFrameIconified(InternalFrameEvent e) {}

		        public void internalFrameClosing(InternalFrameEvent e) {}

		        public void internalFrameOpened(InternalFrameEvent e) {}

		        public void internalFrameClosed(InternalFrameEvent e) {
		          PConnsCoordinator.finish();
		          frames.remove(e.getInternalFrame());
		        }
		      });
		      frames.add(true, frame);
		      desktop.add(frame);
		    }
		    this.maximize(frame);
		    coordinator.start();
  	}  
 	
  // jMenuGraph_actionPerformed
 	 void jMenuGraph_actionPerformed(ActionEvent actionEvent, EntityMID emid) {
 		    boolean first = !GraphCoordinator.exists();
 		    GraphCoordinator coordinator = GraphCoordinator.singleton(
 		        mainFrame,emid);
 		    JInternalFrame frame = coordinator.getInternalFrame();
 		    if (true) {
 		      frame.addInternalFrameListener(new InternalFrameListener() {
 		        public void internalFrameDeactivated(InternalFrameEvent e) {
 		          frames.activate(false, e.getInternalFrame());
 		        }

 		        public void internalFrameActivated(InternalFrameEvent e) {
 		          frames.activate(true, e.getInternalFrame());
 		        }

 		        public void internalFrameDeiconified(InternalFrameEvent e) {}

 		        public void internalFrameIconified(InternalFrameEvent e) {}

 		        public void internalFrameClosing(InternalFrameEvent e) {}

 		        public void internalFrameOpened(InternalFrameEvent e) {}

 		        public void internalFrameClosed(InternalFrameEvent e) {
 		        	GraphCoordinator.finish();
 		        	frames.remove(e.getInternalFrame());
 		        }
 		      });
 		      frames.add(true, frame);
 		      desktop.add(frame);
 		    }
 		    this.maximize(frame);
 		    coordinator.start();
 		  }

  void jMenuHelpAbout_actionPerformed(ActionEvent actionEvent) {
    MainFrame_AboutBox dlg = new MainFrame_AboutBox(mainFrame);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = mainFrame.getSize();
    Point loc = mainFrame.getLocation();
    dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                    (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.pack();
    dlg.setVisible(true);
  }

  private void setModelMenuItems(boolean enabled) {
	this.newItem.setEnabled(enabled);
    this.save.setEnabled(enabled);
//    this.saveAs.setEnabled(enabled);
//    this.verify.setEnabled(enabled);
//    this.export.setEnabled(enabled);
  }

  public void deactivated(JInternalFrame frame) {
    this.setModelMenuItems(false);
    frames.activate(false, frame);
  }

  public void activated(JInternalFrame frame) {
    this.setModelMenuItems(true);
    frames.activate(true, frame);
  }

  public void closed(JInternalFrame frame) {
    frames.remove(frame);
  }
}

