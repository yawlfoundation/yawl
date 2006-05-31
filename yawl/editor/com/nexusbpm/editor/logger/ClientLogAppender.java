package com.nexusbpm.editor.logger;

import javax.jms.JMSException;

import com.nexusbpm.editor.exception.EditorException;

/**
 * Custom log appender used only on the client. This log appender does
 * everything that its superclass does, except sending out log messages
 * that initiate from the client.
 * 
 * @version $Revision: 1.5 $
 * @author Dean Mao
 * @author Daniel Gredler
 */
public class ClientLogAppender { // extends LogAppender {  LogAppender is a capsela class that may not be worth porting

	/**
	 * Default constructor.
	 * @throws Throwable not thrown.
	 */
	public ClientLogAppender() throws Throwable {
		super();
	}//ClientLogAppender()

	/**
	 * @see LogAppender#sendMessage(LogRecordI)
	 */
	protected void sendMessage( LogRecordI logRecord ) throws JMSException {
		CapselaLog.log( logRecord );
	}//sendMessage()

	/**
	 * @see LogAppender#close()
	 */
	public void close() {
		CapselaLog.getServerLog().removeAllListeners();
		CapselaLog.getEngineLog().removeAllListeners();
		CapselaLog.getClientLog().removeAllListeners();
		CapselaLog.getAllLog().removeAllListeners();
//		super.close();
	}//close()

}//ClientLogAppender
