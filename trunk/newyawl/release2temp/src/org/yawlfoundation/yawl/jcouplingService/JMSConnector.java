package org.yawlfoundation.yawl.jcouplingService;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public abstract class JMSConnector {

	private Context context;
	private ConnectionFactory factory;
	private Connection connection;
	private String factoryName = "ConnectionFactory";
	private Session session;
	
	
	public void connect() throws Exception{
		context = new InitialContext();
		factory = (ConnectionFactory) context.lookup(factoryName);
		connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}


	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}


	public ConnectionFactory getFactory() {
		return factory;
	}


	public void setFactory(ConnectionFactory factory) {
		this.factory = factory;
	}


	public Connection getConnection() {
		return connection;
	}


	public void setConnection(Connection connection) {
		this.connection = connection;
	}


	public String getFactoryName() {
		return factoryName;
	}


	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}


	public Session getSession() {
		return session;
	}


	public void setSession(Session session) {
		this.session = session;
	}
	
	
}
