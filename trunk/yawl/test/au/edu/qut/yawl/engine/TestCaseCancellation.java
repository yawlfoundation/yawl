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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.Document;
import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.engine.interfce.EngineGateway;
import au.edu.qut.yawl.engine.interfce.EngineGatewayImpl;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;

/**
 * @author Lachlan Aldred
 * Date: 21/05/2004
 * Time: 15:41:36
 */
public class TestCaseCancellation extends TestCase {
    private YIdentifier _idForTopNet;
    private AbstractEngine _engine;
    private YSpecification _specification;
    private List _taskCancellationReceived = new ArrayList();
    private YWorkItemRepository _repository;
    private List _caseCompletionReceived = new ArrayList();

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YStateException, YPersistenceException, YDataStateException, URISyntaxException {
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);

        _repository = YWorkItemRepository.getInstance();
        URL fileURL = getClass().getResource("CaseCancellation.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = (YSpecification) YMarshal.
                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);

        _engine.loadSpecification(_specification);
        URI serviceURI = new URI("mock://mockedURL/testingCaseCompletion");

        YAWLServiceReference service = new YAWLServiceReference(serviceURI.toString(), null);
        _engine.addYawlService(service);
        _idForTopNet = _engine.startCase(null, _specification.getID(), null, serviceURI);

        ObserverGateway og = new ObserverGateway() {
            public void cancelAllWorkItemsInGroupOf(
                    YAWLServiceReference ys,
                    YWorkItem item) {
                _taskCancellationReceived.add(item);
            }
            public void announceCaseCompletion(YAWLServiceReference yawlService, YIdentifier caseID, Document d) {
                _caseCompletionReceived.add(caseID);
            }
            public String getScheme() {
                return "mock";
            }
            public void announceWorkItem(YAWLServiceReference ys, YWorkItem i) {}
        };
        _engine.registerInterfaceBObserverGateway(og);
    }

    public void testIt() throws InterruptedException, YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
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
        assertTrue(cases.toString(), cases.size() == 0);
    }

    public void testCaseCancel() throws InterruptedException, YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Thread.sleep(150);
        performTask("register");

        Thread.sleep(150);
        Set enabledItems = _repository.getEnabledWorkItems();

        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            if (workItem.getTaskID().equals("register_itinerary_segment")) {
                _engine.startWorkItem(workItem, "admin");
                break;
            }
        }
        _engine.cancelCase(_idForTopNet);
        assertTrue(_taskCancellationReceived.size() > 0);
    }
    
    public void testCaseCancelGateway() throws InterruptedException, YPersistenceException,
    		YStateException, YDataStateException, YQueryException, YSchemaBuildingException,
    		RemoteException, YAuthenticationException {
    	Thread.sleep(100);
        performTask("register");

        // this test is assuming that creating a gateway like this will point to the same engine
        // as the engine that's used for the rest of the tests in this class
        EngineGateway eg = new EngineGatewayImpl( false );
        String handle = eg.connect( "admin", "YAWL" );
        assertNotNull( handle );
        
        Thread.sleep(100);
        Set enabledItems = _repository.getEnabledWorkItems();

        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            if (workItem.getTaskID().equals("register_itinerary_segment")) {
                _engine.startWorkItem(workItem, "admin");
                break;
            }
        }
        String result = eg.cancelCase( _idForTopNet.getId(), handle );
        assertTrue( result, result.startsWith( "<success" ) );
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

    public void testCaseCompletion() throws YPersistenceException, YDataStateException, YSchemaBuildingException, YQueryException, YStateException {
        while(_engine.getAvailableWorkItems().size() > 0 ) {
            YWorkItem item = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            performTask(item.getTaskID());
        }
        assertTrue(_caseCompletionReceived.size() > 0);
    }


    public void performTask(String name) throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Set enabledItems = null;
        Set firedItems = null;
        Set activeItems = null;
        enabledItems = _repository.getEnabledWorkItems();

        for (Iterator iterator = enabledItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            if (workItem.getTaskID().equals(name)) {
                        _engine.startWorkItem(workItem, "admin");
                break;
            }
        }
        firedItems = _repository.getFiredWorkItems();
        for (Iterator iterator = firedItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            _engine.startWorkItem(workItem, "admin");
            break;
        }
        activeItems = _repository.getExecutingWorkItems();
        for (Iterator iterator = activeItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            _engine.completeWorkItem(workItem, "<data/>");
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
