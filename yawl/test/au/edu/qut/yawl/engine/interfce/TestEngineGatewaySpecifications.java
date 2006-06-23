/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Element;
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
        
        TestEngineGateway.loadSpecification(
        		getClass().getResource("OneTwoThreeSpec.xml"), _gateway, _session );
        TestEngineGateway.loadSpecification(
        		EngineTestSuite.class.getResource( "SimpleSpec.xml" ), _gateway, _session );
        TestEngineGateway.loadSpecification(
        		EngineTestSuite.class.getResource( "TestInputParameters1.xml" ), _gateway, _session );
        
        // the spec ID for this spec happens to be the same as the filename
        _specID = "OneTwoThreeSpec.xml";
    }
    
    public void testLoadSpecificationSuccess() throws IOException {
    	String result = TestEngineGateway.loadSpecification(
    			EngineTestSuite.class.getResource( "SplitsAndJoins.xml" ), _gateway, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    }
    
    public void testLoadSpecificationIDInUseFailure() throws IOException {
    	String result = TestEngineGateway.loadSpecification(
    			getClass().getResource( "OneTwoThreeSpec.xml" ), _gateway, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testLoadSpecificationInvalidSpecFailure() throws RemoteException {
    	String result = _gateway.loadSpecification(
    			"<specificationSet><specification></specificationSet>",
    			"TestSpec.xml", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testLoadSpecificationVerificationFailure() throws IOException {
    	String result = TestEngineGateway.loadSpecification(
    			getClass().getResource( "InvalidSplitSpec.xml" ), _gateway, _session );
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
    	
    	String[] text = {
    			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
    			"<specificationSet xmlns=\"http://www.citi.qut.edu.au/yawl\" " +
    			"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"Beta 7.1\" " +
    			"xsi:schemaLocation=\"http://www.citi.qut.edu.au/yawl " +
    			"d:/yawl/schema/YAWL_SchemaBeta7.1.xsd\">",
    			"<specification uri=\"OneTwoThreeSpec.xml\">",
    			"<name>OneTwoThree</name>",
    			"<documentation>A simple spec with 3 sequential tasks</documentation>",
    			"<metaData />",
    			"<schema xmlns=\"http://www.w3.org/2001/XMLSchema\" />",
    			"<decomposition id=\"RootDecomp\" xsi:type=\"NetFactsType\" isRootNet=\"true\">",
    			"<processControlElements>",
    			"<inputCondition id=\"input\">",
    			"<flowsInto>",
    			"<nextElementRef id=\"one\" />",
    			"</flowsInto>",
    			"</inputCondition>",
    			"<task id=\"one\">",
    			"<flowsInto>",
    			"<nextElementRef id=\"two\" />",
    			"</flowsInto>",
    			"<join code=\"xor\" />",
    			"<split code=\"and\" />",
    			"<decomposesTo id=\"DecompOne\" />",
    			"</task>",
    			"<task id=\"two\">",
    			"<flowsInto>",
    			"<nextElementRef id=\"three\" />",
    			"</flowsInto>",
    			"<join code=\"xor\" />",
    			"<split code=\"and\" />",
    			"<decomposesTo id=\"DecompTwo\" />",
    			"</task>",
    			"<task id=\"three\">",
    			"<flowsInto>",
    			"<nextElementRef id=\"finis\" />",
    			"</flowsInto>",
    			"<join code=\"xor\" />",
    			"<split code=\"and\" />",
    			"<decomposesTo id=\"DecompThree\" />",
    			"</task>",
    			"<outputCondition id=\"finis\">",
    			"<name>Finished case</name>",
    			"</outputCondition>",
    			"</processControlElements>",
    			"</decomposition>",
    			"<decomposition id=\"DecompOne\" xsi:type=\"WebServiceGatewayFactsType\" />",
    			"<decomposition id=\"DecompTwo\" xsi:type=\"WebServiceGatewayFactsType\" />",
    			"<decomposition id=\"DecompThree\" xsi:type=\"WebServiceGatewayFactsType\" />",
    			"</specification>",
    			"</specificationSet>"
    	};
    	
    	System.out.println( "hard-coded and results:" );
    	
    	StringTokenizer st = new StringTokenizer( result, "\n\r\f" );
    	
    	for( String str : text ) {
    		System.out.println( str );
    		if( st.hasMoreTokens() ) {
    			System.out.println( st.nextToken() );
    		}
    		System.out.println();
    	}
    	System.out.println( "remaining results:" );
    	while( st.hasMoreTokens() ) {
    		System.out.println( st.nextToken() );
    	}
    	
    	st = new StringTokenizer( result, "\n\r\f" );
    	
    	for( int index = 0; index < text.length; index++ ) {
    		assertTrue( "out of tokens at line " + (index + 1), st.hasMoreTokens() );
    		String line = st.nextToken();
    		assertTrue( "line " + (index + 1) + ":\nhard-coded:\n" + text[ index ] + "\nreceived:\n" + line,
    				line.indexOf( text[ index ] ) >= 0 );
    	}
    }
    
    public void testGetTaskInformationSuccess() throws JDOMException, IOException {
    	String result = _gateway.getTaskInformation( _specID, "three", _session );
    	assertNotNull( result );
    	assertFalse( result, result.startsWith( "<failure" ) );
    	
    	Element root = TestEngineGateway.xmlToRootElement( result );
    	assertNotNull( root );
    	assertTrue( root.toString(), root.getName().equals( "taskInfo" ) );
    	
    	assertNotNull( root.getChildTextNormalize( "specificationID" ) );
    	assertTrue( root.getChildTextNormalize( "specificationID" ),
    			root.getChildTextNormalize( "specificationID" ).equals( "OneTwoThreeSpec.xml" ) );
    	
    	assertNotNull( root.getChildTextNormalize( "taskID" ) );
    	assertTrue( root.getChildTextNormalize( "taskID" ),
    			root.getChildTextNormalize( "taskID" ).equals( "three" ) );
    	
    	assertNotNull( root.getChildTextNormalize( "taskName" ) );
    	assertTrue( root.getChildTextNormalize( "taskName" ),
    			root.getChildTextNormalize( "taskName" ).equals( "DecompThree" ) );
    	
    	assertNotNull( root.getChildTextNormalize( "decompositionID" ) );
    	assertTrue( root.getChildTextNormalize( "decompositionID" ),
    			root.getChildTextNormalize( "decompositionID" ).equals( "DecompThree" ) );
    	
    	assertNotNull( root.getChildTextNormalize( "params" ) );
    	assertTrue( root.getChildTextNormalize( "params" ),
    			root.getChildTextNormalize( "params" ).length() == 0 );
    	
    	assertTrue( "" + root.getContentSize(), root.getContentSize() == 5 );
    }
    
    public void testGetTaskInformationInvalidSpecIDFailure() throws RemoteException {
    	String result = _gateway.getTaskInformation( "invalid_spec_id", "irrelevant", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetTaskInformationInvalidTaskIDFailure() throws RemoteException {
    	String result = _gateway.getTaskInformation( _specID, "invalid_task_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
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
