package org.yawlfoundation.yawl.procletService.editor.pconns;


import org.yawlfoundation.yawl.procletService.models.procletModel.PortConnection;
import org.yawlfoundation.yawl.procletService.models.procletModel.PortConnections;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PConnEditFrame extends JDialog {
	
	private static PConnEditFrame instance = null;
	
	private FrmPConns frmPConns = null;
	
	 // Variables declaration - do not modify
	   private javax.swing.JComboBox channelComboBox;
	   private javax.swing.JComboBox destPortComboBox;
	   private javax.swing.JButton finishButton;
	   private javax.swing.JLabel jLabel1;
	   private javax.swing.JLabel jLabel2;
	   private javax.swing.JLabel jLabel3;
	   private javax.swing.JPanel jPanel1;
	   private javax.swing.JComboBox sourcePortComboBox;
    // End of variables declaration
	
    /** Creates new form NewJFrame */
    public PConnEditFrame(Frame frame) {
    	super(frame,true);
        initComponents();
    }
    
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        finishButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ProcletModels pmodelsInst = ProcletModels.getInstance();
        java.util.List<ProcletPort> ports = pmodelsInst.getPorts();
        // get only these ports which are not connected yet
        java.util.List<PortConnection> pconnsList = PortConnections.getInstance().getPortConnections();
        java.util.List<ProcletPort> portsUnconn = new ArrayList<ProcletPort>();
        boolean found = false;
        for (ProcletPort port : ports) {
        	found = false;
        	for (PortConnection pconn : pconnsList) {
        		if (pconn.getIPort().getPortID().equals(port.getPortID()) || 
        				pconn.getOPort().getPortID().equals(port.getPortID())) {
        			// found
        			found = true;
        			break;
        		}
        	}
        	if (!found) {
        		portsUnconn.add(port);
        	}
        }
        destPortComboBox = new javax.swing.JComboBox(portsUnconn.toArray());
        sourcePortComboBox = new javax.swing.JComboBox(portsUnconn.toArray());
        PortConnections pconnsInst = PortConnections.getInstance();
        java.util.List<String> channels = pconnsInst.getChannels();
        channelComboBox = new javax.swing.JComboBox(channels.toArray());
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Add External Interaction"));

        finishButton.setText("Finish");
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("source");

        jLabel2.setText("destination");

        jLabel3.setText("channel");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                   // .addComponent(jLabel3)
                    )
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(finishButton)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(sourcePortComboBox, 0, 149, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            //.addComponent(channelComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 149, Short.MAX_VALUE)
                            .addComponent(destPortComboBox, 0, 149, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(sourcePortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(destPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    //.addComponent(channelComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    //.addComponent(jLabel3)
                    )
                .addGap(27, 27, 27)
                .addComponent(finishButton)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>


    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (instance != null) {
	     	   // adjust the Graph
	     	   ProcletPort sourcePort = (ProcletPort) sourcePortComboBox.getSelectedItem();
	     	   ProcletPort destPort = (ProcletPort) destPortComboBox.getSelectedItem();
	     	   String channel = (String) channelComboBox.getSelectedItem();
	     	   // 
	     	   PortConnections pconns = PortConnections.getInstance();
	     	   boolean exists = false;
	     	   for (PortConnection pconn : pconns.getPortConnections()) {
	     		   if (pconn.getIPort().getPortID().equals(sourcePort.getPortID()) || 
	     				   pconn.getOPort().getPortID().equals(destPort.getPortID())) {
	     			   exists = true;
	     			   break;
	     		   }
	     	   }
	     	   if (!exists) {
	     		   PortConnection pconn = new PortConnection(sourcePort,destPort,channel);
	     		   pconns.addPortConnection(pconn);
	     		   this.frmPConns.redrawGraph();
	     	   }
	     	   else {
	     		// generate warning!
     			   JOptionPane.showMessageDialog(null,
     					    "Internal Interaction already exists or trying to connect to double ports!",
     					    "Error",
     					    JOptionPane.ERROR_MESSAGE);
	     	   }
	     	   instance.dispose();
	     	   instance.setVisible(false);
	     	   instance = null;
        }
    }
    
    public static void invokePConnEditFrame(FrmPConns frmPConns) {
    	PConnEditFrame.instance = new PConnEditFrame(null);
    	instance.frmPConns = frmPConns;
    	instance.setVisible(true);
    }
    
}

