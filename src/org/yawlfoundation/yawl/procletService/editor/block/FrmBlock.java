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

package org.yawlfoundation.yawl.procletService.editor.block;


import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Transformer;
import org.yawlfoundation.yawl.procletService.blockType.BlockCP;
import org.yawlfoundation.yawl.procletService.blockType.BlockPI;
import org.yawlfoundation.yawl.procletService.blockType.CompleteCaseDeleteCase;
import org.yawlfoundation.yawl.procletService.editor.DesignInternalFrame;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionNode;
import org.yawlfoundation.yawl.procletService.selectionProcess.ProcessEntityMID;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class FrmBlock extends DesignInternalFrame {
  static final String TITLE = "Configuration Interaction Point";

  private static FrmBlock single = null;
  private BlockCoordinator blockCoordinator;
  
  // define variables
  private javax.swing.JPanel bottomPanel;
  private javax.swing.JButton buttonCommit;
  private javax.swing.JComboBox comboBoxAvEmids;
  List<JPanel> createItemPanelList = new ArrayList<JPanel>();
  List<JPanel> nonCreateItemPanelList = new ArrayList<JPanel>();
  List<JPanel> fragmentItemPanelList = new ArrayList<JPanel>();
  List<JSpinner> createItemSpinnerList = new ArrayList<JSpinner>();
  List<JCheckBox> fragmentItemCheckBoxList = new ArrayList<JCheckBox>();
  List<JCheckBox> nonCreateItemCheckBoxList = new ArrayList<JCheckBox>();
  List<List> createItemItemList = new ArrayList<List>();
  List<List> fragmentItemItemList = new ArrayList<List>();
  List<List> nonCreateItemItemList = new ArrayList<List>();
  private javax.swing.JButton finishSelButton;
  private javax.swing.JLabel labelCreatePanel;
  private javax.swing.JLabel labelFragmentPanel;
  private javax.swing.JLabel labelNonCreatePanel;
  private javax.swing.JLabel labelTopPanel;
  private javax.swing.JPanel panelCreate;
  private JScrollPane panelCreateScroll;
  private javax.swing.JPanel panelForCommitButton;
  //private javax.swing.JPanel panelLeft;
  private JScrollPane panelNcreateScroll;
  private javax.swing.JPanel panelNcreate;
  //
  private JScrollPane panelFragmentScroll;
  private javax.swing.JPanel panelFragment;
  
  private javax.swing.JPanel panelRight;
  private javax.swing.JPanel panelTop;
  private javax.swing.JSplitPane splitPaneMain;
  
  private javax.swing.JSplitPane splitPanelLeft;
  
  private javax.swing.JPanel graphOptionsPanel;
  private javax.swing.JPanel topOptionsPanel = new javax.swing.JPanel();
  private javax.swing.JPanel bottomOptionsPanel = new javax.swing.JPanel();
  
  private JComboBox layoutBox = null;
  private VisualizationViewer vv = null;
  private DefaultModalGraphMouse gm;
  
  private Transformer vertexPaint = null;
  private Transformer edgeLabel = null;
  
  private JButton selectButton = new javax.swing.JButton();
  private List<JRadioButton> initRadioButtons = new ArrayList<JRadioButton>();
  private Map<JRadioButton,InteractionNode> initRadioButtonMapping = new HashMap<JRadioButton,InteractionNode>();
  private InteractionNode selectedNode = null;
  // init panel
  private javax.swing.JLabel initMessage;
  private javax.swing.JPanel initMessagePanel;
  private javax.swing.JPanel initNodesPanel;
  private javax.swing.JPanel initPanel;
  private javax.swing.JPanel donePanel;

  private FrmBlock(BlockCoordinator aBlockCoordinator) {
    super(TITLE);
    this.blockCoordinator = aBlockCoordinator;
    try {
    	this.blockCoordinator.initiateReceiver();
    	jbInit();
    	// initiate receiver
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static FrmBlock singleton(BlockCoordinator
                                          anOrganizationCoordinator) {
    if (single == null) {
      single = new FrmBlock(anOrganizationCoordinator);
    }
    return single;
  }
  
  public static void finish() {
	  single = null;
  }

  protected void jbInit() throws Exception {
	  addCreateItemPanels();
	  addNonCreateItemPanels();
	  initComponents();
	  drawGraph();
	  this.setContentPane(splitPaneMain);
  }
  
	private void setVisualizationViewer (VisualizationViewer vviewer) {
		vv = vviewer;
		vv.setPreferredSize(new Dimension(350,350));
		vv.setGraphMouse(gm);
		splitPanelLeft.setRightComponent(new GraphZoomScrollPane(vv));
	}
	
	private void drawGraph() {
		// give other color
		  Transformer vertexPaint = new Transformer() {
			  public Paint transform (Object obj) {
//				  if (obj instanceof InteractionNode) {
//					  InteractionNode objNode = (InteractionNode) obj;
//					  // first get all choice nodes
//					  List<InteractionNode> nodesRel = new ArrayList<InteractionNode>();
//					  if (nonCreateItemItemList != null) {
//						  for (List item : nonCreateItemItemList) {
//							  InteractionNode node = (InteractionNode) item.get(3);
//							  nodesRel.add(node);
//						  }
//					  }
//					  if (createItemItemList != null) {
//						  for (List item : createItemItemList) {
//							  InteractionNode  node = (InteractionNode) item.get(2);
//							  nodesRel.add(node);
//						  }
//					  }
//					  boolean found = false;
//					  for (InteractionNode nodeRel : nodesRel) {
//						  if (objNode.getClassID().equals(nodeRel.getClassID()) && 
//								  objNode.getProcletID().equals(nodeRel.getProcletID()) && 
//								  objNode.getBlockID().equals(nodeRel.getBlockID())) {
//							  // found
//							  found = true;
//							  break;
//						  }
//					  }
//					  if (found) {
//						  return Color.RED;
//					  }
//				  }
				  return Color.BLACK;
			  }
		  };
		  this.vertexPaint = vertexPaint;
		  // edge label
		  Transformer edgeLabel = new Transformer() {
			  public String transform (Object obj) {
				  if (obj instanceof InteractionArc) {
					  InteractionArc arc = (InteractionArc) obj;
					  return arc.getArcStateShort();
				  }
				  return "unknown";
			  }
		  };
		  this.edgeLabel = edgeLabel;
		  // get the new graph
		  InteractionGraphs igraphs = InteractionGraphs.getInstance();
		  InteractionGraph graph = null;
		  if (this.blockCoordinator.getSelectedEmid() != null) {
			  if (this.blockCoordinator.isCP()) {
				  graph = igraphs.getTempGraph(this.blockCoordinator.getSelectedEmid());
			  }
			  else if (this.blockCoordinator.isBlockException()) {
				  graph = igraphs.getTempGraph(this.blockCoordinator.getSelectedEmid());
			  }
			  else if (this.blockCoordinator.isCaseException()) {
				  graph = igraphs.getTempGraph(this.blockCoordinator.getSelectedEmid());
			  }
		  }
		  if (graph != null) {
			  vv = new VisualizationViewer(new CircleLayout(graph));
			  setVisualizationViewer(vv);
			  vv.validate();
			  vv.repaint();
			  splitPanelLeft.repaint();
			  getContentPane().repaint();
			  validate();
			  repaint();
		  }
		  else {
			// give panels a default value
			  splitPanelLeft.removeAll();
			  splitPanelLeft.add(new JPanel());
		  }
	}
  
  private void addCreateItemPanels() {
	  createItemSpinnerList.clear();
	  createItemItemList.clear();
	  createItemPanelList.clear();
	  // get from DB
	  List<List<List>> options = ProcessEntityMID.getOptionsFromDB();
	  List<List> optionsCr = options.get(0);
	  //BlockCP.get
	  for (List option : optionsCr) {
		  // option
		  String classID = (String) option.get(0);
		  String blockID = (String) option.get(1);
		  InteractionNode node = (InteractionNode) option.get(3);
		  //String textLabel = "Proclet Class:" + classID + ",IP:" + blockID;
		  String textLabel = classID;
		  // end option
		  JPanel createItemPanel = new javax.swing.JPanel();
		  JLabel labelCreateItemPanel = new javax.swing.JLabel();
		  SpinnerNumberModel myModel = new SpinnerNumberModel(0 /*initial value*/, 0 /*min*/, Integer.MAX_VALUE /*max*/, 1 /*step*/);
		  JSpinner spinnerCreateItemPanel = new javax.swing.JSpinner(myModel);
		  // create the gui stuff
	      labelCreateItemPanel.setText(textLabel);

	      javax.swing.GroupLayout createItemPanelLayout = new javax.swing.GroupLayout(createItemPanel);
	      createItemPanel.setLayout(createItemPanelLayout);
	      createItemPanelLayout.setHorizontalGroup(
	          createItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, createItemPanelLayout.createSequentialGroup()
	              .addComponent(labelCreateItemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
	              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	              .addComponent(spinnerCreateItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
	      );
	      createItemPanelLayout.setVerticalGroup(
	          createItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	          .addGroup(createItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	              .addComponent(labelCreateItemPanel)
	              .addComponent(spinnerCreateItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	      );
	      //
	      List items = new ArrayList();
	      items.add(classID);
	      items.add(blockID);
	      items.add(node);
	      createItemItemList.add(items);
	      createItemSpinnerList.add(spinnerCreateItemPanel);
	      createItemPanelList.add(createItemPanel);
	  }
  }
  
  public void addFragmentItemPanels() {
	  fragmentItemItemList.clear();
	  fragmentItemPanelList.clear();
	  fragmentItemCheckBoxList.clear();
	  // get from DB
	  List<List<List>> options = ProcessEntityMID.getOptionsFromDB();
	  List<List> optionsFr = options.get(2);
	  //BlockCP.get
	  for (List option : optionsFr) {
		  // option
		  String classID = (String) option.get(0);
		  String procletID = (String) option.get(1);
		  String blockID = (String) option.get(2);
		  InteractionNode node = (InteractionNode) option.get(4);
		  //String textLabel = "Proclet Class:" + classID + ",Proclet Instance:" + procletID + ",IP:" + blockID;
		  String textLabel = classID + "," + procletID + "," + blockID;
		  // end option
		  JPanel fragmentItemPanel = new javax.swing.JPanel();
		  JCheckBox checkBoxFragmentItemPanel = new javax.swing.JCheckBox();
	      //checkBoxNonCreateItemPanel.setText("jCheckBox1");
	      JLabel labelFragmentItemPanel = new javax.swing.JLabel();
	      labelFragmentItemPanel.setText(textLabel);

	      javax.swing.GroupLayout fragmentItemPanelLayout = new javax.swing.GroupLayout(fragmentItemPanel);
	      fragmentItemPanel.setLayout(fragmentItemPanelLayout);
	      fragmentItemPanelLayout.setHorizontalGroup(
	    		  fragmentItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fragmentItemPanelLayout.createSequentialGroup()
	              .addComponent(labelFragmentItemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
	              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	              .addComponent(checkBoxFragmentItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
	              .addContainerGap())
	      );
	      fragmentItemPanelLayout.setVerticalGroup(
	    		  fragmentItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	          .addGroup(fragmentItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	              .addComponent(labelFragmentItemPanel)
	              .addComponent(checkBoxFragmentItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
	      );
	      List items = new ArrayList();
	      items.add(classID);
	      items.add(procletID);
	      items.add(blockID);
	      items.add(node);
	      fragmentItemItemList.add(items);
	      fragmentItemCheckBoxList.add(checkBoxFragmentItemPanel);
	      fragmentItemPanelList.add(fragmentItemPanel);
	  }
  }
  
  public void addNonCreateItemPanels() {
	  nonCreateItemItemList.clear();
	  nonCreateItemPanelList.clear();
	  nonCreateItemCheckBoxList.clear();
	  // get from DB
	  List<List<List>> options = ProcessEntityMID.getOptionsFromDB();
	  List<List> optionsNCr = options.get(1);
	  //BlockCP.get
	  for (List option : optionsNCr) {
		  // option
		  String classID = (String) option.get(0);
		  String procletID = (String) option.get(1);
		  String blockID = (String) option.get(2);
		  InteractionNode node = (InteractionNode) option.get(4);
		  //String textLabel = "Proclet Class:" + classID + ",Proclet Instance:" + procletID + ",IP:" + blockID;
		  String textLabel = classID + "," + procletID + "," + blockID;
		  // end option
		  JPanel nonCreateItemPanel = new javax.swing.JPanel();
		  JCheckBox checkBoxNonCreateItemPanel = new javax.swing.JCheckBox();
	      //checkBoxNonCreateItemPanel.setText("jCheckBox1");
	      JLabel labelNonCreateItemPanel = new javax.swing.JLabel();
	      labelNonCreateItemPanel.setText(textLabel);

	      javax.swing.GroupLayout nonCreateItemPanelLayout = new javax.swing.GroupLayout(nonCreateItemPanel);
	      nonCreateItemPanel.setLayout(nonCreateItemPanelLayout);
	      nonCreateItemPanelLayout.setHorizontalGroup(
	          nonCreateItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nonCreateItemPanelLayout.createSequentialGroup()
	              .addComponent(labelNonCreateItemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
	              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	              .addComponent(checkBoxNonCreateItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
	              .addContainerGap())
	      );
	      nonCreateItemPanelLayout.setVerticalGroup(
	          nonCreateItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	          .addGroup(nonCreateItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	              .addComponent(labelNonCreateItemPanel)
	              .addComponent(checkBoxNonCreateItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
	      );
	      List items = new ArrayList();
	      items.add(classID);
	      items.add(procletID);
	      items.add(blockID);
	      items.add(node);
	      nonCreateItemItemList.add(items);
	      nonCreateItemCheckBoxList.add(checkBoxNonCreateItemPanel);
	      nonCreateItemPanelList.add(nonCreateItemPanel);
	  }
  }
  
  private void buildCreatePanel () {
	  panelCreate.removeAll();
      panelCreate.setBackground(new java.awt.Color(255, 255, 255));

      labelCreatePanel.setText("Instantiate Proclet Instance");   

      javax.swing.GroupLayout panelCreateLayout = new javax.swing.GroupLayout(panelCreate);
      panelCreate.setLayout(panelCreateLayout);
      
      GroupLayout.ParallelGroup parGroup = panelCreateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING);
      GroupLayout.ParallelGroup parGroup1 = parGroup.addComponent(labelCreatePanel, javax.swing.GroupLayout.Alignment.LEADING);
      int counter  = 0;
      for (JPanel panel : createItemPanelList) {
    	  // check if it may be added
    	  InteractionNode source = (InteractionNode) this.createItemItemList.get(counter).get(2);
    	  if (selectedNode.getClassID().equals(source.getClassID()) && 
    			  selectedNode.getProcletID().equals(source.getProcletID()) && 
    			  selectedNode.getBlockID().equals(source.getBlockID())) {
    		  parGroup1.addComponent(panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
    	  }
    	  counter++;
      }
      //parGroup1.addComponent(createItemPanelList.get(0), javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
      // add aditional panels
      
      
      panelCreateLayout.setHorizontalGroup(
          panelCreateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCreateLayout.createSequentialGroup()
              .addContainerGap()
              .addGroup(parGroup
                  )
              .addContainerGap())
      );
      GroupLayout.SequentialGroup seqGroup = panelCreateLayout.createSequentialGroup();
      seqGroup.addComponent(labelCreatePanel);
      counter  = 0;
      for (JPanel panel : createItemPanelList) {
    	  // check if it may be added
    	  InteractionNode source = (InteractionNode) this.createItemItemList.get(counter).get(2);
    	  if (selectedNode.getClassID().equals(source.getClassID()) && 
    			  selectedNode.getProcletID().equals(source.getProcletID()) && 
    			  selectedNode.getBlockID().equals(source.getBlockID())) {
        	  seqGroup.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        	  seqGroup.addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
    	  }
    	  counter++;
      }
      panelCreateLayout.setVerticalGroup(
          panelCreateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(seqGroup)
      );
  }
 
  private void buildFragmentPanel() {
	  panelFragment.removeAll();
	  panelFragment.setBackground(new java.awt.Color(255, 255, 255));

      labelFragmentPanel.setText("Existing or Temporary Proclet Instance");
      
      //

      javax.swing.GroupLayout panelFragmentLayout = new javax.swing.GroupLayout(panelFragment);
      panelFragment.setLayout(panelFragmentLayout);
      
      GroupLayout.ParallelGroup parGroupN = panelFragmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
      parGroupN.addComponent(labelFragmentPanel);
      int counter = 0;
      for (JPanel panel : fragmentItemPanelList) {
    	  // check if it may be added
    	  InteractionNode source = (InteractionNode) this.fragmentItemItemList.get(counter).get(3);
    	  if (selectedNode.getClassID().equals(source.getClassID()) && 
    			  selectedNode.getProcletID().equals(source.getProcletID()) && 
    			  selectedNode.getBlockID().equals(source.getBlockID())) {
    		  parGroupN.addComponent(panel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
    	  }
    	  counter++;
      }
      panelFragmentLayout.setHorizontalGroup(
          panelFragmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelFragmentLayout.createSequentialGroup()
              .addContainerGap()
              .addGroup(parGroupN)
              .addContainerGap())
      );
      
      GroupLayout.SequentialGroup seqGroupN = panelFragmentLayout.createSequentialGroup();
      seqGroupN.addComponent(labelFragmentPanel);
      counter = 0;
      for (JPanel panel : fragmentItemPanelList) {
    	  // check if it may be added
    	  InteractionNode source = (InteractionNode) this.fragmentItemItemList.get(counter).get(3);
    	  if (selectedNode.getClassID().equals(source.getClassID()) && 
    			  selectedNode.getProcletID().equals(source.getProcletID()) && 
    			  selectedNode.getBlockID().equals(source.getBlockID())) {
        	  seqGroupN.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        	  seqGroupN.addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
    	  }
    	  counter++;
      }
      panelFragmentLayout.setVerticalGroup(
          panelFragmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(seqGroupN)
      );
      
  }
  
  private void buildNonCreatePanel() {
	  panelNcreate.removeAll();
	  panelNcreate.setBackground(new java.awt.Color(255, 255, 255));

      labelNonCreatePanel.setText("Internal Interaction");
      
      //

      javax.swing.GroupLayout panelNcreateLayout = new javax.swing.GroupLayout(panelNcreate);
      panelNcreate.setLayout(panelNcreateLayout);
      
      GroupLayout.ParallelGroup parGroupN = panelNcreateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
      parGroupN.addComponent(labelNonCreatePanel);
      int counter = 0;
      for (JPanel panel : nonCreateItemPanelList) {
    	  // check if it may be added
    	  InteractionNode source = (InteractionNode) this.nonCreateItemItemList.get(counter).get(3);
    	  if (selectedNode.getClassID().equals(source.getClassID()) && 
    			  selectedNode.getProcletID().equals(source.getProcletID()) && 
    			  selectedNode.getBlockID().equals(source.getBlockID())) {
    		  parGroupN.addComponent(panel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
    	  }
    	  counter++;
      }
      panelNcreateLayout.setHorizontalGroup(
          panelNcreateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelNcreateLayout.createSequentialGroup()
              .addContainerGap()
              .addGroup(parGroupN)
              .addContainerGap())
      );
      
      GroupLayout.SequentialGroup seqGroupN = panelNcreateLayout.createSequentialGroup();
      seqGroupN.addComponent(labelNonCreatePanel);
      counter  = 0;
      for (JPanel panel : nonCreateItemPanelList) {
    	  // check if it may be added
    	  InteractionNode source = (InteractionNode) this.nonCreateItemItemList.get(counter).get(3);
    	  if (selectedNode.getClassID().equals(source.getClassID()) && 
    			  selectedNode.getProcletID().equals(source.getProcletID()) && 
    			  selectedNode.getBlockID().equals(source.getBlockID())) {
        	  seqGroupN.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        	  seqGroupN.addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE);
    	  }
    	  counter++;
      }
      panelNcreateLayout.setVerticalGroup(
          panelNcreateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(seqGroupN)
      );
      
  }
  
  private VisualizationViewer getVisualizationViewerForString (String layout, Graph graph) {
	  if (layout.equals("FRLayout")) {
			vv = new VisualizationViewer(new FRLayout(graph));
		}
		else if (layout.equals("KKLayout")) {
			vv = new VisualizationViewer(new KKLayout(graph));
		}
		else if (layout.equals("CircleLayout")) {
			vv = new VisualizationViewer(new CircleLayout(graph));
		}
		else if (layout.equals("SpringLayout")) {
			vv = new VisualizationViewer(new SpringLayout(graph));
		}
		else if (layout.equals("SpringLayout2")) {
			vv = new VisualizationViewer(new SpringLayout2(graph));
		}
		else if (layout.equals("ISOMLayout")) {
			vv = new VisualizationViewer(new ISOMLayout(graph));
		}
	  vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
	  if (vertexPaint != null) {
		  vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
	  }
	  if (edgeLabel != null) {
		  vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
	  }
	  vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
	  return vv;
  }
  
  /**
 * 
 */
private void initComponents() {

      splitPaneMain = new javax.swing.JSplitPane();
      //panelLeft = new javax.swing.JPanel();
      panelRight = new javax.swing.JPanel();
      panelTop = new javax.swing.JPanel();
      labelTopPanel = new javax.swing.JLabel();
      graphOptionsPanel = new javax.swing.JPanel();
   // fill combobox properly
      String receive = this.blockCoordinator.getReceiver().receive();
      System.out.println("print receive:" + receive);
      if (this.blockCoordinator.isCP()) {
    	  List<EntityMID> emids = BlockCP.getAvailableEmidsToUser();
          System.out.println("emids:" + emids);
    	  comboBoxAvEmids = new javax.swing.JComboBox(emids.toArray());
      }
      else if (this.blockCoordinator.isCaseException()) {
    	  comboBoxAvEmids = new javax.swing.JComboBox(CompleteCaseDeleteCase.getAvailableEmidsCaseExceptionToUser().toArray() );
      }
      else if (this.blockCoordinator.isBlockException()) {
    	  comboBoxAvEmids = new javax.swing.JComboBox(BlockPI.getAvailableEmidsBlockExceptionToUser().toArray() );
      }
      comboBoxAvEmids.addItem(new EntityMID("EXIT"));
      comboBoxAvEmids.setSelectedItem("EXIT");
      panelForCommitButton = new javax.swing.JPanel();
      buttonCommit = new javax.swing.JButton();
      //create
      panelCreate = new javax.swing.JPanel();
      panelCreateScroll = new JScrollPane(panelCreate);
      labelCreatePanel = new javax.swing.JLabel();
      // fragment
      panelFragment = new javax.swing.JPanel();
      panelFragmentScroll = new JScrollPane(panelFragment);
      labelFragmentPanel = new javax.swing.JLabel();
      // noncreate
      panelNcreate = new javax.swing.JPanel();
      panelNcreateScroll = new JScrollPane(panelNcreate);
      labelNonCreatePanel = new javax.swing.JLabel();
      bottomPanel = new javax.swing.JPanel();
      finishSelButton = new javax.swing.JButton();
      splitPanelLeft = new javax.swing.JSplitPane();
      splitPaneMain.setResizeWeight(0.5);
      
      // initially disable commit and finishSelection button
      buttonCommit.setEnabled(false);
      finishSelButton.setEnabled(false);
//      javax.swing.GroupLayout panelLeftLayout = new javax.swing.GroupLayout(panelLeft);
//      panelLeft.setLayout(panelLeftLayout);
//      panelLeftLayout.setHorizontalGroup(
//          panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//          .addGap(0, 374, Short.MAX_VALUE)
//      );
//      panelLeftLayout.setVerticalGroup(
//          panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//          .addGap(0, 495, Short.MAX_VALUE)
//      );

      //
      splitPanelLeft.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
      
      graphOptionsPanel.setLayout(new javax.swing.BoxLayout(graphOptionsPanel, javax.swing.BoxLayout.PAGE_AXIS));
      
      topOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Mouse Mode"));
      graphOptionsPanel.add(topOptionsPanel);
      
      bottomOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Layout"));
      graphOptionsPanel.add(bottomOptionsPanel);
      
      gm = new DefaultModalGraphMouse();
      JComboBox modeBox = gm.getModeComboBox();
      topOptionsPanel.add(modeBox);
      
//      PortConnections pconnsInst = PortConnections.getInstance();
//      vv = new VisualizationViewer(new CircleLayout(pconnsInst));
      
      // options
      List<String> options = new ArrayList<String>();
      options.add("FRLayout");
      options.add("KKLayout");
      options.add("CircleLayout");
      options.add("SpringLayout");
      options.add("SpringLayout2");
      options.add("ISOMLayout");
      layoutBox = new JComboBox(options.toArray());
      layoutBox.setSelectedItem(options.get(0));
      layoutBox.addActionListener(new ActionListener() {
      	public void actionPerformed(ActionEvent e) {
      		InteractionGraphs igraphs = InteractionGraphs.getInstance();
      		igraphs.buildFromDB();
      		InteractionGraph graph = null; 
      		if (blockCoordinator.isCP()) {
  			  graph = igraphs.getTempGraph(blockCoordinator.getSelectedEmid());
      		}
      		else if (blockCoordinator.isBlockException()) {
  			  graph = igraphs.getTempGraph(blockCoordinator.getSelectedEmid());
      		}
      		else if (blockCoordinator.isCaseException()) {
  			  graph = igraphs.getTempGraph(blockCoordinator.getSelectedEmid());
      		}
      		String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
      		vv = getVisualizationViewerForString(selected,graph);
      		setVisualizationViewer(vv);
      	}
      });
      //layoutBox.setSelectedItem("CircleLayout");
      
      bottomOptionsPanel.add(layoutBox);
      
      splitPanelLeft.setTopComponent(graphOptionsPanel);
      //

      panelTop.setBackground(new java.awt.Color(255, 255, 255));

      labelTopPanel.setText("select entity");

      //comboBoxAvEmids.setModel(new javax.swing.DefaultComboBoxModel());
      comboBoxAvEmids.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
              comboBoxAvEmidsActionPerformed(evt);
          }
      });

      panelForCommitButton.setBackground(new java.awt.Color(255, 255, 255));

      buttonCommit.setText("Commit");
      buttonCommit.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
              buttonCommitActionPerformed(evt);
          }
      });
      
      selectButton.setText("Select");
      selectButton.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
              selectButtonActionPerformed(evt);
          }
      });

      javax.swing.GroupLayout panelForCommitButtonLayout = new javax.swing.GroupLayout(panelForCommitButton);
      panelForCommitButton.setLayout(panelForCommitButtonLayout);
      panelForCommitButtonLayout.setHorizontalGroup(
          panelForCommitButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelForCommitButtonLayout.createSequentialGroup()
              .addComponent(selectButton)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
              .addComponent(buttonCommit)
              .addContainerGap(183, Short.MAX_VALUE))
      );
      panelForCommitButtonLayout.setVerticalGroup(
          panelForCommitButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelForCommitButtonLayout.createSequentialGroup()
              .addGroup(panelForCommitButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(buttonCommit, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                  .addComponent(selectButton))
              .addContainerGap())
      );

      javax.swing.GroupLayout panelTopLayout = new javax.swing.GroupLayout(panelTop);
      panelTop.setLayout(panelTopLayout);
      panelTopLayout.setHorizontalGroup(
          panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelTopLayout.createSequentialGroup()
              .addContainerGap()
              .addGroup(panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(panelForCommitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGroup(panelTopLayout.createSequentialGroup()
                      .addGap(10, 10, 10)
                      .addComponent(comboBoxAvEmids, 0, 557, Short.MAX_VALUE))
                  .addComponent(labelTopPanel))
              .addContainerGap())
      );
      panelTopLayout.setVerticalGroup(
          panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelTopLayout.createSequentialGroup()
              .addComponent(labelTopPanel)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
              .addComponent(comboBoxAvEmids, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
              .addComponent(panelForCommitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addContainerGap(15, Short.MAX_VALUE))
      );
      
      // panelCreate
      buildCreatePanel();
      // panelCreate

      // panelNCreate
      buildFragmentPanel(); 
      buildNonCreatePanel();
      // panelNCreate

      bottomPanel.setBackground(new java.awt.Color(255, 255, 255));

      finishSelButton.setText("Finish selection");
      finishSelButton.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent evt) {
              finishSelButtonActionPerformed(evt);
          }
      });

      javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
      bottomPanel.setLayout(bottomPanelLayout);
      bottomPanelLayout.setHorizontalGroup(
          bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(bottomPanelLayout.createSequentialGroup()
              .addContainerGap(256, Short.MAX_VALUE)
              .addComponent(finishSelButton)
              .addContainerGap(224, Short.MAX_VALUE))
      );
      bottomPanelLayout.setVerticalGroup(
          bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(bottomPanelLayout.createSequentialGroup()
              .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(finishSelButton)
              .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      
      //javax.swing.GroupLayout panelRightLayout = new javax.swing.GroupLayout(panelRight);
     
		panelRight.setOpaque(false);
		panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.PAGE_AXIS));
		panelRight.add(panelTop);
//		panelRight.add(panelCreateScroll);
//		panelRight.add(panelFragmentScroll);
//		panelRight.add(panelNcreateScroll);
//		panelRight.add(bottomPanel);
      
      // panelRight
//      javax.swing.GroupLayout panelRightLayout = new javax.swing.GroupLayout(panelRight);
//      panelRight.setLayout(panelRightLayout);
//      panelRightLayout.setHorizontalGroup(
//          panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//          .addComponent(panelTop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//          .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//          .addComponent(panelCreate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//          .addComponent(panelFragment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//          .addComponent(panelNcreate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)          
//      );
//      panelRightLayout.setVerticalGroup(
//          panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//          .addGroup(panelRightLayout.createSequentialGroup()
//              .addComponent(panelTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//              .addComponent(panelCreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//              .addComponent(panelFragment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//              .addComponent(panelNcreate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//              .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//      );
//
//      panelRightLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {panelCreate, panelNcreate, panelNcreate});
      // panelRight

      splitPaneMain.setRightComponent(panelRight);
      splitPaneMain.setLeftComponent(splitPanelLeft);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
          layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(splitPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
          layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(splitPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
      );
  }// </editor-fold>//GEN-END:initComponents

private void initPanel() {
	// find source nodes
    // inform which emid is chosen and get options
    List<InteractionNode> nodes = new ArrayList<InteractionNode>();
    //create
    for (List item : this.createItemItemList) {
    	InteractionNode node = (InteractionNode) item.get(2);
    	boolean exists = false;
    	for (InteractionNode checkNode : nodes) {
    		if (checkNode.getProcletID().equals(node.getProcletID()) && 
    				checkNode.getClassID().equals(node.getClassID()) && 
    				checkNode.getBlockID().equals(node.getBlockID())) {
    			exists = true;
    			break;
    		}
    	}
    	if (!exists) {
    		nodes.add(node);
    	}
    }
    // non-create
    for (List item : this.nonCreateItemItemList) {
    	InteractionNode node = (InteractionNode) item.get(3);
    	boolean exists = false;
    	for (InteractionNode checkNode : nodes) {
    		if (checkNode.getProcletID().equals(node.getProcletID()) && 
    				checkNode.getClassID().equals(node.getClassID()) && 
    				checkNode.getBlockID().equals(node.getBlockID())) {
    			exists = true;
    			break;
    		}
    	}
    	if (!exists) {
    		nodes.add(node);
    	}
    }
    // fragment
    for (List item : this.fragmentItemItemList) {
    	InteractionNode node = (InteractionNode) item.get(3);
    	boolean exists = false;
    	for (InteractionNode checkNode : nodes) {
    		if (checkNode.getProcletID().equals(node.getProcletID()) && 
    				checkNode.getClassID().equals(node.getClassID()) && 
    				checkNode.getBlockID().equals(node.getBlockID())) {
    			exists = true;
    			break;
    		}
    	}
    	if (!exists) {
    		nodes.add(node);
    	}
    }
	// end source nodes
	javax.swing.JButton doneButton;
	javax.swing.JRadioButton node1RadioButton;
    initPanel = new javax.swing.JPanel();
    initNodesPanel = new javax.swing.JPanel();
    initMessagePanel = new javax.swing.JPanel();
    initMessage = new javax.swing.JLabel();
    donePanel = new javax.swing.JPanel();
    doneButton = new javax.swing.JButton();
    doneButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            doneButtonActionPerformed(evt);
        }
    });

    initNodesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Interaction Nodes"));
    
    javax.swing.GroupLayout initNodesPanelLayout = new javax.swing.GroupLayout(initNodesPanel);
    initNodesPanel.setLayout(initNodesPanelLayout);
    // create radioButtons
    initRadioButtons.clear();
    initRadioButtonMapping.clear();
    boolean setEn = false;
    for (InteractionNode node : nodes) {
    	JRadioButton radioButton = new JRadioButton(node.toString());
    	initRadioButtons.add(radioButton);
    	initRadioButtonMapping.put(radioButton, node);
    	if (!setEn) {
    		radioButton.setSelected(true);
    		setEn = true;
    	}
    }
    // horizontal
	GroupLayout.ParallelGroup pargroup1 = initNodesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
    // add components for radiobutton
    for (JRadioButton radioButton : initRadioButtons) {
    	pargroup1.addComponent(radioButton,javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE);
    }
    initNodesPanelLayout.setHorizontalGroup(pargroup1);
    
    // vertical
//    initNodesPanelLayout.setVerticalGroup(
//    		initNodesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(initNodesPanelLayout.createSequentialGroup()
//                .addComponent(jRadioButton1)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(jRadioButton2)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(jRadioButton3)
//                .addContainerGap(39, Short.MAX_VALUE))
//        );
    GroupLayout.SequentialGroup seqgroup1 = initNodesPanelLayout.createSequentialGroup();
    int size = initRadioButtons.size();
    int counter = 1;
    for (JRadioButton radioButton : initRadioButtons) {
    	seqgroup1.addComponent(radioButton);
    	// do not do for last one
    	if (counter < size) {
    		seqgroup1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
    	}
    	else {
    		seqgroup1.addContainerGap(39, Short.MAX_VALUE);
    	}
    	counter++;
    }
    GroupLayout.ParallelGroup pargroup2 = initNodesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
    pargroup2.addGroup(seqgroup1);
    initNodesPanelLayout.setVerticalGroup(pargroup2);
    // button group
    ButtonGroup bGroup = new ButtonGroup();
    for (JRadioButton radioButton : initRadioButtons) {
    	bGroup.add(radioButton);
    }
    // initMessagePanel
    initMessagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Message"));

    initMessage.setText("Select node for which you want to define interactions");

    javax.swing.GroupLayout initMessagePanelLayout = new javax.swing.GroupLayout(initMessagePanel);
    initMessagePanel.setLayout(initMessagePanelLayout);
    initMessagePanelLayout.setHorizontalGroup(
        initMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(initMessagePanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(initMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
    );
    initMessagePanelLayout.setVerticalGroup(
        initMessagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(initMessagePanelLayout.createSequentialGroup()
            .addGap(29, 29, 29)
            .addComponent(initMessage)
            .addContainerGap(43, Short.MAX_VALUE))
    );

    donePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Done"));

    doneButton.setText("Done");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(donePanel);
    donePanel.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(176, 176, 176)
            .addComponent(doneButton)
            .addContainerGap(195, Short.MAX_VALUE))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(23, 23, 23)
            .addComponent(doneButton)
            .addContainerGap(39, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout initPanelLayout = new javax.swing.GroupLayout(initPanel);
    initPanel.setLayout(initPanelLayout);
    initPanelLayout.setHorizontalGroup(
        initPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(initMessagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(initNodesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(donePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    initPanelLayout.setVerticalGroup(
        initPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(initPanelLayout.createSequentialGroup()
            .addComponent(initMessagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(initNodesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(donePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    // repaint left side
    rebuildLeftPart(true);
	 //
	 vv.validate();
	 vv.repaint();
	 splitPaneMain.repaint();
	 getContentPane().repaint();
	 validate();
	 repaint();
    
    panelRight.removeAll();
    panelRight.repaint();
    initPanel.repaint();
    panelRight.add(initPanel);
    initMessagePanel.repaint();
    initNodesPanel.repaint();
    initPanel.repaint();
	panelRight.repaint();
	splitPaneMain.setRightComponent(panelRight);
	splitPaneMain.repaint();
	this.getContentPane().repaint();
	this.repaint();
}

  private void buttonCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCommitActionPerformed
	  this.blockCoordinator.getReceiver().send("commit");
	  String result = this.blockCoordinator.getReceiver().receive();
	  if (result.equals("ok")) {
		// update the comboBox
		  List<EntityMID> emidList = new ArrayList();
		  if (this.blockCoordinator.isCP()) {
			  emidList = BlockCP.getAvailableEmidsToUser();
		  }
		  else if (this.blockCoordinator.isCaseException()) {
			  emidList = CompleteCaseDeleteCase.getAvailableEmidsCaseExceptionToUser();
		  }
		  else if (this.blockCoordinator.isBlockException()) {
			  emidList = BlockPI.getAvailableEmidsBlockExceptionToUser();
		  }
		  this.comboBoxAvEmids.removeAllItems();
		  System.out.println(emidList);
		  for (EntityMID emid : emidList) {
			  this.comboBoxAvEmids.addItem(emid);
		  }
		  EntityMID emidExit = new EntityMID("EXIT");
		  this.comboBoxAvEmids.addItem(emidExit);
		  JOptionPane.showMessageDialog(this, "graph is ok");
		  // enable the selection of another emid
		  this.finishSelButton.setEnabled(false);
		  this.buttonCommit.setEnabled(false);
		  this.selectButton.setEnabled(true);
	  }
	  else {
		  // assume nok
		  JOptionPane.showMessageDialog(this, "graph is not ok");
		// enable the selection of another emid
		  this.finishSelButton.setEnabled(false);
		  this.buttonCommit.setEnabled(false);
		  this.selectButton.setEnabled(true);
	  }
	  // repaint graph
//	  this.splitPaneMain.setLeftComponent(new JPanel());
//	  splitPaneMain.repaint();
//	  getContentPane().repaint();
//	  validate();
//	  repaint();
	  //this.setVisualizationViewer(vv);
	  // clear left panel
	  splitPanelLeft.removeAll();
	  // repaint options
	  this.addCreateItemPanels();
	  this.addFragmentItemPanels();
	  this.addNonCreateItemPanels();
	  //initComponents();
	  this.buildCreatePanel();
	  this.buildFragmentPanel();
	  this.buildNonCreatePanel();
	  panelCreate.repaint();
	  panelFragment.repaint();
	  panelNcreate.repaint();
	  panelRight.repaint();
	  splitPanelLeft.repaint();
	  splitPaneMain.repaint();
	  this.getContentPane().repaint();
	  this.repaint();
	  this.blockCoordinator.getReceiver().receive();
  }//GEN-LAST:event_buttonCommitActionPerformed
  
  private void rebuildLeftPart(final boolean initPanel) {
	  InteractionGraphs igraphs = InteractionGraphs.getInstance();
	  igraphs.reset();
	  igraphs.buildFromDB();
	  InteractionGraph graph = null;
	  if (this.blockCoordinator.isCP()) {
		  graph = igraphs.getTempGraph(this.blockCoordinator.getSelectedEmid());
	  }
	  else if (this.blockCoordinator.isBlockException()) {
		  graph = igraphs.getTempGraph(this.blockCoordinator.getSelectedEmid());
	  }
	  else if (this.blockCoordinator.isCaseException()) {
		  graph = igraphs.getTempGraph(this.blockCoordinator.getSelectedEmid());
	  }
//	  Layout<Integer,String> layout = new CircleLayout(graph);
//	  this.layoutBox.getSelectedItem()
//	  layout.setSize(new Dimension(300,300));
//	  VisualizationViewer vv = new VisualizationViewer(layout);
//	  //vv.getModel().setRelaxerThreadSleepTime(500);
//	  vv.setPickSupport(new edu.uci.ics.jung.visualization.picking.ShapePickSupport(vv));
//	  vv.setPreferredSize(new Dimension(350,350));
//	  vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//	  this.splitPaneMain.setLeftComponent(new GraphZoomScrollPane(vv));
	  //
	  vv = getVisualizationViewerForString((String) this.layoutBox.getSelectedItem(),graph);
	  vv.setPickSupport(new edu.uci.ics.jung.visualization.picking.ShapePickSupport(vv));
	  vv.setPreferredSize(new Dimension(350,350));
	  vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
	  // transformer for the vertices
		// give other color
	  Transformer vertexPaint = new Transformer() {
		  public Paint transform (Object obj) {
			  if (obj instanceof InteractionNode) {
				  InteractionNode objNode = (InteractionNode) obj;
				  if (initPanel) {
					  Collection<InteractionNode> nodes = initRadioButtonMapping.values();
					  Iterator<InteractionNode> it = nodes.iterator();
					  while (it.hasNext()) {
						  InteractionNode  next = it.next();
						  if (objNode.getClassID().equals(next.getClassID()) && 
								  objNode.getProcletID().equals(next.getProcletID()) && 
								  objNode.getBlockID().equals(next.getBlockID())) {
							  return Color.RED;
						  }
					  }
					  return Color.BLACK;
				  }
				  else {
					  // selecting
					  // paint selected node node
					  if (objNode.getClassID().equals(selectedNode.getClassID()) && 
							  objNode.getProcletID().equals(selectedNode.getProcletID()) && 
							  objNode.getBlockID().equals(selectedNode.getBlockID())) {
						  return Color.RED;
					  }
					  else {
						  // should be one of the selectable nodes
						  if (nonCreateItemItemList != null) {
							  for (List item : nonCreateItemItemList) {
								  InteractionNode node = (InteractionNode) item.get(3);
								  if (node.getClassID().equals(selectedNode.getClassID()) && 
										  node.getProcletID().equals(selectedNode.getProcletID()) && 
										  node.getBlockID().equals(selectedNode.getBlockID())) {
									  return Color.BLACK;
								  }
							  }
						  }
						  if (createItemItemList != null) {
							  for (List item : createItemItemList) {
								  InteractionNode  node = (InteractionNode) item.get(2);
								  if (node.getClassID().equals(selectedNode.getClassID()) && 
										  node.getProcletID().equals(selectedNode.getProcletID()) && 
										  node.getBlockID().equals(selectedNode.getBlockID())) {
									  return Color.BLACK;
								  }
							  }
						  }
						  if (fragmentItemItemList != null) {
							  for (List item : fragmentItemItemList) {
								  InteractionNode  node = (InteractionNode) item.get(3);
								  if (node.getClassID().equals(selectedNode.getClassID()) && 
										  node.getProcletID().equals(selectedNode.getProcletID()) && 
										  node.getBlockID().equals(selectedNode.getBlockID())) {
									  return Color.BLACK;
								  }
							  }
						  }
						  return Color.BLACK;
					  }
				  }
			  }
			  return Color.BLACK;
		  }
	  };
	  this.vertexPaint = vertexPaint;
	  // end transformer
	  if (vertexPaint != null) {
		  vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
	  }
	  if (edgeLabel != null) {
		  vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
	  }
	  vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
	  // put also the new color for the vertices
	  vv.setGraphMouse(gm);
	  //
	  
	  //
	  splitPanelLeft = new javax.swing.JSplitPane();
	  splitPanelLeft.setRightComponent(new GraphZoomScrollPane(vv));
	  splitPanelLeft.setTopComponent(graphOptionsPanel);
	  splitPanelLeft.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	  splitPaneMain.setLeftComponent(splitPanelLeft);
  }

  private void finishSelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishSelButtonActionPerformed
      // get decisionsFromUser
	  List decisions = new ArrayList();
	  // create
	  int counter = 0;
	  for (JSpinner spinner : createItemSpinnerList) {
		  int value = (Integer) spinner.getValue();
		  List items = createItemItemList.get(counter);
		  String classID = (String) items.get(0);
		  String blockID = (String) items.get(1);
		  InteractionNode node = (InteractionNode) items.get(2);
		  // convert
		  List dec = new ArrayList();
		  dec.add(classID);
		  dec.add(blockID);
		  dec.add(Integer.toString(value));
		  dec.add(node.getClassID());
		  dec.add(node.getProcletID());
		  dec.add(node.getBlockID());
		  dec.add(0);
		  decisions.add(dec);
		  counter++;
	  }
	  // fragment
	  counter = 0;
	  for (JCheckBox checkBox : fragmentItemCheckBoxList) {
		  boolean selected = checkBox.isSelected();
		  List items = fragmentItemItemList.get(counter);
		  String classID = (String) items.get(0);
		  String procletID = (String) items.get(1);
		  String blockID = (String) items.get(2);
		  InteractionNode node = (InteractionNode) items.get(3);
		// convert
		  if (selected) {
			  List dec = new ArrayList();
			  dec.add(classID);
			  dec.add(procletID);
			  dec.add(blockID);
			  dec.add(node.getClassID());
			  dec.add(node.getProcletID());
			  dec.add(node.getBlockID());
			  dec.add(1);
			  decisions.add(dec);
		  }
		  counter++;
	  }
	  // noncreate
	  counter = 0;
	  for (JCheckBox checkBox : nonCreateItemCheckBoxList) {
		  boolean selected = checkBox.isSelected();
		  List items = nonCreateItemItemList.get(counter);
		  String classID = (String) items.get(0);
		  String procletID = (String) items.get(1);
		  String blockID = (String) items.get(2);
		  InteractionNode node = (InteractionNode) items.get(3);
		// convert
		  if (selected) {
			  List dec = new ArrayList();
			  dec.add(classID);
			  dec.add(procletID);
			  dec.add(blockID);
			  dec.add(node.getClassID());
			  dec.add(node.getProcletID());
			  dec.add(node.getBlockID());
			  dec.add(1);
			  decisions.add(dec);
		  }
		  counter++;
	  }
	  ProcessEntityMID.sendDecisionsToDB(decisions);
	  // finishSelection
	  this.blockCoordinator.getReceiver().send("finishSelection");
	  // wait for reaction and then repaint graph
	  this.blockCoordinator.getReceiver().receive();
	  // repaint graph
	  //rebuildLeftPart(true);
	  //
	  vv.validate();
	  vv.repaint();
	  splitPaneMain.repaint();
	  getContentPane().repaint();
	  validate();
	  repaint();
	  // wait for new options
	  this.blockCoordinator.getReceiver().receive();
	  this.addCreateItemPanels();
	  this.addFragmentItemPanels();
	  this.addNonCreateItemPanels();
	  // put init panel
	  initPanel();
//	  //initComponents();
//	  this.buildCreatePanel();
//	  this.buildFragmentPanel();
//	  this.buildNonCreatePanel();
//	  panelCreate.repaint();
//	  panelFragment.repaint();
//	  panelNcreate.repaint();
//	  panelRight.repaint();
//	  splitPaneMain.repaint();
//	  this.getContentPane().repaint();
//	  this.repaint();
	  
  }//GEN-LAST:event_finishSelButtonActionPerformed
  
  private void comboBoxAvEmidsActionPerformed(java.awt.event.ActionEvent evt) {
  }
  
  private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {
	  EntityMID emid = (EntityMID) comboBoxAvEmids.getSelectedItem();
	  System.out.println("selected value:" + emid);
	  if (!emid.getValue().equals("EXIT")) {
		  this.blockCoordinator.setSelectedEmid(emid);
		  if (this.blockCoordinator.isCP()) {
			  BlockCP.setEmidSelected(emid);
		  }
		  else if (this.blockCoordinator.isCaseException()) {
			  CompleteCaseDeleteCase.setEmidSelectedCaseException(emid);
		  }
		  else if (this.blockCoordinator.isBlockException()) {
			  BlockPI.setEmidSelectedBlockException(emid);
		  }
		  // send message
		  this.blockCoordinator.getReceiver().send(emid.getValue());
//		  // wait for message
		  this.blockCoordinator.getReceiver().receive();
		  this.addCreateItemPanels();
		  this.addFragmentItemPanels();
		  this.addNonCreateItemPanels();
		  initPanel();
//		  // build already the graph
//		  rebuildLeftPart();
//		  //initComponents();
//		  this.buildCreatePanel();
//		  this.buildFragmentPanel();
//		  this.buildNonCreatePanel();
//		  panelCreate.repaint();
//		  panelFragment.repaint();
//		  panelNcreate.repaint();
//		  panelRight.repaint();
//		  splitPaneMain.repaint();
//		  this.getContentPane().repaint();
//		  this.repaint();
//		  //this.pack();
//		  System.out.println("button!");
		// enable commitButton and selectButton, disable selection emid button
		  this.finishSelButton.setEnabled(true);
		  this.buttonCommit.setEnabled(true);
		  this.selectButton.setEnabled(false);
	  }
	  else if (emid.getValue().equals("EXIT")){
		  // exit!
		  // close frame
		  this.blockCoordinator.finish();
		  this.setClosable(true);
		  try {
			  this.setClosed(true);
		  }
		  catch (Exception e) {
			  e.printStackTrace();
		  }
		  this.blockCoordinator.getReceiver().send("EXIT");
	  }
  }
  
  private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {
	  // get selected node
	  for (JRadioButton radioButton : initRadioButtons) {
		  if (radioButton.isSelected()) {
			  selectedNode = initRadioButtonMapping.get(radioButton);
		  }
	  }
	// enable finish selection button
	  // rebuild graph
	  this.rebuildLeftPart(false);
	  //
	  finishSelButton.setEnabled(true);
	  this.buildCreatePanel();
  	  this.buildFragmentPanel();
  	  this.buildNonCreatePanel();
	  panelRight = new javax.swing.JPanel();
	  panelRight.setOpaque(false);
	  panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.PAGE_AXIS));
	  panelRight.add(panelTop);
	  panelRight.add(panelCreateScroll);
	  panelRight.add(panelFragmentScroll);
	  panelRight.add(panelNcreateScroll);
	  panelRight.add(bottomPanel);
	  panelCreate.repaint();
	  panelFragment.repaint();
	  panelNcreate.repaint();
	  panelRight.repaint();
	  splitPaneMain.setRightComponent(panelRight);
	  splitPaneMain.repaint();
	  this.getContentPane().repaint();
	  this.repaint();
  }
  
  public void actionPerformed(ActionEvent e) {

  }

}



