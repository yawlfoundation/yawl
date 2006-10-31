package com.nexusbpm.editor.util;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import au.edu.qut.yawl.events.JmsProvider;

import junit.framework.TestCase;

public class JmsClient extends TestCase {

	private Context context;

	private Connection connection;

	private Session session;

	protected void start() throws Exception {
		super.setUp();
		Properties p = new Properties();
		p.load(new FileInputStream("editor.properties"));
		context = new InitialContext(p);
		ConnectionFactory factory = (ConnectionFactory) context
				.lookup("ConnectionFactory");
		connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		connection.start();
	}

	protected void end() throws Exception {
		if (context != null) {
			try {
				context.close();
			} catch (NamingException exception) {
				exception.printStackTrace();
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException exception) {
				exception.printStackTrace();
			}
		}
	}

	public void attachListener(MessageListener ml) {
		try {
			Destination dest = (Destination) context.lookup(context
					.getEnvironment().get(JmsProvider.OPENJMS_YAWL_EVENTQUEUE)
					.toString());
			MessageConsumer receiver = session.createConsumer(dest);
			receiver.setMessageListener(ml);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}