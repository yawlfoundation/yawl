/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.jython;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jws.WebService;

import org.python.util.PythonInterpreter;

import com.nexusbpm.services.NexusService;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * The Jython service provides the ability to run arbitrary Jython code in YAWL. Any
 * dynamic variables in the NexusServiceData that are not <tt>code</tt>, <tt>output</tt>,
 * or <tt>error</tt> will be accessible to the Jython code and their values in the
 * Jython interpreter after the code is finished running will be put back into the
 * NexusServiceData to be accessible to other tasks in YAWL.
 * 
 * @author Nathan Rose
 */
@WebService(endpointInterface="com.nexusbpm.services.NexusService", serviceName="JythonService")
public class JythonServiceImpl implements NexusService {
	private boolean initialized = false;
	
	// TODO this is definitely temporary...
	private static final String JYTHON_HOME = "C:/Progra~1/jython-21";
	
	private void initialize() {
		if( !initialized ) {
			initialized = true;
			// Get the Jython home and jar paths.
			File home = new File( JYTHON_HOME );
			File lib = new File( home, "Lib" );
			File jar = new File( home, "jython.jar" );
			// Initialize the python interpreter with the correct home directory and classpath.
			String classPath = System.getProperty( "java.class.path" );
			String jythonJar = jar.getAbsolutePath();
			classPath = classPath + File.pathSeparator + jythonJar;
			Properties props = new Properties();
			props.setProperty( "python.verbose", "debug" );
			props.setProperty( "python.home", JYTHON_HOME );
			props.setProperty( "java.class.path", classPath );
			PythonInterpreter.initialize( System.getProperties(), props, new String[ 0 ] );
		}
	}
	
	public NexusServiceData execute( NexusServiceData data ) {
		
		if( data == null ) {
			return null;
		}
		
		StringWriter outputWriter = new StringWriter();
		StringWriter errWriter = new StringWriter();
		
		System.out.println( "Jython service received data:" );
		System.out.println( data );
		
		try {
			initialize();
			
			// create the interpreter
			PythonInterpreter interp = new PythonInterpreter();
			
			// get a copy of the dynamic variables
			List<String> dynamicVariables = data.getVariableNames();
			
			// Process dynamic attributes (put attribute values into the Jython interpreter).
			// remove the non-dynamic variables
			for( Iterator<String> iter = dynamicVariables.iterator(); iter.hasNext(); ) {
				String name = iter.next();
				if( name.equals( "code" ) ||
						name.equals( "error" ) ||
						name.equals( "output" ) ) {
					iter.remove();
				}
				else {
					interp.set( name, data.get( name ) );
				}
			}
			
			System.out.println( "Jython's code:\n" + data.get( "code" ) );
			
			// Execute the code.
			interp.setOut( outputWriter );
			interp.setErr( errWriter );
			interp.exec( data.get( "code" ) );
			
			// Process dynamic attributes (get attribute values out of the Jython interpreter).
			for( Iterator<String> iter = dynamicVariables.iterator(); iter.hasNext(); ) {
				String varName = iter.next();
				try {
					Serializable varVal = (Serializable) interp.get( varName, Serializable.class );
					if( varVal == null ) {
						data.set( varName, varVal.toString() );
					}
					else {
						data.set( varName, null );
					}
				}
				catch( Throwable t ) {
					errWriter.write( "Error retrieving value of dynamic variable '" + varName +
							"' from the Jython interpreter!\n" );
					t.printStackTrace( new PrintWriter( errWriter ) );
				}
			}
		}
		catch( Exception e ) {
			errWriter.write( "\n" );
			e.printStackTrace( new PrintWriter( errWriter ) );
		}
		
		data.set( "output", outputWriter.toString() );
		data.set( "error", errWriter.toString() );
		
		return data;
	}
}
