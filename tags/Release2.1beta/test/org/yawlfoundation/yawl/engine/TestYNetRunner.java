/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 4/06/2003
 * Time: 17:08:14
 * 
 */
public class TestYNetRunner extends TestCase {
    private YNetRunner _netRunner1;
    private YIdentifier _id1;
    private Document _d;

    public TestYNetRunner(String name) {
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, YEngineStateException, YQueryException, JDOMException, IOException, YStateException, YPersistenceException, YDataStateException {
        URL fileURL = getClass().getResource("YAWL_Specification2.xml");
        File yawlXMLFile1 = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile1.getAbsolutePath())).get(0);
        YEngine engine2 = YEngine.getInstance();
        EngineClearer.clear(engine2);
        engine2.loadSpecification(specification);
        _id1 = engine2.startCase(null, specification.getSpecificationID(), null, null, new YLogDataItemList());
           _netRunner1 = (YNetRunner) engine2._caseIDToNetRunnerMap.get(_id1);
        _d = new Document();
        _d.setRootElement(new Element("data"));
    }


    public void testBasicFireAtomic() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        assertTrue(_netRunner1.getEnabledTasks().contains(_netRunner1.getNetElement("a-top")));
        assertTrue(_netRunner1.getEnabledTasks().size() == 1);
        List children = null;
        try {
            children = _netRunner1.attemptToFireAtomicTask(null, "b-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(children == null);
        try {
            children = _netRunner1.attemptToFireAtomicTask(null, "a-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        assertFalse(((YCondition) _netRunner1.getNetElement("i-top"))
                .containsIdentifier());
        assertTrue(children.size() == 1);
        assertTrue(_netRunner1.isAlive());
        assertTrue(_netRunner1.getEnabledTasks().size() == 0);
        assertTrue(_netRunner1.getBusyTasks().size() == 1);
        _netRunner1.startWorkItemInTask(null, (YIdentifier) children.get(0), "a-top");
        assertTrue(_netRunner1.completeWorkItemInTask(null, null, (YIdentifier) children.get(0), "a-top",_d));
        YCondition anonC = ((YCondition) _netRunner1.getNetElement(
                "c{a-top_b-top}"));
        assertTrue(anonC.contains(_id1));
        assertTrue(_id1.getLocations().contains(anonC));
        assertTrue(((YTask) _netRunner1._net.getNetElement("b-top")).t_enabled(null));
        assertTrue(_netRunner1.isAlive());
        assertTrue("" + _id1.getLocations(), _netRunner1.getEnabledTasks().size() == 1);
        YAtomicTask btop = (YAtomicTask) _netRunner1.getNetElement("b-top");
        List btopChildren = null;
        try {
            btopChildren = _netRunner1.attemptToFireAtomicTask(null, "b-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        int i = 0;
        for (; i < btopChildren.size() && i < btop.getMultiInstanceAttributes().getThreshold();
             i++) {
            _netRunner1.startWorkItemInTask(null, (YIdentifier) btopChildren.get(i), "b-top");

            if (i + 1 == btopChildren.size() || i + 1 == btop.getMultiInstanceAttributes().getThreshold()) {
                assertTrue(_netRunner1.completeWorkItemInTask(null, null, (YIdentifier) btopChildren.get(i), "b-top", _d));
            } else {
                assertFalse(_netRunner1.completeWorkItemInTask(null, null, (YIdentifier) btopChildren.get(i), "b-top", _d));
//System.out.println("i " + i + " childrensize  " + btopChildren.size());
            }
        }
        if (i < btopChildren.size()) {
//System.out.println("got here");
            Exception f = null;
            try {
                _netRunner1.completeWorkItemInTask(null, null, (YIdentifier) btopChildren.get(i), "b-top", _d);
            } catch (Exception e) {
                f = e;
            }
            assertNotNull(f);
        }
        assertTrue("locations (should be one or zero in here): " +_id1.getLocations(),
                _id1.getLocations().size() == 1
                ||
                _id1.getLocations().size() == 0);
/*
        synchronized (_netRunner1) {
            notify();
        }
*/
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(_netRunner1.isAlive());
    }


    public void testAddInstance() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        List children = null;
        try {
            children = _netRunner1.attemptToFireAtomicTask(null, "a-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        _netRunner1.startWorkItemInTask(null,(YIdentifier) children.get(0), "a-top");
        _netRunner1.completeWorkItemInTask(null,null,(YIdentifier) children.get(0), "a-top", _d);
        try {
            children = _netRunner1.attemptToFireAtomicTask(null, "b-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        } catch (YStateException e) {
            e.printStackTrace();
        }
        YIdentifier extraID = _netRunner1.addNewInstance(null, "b-top", null, null);
        assertNull(extraID);
        YIdentifier id = (YIdentifier)children.iterator().next();
        _netRunner1.startWorkItemInTask(null, id, "b-top");
        extraID = _netRunner1.addNewInstance(null, "b-top", id, new Element("stub"));
        assertTrue(children.size() == 7 || extraID.getParent().equals(id.getParent()));
    }


    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYNetRunner.class);
        return suite;
    }
    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
