/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.JDOMException;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.engine.EngineClearer;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.EngineTestSuite;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.SpecificationData;

/**
 * Tests engine gateway functionality having to do with specifications
 * (loading specs, getting a list of loaded specs, etc) but not with
 * cases (instances).
 * 
 * @author Nathan Rose
 */
public class TestEngineGatewaySpecifications extends TestCase {
//    private YIdentifier _idForTopNet;
    private EngineGateway _gateway;
    private String _session;
    private String _specID;
//    private String _caseID;
//    private List _taskCancellationReceived = new ArrayList();
//    private YWorkItemRepository _repository;
//    private List _caseCompletionReceived = new ArrayList();

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
        
        loadSpecification( getClass().getResource("OneTwoThreeSpec.xml") );
        loadSpecification( EngineTestSuite.class.getResource( "SimpleSpec.xml" ) );
        loadSpecification( EngineTestSuite.class.getResource( "TestInputParameters1.xml" ) );
        
        // the spec ID for this spec happens to be the same as the filename
        _specID = "OneTwoThreeSpec.xml";
    }
    
    private String loadSpecification( URL fileURL ) throws IOException {
        File yawlXMLFile = new File(fileURL.getFile());
        String spec = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(yawlXMLFile)));
        String str;
        while( (str = br.readLine()) != null ) {
        	spec += str + "\n";
        }
        return _gateway.loadSpecification( spec, randomFileName(), _session );
    }
    
    private String randomFileName() {
    	Random r = new Random();
    	String[] chars = {"a", "e", "i", "o", "u"};
    	String filename = "";
    	int len = 20;
    	len += r.nextInt(11);
    	for( int i = 0; i < len; i++ ) {
    		filename += chars[ r.nextInt( chars.length ) ];
    	}
    	filename += ".xml";
    	return filename;
    }
    
    public void testLoadSpecificationSuccess() throws IOException {
    	String result = loadSpecification( EngineTestSuite.class.getResource( "SplitsAndJoins.xml" ) );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    }
    
    public void testLoadSpecificationIDInUseFailure() throws IOException {
    	String result = loadSpecification( getClass().getResource( "OneTwoThreeSpec.xml" ) );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testLoadSpecificationInvalidSpecFailure() throws RemoteException {
    	String result = _gateway.loadSpecification(
    			"<specificationSet><specification></specificationSet>", randomFileName(), _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testLoadSpecificationVerificationFailure() throws IOException {
    	String result = loadSpecification( getClass().getResource( "InvalidSplitSpec.xml" ) );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testUnloadSpecificationSuccess() throws RemoteException {
    	String result = _gateway.unloadSpecification( _specID, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    }
    
    public void testUnloadSpecificationFailure() throws RemoteException {
    	String result = _gateway.unloadSpecification( "invalid_spec_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetSpecificationList() throws IOException {
    	String str = _gateway.getSpecificationList( _session );
    	
    	List<SpecificationData> specs = Marshaller.unmarshalSpecificationSummary(
    			"<rootTag>" + str + "</rootTag>" );
    	
    	assertTrue( "" + specs.size(), specs.size() == 3 );
    	
    	for( int i = 0; i < specs.size(); i++ ) {
    		SpecificationData data = specs.get( i );
    		assertTrue( data.getID(),
    				data.getID().equals( "OneTwoThreeSpec.xml" ) ||
    				data.getID().equals( "SimpleSpec.xml" ) ||
    				data.getID().equals( "TestInputParameters1.xml" ) );
    	}
    }
    
    public void testGetProcessDefinitionFailure() throws RemoteException {
    	String result = _gateway.getProcessDefinition( "invalid_spec_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetProcessDefinitionSuccess() throws RemoteException {
    	String result = _gateway.getProcessDefinition( _specID, _session );
    	assertNotNull( result );
    	assertFalse( result, result.startsWith( "<failure" ) );
    	
    	// TODO check what's returned and make sure it's correct (not just that it isn't a failure)
    }
    
    public static void main(String args[]) {
    	try {
    		TestEngineGatewaySpecifications test = new TestEngineGatewaySpecifications();
    		test.setUp();
    		test.testGetSpecificationList();
    		
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
        suite.addTestSuite(TestEngineGatewaySpecifications.class);
        return suite;
    }
}
