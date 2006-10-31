/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.util.YDocumentCleaner;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * Tests the XML specification SplitsAndJoins.xml to ensure that it runs
 * properly. The specification consists of an AND split then join, followed
 * by an OR split then join, followed by an XOR split then join.
 * @author Nathan Rose
 */
public class TestSplitsAndJoins extends TestCase {
    private YIdentifier _idForTopNet;
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private static final int SLEEP_TIME = 100;
    private AbstractEngine _engine;
//    private YSpecification _specification;
    private File _specFile;

    public TestSplitsAndJoins(String name) {
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, YPersistenceException,
    		JDOMException, IOException {
        URL fileURL = getClass().getResource("SplitsAndJoins.xml");
//        File yawlXMLFile = new File(fileURL.getFile());
        _specFile = new File( fileURL.getFile() );
//        _specification = null;
//        _specification = (YSpecification) YMarshal.
//                            unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
    }
    
    private static void sleep( long millis ) {
    	try {
    		Thread.sleep( millis );
    	}
    	catch( InterruptedException e ) {
    	}
    }
    
    public void testSplitsAndJoins() throws YDataStateException, YPersistenceException, YStateException,
    		YSchemaBuildingException, YQueryException, JDOMException, IOException {
    	// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> itemIter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	List<String> ids;
    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
    	YSpecification spec;
    	
    	// load the spec and start it
    	ids = _engine.addSpecifications( _specFile, false, errors );
    	assertNotNull( ids );
    	assertTrue( YVerificationMessage.containsNoErrors( errors ) );
    	spec = _engine.getSpecification( ids.get( 0 ) );
//    	_engine.loadSpecification(_specification);
    	_idForTopNet = _engine.startCase(null, spec.getID(), null, null);
    	
    	
    	// make sure there's 1 enabled item to start
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	
    	// get the enabled item, make sure it's the right one
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "A_5" ) );
    	
    	sleep( SLEEP_TIME );
    	
    	// start task A
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );
    	
    	// complete task A
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	
    	// task A splits to B and C. Make sure there are 2 enabled items.
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 2 );
    	
    	// run the two items (tasks B and C)
    	itemIter = workItems.iterator();
    	while( itemIter.hasNext() ) {
    		item = itemIter.next();
    		netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    		_engine.startWorkItem( item, "admin" );
    	}
    	
    	sleep( SLEEP_TIME );
    	
    	// make sure task D isn't enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete tasks B and C
    	workItems = _workItemRepository.getExecutingWorkItems();
    	assertTrue( workItems.size() == 2 );
    	itemIter = workItems.iterator();
    	while( itemIter.hasNext() ) {
    		item = itemIter.next();
    		_engine.completeWorkItem( item, item.getDataString(), false );
    	}
    	
    	sleep( SLEEP_TIME );
    	
    	
    	// make sure D is enabled now, and run it
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "D_8" ) );
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );
    	
    	// make sure nothing else is enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );

    	// complete task D
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	sleep( SLEEP_TIME );
    	
    	
    	// make sure E is enabled now, and run it
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "E_15" ) );
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );

    	// make sure nothing else is enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete task E
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	sleep( SLEEP_TIME );
    	

    	// task E splits to F and G. Make sure there are 2 enabled items.
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 2 );
    	
    	// run the two items (tasks F and G)
    	itemIter = workItems.iterator();
    	while( itemIter.hasNext() ) {
    		item = itemIter.next();
    		netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    		_engine.startWorkItem( item, "admin" );
    	}
    	
    	sleep( SLEEP_TIME );
    	
    	// make sure task H isn't enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete tasks F and G
    	workItems = _workItemRepository.getExecutingWorkItems();
    	assertTrue( workItems.size() == 2 );
    	itemIter = workItems.iterator();
    	while( itemIter.hasNext() ) {
    		item = itemIter.next();
    		_engine.completeWorkItem( item, item.getDataString(), false );
    	}
    	
    	sleep( SLEEP_TIME );
    	

    	// make sure H is enabled now, and run it
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "H_7" ) );
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );

    	// make sure nothing else is enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete task H
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	sleep( SLEEP_TIME );
    	
    	
    	// make sure I is enabled now, and run it
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "I_6" ) );
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );

    	// make sure nothing else is enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete task I
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	sleep( SLEEP_TIME );
    	
    	
    	// I should flow into K, based on the predicates, but not J or L
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "K_3" ) );
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );

    	// make sure nothing else is enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete task K
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	sleep( SLEEP_TIME );
    	
    	
    	// have to get this before the flow finishes...
    	YNetRunner topNetRunner = _workItemRepository.getNetRunner(_idForTopNet);
    	
    	
    	// make sure M is enabled now, and run it
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "M_13" ) );
    	netRunners.add( _workItemRepository.getNetRunner( item.getCaseID() ) );
    	item = _engine.startWorkItem( item, "admin" );
    	
    	sleep( SLEEP_TIME );

    	// make sure nothing else is enabled yet
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 0 );
    	
    	// complete task M
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	sleep( SLEEP_TIME );
    	
    	
        assertTrue( "" + _workItemRepository.getWorkItems(),
                _workItemRepository.getWorkItems().size() == 0 );
        Iterator<YNetRunner> iterator = netRunners.iterator();
        while (iterator.hasNext()) {
            YNetRunner netRunner = iterator.next();
            assertFalse("" + netRunner.getCaseID(), netRunner.isAlive());
        }
        assertTrue( _workItemRepository.getWorkItems().size() == 0 );
        assertFalse( topNetRunner.isAlive() );
    }
    
    public void testLoadSpec() throws YPersistenceException, JDOMException, IOException {
    	URL fileURL = getClass().getResource("SplitsAndJoins.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        
        assertNull(_engine.getSpecification("SplitsAndJoins.ywl"));
        
        List<YVerificationMessage> msgs = new LinkedList<YVerificationMessage>(); 
        List<String> ids = _engine.addSpecifications( yawlXMLFile, false, msgs );
        assertNotNull( ids );
        assertTrue( msgs.size() + "\n" + YMessagePrinter.getMessageString( msgs ), msgs.size() == 0 );
        assertTrue( ids.size() > 0 );
        assertTrue( "" + ids.size(), ids.size() == 1 );
        
        String id = ids.get(0);
        
        assertNotNull(_engine.getSpecification("SplitsAndJoins.ywl"));
        
        // reading a second time should fail, since the ID is already in the engine.
        ids = _engine.addSpecifications( yawlXMLFile, false, msgs );
        
        assertNotNull( ids );
        assertTrue( msgs.size() + "\n" + YMessagePrinter.getMessageString( msgs ), msgs.size() > 0 );
        
        try {
        	// should work the first time
        	_engine.unloadSpecification( id );
        }
        catch(YStateException e) {
        	e.printStackTrace();
        	fail(e.toString());
        }
        
        assertFalse(_engine.getSpecIDs().contains("SplitsAndJoins.ywl"));
        // the next line works because the engine keeps track of which specs have been unloaded
        assertNotNull(_engine.getSpecification("SplitsAndJoins.ywl"));
        
        try {
        	// second time should fail because it's already unloaded
        	_engine.unloadSpecification( id );
        	fail("An exception should have been thrown");
        }
        catch(YStateException e) {
        	// proper exception was thrown
        }
    }
    
    /**
     * Tests trying to complete a task that wasn't activated, which should
     * cause an exception to be thrown. Calls {@link AbstractEngine#completeWorkItem(YWorkItem, String)}.
     */
    public void testCompleteInactiveTask() throws YDataStateException, YPersistenceException,
			YSchemaBuildingException, YQueryException, JDOMException, IOException {
    	try {
        	// variables
        	Set<YWorkItem> workItems;
        	Iterator<YWorkItem> itemIter;
        	YWorkItem item;
        	List<String> ids;
        	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
        	YSpecification spec;
        	
        	// load the spec and start it
        	ids = _engine.addSpecifications( _specFile, false, errors );
        	assertNotNull( ids );
        	assertTrue( YVerificationMessage.containsNoErrors( errors ) );
        	spec = _engine.getSpecification( ids.get( 0 ) );
//        	_engine.loadSpecification(_specification);
        	_idForTopNet = _engine.startCase(null, spec.getID(), null, null);
        	
        	
        	// make sure there's 1 enabled item to start
        	workItems = _workItemRepository.getEnabledWorkItems();
        	assertTrue( workItems.size() == 1 );
        	
        	// get the enabled item
        	item = workItems.iterator().next();
        	
        	// try to complete it
        	YNetRunner netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
        	_engine.completeWorkItem( item, "<A/>", false );
        	
        	fail( "an error should have been thrown" );
    	}
    	catch( YStateException e ) {
    		// correct exception was thrown.
    	}
    }
    
    /**
     * Tests trying to complete a task that wasn't activated, which should
     * cause an exception to be thrown. Calls
     * {@link YNetRunner#completeWorkItemInTask(YWorkItem, YIdentifier, String, Document)}.
     */
    public void testCompleteInactiveTaskB() throws YDataStateException, YPersistenceException,
			YStateException, YSchemaBuildingException, YQueryException, JDOMException, IOException {
    	try {
        	// variables
        	Set<YWorkItem> workItems;
        	Iterator<YWorkItem> itemIter;
        	YWorkItem item;
        	List<String> ids;
        	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
        	YSpecification spec;
        	
        	// load the spec and start it
        	ids = _engine.addSpecifications( _specFile, false, errors );
        	assertNotNull( ids );
        	assertTrue( YVerificationMessage.containsNoErrors( errors ) );
        	spec = _engine.getSpecification( ids.get( 0 ) );
//        	_engine.loadSpecification(_specification);
        	_idForTopNet = _engine.startCase(null, spec.getID(), null, null);
        	
        	
        	// make sure there's 1 enabled item to start
        	workItems = _workItemRepository.getEnabledWorkItems();
        	assertTrue( workItems.size() == 1 );
        	
        	// get the enabled item
        	item = workItems.iterator().next();
        	
        	// try to complete it
        	YNetRunner netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
        	netRunner.completeWorkItemInTask(item, _idForTopNet, "A_5", new Document(new Element("A")) );
        	
        	fail( "an error should have been thrown" );
    	}
    	catch( RuntimeException e ) {
    		// correct exception was thrown.
    	}
    }
    
    /**
     * Tests that a task that hasn't been fired yet won't start when you call t_start()
     */
    public void testTaskStart() throws YDataStateException, YPersistenceException,
			YStateException, YSchemaBuildingException, YQueryException, JDOMException, IOException {
        // variables
        Set<YWorkItem> workItems;
		Iterator<YWorkItem> itemIter;
		YWorkItem item;
		List<String> ids;
    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
    	YSpecification spec;

		// load the spec and start it
    	ids = _engine.addSpecifications( _specFile, false, errors );
    	assertNotNull( YMessagePrinter.getMessageString( errors ), ids );
    	assertTrue( YMessagePrinter.getMessageString( errors ),
    			YVerificationMessage.containsNoErrors( errors ) );
    	spec = _engine.getSpecification( ids.get( 0 ) );
    	
    	// attempt to reload the spec: should cause an error because the ID is already loaded
    	errors = new LinkedList<YVerificationMessage>();
    	ids = _engine.addSpecifications( _specFile, false, errors );
    	assertNotNull( YMessagePrinter.getMessageString( errors ), ids );
    	assertFalse( YMessagePrinter.getMessageString( errors ),
    			YVerificationMessage.containsNoErrors( errors ) );
    	
//		assertTrue(_engine.loadSpecification( _specification ));
//		assertFalse(_engine.loadSpecification( _specification ));
		_idForTopNet = _engine.startCase( null, spec.getID(), null, null );

		// make sure there's 1 enabled item to start
		workItems = _workItemRepository.getEnabledWorkItems();
		assertTrue( workItems.size() == 1 );

		// get the enabled item
		item = workItems.iterator().next();

		YNetRunner netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
		YTask task = (YTask) netRunner._net.getNetElement( "A_5" );
		task.cancel();
		task.t_start( null );
		
		assertFalse( task.getMIExecuting().containsIdentifier() );
	}
    
    /**
     * Tests suspending a task that's executing, then re-running it and completing it.
     */
    public void testSuspendTask() throws YStateException, YSchemaBuildingException,
    		YDataStateException, YPersistenceException, YQueryException, JDOMException, IOException {
    	// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> itemIter;
    	YWorkItem item;
    	List<String> ids;
    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
    	YSpecification spec;
    	
    	// load the spec and start it
    	ids = _engine.addSpecifications( _specFile, false, errors );
    	assertNotNull( ids );
    	assertTrue( YVerificationMessage.containsNoErrors( errors ) );
    	spec = _engine.getSpecification( ids.get( 0 ) );
//    	_engine.loadSpecification(_specification);
    	_idForTopNet = _engine.startCase(null, spec.getID(), null, null);
    	
    	
    	// make sure there's 1 enabled item to start
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	
    	// get the enabled item, make sure it's the right one
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "A_5" ) );
    	
    	System.out.println( _engine.getStateForCase( item.getCaseID() ) );
    	System.out.println( _engine.getStateTextForCase( item.getCaseID() ) );
    	
    	sleep( SLEEP_TIME );
    	
    	// get task A
    	YNetRunner netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
		YTask task = (YTask) netRunner._net.getNetElement( "A_5" );
		
		assertFalse( task.getMIActive().containsIdentifier() );
		assertFalse( task.getMIComplete().containsIdentifier() );
		assertFalse( task.getMIEntered().containsIdentifier() );
		assertFalse( task.getMIExecuting().containsIdentifier() );
		try {
			task.t_complete( item.getCaseID(), null );
			fail("An exception should have been thrown");
		}
		catch(RuntimeException exc) {
			// proper exception was thrown
		}
		
		// start it
    	item = _engine.startWorkItem( item, "admin" );
    	System.out.println( _engine.getStateForCase( item.getCaseID().toString() ) );
    	System.out.println( _engine.getStateTextForCase( item.getCaseID() ) );
    	
    	sleep( SLEEP_TIME );
    	
    	assertTrue( task.getMIActive().containsIdentifier() );
		assertFalse( task.getMIComplete().containsIdentifier() );
		assertFalse( task.getMIEntered().containsIdentifier() );
		assertTrue( task.getMIExecuting().containsIdentifier() );
		
		try {
			// trying to start it again when it's already executing should fail
			item = _engine.startWorkItem( item, "admin" );
			fail("An exception should have been thrown");
		}
		catch(YStateException e) {
			// proper exception was thrown
		}
		
		assertTrue( task.getMIActive().containsIdentifier() );
		assertFalse( task.getMIComplete().containsIdentifier() );
		assertFalse( task.getMIEntered().containsIdentifier() );
		assertTrue( task.getMIExecuting().containsIdentifier() );
		
		// roll it back so it can be started over
		assertTrue( netRunner.rollbackWorkItem( item.getCaseID(), task.getID() ) );
		// make sure to roll back the status of the work item too...
		// (it should probably be done in YNetRunner.rollbackWorkItem(), but it's not)
		item.rollBackStatus();

		System.out.println( _engine.getStateForCase( item.getCaseID().toString() ) );
		System.out.println( _engine.getStateTextForCase( item.getCaseID() ) );
		
		assertTrue( task.getMIActive().containsIdentifier() );
		assertFalse( task.getMIComplete().containsIdentifier() );
		assertTrue( task.getMIEntered().containsIdentifier() );
		assertFalse( task.getMIExecuting().containsIdentifier() );
		assertTrue( item.getStatus() == YWorkItem.Status.Fired  );
		
		// attempting to roll it back again (when it's already rolled back) shouldn't work
		assertFalse( netRunner.rollbackWorkItem( item.getCaseID(), task.getID() ) );
		
		assertTrue( task.getMIActive().containsIdentifier() );
		assertFalse( task.getMIComplete().containsIdentifier() );
		assertTrue( task.getMIEntered().containsIdentifier() );
		assertFalse( task.getMIExecuting().containsIdentifier() );
		
		try {
			_engine.completeWorkItem( item, item.getDataString(), false );
			fail("An exception should have been thrown");
		}
		catch(YStateException e) {
			// proper exception was thrown
		}
		
		try {
			_engine.completeWorkItem( null, item.getDataString(), false );
			fail("An exception should have been thrown");
		}
		catch(YStateException e) {
			// proper exception was thrown
		}
		
		SAXBuilder builder = new SAXBuilder();
		Document e = null;
        try {
            Document d = builder.build( new StringReader( item.getDataString() ) );
             e = YDocumentCleaner.cleanDocument( d );
        }
        catch(IOException exc) {
        	exc.printStackTrace();
        	fail(exc.toString());
        }
		catch( JDOMException exc ) {
			exc.printStackTrace();
			fail(exc.toString());
		}
		
		try {
			task.t_complete( item.getCaseID(), e );
			fail("An exception should have been thrown");
		}
		catch(RuntimeException exc) {
			// proper exception was thrown
		}
		
		// restart it (should work since it was rolled back earlier)
		item = _engine.startWorkItem( item, "admin" );
		
		System.out.println( _engine.getStateForCase( item.getCaseID().toString() ) );
		System.out.println( _engine.getStateTextForCase( item.getCaseID() ) );
		
		sleep( SLEEP_TIME );
    	
    	assertTrue( task.getMIActive().containsIdentifier() );
		assertFalse( task.getMIComplete().containsIdentifier() );
		assertFalse( task.getMIEntered().containsIdentifier() );
		assertTrue( task.getMIExecuting().containsIdentifier() );
    	
    	// and finally complete it
    	_engine.completeWorkItem( item, item.getDataString(), false );
    	
    	System.out.println( _engine.getStateForCase( item.getCaseID().toString() ) );
    	//System.out.println( _engine.getStateTextForCase( item.getCaseID().toString() ) );
    }
    
    public void testStartNonExistingSpec() throws YSchemaBuildingException, YDataStateException, YPersistenceException {
    	try {
    		_engine.startCase( null, "aninvalidID", null, null );
    		fail("An exception should have been thrown");
    	}
    	catch(YStateException e) {
    		// proper exception was thrown
    	}
    }
    
    public void testFireUnEnabledTask() throws YDataStateException, YPersistenceException,
    		YSchemaBuildingException, YQueryException, JDOMException, IOException {
		try {
			// variables
			Set<YWorkItem> workItems;
			Iterator<YWorkItem> itemIter;
			YWorkItem item;
			List<String> ids;
	    	List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
	    	YSpecification spec;

			// load the spec and start it
	    	ids = _engine.addSpecifications( _specFile, false, errors );
        	assertNotNull( ids );
        	assertTrue( YVerificationMessage.containsNoErrors( errors ) );
        	spec = _engine.getSpecification( ids.get( 0 ) );
//			_engine.loadSpecification( _specification );
			_idForTopNet = _engine.startCase( null, spec.getID(), null, null );

			// make sure there's 1 enabled item to start
			workItems = _workItemRepository.getEnabledWorkItems();
			assertTrue( workItems.size() == 1 );

			// get the enabled item
			item = workItems.iterator().next();

			YNetRunner netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
			YTask task = (YTask) netRunner._net.getNetElement( "D_8" );
			task.t_fire();
			fail( "An exception should have been thrown" );
		}
		catch( YStateException e ) {
			// correct exception was thrown.
		}
	}
    
    public static void main(String args[]) {
    	TestSplitsAndJoins test = new TestSplitsAndJoins("");
    	try {
    		test.setUp();
    		test.testSplitsAndJoins();
    		System.out.println( "success" );
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
//        TestRunner runner = new TestRunner();
//        runner.doRun(suite());
//        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestSplitsAndJoins.class);
        return suite;
    }
}
