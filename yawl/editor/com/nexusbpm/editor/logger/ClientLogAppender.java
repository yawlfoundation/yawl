package com.nexusbpm.editor.logger;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Custom log appender used only on the client. This log appender does
 * everything that its superclass does, except sending out log messages
 * that initiate from the client.
 * 
 * @version $Revision: 1.5 $
 * @author Dean Mao
 * @author Daniel Gredler
 */
public class ClientLogAppender extends AppenderSkeleton { // extends LogAppender {  LogAppender is a capsela class that may not be worth porting

	@Override
	protected void append( LoggingEvent logEvent ) {
        LogRecordI record = null;
        if( logEvent.getThrowableInformation() != null &&
                logEvent.getThrowableInformation().getThrowable() != null ) {
            record = new LogRecordVO(logEvent.level.toInt(), LogRecordI.SOURCE_CLIENT, logEvent.getStartTime(), 0l, eventMessageToString( logEvent.getMessage() ), logEvent.getThrowableInformation().getThrowable());
        }
        else {
            record = new LogRecordVO(logEvent.level.toInt(), LogRecordI.SOURCE_CLIENT, logEvent.getStartTime(), 0l, eventMessageToString( logEvent.getMessage() ) );
        }
		CapselaLog.log( record );
	}
	
	private static String eventMessageToString( Object message ) {
		if( message instanceof Throwable ) {
			StringWriter sw = new StringWriter();
    		((Throwable) message ).printStackTrace( new PrintWriter( sw ) );
    		return sw.toString();
		}
		else if( message != null ) {
			return message.toString();
		}
		else {
			return null;
		}
	}

	public boolean requiresLayout() {
		return false;
	}

	/**
	 * Default constructor.
	 * @throws Throwable not thrown.
	 */
	public ClientLogAppender() throws Throwable {
		super();
	}//ClientLogAppender()

	/**
	 * @see LogAppender#close()
	 */
	public void close() {
		CapselaLog.getServerLog().removeAllListeners();
		CapselaLog.getEngineLog().removeAllListeners();
		CapselaLog.getClientLog().removeAllListeners();
		CapselaLog.getAllLog().removeAllListeners();
	}//close()

}//ClientLogAppender
