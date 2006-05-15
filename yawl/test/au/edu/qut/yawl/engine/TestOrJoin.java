/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.exceptions.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.jdom.JDOMException;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 27/04/2004
 * Time: 15:03:20
 * 
 */
public class TestOrJoin extends TestCase {
//    private YLocalWorklist _localWorklist;
//    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private long _sleepTime = 100;
    private AbstractEngine _engine;

    public TestOrJoin(String name) {
        super(name);
    }


    public void setUp() {
//        YLocalWorklist.clear();

    }

    public void testImproperCompletion() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YAuthenticationException, YDataStateException, YStateException, YQueryException, YPersistenceException {
        URL fileURL = getClass().getResource("TestOrJoin.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
//todo AJH:Obsoltete ????
//        String sessionHandle = _engine.connect("admin", "YAWL");
        _engine.loadSpecification(specification);
        YIdentifier id = _engine.startCase(specification.getID().toString(), null, null);
        {
            YWorkItem itemA = (YWorkItem)_engine.getAvailableWorkItems().iterator().next();
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemA.getCaseID().toString(), itemA.getTaskID());
            _engine.startWorkItem(itemA, "admin");

            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemA = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
            itemA = (YWorkItem) _engine.getChildrenOfWorkItem(
                    itemA).iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemA.getCaseID().toString(), itemA.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemA, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemF = null;
//            Iterator it = _workItemRepository.getEnabledWorkItems().iterator();
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("F")){
                    itemF = item;
                    break;
                }
            }
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemF.getCaseID().toString(), itemF.getTaskID());
            _engine.startWorkItem(itemF, "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemF = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
            itemF = (YWorkItem) _engine.getChildrenOfWorkItem(itemF).iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemF.getCaseID().toString(), itemF.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemF, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemB = null;
//            Iterator it = _workItemRepository.getEnabledWorkItems().iterator();
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("B")){
                    itemB = item;
                    break;
                }
            }
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemB.getCaseID().toString(), itemB.getTaskID());
            _engine.startWorkItem(itemB, "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemB = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
            itemB = (YWorkItem) _engine.getChildrenOfWorkItem(itemB).iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemB.getCaseID().toString(), itemB.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemB, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
//            YWorkItem itemA = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems()
                    .iterator().next();
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemA.getCaseID().toString(), itemA.getTaskID());

            Set its =  _engine.getAvailableWorkItems();
            itemA = _engine.startWorkItem(itemA, "admin");
            assertNotNull(itemA);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            for (Iterator iterator = _workItemRepository.getEnabledWorkItems().iterator(); iterator.hasNext();) {
//                YWorkItem workItem = (YWorkItem) iterator.next();
//                if (workItem.getTaskID().equals("E")) {
//                    fail("There should be no enabled work item 'E' yet.");
//                }
//            }
            for (Iterator iterator = _engine.getAvailableWorkItems().iterator(); iterator.hasNext();) {
                YWorkItem workItem = (YWorkItem) iterator.next();
                if (workItem.getTaskID().equals("E")) {
                    fail("There should be no enabled work item 'E' yet.");
                }
            }

//            itemA = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemA.getCaseID().toString(), itemA.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemA, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
//            for (Iterator iterator = _workItemRepository.getEnabledWorkItems().iterator(); iterator.hasNext();) {
//                YWorkItem workItem = (YWorkItem) iterator.next();
////System.out.println("workItem = " + workItem.toXML() + "/n");
//                if (workItem.getTaskID().equals("E")) {
//                    fail("There should be no enabled work item 'E' yet.");
//                }
//            }
            for (Iterator iterator = _engine.getAvailableWorkItems().iterator(); iterator.hasNext();) {
                YWorkItem workItem = (YWorkItem) iterator.next();
//System.out.println("workItem = " + workItem.toXML() + "/n");
                if (workItem.getTaskID().equals("E")) {
                    fail("There should be no enabled work item 'E' yet.");
                }
            }
        }
    }


    public void testImproperCompletion2() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YDataStateException, YStateException, YQueryException, YPersistenceException {
//        _localWorklist = new YLocalWorklist("Donald2", false);
        URL fileURL2 = getClass().getResource("Test55.xml");
        File yawlXMLFile2 = new File(fileURL2.getFile());
        YSpecification specification2 = null;
        specification2 = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile2.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
        _engine.loadSpecification(specification2);
        _engine.startCase(specification2.getID().toString(), null, null);
        {
//            YWorkItem itemA = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemA.getCaseID().toString(), itemA.getTaskID());
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            itemA = _engine.startWorkItem(itemA, "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemA = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemA.getCaseID().toString(), itemA.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemA, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemF = null;
//            Iterator it = _workItemRepository.getEnabledWorkItems().iterator();
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("5")){
                    itemF = item;
                    break;
                }
            }
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemF.getCaseID().toString(), itemF.getTaskID());
            itemF = _engine.startWorkItem(itemF, "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemF = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemF.getCaseID().toString(), itemF.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemF, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemB = null;
//            Iterator it = _workItemRepository.getEnabledWorkItems().iterator();
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("6")){
                    itemB = item;
                    break;
                }
            }
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemB.getCaseID().toString(), itemB.getTaskID());
            itemB = _engine.startWorkItem(itemB, "admin");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
//            itemB = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemB.getCaseID().toString(), itemB.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemB, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
//            YWorkItem itemA = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemA.getCaseID().toString(), itemA.getTaskID());
            itemA = _engine.startWorkItem(itemA, "admin");
            assertNotNull(itemA);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while(it.hasNext()){
                YWorkItem workItem = (YWorkItem) it.next();
                if (workItem.getTaskID().equals("9")) {
                    fail("There should be no enabled work item '9' yet.");
                }
            }

//            itemA = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemA.getCaseID().toString(), itemA.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemA, "<data/>");
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            it = _engine.getAvailableWorkItems().iterator();
            while(it.hasNext()) {
                YWorkItem workItem = (YWorkItem) it.next();
//System.out.println("workItem = " + workItem.toXML() + "/n");
                if (workItem.getTaskID().equals("9")) {
                    fail("There should be no enabled work item '9' yet.");
                }
            }
        }
    }

    //todo write two more test for or join using cancellationTest.ywl and another variant



    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestOrJoin.class);
        return suite;
    }
}
