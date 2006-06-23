/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 26/11/2004
 * Time: 15:26:54
 */
public class YAWLException extends Exception {
    protected static SAXBuilder _builder = new SAXBuilder();
    protected String _message;
    public static final String MESSAGE_NM = "message";

    private static Logger LOGGER = Logger.getLogger(YAWLException.class);
    
    public YAWLException() {
    }

    public YAWLException(String message) {
        _message = message;
		logForCaller( this );
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public YAWLException(Throwable cause) {
        super(cause);
		logForCaller( this );
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public YAWLException(String message, Throwable cause) {
        super(cause);
        _message = message;
		logForCaller( this );
    }

    public String getMessage() {
        return _message;
    }

    public String toXML() {
        return "<" + this.getClass().getName() + ">" +
                toXMLGuts() +
                "</" + this.getClass().getName() + ">";
    }

    protected String toXMLGuts() {
        return "<message>" + getMessage() + "</message>";
    }

    public static YAWLException unmarshal(Document exceptionDoc) throws JDOMException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String exceptionType = exceptionDoc.getRootElement().getName();
        if ("YDataStateException".equals(exceptionType)) {
            return YDataStateException.unmarshall(exceptionDoc);
        }
        if ("YDataQueryException".equals(exceptionType)) {
            return YDataQueryException.unmarshall(exceptionDoc);
        }
        if ("YDataValidationException".equals(exceptionType)) {
            return YDataValidationException.unmarshall(exceptionDoc);
        }
        YAWLException e = (YAWLException) Class.forName(exceptionType).newInstance();
        e.setMessage(parseMessage(exceptionDoc));
        return e;
    }

    protected static String parseMessage(Document exceptionDoc) {
        return exceptionDoc.getRootElement().getChildText(MESSAGE_NM);
    }

    public void setMessage(String message) {
        _message = message;
    }


    /**
     * Making the boolean as a thread-local variable enables one to turn
     * logging on/off depending on the thread.  Good for debugging purposes!
     */
	static private ThreadLocal _logging = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return Boolean.TRUE;
		}
	};
	/**
     * Returns whether automatic logging of <tt>CapselaException</tt>s is
     * enabled or disabled.
	 * @return whether logging is enabled or disabled.
	 */
	static public boolean logging() {
		return ((Boolean) _logging.get()).booleanValue();
	}
	/**
     * Sets whether automatic logging for <tt>YAWLException</tt>s is
     * enabled or disabled.
     * 
	 * @param logging whether logging should be set to enabled or disabed.
	 * @return whether logging was previously enabled or disabled.
	 */
	static public boolean setLogging( boolean logging ) {
		boolean old = logging();
		if( logging ) {
			_logging.set( Boolean.TRUE );
		}
		else {
			_logging.set( Boolean.FALSE );
		}
		return old;
	}
	/**
	 * Log an exception based on the calling class automatically
	 * 
     * @param exception the exception that should be logged.
	 */
	static protected void logForCaller( Exception exception ) {
		if( ! logging() ) return;
		Logger logger = Logger.getLogger(callerClassName(exception.getClass().getName()));
		Throwable throwable = exception;
		throwable = throwable.getCause() != null ? throwable.getCause() : throwable;
		logger.error("Exception occurred: ", throwable);
	}//logForCaller()

	/**
	 * Determines the calling class name for a specified class name. This is done
	 * by first traversing the current stack trace upwards until the specified 
	 * class name is found. Then the stack trace is traversed further until another
	 * class name but the specified one if found. This is the calling class name.  
	 *  
	 * @param   className  the class whose caller will be determined
	 * @return  the qualified class name from the current call stack
	 */
	static public String callerClassName( String className ) {
		StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace();
		if( stackTraceElements.length == 0 ) return null;
		int index = 0;
		for( ; index < stackTraceElements.length 
			&& ! stackTraceElements[index].getClassName().equals( className ); index++ );
		for( ; index < stackTraceElements.length 
			&& stackTraceElements[index].getClassName().equals( className ); index++ );
		if( index >= stackTraceElements.length ) return null;
		return stackTraceElements[index].getClassName();
	}//callerClassName()
}
