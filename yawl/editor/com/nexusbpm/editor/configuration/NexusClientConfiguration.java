/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.configuration;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;
import au.edu.qut.yawl.util.configuration.UpdateablePropertiesFactoryBean;


public class NexusClientConfiguration extends BootstrapConfiguration{
	
	private String YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_VALUE = "editor.properties";
	private String APPLICATION_CONTEXT_FILE_NAME = "YawlClientApplicationContext.xml";
	private String YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_KEY = "yawl.environment.propertyfilename";
	
	public NexusClientConfiguration() {
		super();
		System.setProperty(YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_KEY, YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_VALUE);
		applicationContext = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_FILE_NAME);
	}
	
	public void refresh() {
		synchronized(applicationContext) {
			((ClassPathXmlApplicationContext) applicationContext).refresh();
		}
	}
	
	public Properties getProperties() throws IOException{
		UpdateablePropertiesFactoryBean b = (UpdateablePropertiesFactoryBean) applicationContext.getBean("propertyConfigurer");
		Properties p = b.getProperties();
		if (p == null) p = new Properties();
		return p;
	}
	
	public void saveProperties() throws IOException{
		UpdateablePropertiesFactoryBean b = (UpdateablePropertiesFactoryBean) applicationContext.getBean("propertyConfigurer");
		b.save();
		refresh();
	}
	public synchronized static BootstrapConfiguration getInstance() {
			return new NexusClientConfiguration();
	}

}
