/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.configuration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.l2fprod.common.propertysheet.AbstractProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetDialog;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheetTable;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import com.l2fprod.common.swing.LookAndFeelTweaks;

public class ConfigurationDialog extends PropertySheetDialog{

	private Property[] properties;
	
	//These need to be set in a resource bundle most likely
	
	public static final String[][] propertyValues = {new String[] {
			"yawlEngineDao.engineUri",
			"yawlEngineDao.userName",
			"yawlEngineDao.password",
			"jmsClient.namingProvider",
			"org.quartz.scheduler.rmi.registryHost",
			"org.quartz.scheduler.rmi.registryPort"
	}, new String[] {
			"YAWL server",
			"YAWL server",
			"YAWL server",
			"Messaging server",
			"Scheduling server",
			"Scheduling server"
	}, new String[] {
			"location",
			"username",
			"password",
			"location",
			"host",
			"port"
	}, new String[] {
		"location of the YAWL server, such as http://localhost:8080/yawl",
		"name of the YAWL user to use in the connection, such as admin",
		"password for the YAWL user, such as YAWL",
		"location of the messaging server, such as tcp://localhost:3035",
		"host address for the scheduling server, such as localhost",
		"port for the scheduling server, such as 1098"
}
	};
	private PropertySheetPanel propertySheetPanel = null; 
	private PropertySheetTable propertySheetTable = null; 
	
	
	
	public ConfigurationDialog(JFrame f, Properties p) {
		super(f);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		LookAndFeelTweaks.tweak();
		List ps = new ArrayList();
		for (int propertyCount = 0; propertyCount < propertyValues[0].length; propertyCount++) {
			String value = p.getProperty(propertyValues[0][propertyCount]);
			if (value == null)
				value = "";
			Property pr = new ConfigurationProperty(propertyValues[1][propertyCount],
					propertyValues[2][propertyCount], propertyValues[0][propertyCount], value, propertyValues[3][propertyCount]);
			ps.add(pr);
		}
		properties = (AbstractProperty[]) ps.toArray(new AbstractProperty[] {});
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setModal(true);
		this.setTitle("Edit Nexus Editor Configuration");
		JLabel label = new JLabel("Editor Configuration");
		
		label.setFont(label.getFont().deriveFont(20.0F));
		this.getBanner().add(label);
        this.getContentPane().add("Center", getPropertySheetPanel());
		this.pack();
        this.setLocationRelativeTo(null);
	}

	public PropertySheetTable getPropertySheetTable() {
		if (propertySheetTable == null) {
			PropertySheetTableModel m = new PropertySheetTableModel();
			m.setProperties(properties);
			propertySheetTable = new PropertySheetTable(m);
		}
		return propertySheetTable;
	}
	
	public Property[] getProperties() {
		return properties;
	}

	/**
	 * This method initializes propertySheetPanel
	 * 	
	 * @return com.l2fprod.common.propertysheet.PropertySheetPanel	
	 */
	private PropertySheetPanel getPropertySheetPanel() {
		if (propertySheetPanel == null) {
			propertySheetPanel = new PropertySheetPanel();
			propertySheetPanel.setSortingCategories(true);
			propertySheetPanel.setToolBarVisible(true);
			propertySheetPanel.setDescriptionVisible(true);
			propertySheetPanel.setTable(getPropertySheetTable());
			propertySheetPanel.setSortingProperties(true);
			propertySheetPanel.setMode(PropertySheet.VIEW_AS_CATEGORIES);
		}
		return propertySheetPanel;
	}

	public static void main(String[] args) {
		NexusClientConfiguration ncc = new NexusClientConfiguration();
		BootstrapConfiguration.setInstance(ncc);
		Properties p = null;
		try {
			p = ncc.getProperties();
		} catch (IOException e2) {e2.printStackTrace();}
		ConfigurationDialog dialog = new ConfigurationDialog(null, p);
		boolean shouldSave = dialog.ask();
		if (shouldSave) {
			Property[] pa = dialog.getProperties();
			for (Property prop: pa) {
				p.setProperty(prop.getName(), prop.getValue().toString());
				try {
					ncc.saveProperties();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
