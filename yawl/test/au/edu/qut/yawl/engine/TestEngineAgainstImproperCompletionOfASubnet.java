/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YSpecification;
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
import java.util.ArrayList;

import org.jdom.JDOMException;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 23/07/2003
 * Time: 12:24:13
 * 
 */
public class TestEngineAgainstImproperCompletionOfASubnet extends TestCase {

    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private long _sleepTime = 500;
    private AbstractEngine engine;
    private YSpecification _specification;
    private File yawlXMLFile;


    public TestEngineAgainstImproperCompletionOfASubnet(String name) {
        super(name);
    }

    public void setUp() throws YSyntaxException, JDOMException, YSchemaBuildingException, IOException, YPersistenceException {
        URL fileURL = getClass().getResource("ImproperCompletion.xml");
        yawlXMLFile = new File(fileURL.getFile());
        _specification = null;
        _specification = (YSpecification) YMarshal.
                unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        engine =  EngineFactory.createYEngine();
    }


    public synchronized void testImproperCompletionSubnet() throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException, IOException, JDOMException {
        EngineClearer.clear(engine);
        engine.addSpecifications(yawlXMLFile, false, new ArrayList());
        engine.startCase(null, _specification.getID(), null, null);
        assertTrue(_workItemRepository.getCompletedWorkItems().size() == 0);
        assertTrue(_workItemRepository.getEnabledWorkItems().size() == 1);
        assertTrue(_workItemRepository.getExecutingWorkItems().size() == 0);
        assertTrue(_workItemRepository.getFiredWorkItems().size() == 0);
        while (_workItemRepository.getEnabledWorkItems().size() > 0 ||
                _workItemRepository.getFiredWorkItems().size() > 0 ||
                _workItemRepository.getExecutingWorkItems().size() > 0) {
            YWorkItem item;
            while (_workItemRepository.getEnabledWorkItems().size() > 0) {
                item = (YWorkItem) _workItemRepository.getEnabledWorkItems().iterator().next();
                engine.startWorkItem(item, "admin");
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            while (_workItemRepository.getFiredWorkItems().size() > 0) {
                item = (YWorkItem) _workItemRepository.getFiredWorkItems().iterator().next();
                engine.startWorkItem(item, "admin");
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            while (_workItemRepository.getExecutingWorkItems().size() > 0) {
                item = (YWorkItem) _workItemRepository.getExecutingWorkItems("admin")
                        .iterator().next();
                engine.completeWorkItem(item, "<data/>", false);
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
        }
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }


    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestEngineAgainstImproperCompletionOfASubnet.class);
        return suite;
    }
}
