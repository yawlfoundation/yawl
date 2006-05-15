/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.TestYAtomicTask;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.SpecReader;
import au.edu.qut.yawl.exceptions.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
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


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YStateException, YPersistenceException, YDataStateException {
        URL fileURL = getClass().getResource("YAWL_Specification2.xml");
        File yawlXMLFile1 = new File(fileURL.getFile());
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile1.getAbsolutePath()).get(0);
        AbstractEngine engine2 = EngineFactory.createYEngine();
        EngineClearer.clear(engine2);
        engine2.loadSpecification(specification);
        _id1 = engine2.startCase(specification.getID(), null, null);
        _netRunner1 = getYNetRunner(engine2, _id1);
        _d = new Document();
        _d.setRootElement(new Element("data"));
    }
    
    public static YNetRunner getYNetRunner(AbstractEngine engine, YIdentifier id) {
    	return (YNetRunner) engine._caseIDToNetRunnerMap.get(id);
    }


    public void testBasicFireAtomic() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        assertTrue(_netRunner1.getEnabledTasks().contains(_netRunner1.getNetElement("a-top")));
        assertTrue(_netRunner1.getEnabledTasks().size() == 1);
        List children = null;
        try {
            children = _netRunner1.attemptToFireAtomicTask("b-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(children == null);
        try {
            children = _netRunner1.attemptToFireAtomicTask("a-top");
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
        _netRunner1.startWorkItemInTask((YIdentifier) children.get(0), "a-top");
        assertTrue(_netRunner1.completeWorkItemInTask(null, (YIdentifier) children.get(0), "a-top",_d));
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
            btopChildren = _netRunner1.attemptToFireAtomicTask("b-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        int i = 0;
        for (; i < btopChildren.size() && i < btop.getMultiInstanceAttributes().getThreshold();
             i++) {
            _netRunner1.startWorkItemInTask((YIdentifier) btopChildren.get(i), "b-top");

            if (i + 1 == btopChildren.size() || i + 1 == btop.getMultiInstanceAttributes().getThreshold()) {
                assertTrue(_netRunner1.completeWorkItemInTask(null, (YIdentifier) btopChildren.get(i), "b-top", _d));
            } else {
                assertFalse(_netRunner1.completeWorkItemInTask(null, (YIdentifier) btopChildren.get(i), "b-top", _d));
//System.out.println("i " + i + " childrensize  " + btopChildren.size());
            }
        }
        if (i < btopChildren.size()) {
//System.out.println("got here");
            Exception f = null;
            try {
                _netRunner1.completeWorkItemInTask(null, (YIdentifier) btopChildren.get(i), "b-top", _d);
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
            children = _netRunner1.attemptToFireAtomicTask("a-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        }
        _netRunner1.startWorkItemInTask((YIdentifier) children.get(0), "a-top");
        _netRunner1.completeWorkItemInTask(null,(YIdentifier) children.get(0), "a-top", _d);
        try {
            children = _netRunner1.attemptToFireAtomicTask("b-top");
        } catch (YDataStateException e) {
            e.printStackTrace();
            fail();
        } catch (YStateException e) {
            e.printStackTrace();
        }
        YIdentifier extraID = _netRunner1.addNewInstance("b-top", null, null);
        assertNull(extraID);
        YIdentifier id = (YIdentifier)children.iterator().next();
        _netRunner1.startWorkItemInTask(id, "b-top");
        extraID = _netRunner1.addNewInstance("b-top", id, new Element("stub"));
        assertTrue(children.size() == 7 || extraID.getParent().equals(id.getParent()));
    }
    
    public void testCancelYawlServiceSpec() {
    	String fileName = "TestCancelYawlServiceSpec.xml";
    	boolean isXmlFileInPackage = true;
    	
    	YSpecification spec = null;
    	YNet root = null;
    	
    	try {
    		spec = SpecReader.readSpecification( fileName, isXmlFileInPackage, TestYAtomicTask.class );
    		List<YDecomposition> decomps = spec.getDecompositions();
    		
    		for(int index = 0; index < decomps.size(); index++) {
    			YDecomposition temp = decomps.get(index);
    			if( "OverseeMusic".equals( temp.getId() ) ) {
    				assert temp instanceof YNet : "decomposition 'OverseeMusic' should be a net!";
    				root = (YNet) temp;
    			}
    		}
    		
    		if( root == null ) {
    			fail( fileName + " should have a net called 'OverseeMusic'" );
    		}
    		
    		YAtomicTask task = (YAtomicTask) root.getInputCondition().getPostsetElements().get(0);
    		
    		AbstractEngine engine2 = EngineFactory.createYEngine();
            EngineClearer.clear(engine2);
            engine2.loadSpecification(spec);
            String idString = engine2.launchCase(spec.getID(), null, null);
            YNetRunner netRunner1 = TestYNetRunner.getYNetRunner(engine2, new YIdentifier(idString));
            Document d = new Document();
            d.setRootElement(new Element("data"));
    		
    		List children = netRunner1.attemptToFireAtomicTask("decideName");
    		assertFalse(spec.getRootNet().getInputCondition().containsIdentifier());
    		
    		netRunner1.cancel();
    		
            assertFalse(task.t_isBusy());
    	}
    	catch(Exception e) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
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
