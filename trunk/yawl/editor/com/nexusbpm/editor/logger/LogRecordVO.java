package com.nexusbpm.editor.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;

/**
 * 
 * May or may not be used anymore.
 * 
 * 
 * @author  Felix L J Mayer
 * @version $Revision: 1.9 $
 * @created May 13, 2004
 */
public class LogRecordVO implements LogRecordI {
    /**
     * Default constructor.
     */
	public LogRecordVO() {
		super();
	}//LogRecordVO()

	/**
	 * Constructor with all mandatory attributes.
     * @param level the visibility level of logging.
     * @param sourceType the source of the message (from one of the SOURCE
     *                   constants in the LogRecordI interface).
     * @param timestamp the time in milliseconds when the logging message was
     *                  produced.
     * @param sequenceNumber the sequence number of the logging record.
     * @param message the log message.
	 */
	public LogRecordVO( int level, int sourceType, long timestamp, 
		long sequenceNumber, String message) {
		_level = level;
		_sourceType = sourceType;
		_milliseconds = timestamp;
		_sequenceNumber = sequenceNumber;
		_message = message;
	}//LogRecordVO()
    public LogRecordVO( int level, int sourceType, long timestamp, long sequenceNumber, String message,
            Throwable t ) {
        this( level, sourceType, timestamp, sequenceNumber, message);
        StringWriter writer = new StringWriter();
        t.printStackTrace( new PrintWriter( writer ) );
        setThrowableMessage( writer.toString() );
    }
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "LogRecordVO: level=" + _level + " sourceType=" + SOURCES[_sourceType]
			+ " loggerName='" + _loggerName + "' milliseconds=" + _milliseconds 
			+ " sequenceNumber=" + _sequenceNumber 
			+ " message='" + _message + "' throwableMessage='" + _throwableMessage 
			+ "' sessionID=" + _sessionID + " originalSessionID=" + _originalSessionID 
			+ " engineName=" + _engineName + " hostname=" + _hostname
			+ " port=" + _port + " username=" + _username 
			+ " threadName=" + _threadName;
	}//toString()

	/**
	 * @return the primary key.
	 */
	public long getId() {
		return 0;
	}//getId()


	/**
	 * The visibility level of logging, determined by the underlying logging 
	 * engine. Log4j supports FATAL, ERROR, WARN, INFO, DEBUG.
	 * @see org.apache.log4j.Level
	 */
	private int _level;
	/**
	 * Gets the visibility level of logging.
	 * @return  the visibility level of logging
	 */
	public int getLevel() {
		return _level;
	}
	/**
	 * @param level
	 */
	public void setLevel(int level) {
		_level = level;
	}


	private String _loggerName;
	/**
	 * @return the name of the logger.
	 */
	public String getLoggerName() {
		return _loggerName;
	}//getLoggerName()
	/**
	 * Sets the name of the logger.
	 * @param loggerName the name of the logger.
	 */
	public void setLoggerName( String loggerName ) {
		_loggerName = loggerName;
	}//setLoggerName()


	/**
	 * The source immediately will indicate whether the logging message comes from
	 * the client, engine, or server.  This value can be CLIENT_TYPE, SERVER_TYPE,
	 * or ENGINE_TYPE from the constants defined at the beginning of this class.
	 */
	private int _sourceType;
	/**
	 * @return the source of the log record.
	 */
	public int getSourceType() {
		return _sourceType;
	}
	/**
	 * Sets the source of the log record.
	 * @param sourceType the source to set for the log record.
	 */
	public void setSourceType(int sourceType) {
		_sourceType = sourceType;
	}


	/**
	 * The time in milliseconds when the logging message was produced.
	 * JMSLogAppender will manage this attribute
	 */
	private long _milliseconds;
	/**
	 * @return the time when the log record was produced.
	 */
	public long getMilliseconds() {
		return _milliseconds;
	}
	/**
	 * Sets the time when the log record was produced.
	 * @param timestamp the time when the log record was produced.
	 */
	public void setMilliseconds(long timestamp) {
		_milliseconds = timestamp;
	}
	/**
	 * @see com.ichg.capsela.framework.logging.LogRecordI#getTimestamp()
	 */
	public Timestamp getTimestamp() {
		return new Timestamp( getMilliseconds() );
	}




	/**
	 * This is likely not needed, but we will still use it in the case the time is 
	 * not sufficient in determining which logging message came before another.  
	 * The JMSLogAppender will manage this attribute.
	 */
	private long _sequenceNumber;
	/**
	 * @return the sequence number of this log record.
	 */
	public long getSequenceNumber() {
		return _sequenceNumber;
	}
	/**
	 * Sets the sequence number of this log record.
	 * @param sequenceNumber the sequence number of this log record.
	 */
	public void setSequenceNumber(long sequenceNumber) {
		_sequenceNumber = sequenceNumber;
	}


	/**
	 * The logging message that the programmer might tag on to indicate something.
	 */
	private String _message;
	/**
	 * @return the logging message of this log record.
	 */
	public String getMessage() {
		return _message;
	}
	/**
	 * Sets the logging message of this log record.
	 * @param message the logging message of this log record.
	 */
	public void setMessage(String message) {
		_message = message;
	}


	/**
	 * The exception message in string form.  The reason we dont serialize the
	 * actual throwable is that some 3rd party tools have created non-serializable
	 * throwables and caused severe server side exceptions.
	 */
	private String _throwableMessage;
	/**
	 * @return the message that was in the exception that caused this log record.
	 */
	public String getThrowableMessage() {
		return _throwableMessage;
	}
	/**
	 * Sets the message that was in the exception that caused this log record.
	 * @param throwableMessage the message that was in the exception.
	 */
	public void setThrowableMessage(String throwableMessage) {
		_throwableMessage = throwableMessage;
	}


	/**
	 * The sessionId of the ClientSessionBean that the user was using to produce 
	 * this logging message.
	 */
	private long _sessionID;
	/**
	 * @return the session ID of the ClientSessionBean that the user was using
	 *         to produce this logging message.
	 */
	public long getSessionID() {
		return _sessionID;
	}
	/**
	 * Sets the session ID of the ClientSessionBean that the user was using.
	 * @param sessionID the session ID of the ClientSessionBean that the user
	 *                  was using.
	 */
	public void setSessionID(long sessionID) {
		_sessionID = sessionID;
	}


	/**
	 * This refers to the session id of the client that initially produced 
	 * messages that resulted in the following logging messages. Only really
	 * used by the engine to produce user specific logging messages.
	 */
	private long _originalSessionID;
	/**
	 * @return the session ID of the client that caused the log record.
	 */
	public long getOriginalSessionID() {
		return _originalSessionID;
	}
	/**
	 * Sets the session ID of the client that caused the log record.
	 * @param originalSessionID the session ID of the client that caused the log
	 *                          record.
	 */
	public void setOriginalSessionID(long originalSessionID) {
		_originalSessionID = originalSessionID;
	}


	/**
	 * The engine name of the engine that produced this logging message.  
	 * Not used by the server or client.
	 */
	private String _engineName;
	/**
	 * @return the name of the engine that produced this logging message.
	 * @hibernate.property column="ENGINE_NAME" not-null="false"
	 */
	public String getEngineName() {
		return _engineName;
	}
	/**
	 * Sets the name of the engine that produced this logging message.
	 * @param string the name of the engine that produced this logging message.
	 */
	public void setEngineName(String string) {
		_engineName = string;
	}


	/**
	 * Refers to the hostname of any log producer. Useful for determining which 
	 * computer the log message actually came from. Note that different engine 
	 * instances may have the same hostname if they are being run on the same 
	 * computer.
	 */
	private String _hostname;
	/**
	 * @return the hostname of the log record producer.
	 */
	public String getHostname() {
		return _hostname;
	}
	/**
	 * Sets the hostname of the log record producer.
	 * @param string the hostname of the log record producer.
	 */
	public void setHostname(String string) {
		_hostname = string;
	}


	/**
	 * The port is useful for determining which JBoss instance the log message 
	 * was produced from.  Since it is only possible to run one particular 
	 * instance per port, this will indicate which instance the message came from.
	 * This is only really used by the server and engine.
	 */
	private int _port;
	/**
	 * @return the port the log message came from.
	 */
	public int getPort() {
		return _port;
	}
	/**
	 * Sets the port the log message came from.
	 * @param i the port the log message came from.
	 */
	public void setPort(int i) {
		_port = i;
	}


	/**
	 * The principal's username.  Set by the client and the server.  Engine 
	 * probably won't use it since it will be setting the originatingSessionId.
	 * This is to indicate the name of the user that created the message either 
	 * from their own client, or caused the server to do some action.
	 */
	private String _username;
	/**
	 * @return the username of the user who caused the log record.
	 */
	public String getUsername() {
		return _username;
	}
	/**
	 * Sets the username who caused the log record.
	 * @param string the username who caused the record.
	 */
	public void setUsername(String string) {
		_username = string;
	}


	/**
	 * The thread name.  useful for debugging purposes.
	 */
	private String _threadName;
	/**
	 * @return the name of the thread the log record came from.
	 */
	public String getThreadName() {
		return _threadName;
	}
	/**
	 * Sets the name of the thread the log record came from.
	 * @param string the name of the thread the log record came from.
	 */
	public void setThreadName(String string) {
		_threadName = string;
	}

}//LogRecordVO
