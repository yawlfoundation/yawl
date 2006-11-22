/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import junit.framework.TestCase;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.services.LocalClientConfiguration;
import com.nexusbpm.services.jms.JmsService;

public class JmsClientTest extends TestCase implements MessageListener {

	private int receiveCount;
	private static Object lock = new Object();

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAttachListener() throws Exception {
		LocalClientConfiguration lc = new LocalClientConfiguration(
				"/testresources/jmsClientApplicationContext.xml",
				"/testresources/jms.client.properties"
		);
		BootstrapConfiguration.setInstance(lc);
		BootstrapConfiguration bc = BootstrapConfiguration.getInstance();
		
		JmsService service = new JmsService();
		String jmsPath = ClassLoader.getSystemResource("testresources/openjms.xml").getPath();
		String sqlPath = ClassLoader.getSystemResource("testresources/create_hsql.sql").getPath();
		System.out.println(jmsPath);
		System.out.println(sqlPath);
		service.setConfigPath(jmsPath);
		service.setSqlPath(sqlPath);
		service.startServer();		
		
		JmsClient c = (JmsClient) bc.getApplicationContext().getBean("jmsClient");
		try {
			c.start();
			c.attachListener(this);
			receiveCount = 1;
			c.getSender().send(c.getSession().createObjectMessage("This is my message"));			
			synchronized(lock) {lock.wait(10000);}
			assertEquals(0, receiveCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void onMessage(Message message) {
		ObjectMessage om = (ObjectMessage) message;
		receiveCount--;
		try {
			message.acknowledge();
			Enumeration e = om.getPropertyNames();
			StringBuilder sb = new StringBuilder("Message: {");
			while (e.hasMoreElements()) {
				String name = e.nextElement().toString();
				String value = om.getStringProperty(name);
				sb.append(name + ":" + value + " ");
			}
			sb.append("}");
			System.out.println(sb.toString());
			//this is going to be for the log window...
			//LOG.error(sb.toString());
			Object o = om.getObject();
			if (receiveCount == 0) {
				synchronized(lock) {lock.notify();}
			}
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
