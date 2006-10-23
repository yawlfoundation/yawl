package au.edu.qut.yawl.events;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

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

public class JmsProviderTest extends TestCase {

	private static Object lock = new Object();
	private Context context;
	private Connection connection;
	private Session session;
	private JmsProvider provider;
	private int receiveCount;
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
		receiveCount = 0;
		provider = JmsProvider.getInstance();
		context = new InitialContext();
		connection = getConnection(context);
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		closeJms();
	}

	public void testProvider() {
		try {
			send();
			synchronized(lock) {lock.wait(2000);}
			assertEquals(1, receiveCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

	public void testJMSEventDispatcher() {
		JMSEventDispatcher ed = new JMSEventDispatcher();
		try {
			createMessageListener();
		ed.fireEvent(createEvent());
		synchronized(lock) {lock.wait(2000);}
		assertEquals(1, receiveCount);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private YCaseEvent createEvent() {
        YCaseEvent caseEvent = new YCaseEvent();
        caseEvent.setCompleted(1);
        return caseEvent;
	}
	
	private void send() throws Exception {
		ObjectMessage o = provider.getObjectMessage(createEvent());
		o.setObjectProperty("completed", 1);
		createMessageListener();
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
	
	private void createMessageListener() throws NamingException, JMSException {
		Destination dest = (Destination) context.lookup(getContextProperty(JmsProvider.OPENJMS_YAWL_EVENTQUEUE));
	    MessageConsumer receiver = session.createConsumer(dest, "completed=1" );
	    receiver.setMessageListener(new MessageListener() {
	        public void onMessage(Message message) {
	        	try {
					assertEquals(((ObjectMessage)message).getObject().getClass(), YCaseEvent.class);
					receiveCount++;
					synchronized(lock) {lock.notify();}
				} catch (JMSException e) {
					fail("error getting message");
				}
	        }
    });
	    connection.start();
	}

}
