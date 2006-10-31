/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.JDOMException;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;

/**
 * Tests the engine gateway functionality having to do with YAWL services.
 * 
 * @author Nathan Rose
 */
public class TestEngineGatewayYAWLServices extends TestCase {
    private EngineGateway _gateway;
    private String _session;
    private static final String SERVICE_URI = "mock://mockedURL/testingEngineGateway";

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException,
    		YStateException, YPersistenceException, YDataStateException, URISyntaxException,
    		YAuthenticationException {
    	// all this is the only way I can figure to remove the admin's old connections...
    	UserList ul = UserList.getInstance();
    	// first create a new user, connect, and delete the admin
    	ul.addUser( "temporary", "password", true );
    	String session = ul.connect( "temporary", "password" );
    	ul.removeUser( "temporary", "admin" );
    	// now recreate the admin, connect the admin, and delete the temporary user
    	ul.addUser( "admin", "YAWL", true );
    	_session = ul.connect( "admin", "YAWL" );
    	ul.removeUser( "admin", "temporary" );
    	
    	_gateway = new EngineGatewayImpl( false );
        EngineClearer.clear( EngineFactory.createYEngine() );
        
        URI serviceURI = new URI( SERVICE_URI );
        YAWLServiceReference service = new YAWLServiceReference(serviceURI.toString(), null);
        
        _gateway.addYAWLService( service.toXML(), _session );
    }
    
    public void testRemoveYAWLServiceSuccess() {
    	// TODO even though it returns "success" the code doesn't remove the service (noted May 31, 2006)
    	String result = _gateway.removeYAWLService( SERVICE_URI, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    }
    
    public void testRemoveYAWLServiceFailure() {
    	String result = _gateway.removeYAWLService( "not_a_service_uri", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testAddYAWLServiceSuccess() throws RemoteException, URISyntaxException {
    	URI serviceURI = new URI( "mock://mockedURL/testingAddingYAWLService" );
        YAWLServiceReference service = new YAWLServiceReference( serviceURI.toString(), null );
        
        String result = _gateway.addYAWLService( service.toXML(), _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    }
    
    public void testAddYAWLServiceFailure() throws RemoteException {
    	String result = _gateway.addYAWLService( "", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetYAWLServiceDocumentationSuccess() throws URISyntaxException, RemoteException {
    	String uriString = "mock://mockedURL/testingYAWLServiceDocumentation";
    	String doc = "blah blah blah, this is the documentation";
    	URI serviceURI = new URI( uriString );
        YAWLServiceReference service = new YAWLServiceReference( serviceURI.toString(), null );
        service.setDocumentation( doc );
        
        String result = _gateway.addYAWLService( service.toXML(), _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	result = _gateway.getYAWLServiceDocumentation( uriString, _session );
    	assertNotNull( result );
    	assertTrue( result, result.equals( doc ) );
    }
    
    public void testGetYAWLServiceDocumentationNullDocFailure() throws RemoteException {
    	String result = _gateway.getYAWLServiceDocumentation( SERVICE_URI, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetYAWLServiceDocumentationInvalidServiceFailure() throws RemoteException {
    	String result = _gateway.getYAWLServiceDocumentation( "invalid_service_uri", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetYAWLServices() throws RemoteException {
    	String result = _gateway.getYAWLServices( _session );
    	assertNotNull( result );
    	assertFalse( result, result.startsWith( "<failure" ) );
    	
    	int length = result.indexOf( "</yawlService>" ) + 14;
    	
    	Map<String, YAWLServiceReference> refs = new HashMap<String, YAWLServiceReference>();
    	
    	String keys = "{"; // convenience string printed out on failure
    	int keycount = 0;
    	
    	while( length > 0 ) {
    		String temp = result.substring( 0, length );
    		System.out.println( "parsing yawl service: " + temp );
    		YAWLServiceReference ref = YAWLServiceReference.unmarshal( temp );
    		refs.put( ref.getYawlServiceID(), ref );
    		
    		if( keycount++ > 0 ) {
    			keys += ", " + ref.getYawlServiceID();
    		}
    		else {
    			keys += ref.getYawlServiceID();
    		}
    		
    		if( length >= result.length() ) {
    			length = -1;
    			result = null;
    		}
    		else {
    			result = result.substring( length );
    			System.out.println( "leftover services to parse: " + result );
    			length = result.indexOf( "</yawlService>" ) + 14;
    		}
    	}
    	
    	keys += "}";
    	
//    	System.out.println( keys );
    	
    	assertTrue( keys, refs.containsKey( "http://localhost:8080/yawlSMSInvoker/ib" ) );
    	assertTrue( keys, refs.containsKey( "mock://mockedURL/testingAddingYAWLService" ) );
    	assertTrue( keys, refs.containsKey( "http://localhost:8080/timeService/ib" ) );
    	assertTrue( keys, refs.containsKey( "http://localhost:8080/yawlWSInvoker/" ) );
    	assertTrue( keys, refs.containsKey( "http://localhost:8080/workletService/ib" ) );
    }
    
    public static void main(String args[]) {
    	try {
    		TestEngineGatewayYAWLServices test = new TestEngineGatewayYAWLServices();
    		test.setUp();
    		test.testGetYAWLServices();
    		
    		System.out.println("Test successful");
    	}
    	catch( Throwable t ) {
//    		System.err.println( t.toString() );
    		t.printStackTrace();
    		System.err.println("Test failed");
    	}
    	
//        TestRunner runner = new TestRunner();
//        runner.doRun(suite());
//        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestEngineGatewayYAWLServices.class);
        return suite;
    }
}
