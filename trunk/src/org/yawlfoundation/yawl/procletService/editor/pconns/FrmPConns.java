package org.yawlfoundation.yawl.procletService.editor.pconns;


import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Transformer;
import org.yawlfoundation.yawl.procletService.editor.DesignInternalFrame;
import org.yawlfoundation.yawl.procletService.models.procletModel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class FrmPConns extends DesignInternalFrame {
	
	static final String TITLE = "External Interactions";

	private static FrmPConns single = null;
	private PConnsCoordinator pconnsCoordinator;
	
    private javax.swing.JButton addChannelButton;
    private javax.swing.JButton addPConnButton;
    private javax.swing.JPanel channelPanel;
    private javax.swing.JPanel graphOptionsPanel;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JPanel pconnsPanel;
    private javax.swing.JButton removeChannelButton;
    private javax.swing.JButton removePConnButton;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSplitPane splitPaneMain;
    private javax.swing.JSplitPane splitPanelLeft;
    
    private javax.swing.JPanel topOptionsPanel = new javax.swing.JPanel();
    private javax.swing.JPanel bottomOptionsPanel = new javax.swing.JPanel();
    
    private JComboBox layoutBox = null;
    
    private VisualizationViewer vv = null;
    private DefaultModalGraphMouse gm;
    
    private Transformer vertexPaint = null;
	  
	private FrmPConns(PConnsCoordinator aPConnsCoordinator) {
		super(TITLE);
	    this.pconnsCoordinator = aPConnsCoordinator;
	    try {
	    	jbInit();
	    }
	    catch (Exception ex) {
	      ex.printStackTrace();
	    }
	}

	public static FrmPConns singleton(PConnsCoordinator
		                                          coord) {
		if (single == null) {
			single = new FrmPConns(coord);
		}
		return single;
	}
	
	 public static void finish() {
		  single = null;
	  }

	protected void jbInit() throws Exception {
		initComponents();
		drawGraph();
		this.setContentPane(splitPaneMain);
	}
	
	private void setVisualizationViewer (VisualizationViewer vviewer) {
		PortConnections pconns = PortConnections.getInstance();
		// give other color
		  Transformer vertexPaint = new Transformer() {
			  public Paint transform (Object obj) {
				  if (obj instanceof ProcletBlock) {
					  if (((ProcletBlock) obj).getBlockID().equals("exception")) {
						return Color.BLUE;  
					  }
					  else {
						  return Color.RED;
					  }
				  }
				  else if (obj instanceof ProcletPort) {
					  return Color.GREEN;  
				  }
				  return Color.RED;
			  }
		  };
		  this.vertexPaint = vertexPaint;
		vv = vviewer;
		//vv.setPickSupport(new edu.uci.ics.jung.visualization.picking.ShapePickSupport(vv));
		vv.setPreferredSize(new Dimension(350,350));
		vv.getRenderContext().setVertexFillPaintTransformer(this.vertexPaint);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());
		vv.setGraphMouse(gm);
		splitPanelLeft.setRightComponent(new GraphZoomScrollPane(vv));
//		vv.validate();
//		vv.repaint();
//		splitPanelLeft.repaint();
//		getContentPane().repaint();
//		validate();
//		repaint();
	}
	
	private void drawGraph() {
		  PortConnections pconnsInst = PortConnections.getInstance();
		  vv = new VisualizationViewer(new CircleLayout(pconnsInst));
		  setVisualizationViewer(vv);
		  vv.validate();
		  vv.repaint();
		  splitPanelLeft.repaint();
		  getContentPane().repaint();
		  validate();
		  repaint();
	}
	
	public void redrawGraph() {
		  vv.validate();
		  vv.repaint();
		  splitPanelLeft.repaint();
		  getContentPane().repaint();
		  validate();
		  repaint();
	}
	
    private void initComponents() {
    	splitPaneMain = new javax.swing.JSplitPane();
        splitPanelLeft = new javax.swing.JSplitPane();
        graphOptionsPanel = new javax.swing.JPanel();
        graphPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        pconnsPanel = new javax.swing.JPanel();
        addPConnButton = new javax.swing.JButton();
        removePConnButton = new javax.swing.JButton();
        channelPanel = new javax.swing.JPanel();
        addChannelButton = new javax.swing.JButton();
        removeChannelButton = new javax.swing.JButton();

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
        		PortConnections pconnsInst = PortConnections.getInstance();
        		String selected = (String) ((JComboBox) e.getSource()).getSelectedItem();
        		if (selected.equals("FRLayout")) {
        			vv = new VisualizationViewer(new FRLayout(pconnsInst));
        		}
        		else if (selected.equals("KKLayout")) {
        			vv = new VisualizationViewer(new KKLayout(pconnsInst));
        		}
        		else if (selected.equals("CircleLayout")) {
        			vv = new VisualizationViewer(new CircleLayout(pconnsInst));
        		}
        		else if (selected.equals("SpringLayout")) {
        			vv = new VisualizationViewer(new SpringLayout(pconnsInst));
        		}
        		else if (selected.equals("SpringLayout2")) {
        			vv = new VisualizationViewer(new SpringLayout2(pconnsInst));
        		}
        		else if (selected.equals("ISOMLayout")) {
        			vv = new VisualizationViewer(new ISOMLayout(pconnsInst));
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

        pconnsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("External Interactions"));

        addPConnButton.setText("Add External Interaction");
        addPConnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPConnButtonActionPerformed(evt);
            }
        });

        removePConnButton.setText("Remove External Interaction");
        removePConnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePConnButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pconnsPanelLayout = new javax.swing.GroupLayout(pconnsPanel);
        pconnsPanel.setLayout(pconnsPanelLayout);
        pconnsPanelLayout.setHorizontalGroup(
            pconnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pconnsPanelLayout.createSequentialGroup()
                .addGap(179, 179, 179)
                .addGroup(pconnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(removePConnButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addPConnButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 114, Short.MAX_VALUE))
                .addContainerGap(119, Short.MAX_VALUE))
        );
        pconnsPanelLayout.setVerticalGroup(
            pconnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pconnsPanelLayout.createSequentialGroup()
                .addComponent(addPConnButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removePConnButton)
                .addContainerGap(181, Short.MAX_VALUE))
        );

        rightPanel.add(pconnsPanel);

        channelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Channels"));

        addChannelButton.setText("Add Channel");
        addChannelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChannelButtonActionPerformed(evt);
            }
        });

        removeChannelButton.setText("Remove Channel");
        removeChannelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeChannelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout channelPanelLayout = new javax.swing.GroupLayout(channelPanel);
        channelPanel.setLayout(channelPanelLayout);
        channelPanelLayout.setHorizontalGroup(
            channelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelPanelLayout.createSequentialGroup()
                .addGap(177, 177, 177)
                .addGroup(channelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(removeChannelButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addChannelButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
                .addContainerGap(121, Short.MAX_VALUE))
        );
        channelPanelLayout.setVerticalGroup(
            channelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(channelPanelLayout.createSequentialGroup()
                .addComponent(addChannelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeChannelButton)
                .addContainerGap(201, Short.MAX_VALUE))
        );

        //rightPanel.add(channelPanel);

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

    private void addPConnButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
    	// first check if we have ports in the model
		ProcletModels inst = ProcletModels.getInstance();
		java.util.List<ProcletPort> ports = inst.getPorts();
		if (!ports.isEmpty()) {
			PConnEditFrame.invokePConnEditFrame(this);
			// when done update
		}
		else {
			JOptionPane.showMessageDialog(null,
	   					"No ports exist for the models! ",
	   					"Error",
	   					JOptionPane.ERROR_MESSAGE);
		}
    }                                              

    private void removePConnButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        PortConnections pconnsInst = PortConnections.getInstance();
        java.util.List<PortConnection> pconns = pconnsInst.getPortConnections();
        if (!pconns.isEmpty()) {
        	PortConnection pconn = (PortConnection) JOptionPane.showInputDialog(
	                null,
	                "select the external interaction you want to delete",
	                "Port Connection",
	                JOptionPane.PLAIN_MESSAGE,
	                null,
	                pconns.toArray(),
	                pconns.get(0));
        	if (pconn != null) {
        		pconnsInst.deletePortConnection(pconn);
        	}
        }
        else {
        	// generate warning
        	JOptionPane.showMessageDialog(null,
   					"No external interactions exist!",
   					"Error",
   					JOptionPane.ERROR_MESSAGE);
        }
    }                                                 

    private void addChannelButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
    	String result = (String) JOptionPane.showInputDialog(
                null,
                "give the name of the channel you want to add",
                "Channel",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
    	if (result != null) {
	    	System.out.println("channel name is " + result);
	    	// check if channel name already exists
	    	PortConnections pconns = PortConnections.getInstance();
	    	if (pconns.getChannels().contains(result)) {
	    		// generate error message
	    		JOptionPane.showMessageDialog(null,
	   					"channel already exist!",
	   					"Error",
	   					JOptionPane.ERROR_MESSAGE);
	    	}
	    	else {
	    		// add the channel
	    		pconns.addChannel(result);
	    	}
    	}
    }                                             

    private void removeChannelButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
    	PortConnections pconns = PortConnections.getInstance();
        java.util.List<String> channels = pconns.getChannels();
    	String channel = (String) JOptionPane.showInputDialog(
                null,
                "select the channel you want to delete",
                "Channel",
                JOptionPane.PLAIN_MESSAGE,
                null,
                channels.toArray(),
                channels.get(0));
    	System.out.println("channel is:" + channel);
    	if (channel != null) {
	    	// check if there are still pcs with this channel
            java.util.List<PortConnection> plist = pconns.getPortConnections();
	    	boolean exists = false;
	    	for (PortConnection pconn : plist) {
	    		if (pconn.getChannel().equals(channel)) {
	    			exists = true;
	    			break;
	    		}
	    	}
	    	if (!exists) {
				pconns.removeChannel(channel);
				drawGraph();
			}
			else {
				JOptionPane.showMessageDialog(null,
	   					"channel cannot be removed",
	   					"Error",
	   					JOptionPane.ERROR_MESSAGE);
			}
    	}
    }                                      	
	
		  
	public void actionPerformed(ActionEvent e) {
			  
	}		  
}
