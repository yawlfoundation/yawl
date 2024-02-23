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

package org.yawlfoundation.yawl.procletService.editor.extra;


import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Transformer;
import org.yawlfoundation.yawl.procletService.editor.DesignInternalFrame;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionArc;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraph;
import org.yawlfoundation.yawl.procletService.interactionGraph.InteractionGraphs;
import org.yawlfoundation.yawl.procletService.models.procletModel.PortConnections;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class FrmGraph extends DesignInternalFrame {

	  static final String TITLE = "Interaction Graph";

	  private static FrmGraph single = null;
	  private GraphCoordinator graphCoordinator;
	  
	  // define variables
	  private javax.swing.JSplitPane splitPaneMain;
	  private javax.swing.JButton addBlockButton;
	  private javax.swing.JButton addPortButton;
	  private javax.swing.JButton addRelationButton;
	  private javax.swing.JButton adjustBlockButton;
	  private javax.swing.JButton adjustPortButton;
	  private javax.swing.JPanel blockRelationsPanel;
	  private javax.swing.JPanel blocksPanel;
	  private javax.swing.JPanel generalPanel;
	  private javax.swing.JPanel graphOptionsPanel;
	  private javax.swing.JPanel graphPanel;
	  private javax.swing.JLabel labelForName;
	  private javax.swing.JTextField nameTextField;
	  private javax.swing.JPanel portsPanel;
	  private javax.swing.JButton removeBlockButton;
	  private javax.swing.JButton removePortButton;
	  private javax.swing.JButton removeRelationButton;
	  private javax.swing.JPanel rightPanel;
	  //private javax.swing.JSplitPane splitPanel;
	  private javax.swing.JSplitPane splitPanelLeft;
	  
	  private javax.swing.JPanel topOptionsPanel = new javax.swing.JPanel();
	  private javax.swing.JPanel bottomOptionsPanel = new javax.swing.JPanel();
	  
	  private JComboBox layoutBox = null;
	  
	  private VisualizationViewer vv = null;
	  private DefaultModalGraphMouse gm;
	  
	  private Transformer vertexPaint = null;
	  private Transformer edgeLabel = null;
	  
	  private EntityMID emidSelected = null;
	  // end variables

	  private FrmGraph(GraphCoordinator aModelCoordinator,EntityMID emid) {
	    super(TITLE);
	    this.graphCoordinator = aModelCoordinator;
	    this.emidSelected = emid;
	    try {
	    	jbInit();
	    }
	    catch (Exception ex) {
	      ex.printStackTrace();
	    }
	  }

	  public static FrmGraph singleton(GraphCoordinator
	                                          coord, EntityMID emid) {
	    if (single == null) {
	      single = new FrmGraph(coord,emid);
	    }
	    return single;
	  }
	  
	  public static void finish() {
		  single = null;
	  }

	  protected void jbInit() throws Exception {
//		  initModel();
		  initComponents();
//		  drawGraph();
		  this.setContentPane(splitPanelLeft);
//		  this.modelCoordinator.setNameTextField(nameTextField);
	  }
	  
		private void setVisualizationViewer (VisualizationViewer vviewer) {
//			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
			vv = vviewer;
			//vv.setPickSupport(new edu.uci.ics.jung.visualization.picking.ShapePickSupport(vv));
			vv.setPreferredSize(new Dimension(350,350));
//			vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
			vv.setGraphMouse(gm);
			vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
			splitPanelLeft.setRightComponent(new GraphZoomScrollPane(vv));
		}
		
		public void redrawGraph() {
			  // get the new graph
			  vv.validate();
			  vv.repaint();
			  splitPanelLeft.repaint();
			  getContentPane().repaint();
			  validate();
			  repaint();
		}
	  
	  
	  /**
	 * 
	 */
	private void initComponents() {
				splitPaneMain = new javax.swing.JSplitPane();
				//
				//splitPanel = new javax.swing.JSplitPane();
				splitPanelLeft = new javax.swing.JSplitPane();
		        graphOptionsPanel = new javax.swing.JPanel();
		        graphPanel = new javax.swing.JPanel();
		        rightPanel = new javax.swing.JPanel();
		        generalPanel = new javax.swing.JPanel();
		        nameTextField = new javax.swing.JTextField();
		        labelForName = new javax.swing.JLabel();
		        blocksPanel = new javax.swing.JPanel();
		        addBlockButton = new javax.swing.JButton();
		        removeBlockButton = new javax.swing.JButton();
		        adjustBlockButton = new javax.swing.JButton();
		        portsPanel = new javax.swing.JPanel();
		        addPortButton = new javax.swing.JButton();
		        removePortButton = new javax.swing.JButton();
		        adjustPortButton = new javax.swing.JButton();
		        blockRelationsPanel = new javax.swing.JPanel();
		        addRelationButton = new javax.swing.JButton();
		        removeRelationButton = new javax.swing.JButton();

		        splitPanelLeft.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		        
		        graphOptionsPanel.setLayout(new javax.swing.BoxLayout(graphOptionsPanel, javax.swing.BoxLayout.PAGE_AXIS));
		        
		        topOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Mouse Mode"));
		        graphOptionsPanel.add(topOptionsPanel);
		        
		        bottomOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Layout"));
		        graphOptionsPanel.add(bottomOptionsPanel);
		        
		        gm = new DefaultModalGraphMouse();
		        JComboBox modeBox = gm.getModeComboBox();
		        topOptionsPanel.add(modeBox);
		        
		        PortConnections pconnsInst = PortConnections.getInstance();
		        vv = new VisualizationViewer(new CircleLayout(pconnsInst));
		        // create the transformers
		        // edge label
		        final Transformer edgeLabel = new Transformer() {
		        	public String transform (Object obj) {
		        		if (obj instanceof InteractionArc) {
		        			InteractionArc arc = (InteractionArc) obj;
		        			return arc.getArcStateShort();
		        		}
		        		return "unknown";
		        	}
		        };
		        this.edgeLabel = edgeLabel;
		        vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
		        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		        // options
		        java.util.List<String> options = new ArrayList<String>();
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
		        		InteractionGraph graph = igraphs.getGraph(emidSelected);
		        		String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
		        		if (selected.equals("FRLayout")) {
		        			vv = new VisualizationViewer(new FRLayout(graph));
		        		}
		        		else if (selected.equals("KKLayout")) {
		        			vv = new VisualizationViewer(new KKLayout(graph));
		        		}
		        		else if (selected.equals("CircleLayout")) {
		        			vv = new VisualizationViewer(new CircleLayout(graph));
		        		}
		        		else if (selected.equals("SpringLayout")) {
		        			vv = new VisualizationViewer(new SpringLayout(graph));
		        		}
		        		else if (selected.equals("SpringLayout2")) {
		        			vv = new VisualizationViewer(new SpringLayout2(graph));
		        		}
		        		else if (selected.equals("ISOMLayout")) {
		        			vv = new VisualizationViewer(new ISOMLayout(graph));
		        		}
		        		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		        		vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
		        		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		        		setVisualizationViewer(vv);
		        	}
		        });
		        layoutBox.setSelectedItem("CircleLayout");
		        
				bottomOptionsPanel.add(layoutBox);
		        
		        splitPanelLeft.setTopComponent(graphOptionsPanel);

		        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
		        graphPanel.setLayout(graphPanelLayout);
		        graphPanelLayout.setHorizontalGroup(
		            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGap(0, 181, Short.MAX_VALUE)
		        );
		        graphPanelLayout.setVerticalGroup(
		            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGap(0, 432, Short.MAX_VALUE)
		        );

		        //splitPanelLeft.setRightComponent(graphPanel);

		        splitPaneMain.setLeftComponent(splitPanelLeft);

		        rightPanel.setLayout(new javax.swing.BoxLayout(rightPanel, javax.swing.BoxLayout.PAGE_AXIS));

		        generalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));

		        labelForName.setText("name");
		        
		        nameTextField.setText("new");

		        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
		        generalPanel.setLayout(generalPanelLayout);
		        generalPanelLayout.setHorizontalGroup(
		            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(generalPanelLayout.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(labelForName)
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
		                .addContainerGap(140, Short.MAX_VALUE))
		        );
		        generalPanelLayout.setVerticalGroup(
		            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(generalPanelLayout.createSequentialGroup()
		                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		                    .addComponent(labelForName)
		                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		                .addContainerGap(96, Short.MAX_VALUE))
		        );

		        rightPanel.add(generalPanel);

		        blocksPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Blocks"));

		        javax.swing.GroupLayout blocksPanelLayout = new javax.swing.GroupLayout(blocksPanel);
		        blocksPanel.setLayout(blocksPanelLayout);
		        blocksPanelLayout.setHorizontalGroup(
		            blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(blocksPanelLayout.createSequentialGroup()
		                .addGap(179, 179, 179)
		                .addGroup(blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
		                    .addComponent(adjustBlockButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(removeBlockButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(addBlockButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
		                .addContainerGap(158, Short.MAX_VALUE))
		        );
		        blocksPanelLayout.setVerticalGroup(
		            blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(blocksPanelLayout.createSequentialGroup()
		                .addComponent(addBlockButton)
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                .addComponent(removeBlockButton)
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                .addComponent(adjustBlockButton)
		                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		        );

		        rightPanel.add(blocksPanel);

		        portsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Ports"));

		        javax.swing.GroupLayout portsPanelLayout = new javax.swing.GroupLayout(portsPanel);
		        portsPanel.setLayout(portsPanelLayout);
		        portsPanelLayout.setHorizontalGroup(
		            portsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(portsPanelLayout.createSequentialGroup()
		                .addGap(177, 177, 177)
		                .addGroup(portsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
		                    .addComponent(adjustPortButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(removePortButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(addPortButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
		                .addContainerGap(159, Short.MAX_VALUE))
		        );
		        portsPanelLayout.setVerticalGroup(
		            portsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(portsPanelLayout.createSequentialGroup()
		                .addComponent(addPortButton)
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                .addComponent(removePortButton)
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                .addComponent(adjustPortButton)
		                .addContainerGap(32, Short.MAX_VALUE))
		        );

		        rightPanel.add(portsPanel);

		        blockRelationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Block Relations"));

		        javax.swing.GroupLayout blockRelationsPanelLayout = new javax.swing.GroupLayout(blockRelationsPanel);
		        blockRelationsPanel.setLayout(blockRelationsPanelLayout);
		        blockRelationsPanelLayout.setHorizontalGroup(
		            blockRelationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(blockRelationsPanelLayout.createSequentialGroup()
		                .addGap(177, 177, 177)
		                .addGroup(blockRelationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
		                    .addComponent(removeRelationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                    .addComponent(addRelationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
		                .addContainerGap(159, Short.MAX_VALUE))
		        );
		        blockRelationsPanelLayout.setVerticalGroup(
		            blockRelationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(blockRelationsPanelLayout.createSequentialGroup()
		                .addComponent(addRelationButton)
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		                .addComponent(removeRelationButton)
		                .addContainerGap(61, Short.MAX_VALUE))
		        );

		        rightPanel.add(blockRelationsPanel);

		        splitPaneMain.setRightComponent(rightPanel);

		        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		        this.setLayout(layout);
		        layout.setHorizontalGroup(
		            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addComponent(splitPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
		        );
		        layout.setVerticalGroup(
		            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addComponent(splitPaneMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
		        );
		}
	  
	  public void actionPerformed(ActionEvent e) {
		  
	  }	
	
	
}
