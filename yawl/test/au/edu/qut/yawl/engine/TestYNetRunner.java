/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.Document;
import org.jdom.Element;

import au.edu.qut.yawl.elements.TestYAtomicTask;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
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
import au.edu.qut.yawl.util.SpecReader;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 4/06/2003
 * Time: 17:08:14
 * 
 */
public class TestYNetRunner extends AbstractTransactionalTestCase {
    private YNetRunner _netRunner1;
    private YIdentifier _id1;
    private YIdentifier _id2;
    
    private Document _d;
    private AbstractEngine _engine;

    public TestYNetRunner(String name) {
        super(name);
    }


    public void setUp() throws Exception {
    	super.setUp();
        URL fileURL = getClass().getResource("YAWL_Specification2.xml");
        File yawlXMLFile1 = new File(fileURL.getFile());
        YSpecification specification;
        specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(yawlXMLFile1.getAbsolutePath()).get(0);
        _engine = EngineFactory.createYEngine();
        EngineClearer.clear(_engine);
        _engine.addSpecifications(yawlXMLFile1, false, new ArrayList());
        _id1 = _engine.startCase(null, specification.getID(), null, null);
        _netRunner1 = getYNetRunner(_engine, _id1);
        _d = new Document();
        _d.setRootElement(new Element("data"));
    }

    public static YNetRunner getYNetRunner(AbstractEngine engine, YIdentifier id) throws YPersistenceException {
        return engine.getNetRunner(id);
    }

    private YWorkItem getWorkItem( YNetRunner runner, String taskID ) throws YPersistenceException {
    	return _engine.getWorkItem( runner.getCaseID() + ":" + taskID );
    }

    public void testBasicFireAtomic() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        assertTrue(_netRunner1.getEnabledTasks().contains(_netRunner1.getNetElement("a-top")));
        assertTrue(_netRunner1.getEnabledTasks().size() == 1);
        List children = null;
        children = _netRunner1.attemptToFireAtomicTask("b-top");
        assertTrue(children == null);
        children = _netRunner1.attemptToFireAtomicTask("a-top");
        assertFalse(((YCondition) _netRunner1.getNetElement("i-top"))
                .containsIdentifier());
        assertTrue(children.size() == 1);
        assertTrue(_netRunner1.isAlive());
        assertTrue(_netRunner1.getEnabledTasks().size() == 0);
        assertTrue(_netRunner1.getBusyTasks().size() == 1);
        _netRunner1.startWorkItemInTask((YIdentifier) children.get(0), "a-top");
        assertTrue(
        		_netRunner1.completeWorkItemInTask(
        				getWorkItem( _netRunner1, "a-top" ),
        				(YIdentifier) children.get(0),
        				"a-top",
        				_d));
        YCondition anonC = ((YCondition) _netRunner1.getNetElement(
                "c(a-top_b-top)"));
        assertTrue(anonC.contains(_id1));
//        assertTrue(_id1.getLocations().contains(anonC));
        assertTrue(((YTask) _netRunner1._net.getNetElement("b-top")).t_enabled(null));
        assertTrue(_netRunner1.isAlive());
//        assertTrue("" + _id1.getLocations(), _netRunner1.getEnabledTasks().size() == 1);
        YAtomicTask btop = (YAtomicTask) _netRunner1.getNetElement("b-top");
        List btopChildren = null;
        btopChildren = _netRunner1.attemptToFireAtomicTask("b-top");
        int i = 0;
        for (; i < btopChildren.size() && i < btop.getMultiInstanceAttributes().getThreshold();
             i++) {
            _netRunner1.startWorkItemInTask((YIdentifier) btopChildren.get(i), "b-top");

            if (i + 1 == btopChildren.size() || i + 1 == btop.getMultiInstanceAttributes().getThreshold()) {
                assertTrue(_netRunner1.completeWorkItemInTask(
                		getWorkItem( _netRunner1, "b-top" ),
                		(YIdentifier) btopChildren.get(i),
                		"b-top",
                		_d));
            } else {
                assertFalse(_netRunner1.completeWorkItemInTask(
                		getWorkItem( _netRunner1, "b-top" ),
                		(YIdentifier) btopChildren.get(i),
                		"b-top",
                		_d));
//System.out.println("i " + i + " childrensize  " + btopChildren.size());
            }
        }
        if (i < btopChildren.size()) {
//System.out.println("got here");
            try {
                _netRunner1.completeWorkItemInTask(
                		getWorkItem( _netRunner1, "b-top" ),
                		(YIdentifier) btopChildren.get(i),
                		"b-top",
                		_d);
                fail("An exception should have been thrown");
            } catch (YStateException e) {
                // exception was supposed to be thrown
            }
        }
//        assertTrue("locations (should be one or zero in here): " +_id1.getLocations(),
//                _id1.getLocations().size() == 1
//                ||
//                _id1.getLocations().size() == 0);
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
        children = _netRunner1.attemptToFireAtomicTask("a-top");
        _netRunner1.startWorkItemInTask((YIdentifier) children.get(0), "a-top");
        _netRunner1.completeWorkItemInTask(
        		getWorkItem( _netRunner1, "a-top" ),
        		(YIdentifier) children.get(0),
        		"a-top",
        		_d);
        try {
            children = _netRunner1.attemptToFireAtomicTask("b-top");
        } catch (YStateException e) {
            // XXX is a state exception permissible here?
            e.printStackTrace();
        }
        YIdentifier extraID = _netRunner1.addNewInstance("b-top", null, null);
        assertNull(extraID);
        YIdentifier id = (YIdentifier)children.iterator().next();
        _netRunner1.startWorkItemInTask(id, "b-top");
        extraID = _netRunner1.addNewInstance("b-top", id, new Element("stub"));
        assertTrue(children.size() == 7 || extraID.getParent().equals(id.getParent()));
    }

    public void testCancelYawlServiceSpec() throws Exception {
        String fileName = "TestCancelYawlServiceSpec.xml";
        boolean isXmlFileInPackage = true;

        YSpecification spec = null;
        YNet root = null;

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
        String idString = engine2.launchCase("SYSTEM", spec.getID(), null, null);
        YNetRunner netRunner1 = TestYNetRunner.getYNetRunner(engine2, new YIdentifier(idString));
        Document d = new Document();
        d.setRootElement(new Element("data"));

        List children = netRunner1.attemptToFireAtomicTask("decideName");
        assertFalse(spec.getRootNet().getInputCondition().containsIdentifier());

        netRunner1.cancel();

        assertFalse(task.t_isBusy());
    }

    public void testCompletionOfDecomposedSpec() {
        try {
        	URL decomposedSpec = getClass().getResource("andrewserror.xml");
        File yawlXMLFile2 = new File(decomposedSpec.getFile());
        YSpecification decspec = (YSpecification) YMarshal.
        		unmarshalSpecifications(yawlXMLFile2.getAbsolutePath()).get(0);
        
        _engine.addSpecifications(yawlXMLFile2, false, new ArrayList());
        _id2 = _engine.startCase(null,decspec.getID(),null,null);
        java.util.Set s = _engine.getAllWorkItems();
        java.util.Iterator it = s.iterator();
        while (it.hasNext()) {
        	YWorkItem work = (YWorkItem) it.next();
        	System.out.println(work.getIDString());
        }
		YWorkItem workItem = _engine.getWorkItem(_id2.toString() + ".1.1:ChildC_12");
		
		_engine.startWorkItem(workItem.getIDString(), "tore");
		//_engine.completeWorkItem(workItem, null, true);
        
		s = YWorkItemRepository.getInstance().getExecutingWorkItems();	
		it = s.iterator();
        while (it.hasNext()) {
        	YWorkItem work = (YWorkItem) it.next();
        	_engine.completeWorkItem(work.getIDString(), "<ChildC></ChildC>", true);
        	System.out.println(work.getIDString());
        }
		System.out.println(_engine.getStateForCase(_id2.toString()));
        } catch (Exception e) {
        	e.printStackTrace();
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
