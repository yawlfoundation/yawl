package com.nexusbpm.editor.configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nexusbpm.services.UpdateablePropertiesFactoryBean;

import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

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
			applicationContext = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT_FILE_NAME);
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
	}
	public synchronized static BootstrapConfiguration getInstance() {
			return new NexusClientConfiguration();
	}

}
