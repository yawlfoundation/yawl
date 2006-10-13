/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.exceptions.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.jdom.JDOMException;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 6/06/2003
 * Time: 12:28:34
 * 
 */
public class TestEngineSystem1 extends TestCase {
    private YNetRunner _netRunner;
    private YIdentifier _idForTopNet;
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private int _sleepTime = 100;
    private AbstractEngine _engine;
    private YSpecification _specification;
    private File yawlXMLFile;

    public TestEngineSystem1(String name) {
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YPersistenceException {
        URL fileURL = getClass().getResource("YAWL_Specification3.xml");
        yawlXMLFile = new File(fileURL.getFile());
        _specification = null;
        _specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();

    }


    public void testDecomposingNets() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException, IOException, JDOMException {
        try {
            EngineClearer.clear(_engine);
            _engine.addSpecifications(yawlXMLFile, false, new ArrayList());
            _idForTopNet = _engine.startCase(null, _specification.getID(), null, null);
            //enabled btop
            Set currWorkItems = _workItemRepository.getEnabledWorkItems();
            YWorkItem anItem = (YWorkItem) currWorkItems.iterator().next();
            assertTrue(currWorkItems.size() == 1 && anItem.getTaskID().equals("b-top"));
            Thread.sleep(_sleepTime);
            //fire btop
//            _localWorklist.startOneWorkItemAndSetOthersToFired(anItem.getCaseID().toString(), anItem.getTaskID());
            anItem = _engine.startWorkItem(anItem, "admin");
            currWorkItems = _workItemRepository.getEnabledWorkItems();
            assertTrue(currWorkItems.isEmpty());
            currWorkItems = _workItemRepository.getExecutingWorkItems();
            assertTrue("" + currWorkItems.size(), currWorkItems.size() == 1);
            anItem = (YWorkItem) currWorkItems.iterator().next();
            Thread.sleep(_sleepTime);
            //complete btop
//            _localWorklist.setWorkItemToComplete(anItem.getCaseID().toString(), anItem.getTaskID(),"<data/>");
            _engine.completeWorkItem(anItem, "<data/>", false);
            //should atumatically fire multi inst comp task ctop
            //get mi attributes of f-leaf-c
            List leafNetRunners = new Vector();
            Set enabled = _workItemRepository.getEnabledWorkItems();
            YMultiInstanceAttributes fLeafCMIAttributes = null;
            Iterator iter = enabled.iterator();
            while (iter.hasNext()) {
                YWorkItem item = (YWorkItem) iter.next();
                YNetRunner leafCRunner = _workItemRepository.getNetRunner(item.getCaseID());
                if (leafNetRunners.size() == 0) {
                    fLeafCMIAttributes =
                            ((YAtomicTask) leafCRunner.getNetElement("f-leaf-c"))
                            .getMultiInstanceAttributes();
                }
                leafNetRunners.add(leafCRunner);
//System.out.println("item "+item);
                assertTrue(leafCRunner != null);
            }
            //fire all the enabled e-leaf-c nodes
            while (_workItemRepository.getEnabledWorkItems().size() > 0) {
                Vector v = new Vector(_workItemRepository.getEnabledWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
                anItem = (YWorkItem) v.get(temp);
                assertEquals(anItem.getCaseID().getParent(), _idForTopNet);
                assertTrue(anItem.getTaskID().equals("e-leaf-c"));
                assertTrue(anItem.getStatus() == YWorkItem.Status.Enabled);
                //fire e-leaf-c
//                _localWorklist.startOneWorkItemAndSetOthersToFired(anItem.getCaseID().toString(), anItem.getTaskID());
                _engine.startWorkItem(anItem, "admin");
                assertTrue("Item status ("+anItem.getStatus()+") should be is parent."
                        , anItem.getStatus() == YWorkItem.Status.IsParent);
                Set executingChildren = _workItemRepository.getExecutingWorkItems();
                assertTrue(executingChildren.containsAll(anItem.getChildren()));
                Thread.sleep(_sleepTime);
            }
            //complete e-leaf-c
            while (_workItemRepository.getExecutingWorkItems().size() > 0) {
                Vector v = new Vector(_workItemRepository.getExecutingWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
                anItem = (YWorkItem) v.get(temp);
//                _localWorklist.setWorkItemToComplete(
//                        anItem.getCaseID().toString(), anItem.getTaskID(),"<data/>");
                _engine.completeWorkItem(anItem, "<data/>", false);
                assertTrue(_workItemRepository.getWorkItem(
                        anItem.getCaseID().toString(), anItem.getTaskID())
                        == null);
                Thread.sleep(_sleepTime);
            }
            currWorkItems = _workItemRepository.getEnabledWorkItems();
            assertTrue(currWorkItems.size() == _workItemRepository.getWorkItems().size());
            // fire all the enabled f-leaf-c and g-leaf-c children
            while (_workItemRepository.getEnabledWorkItems().size() > 0) {
                Vector v = new Vector(_workItemRepository.getEnabledWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
//System.out.println("v.size() : " + v.size() + " InstanceValidator: " + InstanceValidator);
                anItem = (YWorkItem) v.get(temp);
                assertTrue(anItem.getTaskID().equals("f-leaf-c")
                        || anItem.getTaskID().equals("g-leaf-c"));
//                _localWorklist.startOneWorkItemAndSetOthersToFired(
//                        anItem.getCaseID().toString(), anItem.getTaskID());
                _engine.startWorkItem(anItem, "admin");
                if (anItem.getTaskID().equals("g-leaf-c")) {
                    assertTrue(anItem.getChildren().size() == 1);
                    assertTrue(((YWorkItem) anItem.getChildren().iterator().next())
                            .getStatus() == YWorkItem.Status.Executing);
                } else {
                    int numChildren = anItem.getChildren().size();
                    assertTrue(numChildren >= fLeafCMIAttributes.getMinInstances()
                            && numChildren <= fLeafCMIAttributes.getMaxInstances());
                }
                Thread.sleep(_sleepTime);
            }
            //start all the fired f-leaf-c nodes
            while (_workItemRepository.getFiredWorkItems().size() > 0) {

                Vector v = new Vector(_workItemRepository.getFiredWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
                anItem = (YWorkItem) v.get(temp);
                assertTrue(anItem.getTaskID().equals("f-leaf-c"));
//                _localWorklist.startOneWorkItemAndSetOthersToFired(
//                        anItem.getCaseID().toString(), anItem.getTaskID());
                _engine.startWorkItem(anItem, "admin");
                assertTrue(anItem.getStatus() == YWorkItem.Status.Executing);
                assertTrue(_workItemRepository.getWorkItems().contains(anItem));
                Thread.sleep(_sleepTime);
            }
            //complete all of the f-leaf-c and g-leaf-c children
            while (_workItemRepository.getExecutingWorkItems().size() > 0) {
                Vector v = new Vector(_workItemRepository.getExecutingWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
                anItem = (YWorkItem) v.get(temp);
                assertTrue(anItem.getTaskID().equals("f-leaf-c")
                        || anItem.getTaskID().equals("g-leaf-c"));
//                _localWorklist.setWorkItemToComplete(anItem.getCaseID().toString(), anItem.getTaskID(),"<data/>");
                _engine.completeWorkItem(anItem, "<data/>", false);
                if (anItem.getTaskID().equals("g-leaf-c")) {
                    assertFalse(_workItemRepository.getWorkItems().contains(anItem));
                }
                Thread.sleep(_sleepTime);
            }
            //fire all but one of the h-leaf-c children
            while (_workItemRepository.getEnabledWorkItems().size() > 1) {
                Vector v = new Vector(_workItemRepository.getEnabledWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
                anItem = (YWorkItem) v.get(temp);
                assertTrue(anItem.getTaskID().equals("h-leaf-c"));
//                _localWorklist.startOneWorkItemAndSetOthersToFired(anItem.getCaseID().toString(), anItem.getTaskID());
                _engine.startWorkItem(anItem, "admin");
                assertTrue(anItem.getChildren().size() == 1);
                assertTrue(((YWorkItem) anItem.getChildren().iterator().next()).getStatus()
                        == YWorkItem.Status.Executing);
                Thread.sleep(_sleepTime);
            }
            /*
             * Complete two of the h-leaf-c children to ensure that:
             * All child nets are dead, finished.
             * All work items to-do with those nets are removed.
             * And that d-top is enabled. - done
             */
            YNetRunner topNetRunner = _workItemRepository.getNetRunner(_idForTopNet);
            while (_workItemRepository.getExecutingWorkItems().size() > 0) {
                Vector v = new Vector(_workItemRepository.getExecutingWorkItems());
                int temp = (int) Math.abs(Math.floor(Math.random() * v.size()));
                anItem = (YWorkItem) v.get(temp);
                assertTrue(anItem.getTaskID().equals("h-leaf-c"));
//                _localWorklist.setWorkItemToComplete(anItem.getCaseID().toString(), anItem.getTaskID(),"<data/>");
                _engine.completeWorkItem(anItem, "<data/>", false);
                assertFalse(_workItemRepository.getWorkItems().contains(anItem));
                Thread.sleep(_sleepTime);
            }
//System.out.println("state after cancellation : \n " + YStateInspector.inspectState( _idForTopNet));
            assertTrue("" + _workItemRepository.getWorkItems(),
                    _workItemRepository.getWorkItems().size() == 0);
            Iterator iterator = leafNetRunners.iterator();
            while (iterator.hasNext()) {
                YNetRunner leafCRunner = (YNetRunner) iterator.next();
                assertFalse("" + leafCRunner.getCaseID(), leafCRunner.isAlive());
            }
            assertTrue(_workItemRepository.getWorkItems().size() == 0);
            assertFalse(topNetRunner.isAlive());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestEngineSystem1.class);
        return suite;
    }
}
