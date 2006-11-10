/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.ServerException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.jms.config.Configuration;
import org.exolab.jms.config.ConfigurationReader;
import org.exolab.jms.server.JmsServer;
import org.exolab.jms.service.ServiceException;

public class MockMessaging {
	private static final String OPENJMS_HOME = "openjms.home";
	public static final String OPENJMS_YAWL_EVENTQUEUE = "openjms.yawl.eventqueue";
	private static final String OPENJMS_ADMIN_PASSWORD = "openjms.admin.password";
	private static final String OPENJMS_ADMIN_USER = "openjms.admin.user";
	private static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";
	private JmsServer server;
	private static JmsProvider INSTANCE;
	private ResourceBundle bundle;
	Session session;
	Context context;
	Connection connection;
	private MockMessaging() {
		bundle = ResourceBundle.getBundle("jndi");

//		try {
//			initServer();
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
		try {

		Hashtable properties = new Hashtable();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
		properties.put(Context.PROVIDER_URL, "tcp://localhost:3030/");

		context = new InitialContext(properties);			
		
//		context = new InitialContext();
		connection = getConnection(context);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
	private Connection getConnection(Context context) throws Exception {
		ConnectionFactory factory = (ConnectionFactory) context
				.lookup("ConnectionFactory");
		Connection connection = factory.createConnection();
		return connection;
	}

	
	public final void initServer() throws NamingException, ServiceException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException, IOException,
			MarshalException, ValidationException {
		System.setProperty(OPENJMS_HOME, getContextProperty(OPENJMS_HOME));
		Class.forName("org.hsqldb.jdbcDriver").newInstance();
		java.sql.Connection conn = DriverManager.getConnection(
				"jdbc:hsqldb:mem:openjms", "sa", "");
		String setupDb = readStreamAsString(this.getClass()
				.getResourceAsStream("/create_hsql.sql"));
		PreparedStatement s = conn.prepareStatement(setupDb);
		s.execute();
		Configuration config = null;
		config = ConfigurationReader.read(this.getClass().getResourceAsStream(
				"/openjms.xml"));
		server = new JmsServer(config) {
		    public void init() throws NamingException, ServiceException {
		    	try {
		    		registerServices();
		    		getServices().start();
		    	} catch (ServiceException exception) {
		    		throw new ServiceException(
		    				"Failed to start services", exception);
		    		}
		    	}			
			};
		server.init();
	}
	private String getContextProperty(String name) throws NamingException {
		return bundle.getString(name);
	}
	private static String readStreamAsString(InputStream file)
	throws java.io.IOException {
StringBuffer fileData = new StringBuffer(1000);
BufferedReader reader = new BufferedReader(new InputStreamReader(file));
char[] buf = new char[1024];
int numRead = 0;
while ((numRead = reader.read(buf)) != -1) {
	String readData = String.valueOf(buf, 0, numRead);
	fileData.append(readData);
	buf = new char[1024];
}
reader.close();
return fileData.toString();
}

}
