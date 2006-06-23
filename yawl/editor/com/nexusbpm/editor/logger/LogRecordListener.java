package com.nexusbpm.editor.logger;

import au.edu.qut.yawl.util.ObjectMessageListener;

/**
 * A listener for log record events.
 * 
 * @see LogAppender#initializeForClient(String, long, javax.jms.MessageListener)
 * 
 * @author  Felix L J Mayer
 * @version $Revision: 1.3 $
 * @created May 14, 2004
 */
public class LogRecordListener extends ObjectMessageListener {
    /**
     * Default constructor for making <tt>LogRecordListener</tt>s.
     */
	public LogRecordListener() {
		super();
	}//LogRecordListener()

	/**
     * Received messages should contain <tt>LogRecordI</tt>s.
	 * @see com.ichg.capsela.framework.util.ObjectMessageListener#messageClass()
	 */
	protected Class messageClass() {
		return LogRecordI.class;
	}//messageClass()

	/**
     * Logs all messages received as long as the log appender has not been
     * shut down; all received messages are discarded once the log appender
     * has been shut down.
	 * @see com.ichg.capsela.framework.util.ObjectMessageListener#processMessage(java.lang.Object)
	 */
	protected void processMessage( Object message ) throws Exception {
		// If the log appender has been shut down or is in the middle of shutting down,
		// do not try to log any more records, or the client will probably deadlock.
//		if( ! LogAppender.isShutdown() ) {  XXX TODO Uncomment this later when we finish the capsela port
			LogRecordI logRecord = (LogRecordI) message;
			CapselaLog.log( logRecord );
//		}//if
	}//processMessage()

}//LogRecordListener
