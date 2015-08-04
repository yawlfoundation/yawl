package org.yawlfoundation.yawl.procletService.editor.model;


import org.yawlfoundation.yawl.procletService.models.procletModel.BlockRel;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletBlock;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModel;
import org.yawlfoundation.yawl.procletService.models.procletModel.ProcletModels;

import javax.swing.*;
import java.awt.*;

public class BRelEditFrame extends JDialog {
	
	private static BRelEditFrame instance = null;
	
	 // Variables declaration - do not modify
    private javax.swing.JComboBox sourceBlockComboBox;
    private javax.swing.JButton finishButton;
    private javax.swing.JComboBox destBlockComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration
	
	private String sourceBlockID = "";
	private String destBlockID = "";
	
	private FrmModel frmModel = null;
	
    /** Creates new form NewJFrame */
    public BRelEditFrame(Frame frame) {
    	super(frame, true);
        initComponents();
    }
    
    public BRelEditFrame(String sourceBlockID, String destBlockID) {
    	this(null);
    	this.sourceBlockID = sourceBlockID;
    	this.destBlockID = destBlockID;
    	initComponents();
    }
    
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        finishButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ProcletModels pmodelsInst = ProcletModels.getInstance();
        ProcletModel pmodel = pmodelsInst.getProcletClass("new");
        sourceBlockComboBox = new javax.swing.JComboBox(pmodel.getBlocks().toArray());
        destBlockComboBox = new javax.swing.JComboBox(pmodel.getBlocks().toArray());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit Internal Interaction"));

        finishButton.setText("Finish");
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Source Interaction Point");

        jLabel2.setText("Destination Interaction Point");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(82, 82, 82)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(finishButton)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceBlockComboBox, 0, 127, Short.MAX_VALUE)
                            .addComponent(destBlockComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(67, 67, 67))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(destBlockComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(sourceBlockComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(finishButton)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>

    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (instance != null) {
	     	   // adjust the Graph
	     	   ProcletBlock destBlockID = (ProcletBlock) sourceBlockComboBox.getSelectedItem();
	     	   ProcletBlock sourceBlockID = (ProcletBlock) destBlockComboBox.getSelectedItem();
	     	   // get the new graph
	     	   ProcletModels inst = ProcletModels.getInstance();
	     	   ProcletModel pmodel = inst.getProcletClass("new");
	     	   if (pmodel != null) {
	     		   java.util.List<BlockRel> brels = pmodel.getBRels();
	     		   boolean exists = false;
	     		   for (BlockRel brel : brels) {
	     			   if (brel.getIBlock().getBlockID().equals(sourceBlockID.getBlockID()) && 
	     					   brel.getOBlock().getBlockID().equals(destBlockID.getBlockID())) {
	     				   exists = true;
	     				   break;
	     			   }
	     		   }
	     		   // check if only from PI to FO
	     		   boolean PItoFOcheck = true;
	     		   if (sourceBlockID.getBlockType().equals(ProcletBlock.BlockType.PI) &&
	     				   destBlockID.getBlockType().equals(ProcletBlock.BlockType.FO)) {
	     			  PItoFOcheck = true;
	     		   }
	     		   else {
	     			  PItoFOcheck = false;
	     		   }
	     		   if (!exists && PItoFOcheck) {
	     			   // commit to graph
	     			   pmodel.addBRel(sourceBlockID, destBlockID);
	     		   }
	     		   else if (exists && PItoFOcheck) {
	     			   // generate warning!
	     			   JOptionPane.showMessageDialog(null,
	     					    "Block Relation already exists!",
	     					    "Error",
	     					    JOptionPane.ERROR_MESSAGE);
	     		   }
	     		   else if (!PItoFOcheck) {
	     			  JOptionPane.showMessageDialog(null,
	     					    "A block relation may only be from a PI block to FO block!",
	     					    "Error",
	     					    JOptionPane.ERROR_MESSAGE);
	     		   }
	     	   }
	     	   frmModel.redrawGraph();
	     	   instance.dispose();
	     	   instance.setVisible(false);
	     	   instance = null;
        }
    }
    
    public static void invokeBRelEditFrame(FrmModel frmModel) {
    	BRelEditFrame.instance = new BRelEditFrame(null);
    	instance.frmModel = frmModel;
    	instance.setVisible(true);
    }
    
    public static void invokeBRelEditFrameWithSettings(String sourceBlockID, String destBlockID, FrmModel frmModel) {
    	BRelEditFrame.instance = new BRelEditFrame(sourceBlockID, destBlockID);
    	instance.frmModel = frmModel;
    	instance.setVisible(true);
    }

}
