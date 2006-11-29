/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.configuration;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.nexusbpm.services.UpdateablePropertiesFactoryBean;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.YawlEngineDAO;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;
import junit.framework.TestCase;

public class NexusClientConfigurationTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testNexusClientConfiguration() {
		NexusClientConfiguration n = new NexusClientConfiguration();
		assertNotNull(n);
	}

	public void testRefresh() throws IOException{
		NexusClientConfiguration n = new NexusClientConfiguration();
		assertNotNull(n.getProperties());
	}

	public void testGetProperties() throws IOException{
		NexusClientConfiguration n = new NexusClientConfiguration();
		assertNotNull(n.getProperties());
	}

	public void testSaveProperties() throws IOException{
		NexusClientConfiguration n = new NexusClientConfiguration();
		UpdateablePropertiesFactoryBean b;
		b = (UpdateablePropertiesFactoryBean) n.getApplicationContext().getBean("propertyConfigurer");
		b.getProperties().setProperty("testname", "testvalue");
		b.getProperties().list(System.out);
		b.save();
		n.refresh();
		b = (UpdateablePropertiesFactoryBean) n.getApplicationContext().getBean("propertyConfigurer");
		assertTrue(b.getProperties().getProperty("testname").equals("testvalue"));
	}

	public void testGetEngine() {
		YawlEngineDAO dao = (YawlEngineDAO) new NexusClientConfiguration().getApplicationContext().getBean("yawlEngineDao");
		List<YSpecification> list = dao.retrieveByRestriction(YSpecification.class, new Unrestricted());
		assertTrue(list.size() > 0);
	}
}
