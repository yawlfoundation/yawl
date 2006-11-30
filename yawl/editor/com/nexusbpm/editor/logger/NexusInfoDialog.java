/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.logger;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.LogRecord;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import com.nexusbpm.editor.WorkflowEditor;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.SystemColor;

/**
 * The dialog window that pops up when you double click on a log record.
 *
 * @author Matthew Sandoz
 * @created November 29, 2006
 */
public class NexusInfoDialog extends JDialog{
	
	private JPanel mainPanel = null;
	private JScrollPane messageScrollPane = null;
	private JTextArea messageTextArea = null;
	private JPanel messagePanel = null;
	private JPanel buttonPanel = null;
	private JButton okButton = null;

	public NexusInfoDialog(Frame owner, String title, String contents, boolean modal) throws HeadlessException {
		super(owner, title, modal);
		initialize(contents);
	}

	public NexusInfoDialog(Frame owner, LogRecordI record, boolean modal) throws HeadlessException {
		super(owner, "Log Record Details", modal);
		String msg = record.getMessage();
		if( record.getThrowableMessage() != null && record.getThrowableMessage().length() > 0 ) {
			msg += "\n\n" + record.getThrowableMessage();
			initialize(msg);
			this.getMessageTextArea().setLineWrap(false);
		} else {
			initialize(msg);
		}
	}

	public static void showDialog(Frame owner, String title, String contents, boolean modal) throws HeadlessException {
		NexusInfoDialog dialog = new NexusInfoDialog(owner, title, contents, modal);
		dialog.setVisible(true);
		dialog.dispose();
	}
	
	public static void showDialog(Frame owner, LogRecordI record, boolean modal) throws HeadlessException {
		NexusInfoDialog dialog = new NexusInfoDialog(owner, record, modal);
		dialog.setVisible(true);
		dialog.dispose();
	}
	
	private void initialize(String contents) {
		this.setSize(new Dimension(400, 300));
		this.setPreferredSize(new Dimension(400, 300));
		this.setContentPane(getMainPanel());
		this.getMessageTextArea().setText(contents);
		this.getMessageTextArea().setCaretPosition(0); 
		this.setLocationRelativeTo(this.getOwner());
		this.pack();
		this.getOkButton().requestFocus();
	}
	
	/**
	 * This method initializes mainPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.anchor = GridBagConstraints.NORTH;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.insets = new Insets(10, 10, 5, 10);
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.ipady = 0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weightx = 0.0;
			gridBagConstraints1.weighty = 0.0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.ipady = 0;
			gridBagConstraints1.insets = new Insets(5, 10, 10, 10);
			gridBagConstraints1.gridy = 1;
			mainPanel = new JPanel();
			mainPanel.setLayout(new GridBagLayout());
			mainPanel.setBackground(SystemColor.control);
			mainPanel.add(getMessagePanel(), gridBagConstraints2);
			mainPanel.add(getButtonPanel(), gridBagConstraints1);
		}
		return mainPanel;
	}
	/**
	 * This method initializes messageScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getMessageScrollPane() {
		if (messageScrollPane == null) {
			messageScrollPane = new JScrollPane();
			messageScrollPane.setViewportView(getMessageTextArea());
		}
		return messageScrollPane; 
	}
	/**
	 * This method initializes messageTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getMessageTextArea() {
		if (messageTextArea == null) {
			messageTextArea = new JTextArea();
			messageTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
			messageTextArea.setEditable(false);
			messageTextArea.setTabSize(1);
			messageTextArea.setLineWrap(true);
			messageTextArea.setWrapStyleWord(true);
		}
		return messageTextArea;
	}
	/**
	 * This method initializes messagePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMessagePanel() {
		if (messagePanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = -1;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.ipady = 0;
			gridBagConstraints.gridx = -1;
			messagePanel = new JPanel();
			messagePanel.setLayout(new GridBagLayout());
			messagePanel.setBackground(SystemColor.control);
			messagePanel.add(getMessageScrollPane(), gridBagConstraints);
		}
		return messagePanel;
	}
	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.anchor = GridBagConstraints.CENTER;
			gridBagConstraints3.weighty = 1.0;
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			buttonPanel.setBackground(SystemColor.control);
			buttonPanel.add(getOkButton(), gridBagConstraints3);
		}
		return buttonPanel;
	}
	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.setSelected(false);
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					NexusInfoDialog.this.setVisible(false);
				}
			});
		}
		return okButton;
	}
	public static void main(String[] args) {
		Exception e = new Exception("Test Exception");
		Exception e2 = new Exception("Test Exception", e);
		
		LogRecordI record = new LogRecordVO(1,1,new Date().getTime(), 1, "test", e2);
		NexusInfoDialog.showDialog(null, record, true); 
		NexusInfoDialog.showDialog(null, "Event History", "Something was going to happen here." , true); 
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
