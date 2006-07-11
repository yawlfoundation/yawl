/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;

/**
 * Tests the engine gateway functionality having to do with users
 * (creating users, deleting users, etc) and sessions (connecting,
 * calling functions with invalid sessions, etc).
 * 
 * @author Nathan Rose
 */
public class TestEngineGatewayUserFunctionality extends TestCase {
    private EngineGateway _gateway;

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException,
    		YStateException, YPersistenceException, YDataStateException, URISyntaxException,
    		YAuthenticationException {
    	_gateway = new EngineGatewayImpl( false );
        EngineClearer.clear( EngineFactory.createYEngine() );
    }
    
    /**
     * Makes sure various functions return failure when given invalid session handles.
     */
    public void testInvalidSessionHandle() throws RemoteException {
    	String handle = "invalid_session_handle";
    	
    	String result = _gateway.getAvailableWorkItemIDs( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getWorkItemDetails( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getProcessDefinition( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.suspendWorkItem( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.completeWorkItem( "", "", false, handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.startWorkItem( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.createNewInstance( "", "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.describeAllWorkItems( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.checkConnection( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.checkConnectionForAdmin( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getTaskInformation( "", "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.checkElegibilityToAddInstances( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getSpecificationList( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.launchCase( "", "", null, handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getCasesForSpecification( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getCaseState( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.cancelCase( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getChildrenOfWorkItem( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getWorkItemOptions( "", "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.loadSpecification( "", "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.unloadSpecification( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.createUser( "", "", false, handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getUsers( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getYAWLServices( handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.getYAWLServiceDocumentation( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.addYAWLService( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.removeYAWLService( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.deleteUser( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    	
    	result = _gateway.changePassword( "", handle );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	assertFalse( _gateway.enginePersistenceFailure() );
    }
    
    public void testConnectFailure() throws RemoteException {
    	String result = _gateway.connect( "invalidUser", "password" );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testConnectSuccess() throws RemoteException {
    	String result = _gateway.connect( "admin", "YAWL" );
    	assertFalse( result, result.startsWith( "<failure" ) );
    	// try to parse it: if it's a valid handle it will be parsable, if not it will throw an exception
    	Long.parseLong( result );
    }
    
    public void testCreateUserSuccess() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser(
    			"testCreateUserSuccessTemporaryUser", "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	// now delete the user
    	_gateway.deleteUser( "testCreateUserSuccessTemporaryUser", handle );
    }
    
    public void testCreateUserNullUserIDFailure() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser( null, "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testCreateUserUserIDTakenFailure() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser( "admin", "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testCreateUserPasswordTooShortFailure() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser(
    			"testCreateUserPasswordFailureTemporaryUser", "pwd", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testDeleteUserSuccess() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser(
    			"testDeleteUserSuccessTemporaryUser", "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	// now delete the user
    	result = _gateway.deleteUser( "testDeleteUserSuccessTemporaryUser", handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    }
    
    /**
     * deleteUser() should fail if you try to delete yourself
     */
    public void testDeleteUserFailure() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// delete the user
    	String result = _gateway.deleteUser( "admin", handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testChangePasswordSuccess() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser(
    			"testChangePasswordSuccessTemporaryUser", "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );

    	// connect with the user
    	String userHandle = _gateway.connect( "testChangePasswordSuccessTemporaryUser", "password" );
    	assertNotNull( userHandle );
    	assertFalse( userHandle, userHandle.startsWith( "<failure" ) );
    	
    	// now change the password
    	result = _gateway.changePassword( "anotherone", userHandle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	// now logging on with the old password should fail
    	result = _gateway.connect( "testChangePasswordSuccessTemporaryUser", "password" );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	
    	// and logging on with the new password should succeed
    	result = _gateway.connect( "testChangePasswordSuccessTemporaryUser", "anotherone" );
    	assertNotNull( result );
    	assertFalse( result, result.startsWith( "<failure" ) );
    	Long.parseLong( result );
    	
    	// now delete the user
    	result = _gateway.deleteUser( "testChangePasswordSuccessTemporaryUser", handle );
    }
    
    // TODO changing your password won't fail in the current code since the password isn't checked
    public void testChangePasswordFailure() {
    }
    
    public void testCheckConnectionForAdminSuccess() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	assertFalse( handle, handle.startsWith( "<failure" ) );
    	// valid handles should be parsable longs...
    	Long.parseLong( handle );
    	String result = _gateway.checkConnectionForAdmin( handle );
    	assertNotNull( result );
    	assertTrue( result, result.equals( UserList._permissionGranted ) );
    }
    
    /**
     * checkConnectionForAdmin() should fail if the handle is invalid.
     */
    public void testCheckConnectionForAdminInvalidHandleFailure() throws RemoteException {
    	String result = _gateway.checkConnectionForAdmin( "invalid_handle" );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    /**
     * checkConnectionForAdmin() should fail if the user is not an admin.
     */
    public void testCheckConnectionForAdminUserNotAdminFailure() throws RemoteException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create the user
    	String result = _gateway.createUser( "temporaryUser", "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	// connect with the user
    	String userHandle = _gateway.connect( "temporaryUser", "password" );
    	assertNotNull( userHandle );
    	assertFalse( userHandle, userHandle.startsWith( "<failure" ) );
    	
    	// now do the check
    	result = _gateway.checkConnectionForAdmin( userHandle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    	
    	// finally delete the user
    	_gateway.deleteUser( "temporaryUser", handle );
    }
    
    public void testGetUsers() throws JDOMException, IOException {
    	String handle = _gateway.connect( "admin", "YAWL" );
    	assertNotNull( handle );
    	
    	// create a user
    	String result = _gateway.createUser(
    			"testGetUsersTemporaryUser", "password", false, handle );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	result = _gateway.getUsers( handle );
    	assertNotNull( result );
    	assertFalse( result, result.startsWith( "<failure" ) );
    	
    	result = "<userlist>" + result + "</userlist>";
    	
    	Element root = xmlToRootElement( result );
    	
    	assertNotNull( root );
    	assertTrue( "" + root.getContentSize(), root.getContentSize() == 2 );
    	
    	Element user = (Element) root.getContent( 0 );
    	Element admin = (Element) root.getContent( 1 );
    	
    	if( !admin.getChildTextNormalize( "id" ).equals( "admin" ) ) {
    		// swap them if the order isn't right
    		Element temp = user;
    		user = admin;
    		admin = temp;
    	}
    	
    	assertTrue( admin.getChildTextNormalize( "id" ),
    			admin.getChildTextNormalize( "id" ).equals( "admin" ) );
    	assertTrue( admin.getChildTextNormalize( "isAdmin" ),
    			admin.getChildTextNormalize( "isAdmin" ).equals( "true" ) );
    	assertTrue( "" + admin.getContentSize(), admin.getContentSize() == 2 );
    	assertTrue( user.getChildTextNormalize( "id" ),
    			user.getChildTextNormalize( "id" ).equals( "testGetUsersTemporaryUser" ) );
    	assertTrue( user.getChildTextNormalize( "isAdmin" ),
    			user.getChildTextNormalize( "isAdmin" ).equals( "false" ) );
    	assertTrue( "" + user.getContentSize(), user.getContentSize() == 2 );
    	
    	// now delete the user
    	_gateway.deleteUser( "testCreateUserSuccessTemporaryUser", handle );
    }
    
    private Element xmlToRootElement( String xml ) throws JDOMException, IOException {
    	SAXBuilder builder = new SAXBuilder();
    	Document d = builder.build(new StringReader(xml));
    	assertNotNull( d );
    	return d.getRootElement();
    }
    
    public static void main(String args[]) {
    	try {
    		TestEngineGatewayUserFunctionality test = new TestEngineGatewayUserFunctionality();
    		test.setUp();
    		test.testInvalidSessionHandle();
    		
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
        suite.addTestSuite(TestEngineGatewayUserFunctionality.class);
        return suite;
    }
}
