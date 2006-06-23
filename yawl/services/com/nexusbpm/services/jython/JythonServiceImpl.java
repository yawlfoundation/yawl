package com.nexusbpm.services.jython;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.jdom.Document;
import org.jdom.Element;
import org.python.util.PythonInterpreter;

import com.nexusbpm.services.util.XmlUtil;

/**
 * Implementation of the XFire service interface.
 */
@WebService
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
	
	@WebMethod
	@WebResult(name="results")
	public String execute(
			@WebParam(name="code") String code,
			@WebParam(name="output") String output,
			@WebParam(name="error") String error,
			@WebParam(name="vars") String vars ) {
		StringWriter outputWriter = new StringWriter();
		StringWriter errWriter = new StringWriter();
		
		Map<String, String> varMap = new HashMap<String, String>();
		
		try {
			initialize();
			
			// TODO temporary newline hack...
			String ls = System.getProperty( "line.separator" );
			if( ls == null || ls.length() == 0 ) {
				ls = "\n";
			}
			code = code.replaceAll( "@nl;", ls );
			
			System.out.println( "Jython received input code:" + code );
			
			PythonInterpreter interp = new PythonInterpreter();
			
			// Process dynamic attributes (put attribute values into the Jython interpreter).
			varMap = getDynamicVariables( vars, errWriter );
			for( Iterator<String> iter = varMap.keySet().iterator(); iter.hasNext(); ) {
				String varName = iter.next();
				String varVal = varMap.get( varName );
				interp.set( varName, varVal );
			}

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
			interp.exec( code );

//			// Store the output and errors.
//			data.error = errWriter.toString();
//			data.output = outputWriter.toString();
			
			// Process dynamic attributes (get attribute values out of the Jython interpreter).
			for( Iterator<String> iter = varMap.keySet().iterator(); iter.hasNext(); ) {
				String varName = iter.next();
				try {
					Serializable varVal = (Serializable) interp.get( varName, Serializable.class );
					varMap.put( varName, varVal.toString() );
				}
				catch( Throwable t ) {
					errWriter.write( "Error retrieving value of dynamic variable '" + varName +
							"' from the Jython interpreter!\n" );
					t.printStackTrace( new PrintWriter( errWriter ) );
				}
			}
			
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
//			data.output = outputWriter.toString();
			errWriter.write( "\n" );
			e.printStackTrace( new PrintWriter( errWriter ) );
//			data.error = errWriter.toString();
		}
		
		try {
			System.out.println( "Jython service results:\n" +
					getResults( code, outputWriter.toString(), errWriter.toString(), varMap ) );
		}
		catch( Throwable t ) {
		}
		
		return getResults( code, outputWriter.toString(), errWriter.toString(), varMap );
	}
	
	private Map<String, String> getDynamicVariables( String variables, StringWriter errWriter ) {
		Map m = new HashMap<String, String>();
		
		if( variables != null && variables.length() > 0 ) {
			try {
				Document d = XmlUtil.xmlToDocument( XmlUtil.unmarshal( variables ) );
				Element root = d.getRootElement();
				
				for( int index = 0; index < root.getContentSize(); index++ ) {
					if( root.getContent( index ) instanceof Element ) {
						Element child = (Element) root.getContent( index );
						
						String varName = child.getName();
						String varVal = XmlUtil.unmarshal( child.getText() );
						
						System.out.println( "Jython found dynamic var '" + varName + "' with val '" + varVal + "'" );
						
						m.put( varName, varVal );
					}
				}
			}
			catch( Throwable t ) {
				errWriter.write( "Error parsing dynamic variables in the JythonService!\n" );
				errWriter.write( "Variable data:" + variables + "\n" );
				t.printStackTrace( new PrintWriter( errWriter ) );
			}
		}
		
		return m;
	}
	
	private String getResults( String code, String output, String error, Map<String, String> vars ) {
		String ret = "<YAWLParameters>";
		ret += XmlUtil.wrap( "code", code );
		ret += XmlUtil.wrap( "output", output );
		ret += XmlUtil.wrap( "error", error );
		
		for( Iterator<String> iter = vars.keySet().iterator(); iter.hasNext(); ) {
			String varName = iter.next();
			String varVal = vars.get( varName );
			ret += XmlUtil.wrap( varName, varVal );
		}
		
		ret += "</YAWLParameters>";
		
		return ret;
	}
}
