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
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.JDOMException;

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
 /**
 * 
 * @author Lachlan Aldred
 * Date: 27/04/2004
 * Time: 14:23:09
 * 
 */
public class TestImproperCompletion extends AbstractTransactionalTestCase{
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private long _sleepTime = 100;
    private AbstractEngine _engine;
    private YIdentifier _id;
    private YSpecification _specification;
    private File yawlXMLFile;

    public TestImproperCompletion(String name){
        super(name);
    }

    public void setUp() throws Exception {
    	super.setUp();
        URL fileURL = getClass().getResource("TestImproperCompletion.xml");
        yawlXMLFile = new File(fileURL.getFile());
        _specification = (YSpecification) YMarshal.
                            unmarshalSpecifications(
                                    yawlXMLFile.getAbsolutePath()).get(0);

        _engine =  EngineFactory.createYEngine();
    }


    public void testImproperCompletion() throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException, IOException, JDOMException {
        _engine.addSpecifications(yawlXMLFile, false , new ArrayList());
        _id = _engine.startCase(null, _specification.getID(), null, null);
        int numIter = 0;
        Set s = _engine.getCasesForSpecification("TestImproperCompletion");

        assertTrue("s = " + s, s.contains(_id));
        while (numIter < 10 && (_workItemRepository.getEnabledWorkItems().size() > 0 ||
                _workItemRepository.getFiredWorkItems().size() > 0 ||
                _workItemRepository.getExecutingWorkItems().size() > 0)) {
            YWorkItem item;
            while (_workItemRepository.getEnabledWorkItems().size() > 0) {
                item = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
                _engine.startWorkItem(item.getIDString(), "admin");
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            while (_workItemRepository.getFiredWorkItems().size() > 0) {
                item = (YWorkItem) _workItemRepository.getFiredWorkItems().iterator().next();
                _engine.startWorkItem(item.getIDString(), "admin");
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            while (_workItemRepository.getExecutingWorkItems().size() > 0) {
                item = (YWorkItem) _workItemRepository.getExecutingWorkItems().iterator().next();
                System.out.println("OK NOW WERE GOING TO COMPLETE WORK ITEM " + item);
                _engine.completeWorkItem(item.getIDString(), "<data/>", false);
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            numIter ++;
        }
        s = _engine.getCasesForSpecification("TestImproperCompletion");
        assertTrue("s = " + s, s.contains(_id));
        _engine.cancelCase(_id);
        s = _engine.getCasesForSpecification("TestImproperCompletion");
        assertFalse("case set " + s + " should not contain id " + _id, s.contains(_id));
    }


   public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestImproperCompletion.class);
        return suite;
    }
}