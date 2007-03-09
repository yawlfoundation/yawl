/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.util.configuration;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class StandaloneConfiguration extends BootstrapConfiguration {

	public StandaloneConfiguration() {
		super();
		applicationContext = new ClassPathXmlApplicationContext("Standalone.xml");
	
		((ClassPathXmlApplicationContext) applicationContext).refresh();
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
