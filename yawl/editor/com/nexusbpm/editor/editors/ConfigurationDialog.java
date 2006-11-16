/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nexusbpm.services.YawlClientConfiguration;
import com.nexusbpm.services.YawlClientConfigurationFactory;

public class ConfigurationDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField serverUri = null;
	private JTextField jmsUri = null;
	private JTextField quartzUri = null;
	private JLabel serverUriLabel = null;
	private JLabel jmsUriLabel = null;
	private JLabel quartzUriLabel = null;
	JPanel buttons = null;
	JPanel text = null;
	private JPanel grid = null;
	private YawlClientConfiguration config;
	private boolean shouldSave = false;
	
	public static boolean showConfigurationDialog(Frame owner, YawlClientConfiguration configuration) {
		ConfigurationDialog cd = new ConfigurationDialog(owner, configuration);
		cd.getServerUri().setText(configuration.getServerUri());
		cd.getJmsUri().setText(configuration.getJmsUri());
		cd.getQuartzUri().setText(configuration.getQuartzUri());
		cd.setVisible(true);
		cd.dispose();
		return cd.shouldSave;
	}

	public boolean isDirty() {
		boolean dirty = false;
		String newServerUri = getServerUri().getText();
		String newJmsUri = getJmsUri().getText();
		String newQuartzUri = getQuartzUri().getText();
		String oldServerUri = config.getServerUri();
		String oldJmsUri = config.getJmsUri();
		String oldQuartzUri = config.getQuartzUri();
			if (newServerUri != null && !newServerUri.equals(oldServerUri)) {
				dirty = true;
			}
			if (newJmsUri != null && !newJmsUri.equals(oldJmsUri)) {
				dirty = true;
			}
			if (newQuartzUri != null && !newQuartzUri.equals(oldQuartzUri)) {
				dirty = true;
			}
		return dirty;
	}
	
	
	/**
	 * @param owner
	 */
	private ConfigurationDialog(Frame owner, YawlClientConfiguration config) {
		super(owner);
		this.config = config;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		gridBagConstraints10.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints10.gridy = 0;
		gridBagConstraints10.ipadx = 5;
		gridBagConstraints10.ipady = 5;
		gridBagConstraints10.anchor = GridBagConstraints.CENTER;
		gridBagConstraints10.fill = GridBagConstraints.BOTH;
		gridBagConstraints10.weightx = 1.0;
		gridBagConstraints10.weighty = 1.0;
		gridBagConstraints10.gridx = 0;
		this.setLayout(new GridBagLayout());
		this.setTitle("Configuration");
		this.setModal(true);
		this.setBackground(SystemColor.control);
		this.add(getGrid(), gridBagConstraints10);
		this.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (isDirty()) {
				int reply = JOptionPane.showConfirmDialog(null, "Would you like to save your changes first", "Confirm close", JOptionPane.YES_NO_CANCEL_OPTION);
				switch (reply) {
					case JOptionPane.YES_OPTION:
						ConfigurationDialog.this.updateConfiguration();
						shouldSave = true;
						ConfigurationDialog.this.setVisible(false);
						break;
					case JOptionPane.NO_OPTION: 
						ConfigurationDialog.this.setVisible(false);
						break;
					default:break;
				}
				
				}
			}
		});
		this.pack();
	}

	public JPanel getTextPanel() {
		if (text == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 2;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.anchor = GridBagConstraints.CENTER;
			gridBagConstraints7.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.insets = new Insets(5, 5, 2, 2);
			gridBagConstraints6.gridy = 2;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.gridx = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new Insets(5, 5, 2, 2);
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.gridx = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(5, 5, 2, 2);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.gridx = 0;
			text = new JPanel();
			text.setLayout(new GridBagLayout());
			text.setBackground(SystemColor.control);
			serverUriLabel = new JLabel();
			serverUriLabel.setText("Server URI");
			serverUriLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			serverUriLabel.setFont(new Font("Dialog", Font.BOLD, 14));
			serverUriLabel.setName("serverUriLabel");
			jmsUriLabel = new JLabel();
			jmsUriLabel.setText("Messaging URI");
			jmsUriLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			jmsUriLabel.setFont(new Font("Dialog", Font.BOLD, 14));
			jmsUriLabel.setName("jmsUriLabel");
			quartzUriLabel = new JLabel();
			quartzUriLabel.setText("Scheduler URI");
			quartzUriLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			quartzUriLabel.setFont(new Font("Dialog", Font.BOLD, 14));
			quartzUriLabel.setName("quartzUriLabel");
			text.add(serverUriLabel, gridBagConstraints2);
			text.add(getServerUri(), gridBagConstraints3);
			text.add(jmsUriLabel, gridBagConstraints4);
			text.add(getJmsUri(), gridBagConstraints5);
			text.add(quartzUriLabel, gridBagConstraints6);
			text.add(getQuartzUri(), gridBagConstraints7);
		}
		return text;
	}
	
	public JPanel getButtonPanel() {
		if (buttons == null) {
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(5, 8, 5, 8);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.weightx = 2.0;
			gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.weighty = 0.0;
			gridBagConstraints2.ipady = 1;
			gridBagConstraints2.gridx = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.insets = new Insets(5, 8, 5, 8);
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.weightx = 2.0;
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.weighty = 0.0;
			gridBagConstraints1.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(5, 8, 5, 8);
			gridBagConstraints.gridy = 0;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.CENTER;
			gridBagConstraints.ipady = 0;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.ipadx = 0;
			gridBagConstraints.weightx = 6.0;
			gridBagConstraints.weighty = 0.0;
			gridBagConstraints.gridx = 0;
			buttons = new JPanel();
			buttons.setLayout(new GridBagLayout());
			buttons.setBackground(SystemColor.control);
			JButton ok = new JButton("OK");
			ok.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					ConfigurationDialog.this.updateConfiguration();
					shouldSave = true;
					ConfigurationDialog.this.setVisible(false);
				}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int shouldClose = JOptionPane.YES_OPTION;
					if (isDirty()) {
						shouldClose = JOptionPane.showConfirmDialog(ConfigurationDialog.this, "Close configuration without saving updates?", "Confirm close", JOptionPane.YES_NO_OPTION);
					}
					if (shouldClose == JOptionPane.YES_OPTION) {
						ConfigurationDialog.this.setVisible(false);
					}
				}
			});
			JPanel spacer = new JPanel();
			spacer.setBackground(SystemColor.control);
			buttons.add(spacer, gridBagConstraints);
			buttons.add(ok, gridBagConstraints1);
			buttons.add(cancel, gridBagConstraints2);
		}
		return buttons;
	}
	
	/**
	 * This method initializes serverUri	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getServerUri() {
		if (serverUri == null) {
			serverUri = new JTextField("", 30);
			serverUri.setName("serverUri");
			serverUri.setFont(new Font("Dialog", Font.PLAIN, 14));
			serverUri.setBorder(BorderFactory.createLoweredBevelBorder());
			serverUri.setInputVerifier(new InputVerifier() {
				@Override public boolean verify(JComponent input) {
					boolean verify = false;
					try {
						new URI(((JTextField)input).getText());
						verify = true;
					} catch (URISyntaxException e) {
						JOptionPane.showMessageDialog(null, "Please enter a valid URI.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					return verify;
				}
			});
		}
		return serverUri;
	}

	public void updateConfiguration() {
		config.setServerUri(getServerUri().getText());
		config.setJmsUri(getJmsUri().getText());
		config.setQuartzUri(getQuartzUri().getText());
	}
	
	/**
	 * This method initializes jmsUri	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJmsUri() {
		if (jmsUri == null) {
			jmsUri = new JTextField("", 30);
			jmsUri.setName("jmsUri");
			jmsUri.setFont(new Font("Dialog", Font.PLAIN, 14));
			jmsUri.setBorder(BorderFactory.createLoweredBevelBorder());
			jmsUri.setInputVerifier(new InputVerifier() {
				@Override public boolean verify(JComponent input) {
					boolean verify = false;
					try {
						new URI(((JTextField)input).getText());
						verify = true;
					} catch (URISyntaxException e) {
						JOptionPane.showMessageDialog(null, "Please enter a valid URI.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					return verify;
				}
			});
		}
		return jmsUri;
	}


	/**
	 * This method initializes serverUri	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getQuartzUri() {
		if (quartzUri == null) {
			quartzUri = new JTextField("", 30);
			quartzUri.setName("quartzUri");
			quartzUri.setFont(new Font("Dialog", Font.PLAIN, 14));
			quartzUri.setBorder(BorderFactory.createLoweredBevelBorder());
			quartzUri.setInputVerifier(new InputVerifier() {
				@Override public boolean verify(JComponent input) {
					boolean verify = false;
					try {
						new URI(((JTextField)input).getText());
						verify = true;
					} catch (URISyntaxException e) {
						JOptionPane.showMessageDialog(null, "Please enter a valid URI.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					return verify;
				}
			});
		}
		return quartzUri;
	}
	
	/**
	 * This method initializes grid	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGrid() {
		if (grid == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.weighty = 1.0;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.fill = GridBagConstraints.BOTH;
			gridBagConstraints9.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints9.ipadx = 0;
			gridBagConstraints9.ipady = 0;
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.ipady = 0;
			gridBagConstraints8.gridy = 1;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.gridx = 0;
			grid = new JPanel();
			grid.setLayout(new GridBagLayout());
			grid.setBackground(SystemColor.control);
			grid.add(getTextPanel(), gridBagConstraints9);
			grid.add(getButtonPanel(), gridBagConstraints8);
		}
		return grid;
	}

	private String fileName;
	
	public static void main(String[] args) {
		String[] paths = { "YawlClientApplicationContext.xml" };
		ApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
    	YawlClientConfigurationFactory configFactory = (YawlClientConfigurationFactory) ctx.getBean("yawlClientConfigurationFactory");  
        boolean shouldSave = ConfigurationDialog.showConfigurationDialog(null, configFactory.getConfiguration());
        if (shouldSave) {
        	try {
				configFactory.saveConfiguration();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Unable to save configuration due to " + e.getMessage() + ".", "Error", JOptionPane.ERROR_MESSAGE);
			}
        }
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
