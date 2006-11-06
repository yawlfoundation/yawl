/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.logger;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 
 * @see LogRecord
 * @see LogRecordVO
 * 
 * @author  Felix L J Mayer
 * @version $Revision: 1.5 $
 * @created May 14, 2004
 */
public interface LogRecordI extends Serializable {

    /** Constant denoting a log msg with no source. */
	static public final int SOURCE_NONE = 0;
    /** Constant denoting a log msg coming from a client. */
	static public final int SOURCE_CLIENT = 1;
    /** Constant denoting a log msg coming from the server. */
	static public final int SOURCE_SERVER = 2;
    /** Constant denoting a log msg coming from an engine. */
	static public final int SOURCE_ENGINE = 3;
    /**
     * Constant with printable strings corresponding to the msg source
     * constants.
     */
	static public final String[] SOURCES = new String[]{
		"NONE", "CLIENT", "SERVER", "ENGINE" };

    /** The name of the <tt>session ID</tt> attribute of a LogRecord. */
	static public final String ATTR_SESSION_ID = "sessionID";
    /**
     * The name of the <tt>originating session ID</tt> attribute of a LogRecord.
     */
	static public final String ATTR_ORIGINATING_SESSION_ID = "originatingSessionID";

    /** Constant for a <tt>session ID</tt> that has not been set yet. */
	static public final int INVALID_SESSION = -1;

    /**
     * We need this method such that hibernate can map a primary key.
     * @return the primary key.
     */
	public long getId();
    /**
     * Gets the visibility level of logging.
     * @return the visibility level of logging
     */
	public int getLevel();
    /**
     * @return the logger name.
     */
	public String getLoggerName();
    /**
     * @return the source of the message (client, engine, or server).
     */
	public int getSourceType();
    /**
     * @return the time in milliseconds when the logging message was produced.
     */
	public long getMilliseconds();
    /**
     * @return the time in milliseconds when the logging message was produced.
     */
	public Timestamp getTimestamp();
    /**
     * Sets the sequence number of this logging message.
     * @param sequenceNumber the sequence number for the logging message.
     */
	public void setSequenceNumber( long sequenceNumber );
    /**
     * @return the logging message.
     */
	public String getMessage();
    /**
     * @return the exception's message in string form.
     */
	public String getThrowableMessage();
    /**
     * @return the session ID of the ClientSessionBean the user was using to
     *         produce this logging message.
     */
	public long getSessionID();
    /**
     * @return the session ID of the client that initially produced messages
     *         that resulted in this logging message.
     */
	public long getOriginalSessionID();
    /**
     * @return the name of the engine that produced this logging message.
     */
	public String getEngineName();
    /**
     * @return the hostname of the log producer.
     */
	public String getHostname();
    /**
     * @return the port showing which JBoss instance the log message was produced
     *         from.
     */
	public int getPort();
    /**
     * @return the name of the client who either created the message or caused the
     *         server to do some action.
     */
	public String getUsername();
    /**
     * @return the thread name.
     */
	public String getThreadName();

}//LogRecordI
