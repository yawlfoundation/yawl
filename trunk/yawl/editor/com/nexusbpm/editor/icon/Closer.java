/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.icon;

import java.beans.XMLDecoder;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Closes lots of objects easily and relatively quietly
 *
 * @author Mitchell J. Friedman
 */
public class Closer {
	private final static Log LOG = LogFactory.getLog( Closer.class );

	static public void close( Connection connection ) {
		if( null != connection ) {
			try {
				connection.close();
			}
			catch( SQLException e ) {
				LOG.warn( "Closing Connection", e );
			}
		}
	}

	static public void close( ResultSet resultSet ) {
		if( null != resultSet ) {
			try {
				resultSet.close();
			}
			catch( SQLException e ) {
				LOG.warn( "Closing Result Set", e );
			}
		}
	}

	static public void close( Statement statement ) {
		if( null != statement ) {
			try {
				statement.close();
			}
			catch( SQLException e ) {
				LOG.warn( "Closing Statement", e );
			}
		}
	}

	static public void close( Socket socket ) {
		if( null != socket ) {
			try {
				socket.close();
			}
			catch( IOException e ) {
				LOG.warn( "Closing Socket", e );
			}
		}
	}

/*
	static public void close( XMLEncoder xmlEncoder ) {
		if( null != xmlEncoder ) {
			xmlEncoder.close();
		}
	}
*/
	static public void close( XMLDecoder xmlDecoder ) {
		if( null != xmlDecoder ) {
			xmlDecoder.close();
		}
	}

	/**
	 * Generic closer - new in java 1.5 - good for lots of objects
	 * but not DB Connection, ResultSet or Statement
	 * and not Socket
	 *
	 * @param closeable
	 */
	static public void close( Closeable closeable ) {
		if( null != closeable ) {
			try {
				closeable.close();
			}
			catch( IOException e ) {
				LOG.warn( "Closing " + (null == closeable ? "" : closeable.getClass().getName()), e );
			}
		}
	}
}
