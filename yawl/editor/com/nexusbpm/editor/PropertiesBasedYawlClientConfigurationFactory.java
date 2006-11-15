/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nexusbpm.editor.editors.ConfigurationDialog;
import com.nexusbpm.scheduler.QuartzEventDao;
import com.nexusbpm.services.YawlClientConfiguration;
import com.nexusbpm.services.YawlClientConfigurationBean;
import com.nexusbpm.services.YawlClientConfigurationFactory;

public class PropertiesBasedYawlClientConfigurationFactory implements YawlClientConfigurationFactory {

	YawlClientConfiguration configuration;
	public static String KEY_YAWL_SERVER_URI = "nexuseditor.serverUri";
	public static String KEY_JMS_SERVER_URI = "java.naming.provider.url";
	public static String KEY_QUARTZ_SERVER_URI = "nexuseditor.quartzUri";
	private String fileName;
	
	private void loadConfiguration() {
		Properties p = new Properties();
		File f = new File(getFileName());
		try {
			p.load(new FileInputStream(f));
		} catch (IOException e) {
			System.out.println("loading " + f.getAbsolutePath());
			e.printStackTrace();
		}
		YawlClientConfiguration configuration = new YawlClientConfigurationBean();
		setConfiguration(configuration);
		configuration.setQuartzUri(p.getProperty(KEY_QUARTZ_SERVER_URI));
		configuration.setJmsUri(p.getProperty(KEY_JMS_SERVER_URI));
		configuration.setServerUri(p.getProperty(KEY_YAWL_SERVER_URI));
	}
	
	private void ensureInit() {
		if (configuration == null) loadConfiguration();
	}
		
	/* (non-Javadoc)
	 * @see com.nexusbpm.editor.YawlClientConfiguration#save()
	 */
	public void saveConfiguration() throws IOException {
		Properties p = new Properties();
		File f = new File(getFileName());
		p.load(new FileInputStream(f));
		p.setProperty(KEY_QUARTZ_SERVER_URI, configuration.getQuartzUri());
		p.setProperty(KEY_JMS_SERVER_URI, configuration.getJmsUri());
		p.setProperty(KEY_YAWL_SERVER_URI, configuration.getServerUri());
		OutputStream os = new FileOutputStream(f);
		p.store(os, "Nexus Editor Configuration");
	}

	public YawlClientConfiguration getConfiguration() {
		ensureInit();
		return configuration;
	}

	public void setConfiguration(YawlClientConfiguration configuration) {
		this.configuration = configuration;
	}



	public String getFileName() {
		return fileName;
	}



	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
