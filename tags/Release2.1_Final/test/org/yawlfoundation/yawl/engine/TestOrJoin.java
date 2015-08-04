package org.yawlfoundation.yawl.engine;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.logging.YLogDataItemList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

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
    private YEngine _engine;

    public TestOrJoin(String name) {
        super(name);
    }


    public void setUp() {
//        YLocalWorklist.clear();

    }

    public void testImproperCompletion() throws YSchemaBuildingException, YEngineStateException, YSyntaxException, JDOMException, IOException, YAuthenticationException, YDataStateException, YStateException, YQueryException, YPersistenceException {
        URL fileURL = getClass().getResource("TestOrJoin.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
//todo AJH:Obsoltete ????
//        String sessionHandle = _engine.connect("admin", "YAWL");
        _engine.loadSpecification(specification);
        YIdentifier id = _engine.startCase(null, specification.getSpecificationID(), null, null, new YLogDataItemList());
           {
            YWorkItem itemA = (YWorkItem)_engine.getAvailableWorkItems().iterator().next();
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemA.getCaseID().toString(), itemA.getTaskID());
            _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));

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
            _engine.completeWorkItem(itemA, "<data/>", null, false);
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
            _engine.startWorkItem(itemF, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemF = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
            itemF = (YWorkItem) _engine.getChildrenOfWorkItem(itemF).iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemF.getCaseID().toString(), itemF.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemF, "<data/>", null, false);
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
            _engine.startWorkItem(itemB, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemB = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
            itemB = (YWorkItem) _engine.getChildrenOfWorkItem(itemB).iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemB.getCaseID().toString(), itemB.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemB, "<data/>", null, false);
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
            itemA = _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));
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
            _engine.completeWorkItem(itemA, "<data/>", null, false);
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


    public void testImproperCompletion2() throws YSchemaBuildingException, YEngineStateException, YSyntaxException, JDOMException, IOException, YDataStateException, YStateException, YQueryException, YPersistenceException {
//        _localWorklist = new YLocalWorklist("Donald2", false);
        URL fileURL2 = getClass().getResource("Test55.xml");
        File yawlXMLFile2 = new File(fileURL2.getFile());
        YSpecification specification2 = null;
        specification2 = (YSpecification) YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile2.getAbsolutePath())).get(0);
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
        _engine.loadSpecification(specification2);
        YIdentifier id = _engine.startCase(null, specification2.getSpecificationID(), null, null, new YLogDataItemList());
           {
//            YWorkItem itemA = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
//            _localWorklist.startOneWorkItemAndSetOthersToFired(
//                    itemA.getCaseID().toString(), itemA.getTaskID());
            YWorkItem itemA = (YWorkItem) _engine.getAvailableWorkItems().iterator().next();
            itemA = _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemA = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemA.getCaseID().toString(), itemA.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemA, "<data/>", null, false);
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
            itemF = _engine.startWorkItem(itemF, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

//            itemF = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemF.getCaseID().toString(), itemF.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemF, "<data/>", null, false);
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
            itemB = _engine.startWorkItem(itemB, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
//            itemB = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
//            _localWorklist.setWorkItemToComplete(
//                    itemB.getCaseID().toString(), itemB.getTaskID(), "<data/>");
            _engine.completeWorkItem(itemB, "<data/>", null, false);
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
            itemA = _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));
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
            _engine.completeWorkItem(itemA, "<data/>", null, false);
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
