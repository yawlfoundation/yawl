/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.util;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.exceptions.YAWLException;


/**
 * A listener for JMS messages that will listen for <tt>ObjectMessage</tt>s,
 * pull the object out of them, and pass the object to the subclass's
 * implementation of {@link #processMessage(Object)}. The subclass should
 * override the {@link #messageClass()} function so that it returns the type
 * of object expected to be in the messages received.
 *
 * @author Felix L J Mayer
 * @version $Revision: 1.5 $
 * @created May 10, 2004
 * @see javax.jms.MessageListener
 */
public abstract class ObjectMessageListener implements MessageListener {

	static private final Log LOG = LogFactory.getLog( ObjectMessageListener.class );

	/**
	 * Gets an object from an <tt>ObjectMessage</tt>. Throws an exception if the
	 * given message is not an <tt>ObjectMessage</tt> or if the object is not of
	 * the specified type.
	 *
	 * @param message      the message that should contain an object.
	 * @param messageClass the type of object expected to be in the message.
	 * @return the object that was in the message.
	 * @throws YAWLException if the given message is not of type
	 *                          <tt>ObjectMesasge</tt>, if there is a JMS error,
	 *                          or if the message does not contain an object of
	 *                          the specified type.
	 */
	static public final Object receiveObject( Message message, Class messageClass )
			throws YAWLException {
		// Did we receive the exptected ObjectMessage?
		if( !(message instanceof ObjectMessage) ) {
			throw new YAWLException( "Received Message is not an ObjectMessage: " + message );
		}//if
		// Does the ObjectMessage contain the expected EngineTask?
		Object object = null;
		try {
			object = ((ObjectMessage) message).getObject();
		}//try
		catch( JMSException e ) {
			throw new YAWLException( e );
		}//catch
		if( !messageClass.isInstance( object ) ) {
			throw new YAWLException( "Received ObjectMessage does not contain a " );
					//+ Helper.baseClassName( messageClass.getName() ) + ": " + object ); XXX TODO Fix this after the port of editor
		}//if
		LOG.debug( "received object " + object );
		return object;
	}//receiveEngineCommand()

	/**
	 * Default constructor.
	 */
	public ObjectMessageListener() {
		super();
	}//ObjectMessageListener()

	/**
	 * Called by JMS when a message is received.
	 *
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public final void onMessage( Message message ) {
		Object object = null;
		try {
			object = receiveObject( message, messageClass() );
			processMessage( object );
		}//try
		catch( YAWLException e ) {
			// Nothing to do, exception is already logged.
		}//catch
		catch( Throwable t ) {
			LOG.fatal( "Unable to process Message: " + message, t );
		}//catch
	}//onMessage()

	/**
	 * Sublcasses should implement this method such that it returns the class
	 * of object expected to be contained in the <tt>ObjectMessage</tt>s
	 * received by this message listener.
	 *
	 * @return the class of objects expected to be contained in received
	 *         messages.
	 */
	protected abstract Class messageClass();
	/**
	 * This function handles processing the values received from messages. It
	 * will be given the value contained in <tt>ObjectMessage</tt>s so long as
	 * the value is an instance of the class returned by {@link #messageClass()}.
	 *
	 * @param message the object that was contained in the <tt>ObjectMessage</tt>
	 * @throws YAWLException if an error occurs while processing the message.
	 */
	protected abstract void processMessage( Object message ) throws Exception;

}//ObjectMessageListener
