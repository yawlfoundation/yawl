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


import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModel;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Direction;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletPort.Signature;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PortEditFrame extends JDialog {

	private static PortEditFrame instance = null;
	
	   // Variables declaration - do not modify
    private javax.swing.JComboBox blockComboBox;
    private javax.swing.JComboBox cardinalityComboBox;
    private javax.swing.JComboBox directionComboBox;
    private javax.swing.JButton finishButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox multiplicityComboBox;
    private javax.swing.JTextField portIDtextField;
    // End of variables declaration
    
    // predefined values
    private String portIDpv = "";
    private ProcletBlock blockpv = null;
    private ProcletPort.Direction dirpv = null;
    private ProcletPort.Signature multpv = null;
    private ProcletPort.Signature cardpv = null;
    
    private FrmModel frmModel = null;
	
	/** Creates new form NewJFrame */
    public PortEditFrame(Frame frame) {
    	super(frame,true);
        initComponents();
    }
    
    public PortEditFrame (Frame frame, String portID, ProcletBlock block, Direction dir, Signature card,
    		Signature mult) {
    	//this(null);
    	super(frame,true);
    	this.portIDpv = portID;
    	this.blockpv = block;
    	this.dirpv = dir;
    	this.multpv = mult;
    	this.cardpv = card;
    	initComponents();
    }
    
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        finishButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        portIDtextField = new javax.swing.JTextField();
        if (!this.portIDpv.equals("")) {
        	portIDtextField.setText(portIDpv);
        }
        // get blocks
        ProcletModels pmodelInst = ProcletModels.getInstance();
        ProcletModel pmodel = pmodelInst.getProcletClass("new");
        blockComboBox = new javax.swing.JComboBox(pmodel.getBlocks().toArray());
        if (blockpv != null) {
        	blockComboBox.setSelectedItem(blockpv);
        }
        java.util.List<String> valuesDir = new ArrayList<String>();
        valuesDir.add("IN");
        valuesDir.add("OUT");
        directionComboBox = new javax.swing.JComboBox(valuesDir.toArray());
        if (this.dirpv != null) {
        	directionComboBox.setSelectedItem(dirpv.toString());
        }
        java.util.List<String> valuesCard = new ArrayList<String>();
        //valuesCard.add("ZERO");
        valuesCard.add("ONE");
        valuesCard.add("PLUS");
        valuesCard.add("STAR");
        valuesCard.add("QUEST");
        cardinalityComboBox = new javax.swing.JComboBox(valuesCard.toArray());
        if (cardpv != null) {
        	cardinalityComboBox.setSelectedItem(cardpv.toString());
        }
        jLabel5 = new javax.swing.JLabel();
        java.util.List<String> valuesMult = new ArrayList<String>();
        //valuesMult.add("ZERO");
        valuesMult.add("ONE");
        valuesMult.add("PLUS");
        valuesMult.add("STAR");
        valuesMult.add("QUEST");
        multiplicityComboBox = new javax.swing.JComboBox(valuesMult.toArray());
        if (this.multpv != null) {
        	multiplicityComboBox.setSelectedItem(multpv.toString());
        }
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit Port"));

        finishButton.setText("Finish");
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Port Identifier");

        jLabel2.setText("Attached Interaction Point");

        jLabel3.setText("Direction");

        jLabel4.setText("Cardinality");

        jLabel5.setText("Multiplicity");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel5))
                .addGap(82, 82, 82)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(finishButton)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(multiplicityComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(portIDtextField, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                        .addComponent(blockComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(directionComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cardinalityComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(portIDtextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(blockComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(directionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cardinalityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(multiplicityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addComponent(finishButton)
                .addGap(24, 24, 24))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
    
    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	if (instance != null) {
    		// adjust the graph
    		String portID = portIDtextField.getText();
    		ProcletBlock block = (ProcletBlock) blockComboBox.getSelectedItem();
    		Direction dir = ProcletPort.getDirectionFromString((String)directionComboBox.getSelectedItem());
    		Signature card = ProcletPort.getSignatureFromString((String)cardinalityComboBox.getSelectedItem());
    		Signature mult = ProcletPort.getSignatureFromString((String)multiplicityComboBox.getSelectedItem());
    		ProcletModels inst = ProcletModels.getInstance();
    		//
     	   	ProcletModel pmodel = inst.getProcletClass("new");
     	   	if (pmodel != null) {
     	   		ProcletPort exPort = null;
     	   		java.util.List<ProcletPort> ports = pmodel.getPortsBlock(block);
     	   		for (ProcletPort port : ports) {
     	   			if (port.getPortID().equals(portID) || port.getPortID().equals(portIDpv)) {
     	   				exPort = port;
     	   				break;
     	   			}
     	   		}
     	   		if (exPort == null && !portID.equals("")) {
     	   			// 	commit to graph
     	   			ProcletPort portNew = new ProcletPort(portID,dir,card,mult);
     	   			pmodel.addProcletPort(portNew, block);
     	   		}
     	   		else if (exPort != null & portIDpv.equals("")){
     	   			// generate warning!
     	   			JOptionPane.showMessageDialog(null,
     	   					"Port already exists!",
     	   					"Error",
     	   					JOptionPane.ERROR_MESSAGE);
     	   		}
     	   		else if (portID.equals("")) {
     	   			JOptionPane.showMessageDialog(null,
     	   					"The port has no name! Please provide a name!",
     	   					"Error",
     	   					JOptionPane.ERROR_MESSAGE);
     	   		}
     	   		else if (exPort != null && !this.portIDpv.equals("")) {
     	   			// update the values for this port
     	   			exPort.setPortID(portID);
     	   			exPort.setDirection(dir);
     	   			exPort.setMultiplicity(mult);
     	   			exPort.setCardinality(card);
     	   			// connect to a port
     	   			pmodel.deletePort(exPort);
     	   			pmodel.addProcletPort(exPort, block);
     	   		}
     	   	}
     	   	frmModel.redrawGraph();
     	   	instance.dispose();
     	   	instance.setVisible(false);
     	   	instance = null;
    	}
    }
    
    public static void invokePortEditFrame(FrmModel frmModel) {
    	PortEditFrame.instance = new PortEditFrame(null);
    	instance.frmModel = frmModel;
    	instance.setVisible(true);
    }
    
    public static void invokePortEditFrameWithSettings(String portID, ProcletBlock block, Direction dir, Signature card,
    		Signature mult,FrmModel frmModel) {
    	PortEditFrame.instance = new PortEditFrame(null,portID, block, dir, card, mult);
    	instance.frmModel = frmModel;
    	instance.setVisible(true);
    }
    
}
