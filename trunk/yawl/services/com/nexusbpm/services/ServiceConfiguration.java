/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.servlet.ServletConfig;

import org.springframework.web.context.support.XmlWebApplicationContext;

import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

public class ServiceConfiguration extends BootstrapConfiguration {

	private String YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_KEY = "nexus.service.properties.filename";

	public ServiceConfiguration(ServletConfig config) {
		super();
		applicationContext = new XmlWebApplicationContext();
		((XmlWebApplicationContext) applicationContext).setServletConfig(config);
		((XmlWebApplicationContext) applicationContext).setConfigLocations(new String[] {"WEB-INF/" + config.getServletName() + "-servlet.xml"});
		
		String propertiesFileName = ((XmlWebApplicationContext) applicationContext).getServletConfig().getServletName() + "-servlet.properties";
		try {
			propertiesFileName = config.getServletContext().getResource("/WEB-INF/" + propertiesFileName).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setProperty(YAWL_ENVIRONMENT_PROPERTY_FILE_NAME_KEY, propertiesFileName);
		((XmlWebApplicationContext) applicationContext).refresh();
	}
	
	public void refresh() {
		//do not refresh
	}
	
	public Properties getProperties() throws IOException{
		UpdateablePropertiesFactoryBean b = (UpdateablePropertiesFactoryBean) applicationContext.getBean("propertyConfigurer");
		Properties p = b.getProperties();
		if (p == null) p = new Properties();
		return p;
	}
	
	public void saveProperties() throws IOException{
		throw new IOException("Unable to save properties from a service configuration.");
	}
	
}
