/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

import java.io.Serializable;
import java.util.Enumeration;
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

public class JmsProvider {

	public static final String OPENJMS_YAWL_EVENTQUEUE = "openjms.yawl.eventqueue";

	private static JmsProvider INSTANCE;

	private ResourceBundle bundle;

	public static synchronized JmsProvider getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JmsProvider();
		}
		return INSTANCE;
	}
    
    boolean started = false;

	Context context;

	Connection connection;

	Session session;

	private JmsProvider() {
			bundle = ResourceBundle.getBundle("jndi");
			Hashtable<String, String> properties = new Hashtable<String, String>();
			try {
				Enumeration<String> e = bundle.getKeys();
			while(e.hasMoreElements()) {
				String key = e.nextElement();
				String val = bundle.getString(key);
				properties.put(key, val);
			}

			context = new InitialContext(properties);			
			connection = getConnection(context);
			context.getEnvironment();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            started = true;
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

	public MessageProducer getMessageProducer() throws Exception {
		Destination dest = (Destination) context
				.lookup(bundle.getString(OPENJMS_YAWL_EVENTQUEUE));
		MessageProducer sender = session.createProducer(dest);
		connection.start();
		return sender;
	}

	public ObjectMessage createObjectMessage(Serializable o) throws Exception {
		return session.createObjectMessage(o);
	}

	public void sendObjectMessage(ObjectMessage o) throws Exception {
		getMessageProducer().send(o);
	}

}
