/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

import java.io.File;
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

import junit.framework.TestCase;
import au.edu.qut.yawl.util.SpringTestConfiguration;
import au.edu.qut.yawl.util.SpringTestConfiguration.Configuration;

import com.nexusbpm.services.jms.JmsService;

public class JmsProviderTest extends TestCase {

	private static Object lock = new Object();
	private Context context;
	private Connection connection;
	private Session session;
	private JmsProvider provider;
	private int receiveCount;
	private JmsService service;
	private String getContextProperty(String name) throws NamingException {
		return context.getEnvironment().get(name).toString();
	}

	private Connection getConnection(Context context) throws Exception{
     ConnectionFactory factory = (ConnectionFactory) context.lookup("ConnectionFactory");
     Connection connection = factory.createConnection();
     return connection;
	}

	protected void setUp() throws Exception {
		super.setUp();

		SpringTestConfiguration.setupTestConfiguration(Configuration.JMS);
		
		service = new JmsService();
		String jmsPath = ClassLoader.getSystemResource("testresources/openjms.xml").getPath();
		String sqlPath = ClassLoader.getSystemResource("testresources/create_hsql.sql").getPath();
		System.out.println(jmsPath);
		System.out.println(sqlPath);
		service.setConfigPath(jmsPath);
		service.setSqlPath(sqlPath);
		try {
			service.startServer();
		} catch (Exception e) {e.printStackTrace();}		
		
		receiveCount = 0;
		Properties p = new Properties();
		p.load(ClassLoader.getSystemResourceAsStream("testresources/editor.properties"));
		context = new InitialContext(p);
		connection = getConnection(context);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    connection.start();
	    JmsProvider.resetInstance();
		provider = JmsProvider.getInstance();
		
		retrieveAll();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		closeJms();
	}

	public void testProvider() throws Exception {
		receiveCount = 10;
		createMessageListener();
		createMessageListener();
		createMessageListener();
		createMessageListener();
		createMessageListener();
		send("test the Provider");
		send("test the Provider2");
		try {
			synchronized(lock) {lock.wait(10000);}
		}
		catch(InterruptedException e) {
		}
		assertEquals(0, receiveCount);
	}

	public void testJMSEventDispatcher() throws NamingException, JMSException {
		JMSEventDispatcher ed = new JMSEventDispatcher();
		receiveCount = 1;
		createMessageListener();
		ed.fireEvent("testJMSEventDispatcher");
		try {
			synchronized(lock) {lock.wait(10000);}
		}
		catch(InterruptedException e) {
		}
		assertEquals(0, receiveCount);
	}
	
	private void send(Serializable s) throws Exception {
		ObjectMessage o = provider.createObjectMessage(s);
		provider.sendObjectMessage(o);
    }		

	private void closeJms() {
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
	
	private void retrieveAll() throws Exception {
		Destination dest = (Destination) context.lookup(getContextProperty(JmsProvider.OPENJMS_YAWL_EVENTQUEUE));
        session = connection.createSession(
                false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer receiver = session.createConsumer(dest);
        connection.start();
        Message message = null;
        do {
            message = receiver.receive(1000);
        } while (message != null);	
	}
	private int numl = 0;
	private void createMessageListener() throws NamingException, JMSException {
		Destination dest = (Destination) context.lookup(getContextProperty(JmsProvider.OPENJMS_YAWL_EVENTQUEUE));
	    MessageConsumer receiver = session.createConsumer(dest );
	    receiver.setMessageListener(new MessageListener() {
	    	int lnum = 0;
	    	int getnum() {
	    		if (lnum == 0) {lnum = numl++;}
	    		return lnum;
	    	}
	        public void onMessage(Message message) {
	        	try {
					message.acknowledge();
//					System.out.println(getnum() + ">>" + ((ObjectMessage)message).getObject().toString());
					receiveCount--;
//					System.out.println("count now = " + receiveCount);
					if (receiveCount == 0) {
						synchronized(lock) {lock.notify();}
					}
				} catch (JMSException e) {
					fail("error getting message");
				}
	        }
    });
	}

}
