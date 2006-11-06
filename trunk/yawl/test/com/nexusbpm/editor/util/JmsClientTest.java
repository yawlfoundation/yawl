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

public class JmsClientTest extends TestCase implements MessageListener {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAttachListener() {
		JmsClient c = new JmsClient();
		try {
			c.start();
			c.attachListener(this);
			while(true) {Thread.sleep(1000);}
//			c.end();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void onMessage(Message arg0) {
		ObjectMessage om = (ObjectMessage) arg0;
		try {
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
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
