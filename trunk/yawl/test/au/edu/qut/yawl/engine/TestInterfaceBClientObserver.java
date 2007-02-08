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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * Tests how the engine interacts with the interface B client observer.
 * 
 * @author Nathan Rose
 */
public class TestInterfaceBClientObserver extends AbstractTransactionalTestCase {
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private AbstractEngine _engine;
    private YSpecification _specification;
    private MockInterfaceBClientObserver _mockObserver;

    public TestInterfaceBClientObserver(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    	super.setUp();
    	_engine =  EngineFactory.createYEngine();
        _mockObserver = new MockInterfaceBClientObserver();
        _engine.registerInterfaceBObserver( _mockObserver );
    	URL fileURL = TestInterfaceBClientObserver.class.getResource( "SimpleSpec.xml" );
        List<YVerificationMessage> errors = new LinkedList<YVerificationMessage>();
        List<String> specIDs = _engine.addSpecifications( new File( fileURL.getFile() ), false, errors );
        assertNotNull( YMessagePrinter.getMessageString( errors ), specIDs );
    	assertTrue( YMessagePrinter.getMessageString( errors ),
    			YVerificationMessage.containsNoErrors( errors ) );
    	_specification = _engine.getSpecification( specIDs.get( 0 ) );
    }
    
    public void tearDown() {
    	// registering null isn't the best way, but we don't want the mock observer lingering in the engine...
    	_engine.registerInterfaceBObserver( null );
    }
    
    /**
     * Tests that the engine tells the interface B observer when
     * a case is started or finished.
     */
    public void testTaskStartingAndFinishing() throws JDOMException, IOException, YQueryException,
    		YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException {
		// variables
    	Set<YWorkItem> workItems;
    	YWorkItem item;
    	YNetRunner netRunner;
    	
    	// start the spec
    	_engine.startCase(null, _specification.getID(), null, null);
    	
    	
    	// make sure there's 1 enabled item to start
    	workItems = _workItemRepository.getEnabledWorkItems();
    	assertTrue( workItems.size() == 1 );
    	
    	// get the enabled item, make sure it's the right one
    	item = workItems.iterator().next();
    	assertTrue( item.getTaskID(), item.getTaskID().equals( "record" ) );
    	
    	// start task 'record'
    	netRunner = _workItemRepository.getNetRunner( item.getCaseID() );
    	item = _engine.startWorkItem( item.getIDString(), "admin" );
    	
    	Thread.yield();
    	
    	assertNotNull( _mockObserver.specToCase.get( _specification.getID() ) );
    	
    	// complete task 'record'
    	// TODO to get the engine to tell the observer to remove the task I had to call kick() first...
    	netRunner.kick();
    	netRunner.completeWorkItemInTask( item, item.getCaseID(), item.getTaskID(),
    			new Document( new Element( "Prepare" ) ) );
//    	_engine.completeWorkItem( item, item.getDataString() );
    	
    	assertNull( _mockObserver.specToCase.get( _specification.getID() ) );
    }
    
    private static class MockInterfaceBClientObserver implements InterfaceBClientObserver {
    	private Map<String, String> specToCase = new HashMap<String, String>();
    	private Map<String, String> caseToSpec = new HashMap<String, String>();
    	
		public void addCase( String specID, String caseIDStr ) {
			specToCase.put( specID, caseIDStr );
			caseToSpec.put( caseIDStr, specID );
			assertTrue( "caseToSpec.size():" + specToCase.size() +
					", specToCase.size():" +caseToSpec.size(),
					specToCase.size() == caseToSpec.size() );
		}

		public void removeCase( String caseIDStr ) {
			String specID = caseToSpec.get( caseIDStr );
			caseToSpec.remove( caseIDStr );
			specToCase.remove( specID );
			assertTrue( "caseToSpec.size():" + specToCase.size() +
					", specToCase.size():" +caseToSpec.size(),
					specToCase.size() == caseToSpec.size() );
		}
    }
    
    public static void main(String args[]) {
    	TestInterfaceBClientObserver test = new TestInterfaceBClientObserver("");
    	try {
    		test.setUp();
    		test.testTaskStartingAndFinishing();
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
        suite.addTestSuite(TestInterfaceBClientObserver.class);
        return suite;
    }
}
