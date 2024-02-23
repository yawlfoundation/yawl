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

package org.yawlfoundation.yawl.procletService.editor.model;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import org.apache.commons.collections15.Transformer;
import org.yawlfoundation.yawl.procletService.editor.DesignInternalFrame;
import org.yawlfoundation.yawl.procletService.models.procletModel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class FrmModel extends DesignInternalFrame {
  static final String TITLE = "Model";

  private static FrmModel single = null;
  private ModelCoordinator modelCoordinator;
  
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
  private Transformer vertexLabelTransformer = null;
  private Transformer edgeStrokeTransformer = null;
  // end variables

  private FrmModel(ModelCoordinator aModelCoordinator) {
    super(TITLE);
    this.modelCoordinator = aModelCoordinator;
    try {
    	jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static FrmModel singleton(ModelCoordinator
                                          coord) {
    if (single == null) {
      single = new FrmModel(coord);
    }
    return single;
  }
  
  public static void finish() {
	  single = null;
  }

  protected void jbInit() throws Exception {
	  initModel();
	  initComponents();
	  drawGraph();
	  this.setContentPane(splitPaneMain);
	  this.modelCoordinator.setNameTextField(nameTextField);
  }
  
	private void setVisualizationViewer (VisualizationViewer vviewer) {
		if (vviewer != null) {
			vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
			vv = vviewer;
			//vv.setPickSupport(new edu.uci.ics.jung.visualization.picking.ShapePickSupport(vv));
			vv.setPreferredSize(new Dimension(350,350));
			vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
			vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
			vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
			vv.setGraphMouse(gm);
			splitPanelLeft.setRightComponent(new GraphZoomScrollPane(vv));
		}
	}
	
	private void drawGraph() {
		// give other color
		  Transformer vertexPaint = new Transformer() {
			  public Paint transform (Object obj) {
				  if (obj instanceof ProcletBlock) {
					  if (((ProcletBlock) obj).getBlockID().equals("exception")) {
						  return Color.GRAY;
						//return Color.BLUE;  
					  }
					  else {
						  //return Color.RED;
						  return Color.BLACK;
					  }
				  }
				  else if (obj instanceof ProcletPort) {
					  //return Color.GREEN;  
					  return Color.WHITE;
				  }
				  //return Color.RED;
				  return Color.BLACK;
			  }
		  };
		  this.vertexPaint = vertexPaint;
		  // vertexLabelTransformer
		  Transformer vertexLabelTransformer = new Transformer() {
			  public String transform (Object obj) {
				  if (obj instanceof ProcletBlock) {
					  // convert first the value
					  String val = ((ProcletBlock) obj).getBlockType().toString();
					  String convertedValue = "";
					  if (val.equals("FO")) {
						  convertedValue = "OUTBOX";
					  }
					  else if (val.equals("PI")) {
						  convertedValue = "INBOX";
					  }
					  else {
						  convertedValue = "CP";
					  }
					  return convertedValue + ":" +  ((ProcletBlock) obj).getBlockID();
				  }
				  else if (obj instanceof ProcletPort) {
					  return ((ProcletPort) obj).getPortID() + ":" + 
					  ProcletPort.getShortSignature(((ProcletPort) obj).getCardinality()) + "," + 
					  ProcletPort.getShortSignature(((ProcletPort) obj).getMultiplicity());
				  }
				  return "";
			  }
		  };
		  // strokeTransformer
		  Transformer<String, Stroke> edgeStrokeTransformer =
			  new Transformer() {
			  	public Stroke transform(Object obj) {
			  		if (obj instanceof BlockPortEdge) {
			  			return new BasicStroke();
			  		}
			  		else {
			  			float dash[] = {10.0f};
			  			final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			  				  BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
			  			return edgeStroke;
			  		}
			  	}
		  };
		  this.edgeStrokeTransformer = edgeStrokeTransformer;
		  this.vertexLabelTransformer = vertexLabelTransformer;
		  // get the new graph
		  ProcletModels pmodelsInst = ProcletModels.getInstance();
		  ProcletModel pmodel = pmodelsInst.getProcletClass("new");
		  if (pmodel != null) {
			  vv = new VisualizationViewer(new CircleLayout(pmodel));
			  setVisualizationViewer(vv);
			  vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
			  vv.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);
			  vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
			  vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
			  vv.validate();
			  vv.repaint();
			  splitPanelLeft.repaint();
			  getContentPane().repaint();
			  validate();
			  repaint();
		  }
	}
	
	public void redrawGraph() {
		  // get the new graph
		if (vv == null) {
			drawGraph();
		}
		  vv.validate();
		  vv.repaint();
		  splitPanelLeft.repaint();
		  getContentPane().repaint();
		  validate();
		  repaint();
	}
  
  private void initModel() {
	  // delete model with new
	  ProcletModel pmodel = new ProcletModel("new");
	  pmodel.deleteProcletModelFromDB();
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
	        		ProcletModels pmodelsInst = ProcletModels.getInstance();
	        		ProcletModel pmodel = pmodelsInst.getProcletClass("new");
	        		String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
	        		if (pmodel != null && pmodel.getVertexCount() > 0) {
		        		if (selected.equals("FRLayout")) {
		        			vv = new VisualizationViewer(new FRLayout(pmodel));
		        		}
		        		else if (selected.equals("KKLayout")) {
		        			vv = new VisualizationViewer(new KKLayout(pmodel));
		        		}
		        		else if (selected.equals("CircleLayout")) {
		        			vv = new VisualizationViewer(new CircleLayout(pmodel));
		        		}
		        		else if (selected.equals("SpringLayout")) {
		        			vv = new VisualizationViewer(new SpringLayout(pmodel));
		        		}
		        		else if (selected.equals("SpringLayout2")) {
		        			vv = new VisualizationViewer(new SpringLayout2(pmodel));
		        		}
		        		else if (selected.equals("ISOMLayout")) {
		        			vv = new VisualizationViewer(new ISOMLayout(pmodel));
		        		}
	        		}
	        		else {
	        			vv = null;
	        		}
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

	        labelForName.setText("Name Model");

	        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
	        generalPanel.setLayout(generalPanelLayout);
	        generalPanelLayout.setHorizontalGroup(
	            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(generalPanelLayout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(labelForName)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(108, Short.MAX_VALUE))
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

	        blocksPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Interaction Points"));

	        addBlockButton.setText("Add Interaction Point");
	        addBlockButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                addBlockButtonActionPerformed(evt);
	            }
	        });

	        removeBlockButton.setText("Remove Interaction Point");
	        removeBlockButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                removeBlockButtonActionPerformed(evt);
	            }
	        });

	        adjustBlockButton.setText("Adjust Interaction Point");
	        adjustBlockButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                adjustBlockButtonActionPerformed(evt);
	            }
	        });

	        javax.swing.GroupLayout blocksPanelLayout = new javax.swing.GroupLayout(blocksPanel);
	        blocksPanel.setLayout(blocksPanelLayout);
	        blocksPanelLayout.setHorizontalGroup(
	            blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(blocksPanelLayout.createSequentialGroup()
	                .addGap(179, 179, 179)
	                .addGroup(blocksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(adjustBlockButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(addBlockButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 114, Short.MAX_VALUE)
	                    .addComponent(removeBlockButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(86, Short.MAX_VALUE))
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

	        addPortButton.setText("Add Port");
	        addPortButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                addPortButtonActionPerformed(evt);
	            }
	        });

	        removePortButton.setText("Remove Port");
	        removePortButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                removePortButtonActionPerformed(evt);
	            }
	        });

	        adjustPortButton.setText("Adjust Port");
	        adjustPortButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                adjustPortButtonActionPerformed(evt);
	            }
	        });

	        javax.swing.GroupLayout portsPanelLayout = new javax.swing.GroupLayout(portsPanel);
	        portsPanel.setLayout(portsPanelLayout);
	        portsPanelLayout.setHorizontalGroup(
	            portsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(portsPanelLayout.createSequentialGroup()
	                .addGap(177, 177, 177)
	                .addGroup(portsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(adjustPortButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(addPortButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
	                    .addComponent(removePortButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addContainerGap(90, Short.MAX_VALUE))
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

	        blockRelationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Internal Interactions"));

	        addRelationButton.setText("Add Internal Interaction");
	        addRelationButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                addRelationButtonActionPerformed(evt);
	            }
	        });

	        removeRelationButton.setText("Remove Internal Interaction");
	        removeRelationButton.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                removeRelationButtonActionPerformed(evt);
	            }
	        });

	        javax.swing.GroupLayout blockRelationsPanelLayout = new javax.swing.GroupLayout(blockRelationsPanel);
	        blockRelationsPanel.setLayout(blockRelationsPanelLayout);
	        blockRelationsPanelLayout.setHorizontalGroup(
	            blockRelationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(blockRelationsPanelLayout.createSequentialGroup()
	                .addGap(177, 177, 177)
	                .addGroup(blockRelationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
	                    .addComponent(addRelationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                    .addComponent(removeRelationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	                .addContainerGap(103, Short.MAX_VALUE))
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
	
	// action performed
	private void addBlockButtonActionPerformed(java.awt.event.ActionEvent evt) {
		BlockEditFrame.invokeBlockEditFrame(this);
		System.out.println("done with the block!");
	}

	private void removeBlockButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ProcletModels inst = ProcletModels.getInstance();
		ProcletModel pmodel = inst.getProcletClass("new");
		java.util.List<ProcletBlock> blocks = pmodel.getBlocks();
		if (!blocks.isEmpty()) {
			ProcletBlock block = (ProcletBlock) JOptionPane.showInputDialog(
	                null,
	                "select the interaction point you want to delete",
	                "Message",
	                JOptionPane.PLAIN_MESSAGE,
	                null,
	                blocks.toArray(),
	                blocks.get(0));
			// delete the block
			if (block != null) {
				pmodel.deleteBlock(block);
				this.redrawGraph();
			}
		}
		else {
			// no blocks!
			 JOptionPane.showMessageDialog(null,
					    "No interaction points to delete!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
		}
	}

	private void adjustBlockButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// get the blocks for the new model
		ProcletModels inst = ProcletModels.getInstance();
		ProcletModel pmodel = inst.getProcletClass("new");
		java.util.List<ProcletBlock> blocks = pmodel.getBlocks();
		if (!blocks.isEmpty()) {
			ProcletBlock block = (ProcletBlock) JOptionPane.showInputDialog(
	                null,
	                "select the interaction point you want to manipulate",
	                "Message",
	                JOptionPane.PLAIN_MESSAGE,
	                null,
	                blocks.toArray(),
	                blocks.get(0));
			if (block != null) {
				BlockEditFrame.invokeBlockEditFrameWithSettings(block.getBlockID(),block.getBlockType(), 
						block.isCreate(),block.getTimeOut(),this);
			}
		}
		else {
			// no blocks!
			 JOptionPane.showMessageDialog(null,
					    "No interaction points to manipulate!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addPortButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// first check if we have blocks in the model
		ProcletModels inst = ProcletModels.getInstance();
		ProcletModel pmodel = inst.getProcletClass("new");
		if (!pmodel.getBlocks().isEmpty()) {
			PortEditFrame.invokePortEditFrame(this);
		}
		else {
			JOptionPane.showMessageDialog(null,
	   					"No interaction points exist in the model! " + 
	   					"Please define first an interaction point to which you can connect a port to!",
	   					"Error",
	   					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void removePortButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ProcletModels inst = ProcletModels.getInstance();
		ProcletModel pmodel = inst.getProcletClass("new");
		java.util.List<ProcletPort> ports = pmodel.getPorts();
		if (!ports.isEmpty()) {
			ProcletPort port = (ProcletPort) JOptionPane.showInputDialog(
	                null,
	                "select the port you want to delete",
	                "Message",
	                JOptionPane.PLAIN_MESSAGE,
	                null,
	                ports.toArray(),
	                ports.get(0));
			System.out.println("port is " + port);
			// delete the block
			if (port != null) {
				pmodel.deletePort(port);
			}
			redrawGraph();
		}
		else {
			// no blocks!
			 JOptionPane.showMessageDialog(null,
					    "No ports to delete!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
		}
	}

	private void adjustPortButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// get the ports for the new model
		ProcletModels inst = ProcletModels.getInstance();
		ProcletModel pmodel = inst.getProcletClass("new");
		java.util.List<ProcletPort> ports = pmodel.getPorts();
		if (!ports.isEmpty()) {
			ProcletPort port = (ProcletPort) JOptionPane.showInputDialog(
	                null,
	                "select the port you want to manipulate",
	                "Message",
	                JOptionPane.PLAIN_MESSAGE,
	                null,
	                ports.toArray(),
	                ports.get(0));
			System.out.println("port is " + port);
			if (port != null) {
				// get corresponding block
				ProcletBlock blockFind = null;
				java.util.List<BlockPortEdge> edges = pmodel.getBlockPortEdges();
				for (BlockPortEdge edge : edges) {
					if (edge.getPort().getPortID().equals(port.getPortID())) {
						blockFind = edge.getBlock(); 
					}
				}
				if (blockFind != null) {
					PortEditFrame.invokePortEditFrameWithSettings(port.getPortID(),blockFind,
							port.getDirection(),port.getCardinality(),port.getMultiplicity(),this);
				}
			}
			//
		}
		else {
			// no blocks!
			 JOptionPane.showMessageDialog(null,
					    "No interaction points to manipulate!",
					    "Error",
					    JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addRelationButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ProcletModels pmodelsInst = ProcletModels.getInstance();
		ProcletModel pmodel = pmodelsInst.getProcletClass("new");
		if (pmodel != null && !pmodel.getBlocks().isEmpty()) {
			BRelEditFrame.invokeBRelEditFrame(this);
		}
		else {
			JOptionPane.showMessageDialog(null,
				    "No interaction points available!",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}

	private void removeRelationButtonActionPerformed(java.awt.event.ActionEvent evt) {
		ProcletModels pmodelsInst = ProcletModels.getInstance();
		ProcletModel pmodel = pmodelsInst.getProcletClass("new");
		java.util.List<BlockRel> brels = pmodel.getBRels();
		if (!brels.isEmpty()) {
			BlockRel selected = (BlockRel) JOptionPane.showInputDialog(
	                this,
	                "select the internal interaction you want to delete",
	                "Message",
	                JOptionPane.PLAIN_MESSAGE,
	                null,
	                brels.toArray(),
	                brels.get(0));
			if (selected != null) {
				pmodel.deleteBRel(selected);
			}
			redrawGraph();
		}
		else {
			JOptionPane.showMessageDialog(null,
				    "No internal interactions to delete!",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
  
  public void actionPerformed(ActionEvent e) {
	  
  }

}




