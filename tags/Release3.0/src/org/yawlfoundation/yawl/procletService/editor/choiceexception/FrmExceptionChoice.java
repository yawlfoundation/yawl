package org.yawlfoundation.yawl.procletService.editor.choiceexception;


import org.yawlfoundation.yawl.procletService.blockType.CompleteCaseDeleteCase;
import org.yawlfoundation.yawl.procletService.editor.DesignInternalFrame;
import org.yawlfoundation.yawl.procletService.util.EntityMID;

import java.awt.event.ActionEvent;
import java.util.List;

public class FrmExceptionChoice extends DesignInternalFrame {
	
	static final String TITLE = "Exception Proclet Instance";

	  private static FrmExceptionChoice single = null;
	  private static ExceptionChoiceCoordinator exceptionChoiceCoordinator;
	  
	   // Variables declaration - do not modify
	  private javax.swing.JPanel decisionPanel;
	    private javax.swing.JPanel emidsPanel;
	    private javax.swing.JTextField emidsTextField;
	    private javax.swing.JComboBox exceptionComboBox;
	    private javax.swing.JButton handleButton;
	    private javax.swing.JButton ignoreButton;
	    private javax.swing.JLabel jLabel1;
	    private javax.swing.JScrollPane jScrollPane1;
	    private javax.swing.JPanel mainPanel;
	    private javax.swing.JButton selectButton;
	    private javax.swing.JPanel selectionPanel;
	    // End of variables declaration
	  
	  private FrmExceptionChoice(ExceptionChoiceCoordinator aChoiceCoordinator) {
		    super(TITLE);
		    this.exceptionChoiceCoordinator = aChoiceCoordinator;
		    try {
		    	jbInit();
		    }
		    catch (Exception ex) {
		      ex.printStackTrace();
		    }
		  }

		  public static FrmExceptionChoice singleton(ExceptionChoiceCoordinator
		                                          coord) {
		    if (single == null) {
		      single = new FrmExceptionChoice(coord);
		    }
		    return single;
		  }
		  
		  public static void finish() {
			  single = null;
		  }

		  protected void jbInit() throws Exception {
			  initComponents();
			  this.setContentPane(mainPanel);
			  initializeFields();
		  }
		  
		  private void initializeFields() {
			  List<List> exc = CompleteCaseDeleteCase.getExceptions();
			  for (List l : exc) {
				  String classID = (String) l.get(1);
				  String procletID = (String) l.get(2);
				  String result = classID + "," + procletID;
				  exceptionComboBox.addItem(result);
			  }
			  this.handleButton.setEnabled(false);
			  this.ignoreButton.setEnabled(false);
		  }
		  
		   private void initComponents() {

		        mainPanel = new javax.swing.JPanel();
		        selectionPanel = new javax.swing.JPanel();
		        exceptionComboBox = new javax.swing.JComboBox();
		        jLabel1 = new javax.swing.JLabel();
		        selectButton = new javax.swing.JButton();
		        emidsPanel = new javax.swing.JPanel();
		        jScrollPane1 = new javax.swing.JScrollPane();
		        emidsTextField = new javax.swing.JTextField();
		        decisionPanel = new javax.swing.JPanel();
		        handleButton = new javax.swing.JButton();
		        ignoreButton = new javax.swing.JButton();
		        
		        emidsTextField.setEditable(false);

		        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.Y_AXIS));

		        selectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Exception"));

		        exceptionComboBox.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                exceptionComboBoxActionPerformed(evt);
		            }
		        });

		        jLabel1.setText("Proclet Class and Proclet Instance");

		        selectButton.setText("Select");
		        selectButton.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                selectButtonActionPerformed(evt);
		            }
		        });

		        javax.swing.GroupLayout selectionPanelLayout = new javax.swing.GroupLayout(selectionPanel);
		        selectionPanel.setLayout(selectionPanelLayout);
		        selectionPanelLayout.setHorizontalGroup(
		            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(selectionPanelLayout.createSequentialGroup()
		                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                    .addGroup(selectionPanelLayout.createSequentialGroup()
		                        .addGap(22, 22, 22)
		                        .addComponent(jLabel1)
		                        .addGap(18, 18, 18)
		                        .addComponent(exceptionComboBox, 0, 386, Short.MAX_VALUE))
		                    .addGroup(selectionPanelLayout.createSequentialGroup()
		                        .addGap(163, 163, 163)
		                        .addComponent(selectButton)))
		                .addContainerGap())
		        );
		        selectionPanelLayout.setVerticalGroup(
		            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(selectionPanelLayout.createSequentialGroup()
		                .addContainerGap()
		                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		                    .addComponent(exceptionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                    .addComponent(jLabel1))
		                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
		                .addComponent(selectButton)
		                .addGap(39, 39, 39))
		        );

		        mainPanel.add(selectionPanel);

		        emidsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Affected Entities"));

		        jScrollPane1.setViewportView(emidsTextField);

		        javax.swing.GroupLayout emidsPanelLayout = new javax.swing.GroupLayout(emidsPanel);
		        emidsPanel.setLayout(emidsPanelLayout);
		        emidsPanelLayout.setHorizontalGroup(
		            emidsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(emidsPanelLayout.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
		                .addContainerGap())
		        );
		        emidsPanelLayout.setVerticalGroup(
		            emidsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(emidsPanelLayout.createSequentialGroup()
		                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
		                .addContainerGap())
		        );

		        mainPanel.add(emidsPanel);

		        decisionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Decision"));

		        handleButton.setText("Handle Exception");
		        handleButton.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                handleButtonActionPerformed(evt);
		            }
		        });

		        ignoreButton.setText("Ignore Exception");
		        ignoreButton.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		                ignoreButtonActionPerformed(evt);
		            }
		        });

		        javax.swing.GroupLayout decisionPanelLayout = new javax.swing.GroupLayout(decisionPanel);
		        decisionPanel.setLayout(decisionPanelLayout);
		        decisionPanelLayout.setHorizontalGroup(
		            decisionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(decisionPanelLayout.createSequentialGroup()
		                .addGap(224, 224, 224)
		                .addGroup(decisionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
		                    .addComponent(ignoreButton)
		                    .addComponent(handleButton))
		                .addContainerGap(227, Short.MAX_VALUE))
		        );
		        decisionPanelLayout.setVerticalGroup(
		            decisionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addGroup(decisionPanelLayout.createSequentialGroup()
		                .addContainerGap()
		                .addComponent(handleButton)
		                .addGap(18, 18, 18)
		                .addComponent(ignoreButton)
		                .addContainerGap(20, Short.MAX_VALUE))
		        );

		        mainPanel.add(decisionPanel);

		        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		        this.setLayout(layout);
		        layout.setHorizontalGroup(
		            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
		        );
		        layout.setVerticalGroup(
		            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		            .addComponent(mainPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
		        );
		    }// </editor-fold>

		    private void exceptionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
		        // TODO add your handling code here:
		    }

		    private void selectButtonActionPerformed(java.awt.event.ActionEvent evt) {
		    	String item = (String) exceptionComboBox.getSelectedItem();
		    	String [] split = item.split(",");
		    	String classID = split[0];
		    	String procletID = split[1];
		    	List<List> exc = CompleteCaseDeleteCase.getExceptions();
		    	for (List l : exc) {
		    		List<EntityMID> emids = (List<EntityMID>) l.get(0);
		    		String classIDl = (String) l.get(1);
		    		String procletIDl = (String) l.get(2);
		    		if (classID.equals(classIDl) && procletID.equals(procletIDl)) {
		    			String emidsStr = "";
		    			for (EntityMID emid : emids) {
		    				emidsStr = emidsStr + emid + ";";
		    			}
		    			emidsStr = emidsStr.substring(0,emidsStr.length()-1);
		    			emidsTextField.setText(emidsStr);
		    			break;
		    		}
		    	}
		    	this.handleButton.setEnabled(true);
				this.ignoreButton.setEnabled(true);
		    }

		    private void handleButtonActionPerformed(java.awt.event.ActionEvent evt) {
		    	String item = (String) exceptionComboBox.getSelectedItem();
		    	String [] split = item.split(",");
		    	String classID = split[0];
		    	String procletID = split[1];
		    	List<List> exc = CompleteCaseDeleteCase.getExceptions();
		    	for (List l : exc) {
		    		List<EntityMID> emids = (List<EntityMID>) l.get(0);
		    		String classIDl = (String) l.get(1);
		    		String procletIDl = (String) l.get(2);
		    		if (classID.equals(classIDl) && procletID.equals(procletIDl)) {
		    			// remove from list
		    			exceptionComboBox.removeItem(item);
		    			// remove from db
		    			CompleteCaseDeleteCase.deleteException(classID, procletID, "exception");
		    			CompleteCaseDeleteCase.deleteExceptionCaseSelected(classID, procletID, "exception");
		    			// put it in db for emids
		    			CompleteCaseDeleteCase.publishExceptionCase(classID, procletID, "exception");
		    			break;
		    		}
		    	}
		    	this.handleButton.setEnabled(false);
				this.ignoreButton.setEnabled(false);
				emidsTextField.setText("");
				// 
				if (exceptionComboBox.getItemCount() == 0) {
					this.selectButton.setEnabled(false);
				}
		    }

		    private void ignoreButtonActionPerformed(java.awt.event.ActionEvent evt) {
		        // throw away
		    	String item = (String) exceptionComboBox.getSelectedItem();
		    	String [] split = item.split(",");
		    	String classID = split[0];
		    	String procletID = split[1];
		    	List<List> exc = CompleteCaseDeleteCase.getExceptions();
		    	for (List l : exc) {
		    		List<EntityMID> emids = (List<EntityMID>) l.get(0);
		    		String classIDl = (String) l.get(1);
		    		String procletIDl = (String) l.get(2);
		    		if (classID.equals(classIDl) && procletID.equals(procletIDl)) {
		    			// remove from list
		    			exceptionComboBox.removeItem(item);
		    			// remove from db
		    			CompleteCaseDeleteCase.deleteException(classID, procletID, "exception");
		    			CompleteCaseDeleteCase.deleteExceptionCaseSelected("none", "none", "exception");
		    			// put it in db for emids
		    			CompleteCaseDeleteCase.publishExceptionCase("none", "none", "exception");
		    			break;
		    		}
		    	}
		    	this.handleButton.setEnabled(false);
				this.ignoreButton.setEnabled(false);
				emidsTextField.setText("");
				// 
				if (exceptionComboBox.getItemCount() == 0) {
					this.selectButton.setEnabled(false);
				}
		    }
		  
		  public void actionPerformed(ActionEvent e) {
			  
		  }

}
