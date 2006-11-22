/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JmsClient  {

	private Context context;

	private Connection connection;

	private Session session;

	private String namingProvider;
	private String userName;
	private String password;
	private String homeDirectory;
	private String queueName;
	private MessageProducer sender;
	
	public MessageProducer getSender() {
		return sender;
	}

	public void start() throws Exception {
		Properties p = new Properties();
		p.setProperty("java.naming.provider.url", namingProvider);
		p.setProperty("openjms.home",  homeDirectory);

		context = new InitialContext(p);
		ConnectionFactory factory = (ConnectionFactory) context
				.lookup("ConnectionFactory");
		connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		connection.start();
		Destination dest = (Destination) context.lookup(queueName);

		sender = session.createProducer(dest);
	}

	public void end() throws Exception {
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
			Destination dest = (Destination) context.lookup(queueName);
			MessageConsumer receiver = session.createConsumer(dest);
			receiver.setMessageListener(ml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNamingProvider() {
		return namingProvider;
	}

	public void setNamingProvider(String namingProvider) {
		this.namingProvider = namingProvider;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getHomeDirectory() {
		return homeDirectory;
	}

	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Session getSession() {
		return session;
	}
}