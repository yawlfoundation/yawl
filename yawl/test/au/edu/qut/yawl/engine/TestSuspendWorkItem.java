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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.Document;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.unmarshal.YMarshal;

/**
 * @author Nathan Rose
 */
public class TestSuspendWorkItem extends AbstractTransactionalTestCase {
    private AbstractEngine _engine;
    private YSpecification _specification;
    private List _taskCancellationReceived = new ArrayList();
    private YWorkItemRepository _repository;
    private List _caseCompletionReceived = new ArrayList();

    public void setUp() throws Exception {
    	super.setUp();
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);

        _repository = YWorkItemRepository.getInstance();
        URL fileURL = getClass().getResource("CaseCancellation.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = (YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);

        _engine.loadSpecification(_specification);
        URI serviceURI = new URI("mock://mockedURL/testingCaseCompletion");

        YAWLServiceReference service = new YAWLServiceReference(serviceURI.toString());
        _engine.addYawlService(service);
        _engine.startCase(null, _specification.getID(), null, serviceURI);

        ObserverGateway og = new ObserverGateway() {
            public void cancelAllWorkItemsInGroupOf(
                    URI ys,
                    YWorkItem item) {
                _taskCancellationReceived.add(item);
            }
            public void announceCaseCompletion(URI yawlService, YIdentifier caseID, Document d) {
                _caseCompletionReceived.add(caseID);
            }
            public String getScheme() {
                return "mock";
            }
            public void announceWorkItem(URI ys, YWorkItem i) {}
        };
        _engine.registerInterfaceBObserverGateway(og);
    }
    
    public void testSuspendWorkItem()
			throws URISyntaxException, YPersistenceException, JDOMException, IOException, YStateException,
			YDataStateException, YQueryException, YSchemaBuildingException {
		// get the correct work item
    	Set<YWorkItem> workItems = _repository.getEnabledWorkItems();
    	YWorkItem workItem = null;
    	
    	// start the work item
        for( Iterator<YWorkItem> iterator = workItems.iterator(); iterator.hasNext(); ) {
            workItem = iterator.next();
            if( workItem.getTaskID().equals( "register" ) ) {
            	workItem = _engine.startWorkItem( workItem.getIDString(), "admin" );
                break;
            }
            workItem = null;
        }
        
        assertNotNull( workItem );
		
		// the engine code as of June 02, 2006 should do nothing if you try to suspend
		// a work item that's not executing
        _engine.suspendWorkItem( workItem.getIDString() );
		
		// start the work item
        workItem = _engine.unsuspendWorkItem( workItem.getIDString() );
		assertNotNull( workItem );
		assertTrue(""+ workItem.getStatus(), workItem.getStatus().equals( YWorkItem.Status.Executing ) );
		
		// the item should be executing now
		workItems = _repository.getExecutingWorkItems( "admin" );
		assertNotNull( workItems );
		assertTrue( "" + workItems.size(), workItems.size() == 1 );
		
		workItem = workItems.iterator().next();
		assertNotNull( workItem );
		
		// suspend the work item
		_engine.suspendWorkItem( workItem.getIDString() );
		
		// now the work item should be in the "fired" state
		workItems = _repository.getSuspendedWorkItems();
		assertNotNull( workItems );
		assertTrue( "" + workItems.size(), workItems.size() == 1 );
	}
    
    public void testRestartSuspendedWorkItem()
			throws YPersistenceException, URISyntaxException, JDOMException, IOException,
			YStateException, YDataStateException, YQueryException, YSchemaBuildingException {
		// utilize this other test to start the work item then suspend it
		testSuspendWorkItem();
		
		// get the work item (which should be in the "fired" state)
		Set<YWorkItem> items = _repository.getSuspendedWorkItems();
		assertNotNull( items );
		assertTrue( "" + items.size(), items.size() == 1 );
		
		YWorkItem item = items.iterator().next();
		assertNotNull( item );
		assertNotNull( item.toXML(), item.getTaskID() );
		assertTrue( item.getTaskID(), item.getTaskID().equals( "register" ) );
		
		// now we should be able to start it back up again
		item = _engine.unsuspendWorkItem( item.getIDString() );
		assertNotNull( item );
		assertNotNull( item.toXML(), item.getStatus() );
		assertTrue(""+ item.getStatus(), item.getStatus().equals( YWorkItem.Status.Executing ) );
		
		// to be cautious: make sure any work items that are put in the fired state get run
		startAllFiredWorkItems();
		
		// now we should be able to finish it
		completeAllExecutingWorkItems();
		
		// now task "register_itinerary_segment" should be enabled
		items = _repository.getEnabledWorkItems();
		assertNotNull( items );
		assertTrue( "" + items.size(), items.size() == 3 );
		
		for( Iterator<YWorkItem> iterator = items.iterator(); iterator.hasNext(); ) {
			item = iterator.next();
			assertNotNull( item );
			assertNotNull( item.toXML(), item.getTaskID() );
			assertTrue( item.getTaskID(),
					item.getTaskID().equals( "register_itinerary_segment" ) ||
					item.getTaskID().equals( "cancel" ) );
		}
	}
    
    public void testSuspendNullWorkItem() throws YPersistenceException, YStateException {
    	YWorkItem retval = _engine.suspendWorkItem( null );
    	assertNull( "return value should have been null", retval );
    }
    
    /**
     * Tests rolling back a work item to the fired state where the work item's status
     * and the task's status are out of synch.
     */
    public void testRollbackOutOfSynchWorkItem() throws YStateException, YDataStateException,
    YQueryException, YSchemaBuildingException, YPersistenceException {
    	// get the correct work item
    	Set<YWorkItem> workItems = _repository.getEnabledWorkItems();
    	YWorkItem workItem = null;
    	YWorkItem original = null;
    	
    	// start the work item
        for( Iterator<YWorkItem> iterator = workItems.iterator(); iterator.hasNext(); ) {
            original = iterator.next();
            if( original.getTaskID().equals( "register" ) ) {
            	workItem = _engine.startWorkItem( original.getIDString(), "admin" );
                break;
            }
        }
        
        assertNotNull( workItem );
		
		// the engine code as of June 02, 2006 should do nothing if you try to suspend
		// a work item that's not executing
        _engine.suspendWorkItem( original.getIDString() );
		
		// the item should be executing now
		workItems = _repository.getExecutingWorkItems( "admin" );
		assertNotNull( workItems );
		assertTrue( "" + workItems.size(), workItems.size() == 1 );
		
		workItem = workItems.iterator().next();
		assertNotNull( workItem );
		
		// mess up the atomic task to desynchronize the task and work item
		YNetRunner netRunner = YEngine._workItemRepository.getNetRunner(workItem.getCaseID().getParent());
    	YAtomicTask task = (YAtomicTask) netRunner._net.getNetElement( "register" );
    	task.getMIExecuting().removeAll();
		
		// rollback the work item
    	try {
    		_engine.rollbackWorkItem( workItem.getIDString(), "admin" );
    		fail( "An exception should have been thrown" );
    	}
    	catch( YStateException e ) {
    		// proper exception was thrown
    	}
    }
    
    private void startAllFiredWorkItems() throws YStateException, YDataStateException, YQueryException,
    		YSchemaBuildingException, YPersistenceException {
    	Set<YWorkItem> firedItems = _repository.getFiredWorkItems();
        for (Iterator<YWorkItem> iterator = firedItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = iterator.next();
            _engine.startWorkItem(workItem.getIDString(), "admin");
            break;
        }
    }
    
    private void completeAllExecutingWorkItems() throws YStateException, YDataStateException,
    		YQueryException, YSchemaBuildingException, YPersistenceException {
    	Set<YWorkItem> executingItems = _repository.getExecutingWorkItems();
        for (Iterator<YWorkItem> iterator = executingItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = iterator.next();
            _engine.completeWorkItem(workItem.getIDString(), workItem.getDataString(), false );
            break;
        }
    }
    
    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestSuspendWorkItem.class);
        return suite;
    }
}
