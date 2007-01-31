/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.Document;

import au.edu.qut.yawl.elements.YAWLServiceReference;
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
 * @author Lachlan Aldred
 * Date: 21/05/2004
 * Time: 15:41:36
 */
public class TestCaseCancellation extends AbstractTransactionalTestCase {
    private YIdentifier _idForTopNet;
    private AbstractEngine _engine;
    private YSpecification _specification;
    private List _taskCancellationReceived = new ArrayList();
    private YWorkItemRepository _repository;
    private List _caseCompletionReceived = new ArrayList();

    public void setUp() throws Exception {
    	super.setUp();
        try {
			_engine =  EngineFactory.createYEngine();
			EngineClearer.clear(_engine);
		} catch (RuntimeException e) {
			super.tearDown();
			throw new Exception("Test setup failed", e);
		}

        URI serviceURI = new URI("mock://mockedURL/testingCaseCompletion");
        YAWLServiceReference service = new YAWLServiceReference(serviceURI.toString());
        Set services = _engine.getYAWLServices();
        for (Iterator iterator = services.iterator(); iterator.hasNext();) {
            YAWLServiceReference serviceRef = (YAWLServiceReference) iterator.next();
            if(serviceRef.getURI().equals(service.getURI())) {
                _engine.removeYawlService(service.getURI());
            }
        }
        _engine.addYawlService(service);
        
        service = new YAWLServiceReference("mock://mockedURL/bla/bla/bla");
        services = _engine.getYAWLServices();
        for (Iterator iterator = services.iterator(); iterator.hasNext();) {
            YAWLServiceReference serviceRef = (YAWLServiceReference) iterator.next();
            if(serviceRef.getURI().equals(service.getURI())) {
                _engine.removeYawlService(service.getURI());
            }
        }
        _engine.addYawlService(service);

        _repository = YWorkItemRepository.getInstance();
        URL fileURL = getClass().getResource("CaseCancellation.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = (YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);

        _engine.addSpecifications(yawlXMLFile, false, new ArrayList());
        
        _idForTopNet = _engine.startCase(null, _specification.getID(), null, serviceURI);
   
        
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

    public void testIt() throws InterruptedException, YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        System.out.println(); _engine.getCasesForSpecification(_specification.getID()).size();

    	Thread.sleep(150);
        performTask("register");
        Thread.sleep(150);
        performTask("register_itinerary_segment");
        Thread.sleep(150);
        performTask("register_itinerary_segment");
        Thread.sleep(150);
        performTask("flight");
        Thread.sleep(150);
        performTask("flight");
        Thread.sleep(150);
        performTask("cancel");
        Set cases = _engine.getCasesForSpecification(_specification.getID());
        assertEquals(cases.toString(), 0, cases.size());
    }

    public void testCaseCancel() throws InterruptedException, YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Thread.sleep(150);
        performTask("register");

        Thread.sleep(150);
        Set enabledItems = _repository.getEnabledWorkItems();

        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            if (workItem.getTaskID().equals("register_itinerary_segment")) {
                _engine.startWorkItem(workItem.getIDString(), "admin");
                break;
            }
        }
        _engine.cancelCase(_idForTopNet);
        assertTrue(_taskCancellationReceived.size() > 0);
    }
    
    public void testCaseCancelNull() throws YPersistenceException {
    	try {
    		_engine.cancelCase( null );
    		fail( "An exception should have been thrown." );
    	}
    	catch( IllegalArgumentException e ) {
    		// proper exception was thrown
    	}
    }

    public void testCaseCompletion() throws YPersistenceException, YDataStateException, YSchemaBuildingException, YQueryException, YStateException, InterruptedException {
    	while(_engine.getAvailableWorkItems().size() > 0 ) {
            YWorkItem item = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            performTask(item.getTaskID());
        }
        assertTrue(_caseCompletionReceived.size() > 0);
    }


    public void performTask(String name) throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Set enabledItems;
        Set firedItems;
        Set activeItems;
        enabledItems = _repository.getEnabledWorkItems();

        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            if (workItem.getTaskID().equals(name)) {
                        _engine.startWorkItem(workItem.getIDString(), "admin");
                break;
            }
        }
        firedItems = _repository.getFiredWorkItems();
        for (Iterator iterator = firedItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            _engine.startWorkItem(workItem.getIDString(), "admin");
            break;
        }
        activeItems = _repository.getExecutingWorkItems();
        for (Iterator iterator = activeItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            _engine.completeWorkItem(workItem.getIDString(), "<data/>", false);
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
        suite.addTestSuite(TestCaseCancellation.class);
        return suite;
    }
}
