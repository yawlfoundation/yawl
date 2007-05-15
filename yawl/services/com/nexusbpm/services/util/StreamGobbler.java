package com.nexusbpm.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class that processes streams on a separate thread, used in
 * conjunction with Runtime.exec(); we use a separate thread to
 * read the subprocess error and output streams, because "failure
 * to promptly write the input stream or read the output stream of
 * the subprocess may cause the subprocess to block, and even
 * deadlock," as per the JDK's javadoc.
 *
 * @author Dani Gredler
 * @version $Revision: 8336 $
 * @created May 20, 2004
 */
public class StreamGobbler extends Thread {
	private final static Log LOG = LogFactory.getLog( StreamGobbler.class );

	private boolean debug;
	private InputStream is;
	private StringBuffer contents;

	/**
	 * Creates a new StreamGobbler
	 *
	 * @param is    the input stream to read from
	 * @param debug whether debug messages should be added to the log
	 */
	public StreamGobbler( InputStream is, boolean debug ) {
		this.is = is;
		this.debug = debug;
		this.contents = new StringBuffer();
	}

	/**
	 * Returns however much of the input stream that has been read so far.
	 *
	 * @return the contents of the stream read so far.
	 */
	public String getStreamContents() {
		return this.contents.toString();
	}

	/**
	 * Reads from the input stream on a separate thread.
	 *
	 * @see java.lang.Thread
	 */
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader( is );
			BufferedReader br = new BufferedReader( isr );
			char[] buffer = new char[ 1024 ];
			int size;
			while( (size = br.read( buffer )) != -1 ) {
				this.contents.append( buffer, 0, size );
				if( this.debug ) {
					LOG.debug( String.valueOf( buffer, 0, size ) );
				}
			}
		}
		catch( IOException ioe ) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Waits for the specified string to appear at the end of this stream and then returns.
	 * If the specified number of milliseconds elapse before the specified string is encountered,
	 * then this method logs a warning and returns.
	 *
	 * @param s             The string to search for at the end of this stream.
	 * @param timeoutMillis The number of milliseconds to wait before timing out.
	 */
	public void waitFor( String s, long timeoutMillis ) {
		try {
			long timeoutTime = System.currentTimeMillis() + timeoutMillis;
			int initialLength = this.contents.length();
			while( true ) {
				Thread.sleep( 100 );
				if( this.contents.length() > initialLength ) {
					String newContent = this.contents.toString().substring( initialLength );
					if( newContent.endsWith( s ) ) {
						break;
					}
				}
				if( System.currentTimeMillis() >= timeoutTime ) {
					LOG.warn( "StreamGobbler.waitFor('" + s + "') timed out after " + timeoutMillis + " millis." );
					break;
				}
			}
		}
		catch( InterruptedException e ) {
			LOG.error( e.getMessage(), e );
		}
	}

}
