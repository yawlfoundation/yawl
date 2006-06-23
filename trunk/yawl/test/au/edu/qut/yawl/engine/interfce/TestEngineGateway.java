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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

/**
 * Tests engine gateway functionality having to do with work items
 * and cases (such as starting or cancelling a case, starting or
 * completing a work item, getting case state, etc).
 * 
 * @author Nathan Rose
 */
public class TestEngineGateway extends TestCase {
//    private YIdentifier _idForTopNet;
    private EngineGateway _gateway;
    private String _session;
    private String _specID;
    private String _caseID;
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
        
        loadSpecification( getClass().getResource( "OneTwoThreeSpec.xml" ), _gateway, _session );
        
        // the spec ID for this spec happens to be the same as the filename
        _specID = "OneTwoThreeSpec.xml";
    }
    
    public static String loadSpecification( URL fileURL, EngineGateway gateway, String session )
			throws IOException {
		File yawlXMLFile = new File(fileURL.getFile());
		String spec = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(yawlXMLFile)));
		String str;
		while( (str = br.readLine()) != null ) {
			spec += str + "\n";
		}
		return gateway.loadSpecification( spec, randomFileName(), session );
	}
    
    public static String randomFileName() {
    	Random r = new Random();
    	String[] chars = {"a", "c", "e", "h", "i", "l", "o", "s", "u", "w", "y"};
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
    
    public void testExecuteMiSpecification()
    		throws URISyntaxException, YPersistenceException, JDOMException, IOException {
    	loadSpecification( getClass().getResource( "TestSimpleMi.xml" ), _gateway, _session );
    	_specID = "TestSimpleMi.xml";
    	launchCase();
    	
    	WorkItemRecord item = getEnabledWorkItems().get( "record" );
    	assertNotNull( item );
    	
    	// some of the code for this test comes from performTask() ... it should probably be refactored
    	
        // check the options for the work item
        String result = _gateway.getWorkItemOptions( item.getID(), "", _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        Set<String> options = parseOptions( result );
        assertNotNull( options );
        assertTrue( "" + options.size(), options.contains( "start" ) );
        assertFalse( "" + options.size(), options.contains( "suspend" ) );
        assertFalse( "" + options.size(), options.contains( "complete" ) );
        
        // start the work item
        System.out.println( "Starting task: " + item.getTaskID() +
        		" in work item: " + item.getID().toString() );
        result = _gateway.startWorkItem( item.getID().toString(), _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // make sure there were children created
        result = _gateway.getChildrenOfWorkItem( item.getID(), _session );
        assertNotNull( result );
        assertTrue( result, result.length() > 0 );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // parent work item shouldn't be able to add instances
        result = _gateway.checkElegibilityToAddInstances( item.getID(), _session );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<failure" ) );
        
        // child work item in fired state shouldn't be able to add instances
        Set<WorkItemRecord> items = getWorkItemsWithStatus( YWorkItem.Status.Fired );
        assertNotNull( items );
        assertTrue( "" + items.size(), items.size() == 1 );
        
        item = items.iterator().next();
        assertNotNull( item );
        
        result = _gateway.checkElegibilityToAddInstances( item.getID(), _session );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<failure" ) );
        
        // child work item in executing state should be able to add instances
        items = getWorkItemsWithStatus( YWorkItem.Status.Executing );
        assertNotNull( items );
        assertTrue( "" + items.size(), items.size() == 1 );
        
        item = items.iterator().next();
        assertNotNull( item );
        
        result = _gateway.checkElegibilityToAddInstances( item.getID(), _session );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<success" ) );
        
        // check the options for the work item
        result = _gateway.getWorkItemOptions( item.getID(), "", _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        options = parseOptions( result );
        assertNotNull( options );
        assertFalse( "" + options.size(), options.contains( "start" ) );
        assertTrue( "" + options.size(), options.contains( "suspend" ) );
        assertTrue( "" + options.size(), options.contains( "complete" ) );
        assertTrue( "" + options.size(), options.contains( "addNewInstance" ) );
        
        // create 3 new instances
        result = _gateway.createNewInstance( item.getID(), "<blah/>", _session );
        System.out.println( result );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<success><workItem><taskID>" ) );
        
        result = _gateway.createNewInstance( item.getID(), "<blah/>", _session );
        System.out.println( result );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<success><workItem><taskID>" ) );
        
        result = _gateway.createNewInstance( item.getID(), "<blah/>", _session );
        System.out.println( result );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<success><workItem><taskID>" ) );
        
        // we should have reached the limit set in the specification for number of instances
        result = _gateway.createNewInstance( item.getID(), "<blah/>", _session );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<failure" ) );
        
        // make sure any work items that all fired work items get started
        startAllFiredWorkItems();
        
        // now finish any work items that are executing
        completeAllExecutingWorkItems();
        
        items = getWorkItemsWithStatus( YWorkItem.Status.Executing );
        assertNotNull( items );
        assertTrue( "" + items.size(), items.size() == 0 );
    }
    
    public void testGetNonExistentWorkItemDetails() throws RemoteException {
    	String result = _gateway.getWorkItemDetails( "invalid_work_item_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testStartNonExistentWorkItem() throws RemoteException {
    	String result = _gateway.startWorkItem( "invalid_work_item_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testCompleteNonExistentWorkItem() throws RemoteException {
    	String result = _gateway.completeWorkItem( "invalid_work_item_id", null, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testGetOptionsForNonExistentWorkItem() throws RemoteException {
    	String result = _gateway.getWorkItemOptions( "invalid_work_item_id", "", _session );
    	assertNotNull( result );
    	assertTrue( result, result.length() == 0 );
    }
    
    public void testSuspendWorkItem()
    		throws URISyntaxException, YPersistenceException, JDOMException, IOException {
    	launchCase();
    	
        // get the correct work item
        Map<String, WorkItemRecord> itemsMap = getEnabledWorkItems();
        assertNotNull( itemsMap );
        WorkItemRecord item = itemsMap.get( "one" );
        assertNotNull( item );
        
        // start the work item
        String result = _gateway.startWorkItem( item.getID().toString(), _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // the item should be executing now
        Set<WorkItemRecord> items = getWorkItemsWithStatus( YWorkItem.Status.Executing );
        assertNotNull( items );
        assertTrue( "" + items.size(), items.size() == 1 );
        
        item = items.iterator().next();
        assertNotNull( item );
        
        // suspend the work item
    	result = _gateway.suspendWorkItem( item.getID(), _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<success" ) );
    	
    	// now the work item should be in the "fired" state
    	items = getWorkItemsWithStatus( YWorkItem.Status.Fired );
    	assertNotNull( items );
    	assertTrue( "" + items.size(), items.size() == 1 );
    }
    
    public void testRestartSuspendedWorkItem()
			throws YPersistenceException, URISyntaxException, JDOMException, IOException {
		// utilize this other test to start the work item then suspend it
		testSuspendWorkItem();
		
		// get the work item (which should be in the "fired" state)
		Set<WorkItemRecord> items = getWorkItemsWithStatus( YWorkItem.Status.Fired );
		assertNotNull( items );
		assertTrue( "" + items.size(), items.size() == 1 );
		
		WorkItemRecord item = items.iterator().next();
		assertNotNull( item );
		
		// now we should be able to start it back up again
		String result = _gateway.startWorkItem( item.getID(), _session );
		assertNotNull( result );
		assertTrue( result, result.startsWith( "<success" ) );
		
		// to be cautious: make sure any work items that are put in the fired state get run
		startAllFiredWorkItems();
		
		// now we should be able to finish it
		completeAllExecutingWorkItems();
		
		// now task "two" should be enabled
		Map<String, WorkItemRecord> itemMap = getEnabledWorkItems();
		assertNotNull( itemMap );
		assertTrue( "" + itemMap.size(), itemMap.containsKey( "two" ) );
	}
    
    /**
     * TODO this test just compares the XML to a hard-coded string because the XML that
     * gets output cannot be turned into any kind of YAWL object without manually
     * parsing through the XML and rebuilding things. Is this the functionality we want?
     */
    public void testGetCaseStateSuccess()
			throws URISyntaxException, YPersistenceException, JDOMException, IOException {
		launchCase();
		
		String xml = _gateway.getCaseState( _caseID, _session );
		assertNotNull( xml );
		assertTrue( xml, xml.equals(
				"<caseState caseID=\"" + _caseID + "\" specID=\"OneTwoThreeSpec.xml\">" +
					"<condition id=\"InputCondition:input\" name=\"null\" documentation=\"null\">" +
						"<identifier>" + _caseID + "</identifier>" +
						"<flowsInto><nextElementRef id=\"one\" documentation=\"\"/></flowsInto>" +
					"</condition>" +
				"</caseState>" ) );
		
		// get the correct work item
		Map<String, WorkItemRecord> itemsMap = getEnabledWorkItems();
		assertNotNull( itemsMap );
		WorkItemRecord item = itemsMap.get( "one" );
		assertNotNull( item );
		
		// start the work item
		String result = _gateway.startWorkItem( item.getID().toString(), _session );
		assertNotNull( result );
		assertFalse( result, result.startsWith( "<failure" ) );
		
		xml = _gateway.getCaseState( _caseID, _session );
		assertNotNull( xml );
		assertTrue( xml, xml.equals(
				"<caseState caseID=\"" + _caseID + "\" specID=\"OneTwoThreeSpec.xml\">" +
					"<task id=\"AtomicTask:one\" name=\"DecompOne\">" +
						"<internalCondition id=\"mi_active[AtomicTask:one]\">" +
							"<identifier>" + _caseID + ".1</identifier>" +
						"</internalCondition>" +
						"<internalCondition id=\"executing[AtomicTask:one]\">" +
							"<identifier>" + _caseID + ".1</identifier>" +
						"</internalCondition>" +
					"</task>" +
				"</caseState>" ) );
		
		// the item should be executing now
		Set<WorkItemRecord> items = getWorkItemsWithStatus( YWorkItem.Status.Executing );
		assertNotNull( items );
		assertTrue( "" + items.size(), items.size() == 1 );
		
		item = items.iterator().next();
		assertNotNull( item );
		
		// suspend the work item
		result = _gateway.suspendWorkItem( item.getID(), _session );
		assertNotNull( result );
		assertTrue( result, result.startsWith( "<success" ) );
		
		// now the work item should be in the "fired" state
		items = getWorkItemsWithStatus( YWorkItem.Status.Fired );
		assertNotNull( items );
		assertTrue( "" + items.size(), items.size() == 1 );
		
		xml = _gateway.getCaseState( _caseID, _session );
		assertNotNull( xml );
		assertTrue( xml, xml.equals(
				"<caseState caseID=\"" + _caseID + "\" specID=\"OneTwoThreeSpec.xml\">" +
					"<task id=\"AtomicTask:one\" name=\"DecompOne\">" +
						"<internalCondition id=\"mi_active[AtomicTask:one]\">" +
							"<identifier>" + _caseID + ".1</identifier>" +
						"</internalCondition>" +
						"<internalCondition id=\"mi_entered[AtomicTask:one]\">" +
							"<identifier>" + _caseID + ".1</identifier>" +
						"</internalCondition>" +
					"</task>" +
				"</caseState>" ) );
	}
    
    /**
     * Tests getting the case state from an invalid case id
     */
    public void testGetCaseStateFailure() throws RemoteException {
    	String result = _gateway.getCaseState( "invalid_case_id", _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }
    
    public void testCaseCancel()
    		throws URISyntaxException, YPersistenceException, JDOMException, IOException {
    	// start the case and run the first task
    	launchCase();
        performTask("one");
        
        Set<WorkItemRecord> enabledItems = getWorkItemsWithStatus( YWorkItem.Status.Enabled );
        
        // start the next task
        for( Iterator<WorkItemRecord> iterator = enabledItems.iterator(); iterator.hasNext(); ) {
            WorkItemRecord item = iterator.next();
            if( item.getTaskID().equals( "two" ) ) {
            	String result = _gateway.startWorkItem( item.getID(), _session );
            	assertNotNull( result );
            	assertTrue( result, result.startsWith( "<success" ) );
                break;
            }
        }
        
        // get the case ID for the running case
        Set<String> cases = getCasesForSpec( _specID );
        assertNotNull( cases );
        assertTrue( "" + cases.size(), cases.size() == 1 );
        
        // cancel the case
        String result = _gateway.cancelCase( cases.iterator().next(), _session );
        assertNotNull( result );
        assertTrue( result, result.startsWith( "<success" ) );
        
        // ensure that no cases are running
        cases = getCasesForSpec( _specID );
        assertNotNull( cases );
        assertTrue( "" + cases.size(), cases.size() == 0 );
    }
    
    public void testMultipleCases() throws URISyntaxException, JDOMException, IOException {
        // TODO test having multiple cases running
    	Set<String> caseIDs = new HashSet<String>();
    	Set<String> xmlCaseIDs = new HashSet<String>();
    	
    	String xml = _gateway.getCasesForSpecification( _specID, _session );
    	Element root = xmlToRootElement( "<caseIDs>" + xml + "</caseIDs>" );
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		xmlCaseIDs.add( ((Element) root.getContent( index )).getTextNormalize() );
    	}
    	
    	assertTrue( caseIDs.size() + " " + xmlCaseIDs.size(), setEquality( caseIDs, xmlCaseIDs ) );
    	
    	launchCase();
    	caseIDs.add( _caseID );
    	
    	xml = _gateway.getCasesForSpecification( _specID, _session );
    	root = xmlToRootElement( "<caseIDs>" + xml + "</caseIDs>" );
    	xmlCaseIDs.clear();
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		xmlCaseIDs.add( ((Element) root.getContent( index )).getTextNormalize() );
    	}
    	
    	assertTrue( caseIDs.size() + " " + xmlCaseIDs.size(), setEquality( caseIDs, xmlCaseIDs ) );
    	
    	launchCase();
    	caseIDs.add( _caseID );
    	
    	xml = _gateway.getCasesForSpecification( _specID, _session );
    	root = xmlToRootElement( "<caseIDs>" + xml + "</caseIDs>" );
    	xmlCaseIDs.clear();
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		xmlCaseIDs.add( ((Element) root.getContent( index )).getTextNormalize() );
    	}
    	
    	assertTrue( caseIDs.size() + " " + xmlCaseIDs.size(), setEquality( caseIDs, xmlCaseIDs ) );
    	
    	launchCase();
    	caseIDs.add( _caseID );
    	
    	xml = _gateway.getCasesForSpecification( _specID, _session );
    	root = xmlToRootElement( "<caseIDs>" + xml + "</caseIDs>" );
    	xmlCaseIDs.clear();
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		xmlCaseIDs.add( ((Element) root.getContent( index )).getTextNormalize() );
    	}
    	
    	assertTrue( caseIDs.size() + " " + xmlCaseIDs.size(), setEquality( caseIDs, xmlCaseIDs ) );
    	
    	launchCase();
    	caseIDs.add( _caseID );
    	
    	xml = _gateway.getCasesForSpecification( _specID, _session );
    	root = xmlToRootElement( "<caseIDs>" + xml + "</caseIDs>" );
    	xmlCaseIDs.clear();
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		xmlCaseIDs.add( ((Element) root.getContent( index )).getTextNormalize() );
    	}
    	
    	assertTrue( caseIDs.size() + " " + xmlCaseIDs.size(), setEquality( caseIDs, xmlCaseIDs ) );
    }
    
    private static boolean setEquality( Set set1, Set set2 ) {
    	if( set1 == null && set2 == null ) {
    		return true;
    	}
    	if( set1 == null || set2 == null ) {
    		return false;
    	}
    	return ( set1.containsAll( set2 ) && set2.containsAll( set1 ) );
    }
    
    public void testCaseCancelNull() throws RemoteException {
    	String result = _gateway.cancelCase( null, _session );
    	assertNotNull( result );
    	assertTrue( result, result.startsWith( "<failure" ) );
    }

    private void launchCase() throws RemoteException, URISyntaxException {
    	String caseData = "<RootDecomp></RootDecomp>";
    	String ret = _gateway.launchCase( _specID, caseData,
    			new URI("mock://mockedURL/testingNonExistent"), _session );
    	assertNotNull( ret );
    	assertFalse( ret, ret.startsWith( "<failure" ) );
    	Long.parseLong( ret );
    	_caseID = ret;
    }

    private void performTask(String name) throws YPersistenceException, JDOMException, IOException {
        // get all enabled work items
        Map<String, WorkItemRecord> itemsMap = getEnabledWorkItems();
        assertNotNull( itemsMap );
        assertTrue( name + ":" + itemsMap.size(), itemsMap.size() > 0 );
        assertTrue( name + ":" + itemsMap.size(), itemsMap.containsKey( name ) );
        
        // get the correct work item out
        WorkItemRecord item = itemsMap.get( name );
        
        // TODO the following code causes a null pointer exception.
        // uncomment it once the bug in YWorkItemRepository.getChildrenOf(YWorkItem) is fixed
//        String result = _gateway.getChildrenOfWorkItem( item.getID(), _session );
//        assertNotNull( result );
//        assertTrue( result, result.length() == 0 );
        
        // check the options for the work item
        String result = _gateway.getWorkItemOptions( item.getID(), "", _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        Set<String> options = parseOptions( result );
        assertNotNull( options );
        assertTrue( "" + options.size(), options.contains( "start" ) );
        assertFalse( "" + options.size(), options.contains( "suspend" ) );
        assertFalse( "" + options.size(), options.contains( "complete" ) );
        
        // start the work item
        System.out.println( "Starting task: " + item.getTaskID() +
        		" in work item: " + item.getID().toString() );
        result = _gateway.startWorkItem( item.getID().toString(), _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // make sure there were children created
        result = _gateway.getChildrenOfWorkItem( item.getID(), _session );
        assertNotNull( result );
        assertTrue( result, result.length() > 0 );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // make sure any work items that are put in the fired state get run
        startAllFiredWorkItems();
        
        // now finish any work items that are executing
        completeAllExecutingWorkItems();
    }
    
    /**
     * Given an XML string of options for a work item, returns a set of the names of
     * the options for that work item.
     * Options are: start, suspend, complete, addNewInstance
     */
    private Set<String> parseOptions( String xml ) throws JDOMException, IOException {
    	Set<String> options = new HashSet<String>();
    	
    	Element root = xmlToRootElement( "<options>" + xml + "</options>" );
    	assertNotNull( root );
    	
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		Element option = (Element) root.getContent( index );
    		assertNotNull( option );
    		assertTrue( option.toString(), option.getName().equals( "option" ) );
    		
    		String op = option.getAttributeValue( "operation" );
    		assertNotNull( op );
    		
    		options.add( op );
    	}
    	
    	return options;
    }
    
    private void startAllFiredWorkItems()
    		throws YPersistenceException, JDOMException, IOException {
    	Set<WorkItemRecord> items = getWorkItemsWithStatus( YWorkItem.Status.Fired );
    	WorkItemRecord item;
        for( Iterator<WorkItemRecord> iterator = items.iterator(); iterator.hasNext(); ) {
        	item = iterator.next();
        	assertNotNull( item );
        	
            // check the options for the work item
            String result = _gateway.getWorkItemOptions( item.getID(), "", _session );
            assertNotNull( result );
            assertFalse( result, result.startsWith( "<failure" ) );
            Set<String> options = parseOptions( result );
            assertNotNull( options );
            assertTrue( "" + options.size(), options.contains( "start" ) );
            assertFalse( "" + options.size(), options.contains( "suspend" ) );
            assertFalse( "" + options.size(), options.contains( "complete" ) );
        	
            // then start it
        	System.out.println( "Starting task: " + item.getTaskID() +
	        		" in work item: " + item.getID() );
			result = _gateway.startWorkItem( item.getID(), _session );
			assertNotNull( result );
			assertFalse( result, result.startsWith( "<failure" ) );
        }
    }
    
    private void completeAllExecutingWorkItems()
    		throws YPersistenceException, RemoteException, JDOMException, IOException {
    	Set<WorkItemRecord> items;
    	WorkItemRecord item;
    	while( (items = getWorkItemsWithStatus( YWorkItem.Status.Executing )).size() > 0 ) {
        	item = items.iterator().next();
        	assertNotNull( item );
        	
            // check the options for the work item
            String result = _gateway.getWorkItemOptions( item.getID(), "", _session );
            assertNotNull( result );
            assertFalse( result, result.startsWith( "<failure" ) );
            Set<String> options = parseOptions( result );
            assertNotNull( options );
            assertFalse( "" + options.size(), options.contains( "start" ) );
            assertTrue( "" + options.size(), options.contains( "suspend" ) );
            assertTrue( "" + options.size(), options.contains( "complete" ) );
            
            // then complete it
        	System.out.println( "Completing task: " + item.getTaskID() +
					" in work item: " + item.getID() );
			result = _gateway.completeWorkItem( item.getID(), item.getDataListString(), _session );
			assertNotNull( result );
			assertFalse( result, result.startsWith( "<failure" ) );
        }
    }
    
    /**
     * @return a map that maps task IDs to enabled work items for those tasks.
     */
    private Map<String, WorkItemRecord> getEnabledWorkItems()
    		throws JDOMException, IOException,YPersistenceException {
        // get all enabled and fired work item IDs
        String result = _gateway.getAvailableWorkItemIDs( _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        // convert the XML string to an object tree
        Element root = xmlToRootElement( result );
        assertNotNull( root );
        
        Map<String, WorkItemRecord> items = new HashMap<String, WorkItemRecord>();
        
        // go through the object tree
        for( int index = 0; index < root.getContentSize(); index++ ) {
        	Element child = (Element) root.getContent( index );
        	assertNotNull( "index:" + index + "\n" + result, child );
        	
        	String id = child.getTextNormalize();
        	assertNotNull( "index:" + index + "\n" + result, id );
        	
        	// get the work item record for each ID
        	WorkItemRecord item = getWorkItemFromID( id );
        	assertNotNull( id, item );
        	
        	if( item.getStatus().toString().equals( YWorkItem.Status.Enabled.toString() ) )
        		items.put( item.getTaskID(), item );
        }
        
    	return items;
    }
    
    /**
     * @return a set of work item records for all work items with the given status.
     */
    private Set<WorkItemRecord> getWorkItemsWithStatus( YWorkItem.Status status )
    		throws YPersistenceException, JDOMException, IOException {
        // to get the executing work items we'll have to filter through all of them
        // (no convenience method is located in the gateway, only the repository itself)
        String result = _gateway.describeAllWorkItems( _session );
        assertNotNull( result );
        assertFalse( result, result.startsWith( "<failure" ) );
        
        Set<WorkItemRecord> items = new HashSet<WorkItemRecord>();
        
        // go through all the work items
        Element root = xmlToRootElement( "<allItems>" + result + "</allItems>" );
        for( int index = 0; index < root.getContentSize(); index++ ) {
        	Element child = (Element) root.getContent( index );
        	assertNotNull( "index:" + index + "\n" + result, child );
        	assertNotNull( "index:" + index + "\n" + result, child.getName() );
        	assertTrue( child.getName(), child.getName().equals( "workItem" ) );
        	
        	// get the ID
        	String id =
        		child.getChildTextNormalize( "caseID" ) + ":" +
    			child.getChildTextNormalize( "taskID" );
        	
        	// get the work item record for each ID
        	WorkItemRecord item = getWorkItemFromID( id );
        	assertNotNull( id, item );
        	
        	if( item.getStatus().toString().equals( status.toString() ) )
        		items.add( item );
        }
        
    	return items;
    }
    
    /**
     * @return a set of case IDs for the given spec.
     */
    private Set<String> getCasesForSpec( String specID ) throws JDOMException, IOException {
    	String result = _gateway.getCasesForSpecification( specID, _session );
    	assertNotNull( result );
    	assertFalse( result.startsWith( "<failure" ) );
    	
    	Element root = xmlToRootElement( "<cases>" + result + "</cases>" );
    	
    	Set<String> caseIDs = new HashSet<String>();
    	
    	for( int index = 0; index < root.getContentSize(); index++ ) {
    		Element child = (Element) root.getContent( index );
    		assertNotNull( child );
    		assertNotNull( child.getName() );
    		assertTrue( child.getName(), child.getName().equals( "caseID" ) );
    		
    		String id = child.getTextNormalize();
    		assertNotNull( id );
    		assertTrue( id.length() > 0 );
    		caseIDs.add( id );
    	}
    	
    	return caseIDs;
    }
    
    private WorkItemRecord getWorkItemFromID( String workItemID ) throws JDOMException,
    		IOException, YPersistenceException {
    	return Marshaller.unmarshalWorkItem( _gateway.getWorkItemDetails( workItemID, _session ) );
//    	return EngineFactory.createEngine().getWorkItem( workItemID );
    }
    
    public static Element xmlToRootElement( String xml ) throws JDOMException, IOException {
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
