package com.nexusbpm.services.jython;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.python.util.PythonInterpreter;

/**
 * Implementation of the XFire service interface.
 */
public class JythonServiceImpl implements JythonService {
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

	public String execute( String inputData ) {
		JythonData data = new JythonData();
		
		StringWriter outputWriter = new StringWriter();
		StringWriter errWriter = new StringWriter();
		
		try {
			initialize();
			
			System.out.println( "Jython received input data:" + inputData );
			
//			data = new JythonData( XmlUtil.unflattenXML( inputData ) );
			data = new JythonData( "<JythonComponent><code>" + inputData + "</code></JythonComponent>" );
			
			PythonInterpreter interp = new PythonInterpreter();

//			// Process dynamic attributes (put attribute values into the Jython interpreter).
//			Map<String, ComponentAttribute> attributes = this.getComponentAttributes();
//			HashSet<String> nameSet = new HashSet<String>();
//			for( Iterator<String> iter = attributes.keySet().iterator(); iter.hasNext(); ) {
//				nameSet.add( iter.next() );
//			}
//			for( Iterator<String> iter = attributes.keySet().iterator(); iter.hasNext(); ) {
//				String attributeName = iter.next();
//				ComponentAttribute attribute = attributes.get( attributeName );
//				if( attribute.isDynamic() && attribute instanceof ScalarAttribute ) {
//					interp.set( attributeName, ( (ScalarAttribute) attribute ).getValue() );
//				}
//			}

			// Execute the code.
			interp.setOut( outputWriter );
			interp.setErr( errWriter );
			interp.exec( data.code );

			// Store the output and errors.
			data.error = errWriter.toString();
			data.output = outputWriter.toString();

//			// Process dynamic attributes (get attribute values out of the Jython interpreter).
//			for( Iterator<String> iter = nameSet.iterator(); iter.hasNext(); ) {
//				String attributeName = iter.next();
//				ComponentAttribute attribute = this.getComponentAttribute( attributeName );
//				if( attribute.isDynamic() && attribute instanceof ScalarAttribute ) {
//					// Remove the old scalar component attribute.
//					// An update and commit must happen or else the attribute is not removed properly!!
//					removeComponentAttribute( attributeName );
//					PersistenceManager.instance().update( this );
//					PersistenceManager.instance().commit( true );
//					// Add the new component attribute. If the variable is already a component attribute,
//					// we just add it to the component; if the variable is just a regular object, it gets
//					// wrapped inside a new scalar component attribute.
//					Serializable variable = (Serializable) interp.get( attributeName, Serializable.class );
//					ComponentAttribute newAttribute;
//					if( variable instanceof ComponentAttribute ) {
//						newAttribute = this.addComponentAttribute( attribute.getName(), (ComponentAttribute) variable );
//					}
//					else {
//						newAttribute = this.addScalarAttribute( attribute.getName(), variable );
//					}
//					newAttribute.setDynamic( true );
//					newAttribute.setPublic( true );
//				}
//			}
		}
		catch( Exception e ) {
			data.output = outputWriter.toString();
			errWriter.write( "\n" );
			e.printStackTrace( new PrintWriter( errWriter ) );
			data.error = errWriter.toString();
		}
		
		try {
			System.out.println( data.getResult() );
		}
		catch( Throwable t ) {
		}
		
//		return data.getResult();
		return data.output;
	}
	
	class JythonData {
		String code;
		String error;
		String output;
		
		private JythonData( String xml ) throws JDOMException, IOException {
			Document doc = XmlUtil.xmlToDocument( xml );
			
			Element root = doc.getRootElement();
			
			this.code = root.getChildText( "code" );
			this.error = root.getChildText( "error" );
			this.output = root.getChildText( "output" );
		}
		
		private JythonData() {
			code = "";
			error = "";
			output = "";
		}
		
		private String getResult() {
			return "<JythonComponent><code>" + code + "</code>" +
				"<error>" + error + "</error>" +
				"<output>" + output + "</output></JythonComponent>";
		}
	}
}
