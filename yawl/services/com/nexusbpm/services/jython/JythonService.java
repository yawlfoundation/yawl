/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.jython;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
public class JythonService implements NexusService {
	private static boolean initialized = false;
	
    private static String JYTHON_HOME = "C:/Progra~1/jython-21"; // default if jython.properties isn't found
	
    private void initialize() {
        if( !initialized ) {
        	initialized = true;
            // get jython properties
            InputStream in = NexusService.class.getResourceAsStream( "jython.properties" );
            Properties p = new Properties();
            if( in == null ) {
                System.out.println( "Could not find jython properties file! Using default properties." );
                p.setProperty( "python.home", JYTHON_HOME );
            }
            else {
                try {
                    p.load( in );
                    if (p.getProperty( "jython.home" ) != null) {
                    	JYTHON_HOME = p.getProperty( "jython.home" );
                    }
                    System.out.println( "Using jython home:" + JYTHON_HOME );
                }
                catch( IOException e ) {
                    System.out.println( "Error reading in jython.properties!" );
                    e.printStackTrace( System.out );
                }
            }
            
            // Get the Jython home and jar paths.
            File home = new File( JYTHON_HOME );
            File jar = new File( home, "jython.jar" );
            // Initialize the python interpreter with the correct home directory and classpath.
            String classPath = System.getProperty( "java.class.path" );
            String jythonJar = jar.getAbsolutePath();
            classPath = classPath + File.pathSeparator + jythonJar;
            //props.setProperty( "python.verbose", "debug" );
//            props.setProperty( "python.home", JYTHON_HOME );
            if( p.getProperty( "java.class.path" ) == null ) {
            	p.setProperty( "java.class.path", classPath );
            }
            PythonInterpreter.initialize( System.getProperties(), p, new String[ 0 ] );
        }
    }
	
	public NexusServiceData execute( NexusServiceData data ) {
		
		if( data == null ) {
			return null;
		}
		
		StringWriter outputWriter = new StringWriter();
		StringWriter errWriter = new StringWriter();
		
		try {
			initialize();
			
			// create the interpreter
			PythonInterpreter interp = new PythonInterpreter();
			
			// get a copy of the dynamic variables
			List<String> dynamicVariables = data.getVariableNames();
			
			// Process dynamic attributes (put attribute values into the Jython interpreter).
			// remove the non-dynamic variables
//			for( Iterator<String> iter = dynamicVariables.iterator(); iter.hasNext(); ) {
//				String name = iter.next();
//				if( name.equals( "code" ) ||
//						name.equals( "error" ) ||
//						name.equals( "output" ) ) {
//					iter.remove();
//				}
//				else {
//                    System.out.println( "Jython service received variable '" + name + "'" );
//					interp.set( name, data.get( name ) );
//				}
//			}
            interp.set( "data", data );
			
			System.out.println( "Jython's code:\n" + data.getPlain( "code" ) );
			
			// Execute the code.
			interp.setOut( outputWriter );
			interp.setErr( errWriter );
			interp.exec( data.getPlain( "code" ) );
			
			// Process dynamic attributes (get attribute values out of the Jython interpreter).
            try {
                data = (NexusServiceData) interp.get( "data", NexusServiceData.class );
            }
            catch( Throwable t ) {
                errWriter.write( "Error retrieving resulting data from the Jython interpreter!\n" );
                t.printStackTrace( new PrintWriter( errWriter ) );
            }
//			for( Iterator<String> iter = dynamicVariables.iterator(); iter.hasNext(); ) {
//				String varName = iter.next();
//				try {
//					Serializable varVal = (Serializable) interp.get( varName, Serializable.class );
//					if( varVal != null ) {
//						data.set( varName, varVal.toString() );
//					}
//					else {
//						data.set( varName, null );
//					}
//				}
//				catch( Throwable t ) {
//					errWriter.write( "Error retrieving value of dynamic variable '" + varName +
//							"' from the Jython interpreter!\n" );
//					t.printStackTrace( new PrintWriter( errWriter ) );
//				}
//			}
		}
		catch( Exception e ) {
            if( errWriter.toString().length() > 0 ) {
                errWriter.write( "\n" );
            }
			e.printStackTrace( new PrintWriter( errWriter ) );
            e.printStackTrace( System.err );
		}
		
		data.setPlain( "output", outputWriter.toString() );
		data.setPlain( "error", errWriter.toString() );
		
		return data;
	}
}
