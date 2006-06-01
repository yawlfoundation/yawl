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
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import au.edu.qut.yawl.engine.EngineTestSuite;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.SpecificationData;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

/**
 * @author Nathan Rose
 */
public class TestEngineGateway extends TestCase {
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
        
        URL fileURL = getClass().getResource("OneTwoThreeSpec.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        String spec = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(yawlXMLFile)));
        String str;
        while( (str = br.readLine()) != null ) {
        	spec += str + "\n";
        }
        _gateway.loadSpecification( spec, randomFileName(), _session );
        
        // the spec ID for this spec happens to be the same as the filename
        _specID = "OneTwoThreeSpec.xml";
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

    public void testExecuteSpecification() throws YDataStateException, YStateException,
    		YQueryException, YSchemaBuildingException, YPersistenceException,
    		InterruptedException, URISyntaxException, JDOMException, IOException {
		launchCase();
		performTask( "one" );
		performTask( "two" );
		performTask( "three" );
		
		String str = _gateway.getAvailableWorkItemIDs( _session );
		
		Element root = xmlToRootElement( str );
		
		assertTrue( str, root.getContentSize() == 0 );
    }

//    public void testCaseCancel() throws InterruptedException, YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
//        Thread.sleep(150);
//        performTask("register");
//
//        Thread.sleep(150);
//        Set enabledItems = _repository.getEnabledWorkItems();
//
//        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
//            YWorkItem workItem = (YWorkItem) iterator.next();
//            if (workItem.getTaskID().equals("register_itinerary_segment")) {
//                _engine.startWorkItem(workItem, "admin");
//                break;
//            }
//        }
//        _engine.cancelCase(_idForTopNet);
//        assertTrue(_taskCancellationReceived.size() > 0);
//    }
    
//    public void testCaseCancelGateway() throws InterruptedException, YPersistenceException,
//    		YStateException, YDataStateException, YQueryException, YSchemaBuildingException,
//    		RemoteException, YAuthenticationException {
//    	Thread.sleep(100);
//        performTask("register");
//
//        // this test is assuming that creating a gateway like this will point to the same engine
//        // as the engine that's used for the rest of the tests in this class
//        EngineGateway eg = new EngineGatewayImpl( false );
//        String handle = eg.connect( "admin", "YAWL" );
//        assertNotNull( handle );
//        
//        Thread.sleep(100);
//        Set enabledItems = _repository.getEnabledWorkItems();
//
//        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
//            YWorkItem workItem = (YWorkItem) iterator.next();
//            if (workItem.getTaskID().equals("register_itinerary_segment")) {
//                _engine.startWorkItem(workItem, "admin");
//                break;
//            }
//        }
//        String result = eg.cancelCase( _idForTopNet.getId(), handle );
//        assertTrue( result, result.startsWith( "<success" ) );
//    }
    
//    public void testCaseCancelNull() throws YPersistenceException {
//    	try {
//    		_engine.cancelCase( null );
//    		fail( "An exception should have been thrown." );
//    	}
//    	catch( IllegalArgumentException e ) {
//    		// proper exception was thrown
//    	}
//    }

    private void launchCase() throws RemoteException, URISyntaxException {
    	String caseData = "<RootDecomp></RootDecomp>";
    	String ret = _gateway.launchCase( _specID, caseData,
    			new URI("mock://mockedURL/testingNonExistent"), _session );
    	assertNotNull( ret );
    	assertFalse( ret, ret.startsWith( "<failure" ) );
//    	_caseID = ret;
    }

    private void performTask(String name) throws YDataStateException, YStateException, YQueryException,
    		YSchemaBuildingException, YPersistenceException, JDOMException, IOException, InterruptedException {
        Set enabledItems = null;
        Set firedItems = null;
        Set activeItems = null;
        
        // gets all enabled and fired work items
        String str = _gateway.getAvailableWorkItemIDs( _session );
        System.out.println( "Available work items before starting:" );
        System.out.println( str );
        
        // get the work item (assuming there's only one)
        Element root = xmlToRootElement( str );
        assertTrue( name + ":" + str, root.getContentSize() > 0 );
        assertNotNull( name + ":" + str, root.getChild( "workItemID" ) );
        String workItemID = root.getChildTextNormalize( "workItemID" );
        WorkItemRecord item = getWorkItemFromID( workItemID );
        assertNotNull( workItemID, item );
        
        // make sure it's the correct one
        assertTrue( "intended task:" + name + " " + "actual task:" + item.getTaskID(),
        		item.getTaskID().equals( name ) );
        
        // start the work item
        System.out.println( "Starting task: " + item.getTaskID() +
        		" in work item: " + item.getID().toString() );
        String result = _gateway.startWorkItem( item.getID().toString(), _session );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // make sure any work items that are put in the fired state get run
        str = _gateway.getAvailableWorkItemIDs( _session );
        System.out.println( "Available work items after starting:" );
        System.out.println( str );
        
        // go through all the work items
        root = xmlToRootElement( str );
        for( int index = 0; index < root.getContentSize(); index++ ) {
        	Element child = (Element) root.getContent( index );
        	if( child.getName().equals( "workItemID" ) ) {
        		WorkItemRecord childItem = getWorkItemFromID( child.getTextNormalize() );
        		assertNotNull( childItem );
        		// start the ones that are fired
        		if( childItem.getStatus().equals( YWorkItem.statusFired ) ) {
        			System.out.println( "Starting task: " + childItem.getTaskID() +
        	        		" in work item: " + childItem.getID() );
        			result = _gateway.startWorkItem( child.getTextNormalize(), _session );
        			assertFalse( result, result.startsWith( "<failure" ) );
        		}
        	}
        }
        
        // to get the executing work items we'll have to filter through all of them
        // (no convenience method is located in the gateway, only the repository itself)
        str = _gateway.describeAllWorkItems( _session );
        System.out.println( "All work items after starting fired items:" );
        System.out.println( str );
        
        // go through all the work items
        root = xmlToRootElement( "<allItems>" + str + "</allItems>" );
        List<WorkItemRecord> itemsToFinish = new LinkedList<WorkItemRecord>();
        for( int index = 0; index < root.getContentSize(); index++ ) {
        	Element child = (Element) root.getContent( index );
        	if( child.getName().equals( "workItem" ) ) {
        		int attempts = 0;
        		workItemID =
        			child.getChildTextNormalize( "caseID" ) + ":" +
        			child.getChildTextNormalize( "taskID" );
        		WorkItemRecord childItem = getWorkItemFromID( workItemID );
        		assertNotNull( workItemID, childItem );
        		if( childItem.getStatus().equals( YWorkItem.statusExecuting ) ) {
        			// add items that are executing to the list instead of completing them now.
        			// Do this because if there's a parent work item and a child work item and
        			// you finish the child work item before the parent work item is evaluated,
        			// then the parent work item disappears, but it's still in the XML string
        			// that was returned, so when you call getWorkItemFromID() to get the parent
        			// work item, it returns null and causes things to fail
        			itemsToFinish.add( childItem );
        		}
//        		// complete the ones that are executing
//        		if( childItem.getStatus().equals( YWorkItem.statusExecuting ) ) {
//        			System.out.println( "Completing task: " + childItem.getTaskID() +
//        					" in work item: " + childItem.getWorkItemID().toString() );
//        			_gateway.completeWorkItem( workItemID, childItem.getDataString(), _session );
//        		}
        	}
        }
        // now go through and actually finish those items
        for( int index = 0; index < itemsToFinish.size(); index++ ) {
        	WorkItemRecord childItem = itemsToFinish.get( index );
			System.out.println( "Completing task: " + childItem.getTaskID() +
					" in work item: " + childItem.getID() );
			result = _gateway.completeWorkItem( childItem.getID(),
					childItem.getDataListString(), _session );
			assertFalse( result, result.startsWith( "<failure" ) );
        }
    }
    
    public void testGetNonExistentWorkItemDetails() throws RemoteException {
    	String result = _gateway.getWorkItemDetails( "invalid_work_item_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    private WorkItemRecord getWorkItemFromID( String workItemID ) throws JDOMException,
    		IOException, YPersistenceException {
    	return Marshaller.unmarshalWorkItem( _gateway.getWorkItemDetails( workItemID, _session ) );
//    	return EngineFactory.createEngine().getWorkItem( workItemID );
    }
    
    private Element xmlToRootElement( String xml ) throws JDOMException, IOException {
    	SAXBuilder builder = new SAXBuilder();
    	Document d = builder.build(new StringReader(xml));
    	assertNotNull( d );
    	return d.getRootElement();
    }
    
    public static void main(String args[]) {
    	try {
    		TestEngineGateway test = new TestEngineGateway();
    		test.setUp();
    		test.testExecuteSpecification();
    		
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
        suite.addTestSuite(TestEngineGateway.class);
        return suite;
    }
}
