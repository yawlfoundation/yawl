package org.yawlfoundation.yawl.engine;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 27/04/2004
 * Time: 15:03:20
 * 
 */
public class TestOrJoin extends TestCase {
    private long _sleepTime = 100;
    private YEngine _engine;

    public TestOrJoin(String name) {
        super(name);
    }


    public void setUp() {

    }

    public void testImproperCompletion() throws YSchemaBuildingException, YEngineStateException, YSyntaxException, JDOMException, IOException, YAuthenticationException, YDataStateException, YStateException, YQueryException, YPersistenceException {
        URL fileURL = getClass().getResource("TestOrJoin.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        YSpecification specification = YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
        _engine.loadSpecification(specification);
        YIdentifier id = _engine.startCase(specification.getSpecificationID(), null, null,
                null, new YLogDataItemList(), null, false);
           {
            YWorkItem itemA = _engine.getAvailableWorkItems().iterator().next();
            _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));

            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemA = (YWorkItem) _engine.getChildrenOfWorkItem(itemA).iterator().next();
            _engine.completeWorkItem(itemA, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemF = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("F")){
                    itemF = item;
                    break;
                }
            }
            _engine.startWorkItem(itemF, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemF = (YWorkItem) _engine.getChildrenOfWorkItem(itemF).iterator().next();
            _engine.completeWorkItem(itemF, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemB = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("B")){
                    itemB = item;
                    break;
                }
            }
            _engine.startWorkItem(itemB, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            itemB = (YWorkItem) _engine.getChildrenOfWorkItem(itemB).iterator().next();
            _engine.completeWorkItem(itemB, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemA = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("A")){
                    itemA = item;
                    break;
                }
            }
            assertNotNull("itemA parent is null", itemA);
            itemA = _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));
            assertNotNull("itemA child is null", itemA);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            for (Iterator iterator = _engine.getAvailableWorkItems().iterator(); iterator.hasNext();) {
                YWorkItem workItem = (YWorkItem) iterator.next();
                if (workItem.getTaskID().equals("E")) {
                    fail("There should be no enabled work item 'E' yet.");
                }
            }

            _engine.completeWorkItem(itemA, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            for (Iterator iterator = _engine.getAvailableWorkItems().iterator(); iterator.hasNext();) {
                YWorkItem workItem = (YWorkItem) iterator.next();
                if (workItem.getTaskID().equals("E")) {
                    fail("There should be no enabled work item 'E' yet.");
                }
            }
        }
    }


    public void testImproperCompletion2() throws YSchemaBuildingException, YEngineStateException, YSyntaxException, JDOMException, IOException, YDataStateException, YStateException, YQueryException, YPersistenceException {
        URL fileURL2 = getClass().getResource("Test55.xml");
        File yawlXMLFile2 = new File(fileURL2.getFile());
        YSpecification specification2 = YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile2.getAbsolutePath())).get(0);
        _engine = YEngine.getInstance();
        EngineClearer.clear(_engine);
        _engine.loadSpecification(specification2);
        YIdentifier id = _engine.startCase(specification2.getSpecificationID(), null,
                null, null, new YLogDataItemList(), null, false);
           {
            YWorkItem itemA = _engine.getAvailableWorkItems().iterator().next();
            itemA = _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            _engine.completeWorkItem(itemA, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemF = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("5")){
                    itemF = item;
                    break;
                }
            }
            itemF = _engine.startWorkItem(itemF, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            _engine.completeWorkItem(itemF, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemB = null;
            Iterator it = _engine.getAvailableWorkItems().iterator();
            while (it.hasNext()) {
                YWorkItem item = (YWorkItem) it.next();
                if(item.getTaskID().equals("6")){
                    itemB = item;
                    break;
                }
            }
            itemB = _engine.startWorkItem(itemB, _engine.getExternalClient("admin"));
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            _engine.completeWorkItem(itemB, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        {
            YWorkItem itemA = _engine.getAvailableWorkItems().iterator().next();
            assertNotNull("itemA parent is null", itemA);
            itemA = _engine.startWorkItem(itemA, _engine.getExternalClient("admin"));
            assertNotNull("itemA child is null", itemA);
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

            _engine.completeWorkItem(itemA, "<data/>", null, WorkItemCompletion.Normal);
            try {
                Thread.sleep(_sleepTime);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            it = _engine.getAvailableWorkItems().iterator();
            while(it.hasNext()) {
                YWorkItem workItem = (YWorkItem) it.next();
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
