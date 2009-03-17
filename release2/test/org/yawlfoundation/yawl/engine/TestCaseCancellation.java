/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Lachlan Aldred
 * Date: 21/05/2004
 * Time: 15:41:36
 */
public class TestCaseCancellation extends TestCase {
    private YIdentifier _idForTopNet;
    private YEngine _engine;
    private YSpecification _specification;
    private List _taskCancellationReceived = new ArrayList();
    private YWorkItemRepository _repository;
    private List _caseCompletionReceived = new ArrayList();

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YStateException, YPersistenceException, YDataStateException, URISyntaxException, YEngineStateException, YQueryException {
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);

        _repository = YWorkItemRepository.getInstance();
        URL fileURL = getClass().getResource("CaseCancellation.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = (YSpecification) YMarshal.
                    unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);

        _engine.loadSpecification(_specification);
        URI serviceURI = new URI("mock://mockedURL/testingCaseCompletion");

        YAWLServiceReference service = new YAWLServiceReference(serviceURI.toString(), null);
        _engine.addYawlService(service);
        _idForTopNet = _engine.startCase(null, null, _specification.getID(), null, serviceURI);

        ObserverGateway og = new ObserverGateway() {
            public void cancelAllWorkItemsInGroupOf(
                              Announcements<CancelWorkItemAnnouncement> announcements) {
                _taskCancellationReceived.add(announcements);
            }
            public void announceCaseCompletion(YAWLServiceReference yawlService, YIdentifier caseID, Document d) {
                _caseCompletionReceived.add(caseID);
            }
            public void announceCaseCompletion(YIdentifier caseID, Document d) {
                _caseCompletionReceived.add(caseID);
            }
            public String getScheme() {
                return "mock";
            }
            public void announceWorkItems(Announcements<NewWorkItemAnnouncement> a) {}
            public void announceTimerExpiry(YAWLServiceReference ys, YWorkItem i) {}
            public void announceNotifyEngineInitialised(Set<YAWLServiceReference> ys) {}
            public void announceNotifyCaseCancellation(Set<YAWLServiceReference> ys, YIdentifier i) {}
            public void announceCaseSuspended(YIdentifier id) {}
            public void announceCaseSuspending(YIdentifier id) {}
            public void announceCaseResumption(YIdentifier id) {}
            public void announceWorkItemStatusChange(YWorkItem item, YWorkItemStatus old, YWorkItemStatus anew) {}
        };
        _engine.registerInterfaceBObserverGateway(og);
    }

    public void testIt() throws InterruptedException, YDataStateException, YEngineStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Thread.sleep(400);
        performTask("register");
        Thread.sleep(400);
        performTask("register_itinerary_segment");
        Thread.sleep(400);
        performTask("register_itinerary_segment");
        Thread.sleep(400);
        performTask("flight");
        Thread.sleep(400);
        performTask("flight");
        Thread.sleep(400);
        performTask("cancel");
        Set cases = _engine.getCasesForSpecification(_specification.getSpecificationID());
        assertTrue(cases.toString(), cases.size() == 0);
    }

    public void testCaseCancel() throws InterruptedException, YDataStateException, YEngineStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Thread.sleep(400);
        performTask("register");

        Thread.sleep(400);
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

    public void testCaseCompletion() throws YPersistenceException, YEngineStateException, YDataStateException, YSchemaBuildingException, YQueryException, YStateException {
        while(_engine.getAvailableWorkItems().size() > 0 ) {
            YWorkItem item = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            performTask(item.getTaskID());
        }
        assertTrue(_caseCompletionReceived.size() > 0);
    }


    public void performTask(String name) throws YDataStateException, YStateException, YEngineStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
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
            _engine.completeWorkItem(workItem, "<data/>", false);
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
