package com.nexusbpm.editor.component;
import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <b>Use case:</b><br>
 * <p>The Jython component allows arbitrary Jython code to be executed within a
 * flow before flow execution continues. Dynamic attributes on the component may
 * be used as variables inside the Jython component.</p>
 * 
 * This component requires that Jython 2.1 be installed on the server machine.
 * 
 * @author              HoY
 * @author              Toby Ho
 * @author              Daniel Gredler
 * @created             February 19, 2003
 * @hibernate.subclass  discriminator-value="29"
 * @javabean.class      name="JythonComponent"
 *                      displayName="Jython Component"
 */
public class JythonComponent extends Component {

	private static final long serialVersionUID = -6987215494408278869L;
	private static final Log LOG = LogFactory.getLog( JythonComponent.class );
	static public final String HTTP_PROXY_HOST = "http.proxyHost";
	static public final String HTTP_PROXY_PORT = "http.proxyPort";

	/**
	 * Initializes the Jython runtime using <tt>PythonInterpreter.initialize()</tt>.
	 * This method should only be called once per JVM, so it is called from
	 * {@link com.ichg.capsela.jmx.ServerLifecycle#startService}.
	 * 
	 * @see <a href="http://www.jython.org/docs/registry.html">The Jython Registry</a>
	 */
	public static void initJython() {
		try {
			// Initialize the http.ProxyHost and http.ProxyPort system properties if they
			// have not been initialized yet.
			if( System.getProperty( HTTP_PROXY_HOST ) == null &&
				System.getProperty( HTTP_PROXY_PORT ) == null ) {
//				Properties deployProps = ServiceLocator.instance().deploymentProperties();
//				String httpProxyHost = deployProps.getProperty( HTTP_PROXY_HOST );
//				String httpProxyPort = deployProps.getProperty( HTTP_PROXY_PORT );
//				System.setProperty( HTTP_PROXY_HOST, httpProxyHost );
//				System.setProperty( HTTP_PROXY_PORT, httpProxyPort );
			}
			// If the Java HTTP proxy properties are set, but the Jython HTTP proxy property is not set, set it.
			//if( System.getProperty( "http_proxy" ) == null &&
			//	System.getProperty( ServiceLocator.HTTP_PROXY_HOST ) != null &&
			//	System.getProperty( ServiceLocator.HTTP_PROXY_PORT ) != null ) {
			//	String httpProxyHost = System.getProperty( ServiceLocator.HTTP_PROXY_HOST );
			//	String httpProxyPort = System.getProperty( ServiceLocator.HTTP_PROXY_PORT );
			//	System.setProperty( "http_proxy", "http://" + httpProxyHost + ":" + httpProxyPort );
			//}
			// Get the Jython home and jar paths.
//			Properties deployProps = ServiceLocator.instance().deploymentProperties();
//			String jythonHome = deployProps.getProperty( ServiceLocator.JYTHON_HOME );
//			File home = new File( jythonHome );
//			File lib = new File( home, "Lib" );
//			File jar = new File( home, "jython.jar" );
			// If the home directory does not exist, PythonInterpreter.initialize() automatically
			// creates it, so it is more meaningful to check for the lib subdirectory.
//			if( ! lib.exists() ) {
//				LOG.warn( "The jython library directory '" + lib.getAbsolutePath() + "' does not exist!" );
//			}
//			// If the jython.jar file does not exist, warn the user.
//			if( ! jar.exists() ) {
//				LOG.warn( "The jython jar file '" + jar.getAbsolutePath() + "' does not exist!" );
//			}
			// Initialize the python interpreter with the correct home directory and classpath.
//			String classPath = System.getProperty( "java.class.path" );
//			String jythonJar = jar.getAbsolutePath();
//			String capselaJar = ServiceLocator.getJarPath();
//			String capselaLibJars = ServiceLocator.getLibJarPaths();
//			classPath = classPath + File.pathSeparator + jythonJar + File.pathSeparator + capselaJar;
//			classPath = classPath + File.pathSeparator + capselaLibJars;
//			java.util.Properties props = new java.util.Properties();
//			props.setProperty( "python.verbose", "debug" );
//			props.setProperty( "python.home", jythonHome );
//			props.setProperty( "java.class.path", classPath );
//			LOG.debug( "Using python.home=" + jythonHome );
//			LOG.debug( "Using java.class.path=" + classPath );
//			PythonInterpreter.initialize( System.getProperties(), props, new String[ 0 ] );
		}
		catch( Exception e ) {
			// CapselaException is already logged.
		}
	}//initJython()

	/**
	 * Creates a new empty <code>JythonComponent</code>, needed for Hibernate.
	 */
	public JythonComponent() {
		super();
	} //JythonComponent()
	/**
	 * Creates a new <code>JythonComponent</code> with mandatory attributes.
	 * @param name the <code>JythonComponent</code>'s name.
	 * @param folder the <code>JythonComponent</code>'s parent folder.
	 */
	public JythonComponent(String name) {
		super(name);
	} //JythonComponent()
	/**
	 * @see com.ichg.capsela.framework.domain.DomainObject#construct()
	 */
	protected void construct() {
		super.construct();
//		setTypeID(new Long(ComponentType.JYTHON));
	} //construct()


	/**
	 * @see Component#clientValidate(Collection)
	 */
	public Collection<ValidationMessage> clientValidate( Collection<ValidationMessage> validationErrors ) {
		validationErrors = null;//super.clientValidate( validationErrors );
		// The Jython component should have some code to execute.
//		if( getCode() == null || getCode().length() == 0 ) {
//			String name = this.getName();
//			String msg = "The Jython component '" + name + "' is missing code to execute.";
//
//			if (null != validationErrors) {
//				validationErrors.add( new ValidationMessage( msg, this, ValidationMessage.WARNING ) );
//			}
//		}//if
		return validationErrors;
	}//clientValidate()


	/**
	 * @see Component#run()
	 */
	public void run() throws Exception {

		this.setOutput( "" );
		this.setErr( "" );

		if( _code == null ) return;

		StringWriter outputWriter = new StringWriter();
		StringWriter errWriter = new StringWriter();

//		try {
//			PythonInterpreter interp = new PythonInterpreter();

			// Process dynamic attributes (put attribute values into the Jython interpreter).
//			Map<String, ComponentAttribute> attributes = this.getComponentAttributes();
			HashSet<String> nameSet = new HashSet<String>();
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
//			interp.setOut( outputWriter );
//			interp.setErr( errWriter );
//			interp.exec( getCode() );
//
//			// Store the output and errors.
//			this.setOutput( outputWriter.toString() );
//			this.setErr( errWriter.toString() );
//
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

//		}
//		catch( PyException e ) {
//			setOutput( outputWriter.toString() );
//			setErr( getErr() + e.toString() );
//			LOG.debug( getOutput() );
//			LOG.debug( getErr() );
//			throw new Exception( e );
//		}

	}//run()

	/** The name of the <tt>code</tt> attribute of a Jython component. */
	private final static String ATTR_CODE = "code";
	/** The Jython code to be executed. */
	private String _code;
	/**
	 * @return the Jython code to be executed.
	 * @hibernate.property column="CODE"
	 *                     length="10000000"
	 * @javabean.property  displayName="Code"
	 *                     hidden="false"
	 */
	public String getCode() {
		return _code;
	}//getCode()
	/**
	 * Sets the Jython code that will be executed.
	 * @param code the Jython code to be executed.
	 */
	public void setCode(String code) {
//		updateAttribute(ATTR_CODE, code);
		this._code = code;
	}//setCode()

	/** The name of the <tt>output</tt> attribute of a Jython component. */
	private final static String ATTR_OUTPUT = "output";
	/** The output result of executing the Jython code. */
	private String _output;
	/**
	 * @return the output result of executing the Jython code.
	 * @hibernate.property column="OUTPUT_RESULT"
	 *                     length="10000000"
	 * @javabean.property  displayName="Output"
	 *                     hidden="false"
	 */
	public String getOutput() {
		return _output;
	}//getOutput()
	/**
	 * Sets the output result of executing the Jython code.
	 * @param output the output result of executing the Jython code.
	 */
	public void setOutput(String output) {
//		updateAttribute(ATTR_OUTPUT, output);
		this._output = output;
	}//setOutput()

	/** The name of the <tt>error</tt> attribute of a Jython component. */
	private final static String ATTR_ERR = "err";
	/** The error output that resulted from running the Jython code. */
	private String _err;
	/**
	 * @return the error output that resulted from executing the Jython code.
	 * @hibernate.property column="ERROR_RESULT"
	 *                     length="10000000"
	 * @javabean.property  displayName="Err"
	 *                     hidden="false"
	 */
	public String getErr() {
		return _err;
	}//getErr()
	/**
	 * Sets the error output that resulted from running the Jython code.
	 * @param err the error output that resulted from running the Jython code.
	 */
	public void setErr(String err) {
//		updateAttribute(ATTR_ERR, err);
		this._err = err;
	}//setErr()
}
