package au.edu.qut.yawl.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.xml.DOMConfigurator;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;
import org.exolab.jms.config.Configuration;
import org.exolab.jms.config.ConfigurationManager;
import org.exolab.jms.config.ConfigurationReader;
import org.exolab.jms.config.LoggerConfiguration;
import org.exolab.jms.server.JmsServer;
import org.exolab.jms.server.ServerException;
import org.exolab.jms.service.ServiceException;

public class JmsProvider {

	private static final String OPENJMS_HOME = "openjms.home";

	public static final String OPENJMS_YAWL_EVENTQUEUE = "openjms.yawl.eventqueue";

	private static final String OPENJMS_ADMIN_PASSWORD = "openjms.admin.password";

	private static final String OPENJMS_ADMIN_USER = "openjms.admin.user";

	private static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";

	private JmsServer server;

	private static JmsProvider INSTANCE;

	private ResourceBundle bundle;

	public static synchronized JmsProvider getInstance() {
		if (INSTANCE == null)
			INSTANCE = new JmsProvider();
		return INSTANCE;
	}

	Context context;

	Connection connection;

	Session session;

	private JmsProvider() {
		try {
			bundle = ResourceBundle.getBundle("jndi");

			initServer();

			Hashtable properties = new Hashtable();
			properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.exolab.jms.jndi.InitialContextFactory");
			properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");

			context = new InitialContext(properties);			
			
//			context = new InitialContext();
			connection = getConnection(context);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
//		config.getLoggerConfiguration().setFile(
//				this.getClass().getResource("/log4j.properties").getPath());
		server = new JmsServer(config) {
		    public void init() throws NamingException, ServiceException {
		    	try {
		    		registerServices();
		    		getServices().start();
		    	} catch (ServiceException exception) {
		    		throw new ServerException(
		    				"Failed to start services", exception);
		    		}
		    	}			
			};
		server.init();
	}

	public void stopServer() throws Exception {
		String url = getContextProperty(JAVA_NAMING_PROVIDER_URL).toString();
		String user = getContextProperty(OPENJMS_ADMIN_USER);
		String password = getContextProperty(OPENJMS_ADMIN_PASSWORD);
		JmsAdminServerIfc admin = AdminConnectionFactory.create(url, user,
				password);
		Thread.sleep(5000);
		admin.stopServer();
		Thread.sleep(5000);
		java.sql.Connection conn = DriverManager.getConnection(
				"jdbc:hsqldb:mem:openjms", "sa", "");
		conn.createStatement().execute("SHUTDOWN");
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

	private String getContextProperty(String name) throws NamingException {
		return bundle.getString(name);
	}

	private Connection getConnection(Context context) throws Exception {
		ConnectionFactory factory = (ConnectionFactory) context
				.lookup("ConnectionFactory");
		Connection connection = factory.createConnection();
		return connection;
	}

	public MessageProducer getMessageProducer() throws Exception {
		Destination dest = (Destination) context
				.lookup(getContextProperty(OPENJMS_YAWL_EVENTQUEUE));
		MessageProducer sender = session.createProducer(dest);
		connection.start();
		return sender;
	}

	public ObjectMessage getObjectMessage(Serializable o) throws Exception {
		return session.createObjectMessage(o);
	}

	public void sendObjectMessage(ObjectMessage o) throws Exception {
		getMessageProducer().send(o);
	}

}
