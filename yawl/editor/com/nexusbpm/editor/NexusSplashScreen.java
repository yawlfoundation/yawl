package com.nexusbpm.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.JProgressBar;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.SystemColor;
import java.awt.Rectangle;
import javax.swing.JButton;
import java.awt.Point;
import javax.swing.border.BevelBorder;
import javax.swing.JTextField;

import org.python.modules.thread;

import java.awt.Cursor;
import javax.swing.border.SoftBevelBorder;

public class NexusSplashScreen extends JWindow {
	private JProgressBar loadProgressBar = null;
	private JPanel mainPanel = null;
	private JLabel nameLabel = null;
	private JButton iconButton = null;
	private JTextField currentOperationTextField = null;

	/**
	 * This method initializes 
	 * 
	 */
	public NexusSplashScreen() {
		super();
		initialize();
		setLocationRelativeTo(null);
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setSize(new Dimension(460, 140));
        this.setContentPane(getMainPanel());
			
	}

	/**
	 * This method initializes loadProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getLoadProgressBar() {
		if (loadProgressBar == null) {
			loadProgressBar = new JProgressBar();
			loadProgressBar.setBackground(SystemColor.window);
			loadProgressBar.setBounds(new Rectangle(160, 55, 286, 22));
			loadProgressBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			loadProgressBar.setStringPainted(true);
			loadProgressBar.setValue(0);
		}
		return loadProgressBar;
	}

	/**
	 * This method initializes mainPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			nameLabel = new JLabel();
			nameLabel.setText("Loading Nexus Editor...");
			nameLabel.setBounds(new Rectangle(160, 10, 286, 31));
			nameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
			nameLabel.setBackground(new Color(238, 238, 238));
			nameLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			nameLabel.setName("");
			mainPanel = new JPanel();
			mainPanel.setLayout(null);
			mainPanel.setBackground(SystemColor.control);
			mainPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
			mainPanel.add(nameLabel, null);
			mainPanel.add(getLoadProgressBar(), null);
			mainPanel.add(getIconButton(), null);
			mainPanel.add(getCurrentOperationTextField(), null);
		}
		return mainPanel;
	}

	/**
	 * This method initializes iconButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getIconButton() {
		if (iconButton == null) {
			iconButton = new JButton();
			iconButton.setIcon(new ImageIcon(getClass().getResource("/com/nexusbpm/editor/icon/128x128/apps/kivio.png")));
			iconButton.setSize(new Dimension(136, 117));
			iconButton.setAlignmentX(1.0F);
			iconButton.setAlignmentY(1.0F);
			iconButton.setBorder(null);
			iconButton.setBackground(SystemColor.control);
			iconButton.setLocation(new Point(10, 10));
		}
		return iconButton;
	}

	/**
	 * This method initializes currentOperationTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCurrentOperationTextField() {
		if (currentOperationTextField == null) {
			currentOperationTextField = new JTextField();
			currentOperationTextField.setBounds(new Rectangle(160, 85, 286, 31));
			currentOperationTextField.setBorder(null);
			currentOperationTextField.setText("");
			currentOperationTextField.setOpaque(false);
			currentOperationTextField.setBackground(null);
		}
		return currentOperationTextField;
	}

	public void update(int percent, String operation) {
		this.getLoadProgressBar().setValue(percent);
		this.getCurrentOperationTextField().setText(operation);
	}
	
	public static void main(String[] args) {
	    // Throw a nice little title page up on the screen first
	    NexusSplashScreen splash = new NexusSplashScreen();
	    splash.setVisible(true);
	    try {
			splash.update(0, "starting init");
			Thread.sleep(1000);
			splash.update(33, "executing the doing something move");
			Thread.sleep(1000);
			splash.update(66, "postmove operations");
			Thread.sleep(1000);
			splash.update(100, "finishing");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    splash.setVisible(false);

	  }
}  //  @jve:decl-index=0:visual-constraint="10,10"
