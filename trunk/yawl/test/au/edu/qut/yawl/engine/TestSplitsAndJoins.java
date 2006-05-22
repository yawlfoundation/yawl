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
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;

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
    private YSpecification _specification;

    public TestSplitsAndJoins(String name) {
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, YPersistenceException,
    		JDOMException, IOException {
        URL fileURL = getClass().getResource("SplitsAndJoins.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = null;
        _specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
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
    		YSchemaBuildingException, YQueryException {
    	// variables
    	Set<YWorkItem> workItems;
    	Iterator<YWorkItem> itemIter;
    	YWorkItem item;
    	List<YNetRunner> netRunners = new Vector<YNetRunner>();
    	
    	// load the spec and start it
    	_engine.loadSpecification(_specification);
    	_idForTopNet = _engine.startCase(null, _specification.getID(), null, null);
    	
    	
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
    	
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
    		_engine.completeWorkItem( item, item.getDataString() );
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
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
    		_engine.completeWorkItem( item, item.getDataString() );
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
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
    	_engine.completeWorkItem( item, item.getDataString() );
    	
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
    
    /**
     * Tests trying to complete a task that wasn't activated, which should
     * cause an exception to be thrown. Calls {@link AbstractEngine#completeWorkItem(YWorkItem, String)}.
     */
    public void testCompleteInactiveTest() throws YDataStateException, YPersistenceException,
			YSchemaBuildingException, YQueryException {
    	try {
        	// variables
        	Set<YWorkItem> workItems;
        	Iterator<YWorkItem> itemIter;
        	YWorkItem item;
        	
        	// load the spec and start it
        	_engine.loadSpecification(_specification);
        	_idForTopNet = _engine.startCase(null, _specification.getID(), null, null);
        	
        	
        	// make sure there's 1 enabled item to start
        	workItems = _workItemRepository.getEnabledWorkItems();
        	assertTrue( workItems.size() == 1 );
        	
        	// get the enabled item
        	item = workItems.iterator().next();
        	
        	// try to complete it
        	YNetRunner netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
        	_engine.completeWorkItem( item, "<A/>" );
        	
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
    public void testCompleteInactiveTestB() throws YDataStateException, YPersistenceException,
			YStateException, YSchemaBuildingException, YQueryException {
    	try {
        	// variables
        	Set<YWorkItem> workItems;
        	Iterator<YWorkItem> itemIter;
        	YWorkItem item;
        	
        	// load the spec and start it
        	_engine.loadSpecification(_specification);
        	_idForTopNet = _engine.startCase(null, _specification.getID(), null, null);
        	
        	
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
