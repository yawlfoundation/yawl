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
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 23/07/2003
 * Time: 12:24:13
 * 
 */
public class TestEngineAgainstImproperCompletionOfASubnet extends TestCase {

    private YIdentifier _idForTopNet;
    private YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private long _sleepTime = 500;
    private YEngine engine;
    private YSpecification _specification;


    public TestEngineAgainstImproperCompletionOfASubnet(String name) {
        super(name);
    }

    public void setUp() throws YSyntaxException, JDOMException, YSchemaBuildingException, IOException {
//        new YLocalWorklist("Barbara");
        URL fileURL = getClass().getResource("ImproperCompletion.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = null;
        _specification = (YSpecification) YMarshal.
                unmarshalSpecifications(StringUtil.fileToString(yawlXMLFile.getAbsolutePath())).get(0);
        engine = YEngine.getInstance();
    }


    public synchronized void testImproperCompletionSubnet() throws YDataStateException, YEngineStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        EngineClearer.clear(engine);
        engine.loadSpecification(_specification);
        _idForTopNet = engine.startCase(null, null, _specification.getURI(), null, null);
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
//System.out.println("TestEngine::() item = " + item);
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
                item = (YWorkItem) _workItemRepository.getExecutingWorkItems()
                        .iterator().next();
                engine.completeWorkItem(item, "<data/>",false);
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
