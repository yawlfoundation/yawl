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
import java.util.Set;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 27/04/2004
 * Time: 14:23:09
 * 
 */
public class TestImproperCompletion extends TestCase{
    private YWorkItemRepository _workItemRepository;
    private long _sleepTime = 100;
    private YEngine _engine;
    private YIdentifier _id;
    private YSpecification _specification;

    public TestImproperCompletion(String name){
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        URL fileURL = getClass().getResource("TestImproperCompletion.xml");
        File yawlXMLFile = new File(fileURL.getFile());
        _specification = YMarshal.
                            unmarshalSpecifications(StringUtil.fileToString(
                                    yawlXMLFile.getAbsolutePath())).get(0);

        _engine = YEngine.getInstance();
        _workItemRepository = _engine.getWorkItemRepository();
    }

    private String trim(String casesRaw) {
        int begin = casesRaw.indexOf("<caseID>") + 8;
        int end = casesRaw.indexOf("</caseID>");
        if(casesRaw.length() > 12){
            return casesRaw.substring(begin, end);
        }
        else return "";
    }

    public void testImproperCompletion() throws YStateException, YDataStateException,
            YEngineStateException, YQueryException, YSchemaBuildingException,
            YPersistenceException, YLogException {
        EngineClearer.clear(_engine);
        _engine.loadSpecification(_specification);
        _id = _engine.startCase(_specification.getSpecificationID(), null, null, null,
                new YLogDataItemList(), null, false);
           int numIter = 0;
        Set s = _engine.getCasesForSpecification(_specification.getSpecificationID());
        assertTrue("s = " + s, s.contains(_id));
        while (numIter < 10 && (_workItemRepository.getEnabledWorkItems().size() > 0 ||
                _workItemRepository.getFiredWorkItems().size() > 0 ||
                _workItemRepository.getExecutingWorkItems().size() > 0)) {
            YWorkItem item;
            while (_workItemRepository.getEnabledWorkItems().size() > 0) {
                item = _workItemRepository.getEnabledWorkItems().iterator().next();
                _engine.startWorkItem(item, _engine.getExternalClient("admin"));
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            while (_workItemRepository.getFiredWorkItems().size() > 0) {
                item = _workItemRepository.getFiredWorkItems().iterator().next();
                _engine.startWorkItem(item, _engine.getExternalClient("admin"));
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            while (_workItemRepository.getExecutingWorkItems().size() > 0) {
                item = _workItemRepository.getExecutingWorkItems().iterator().next();
                _engine.completeWorkItem(item, "<data/>", null, YEngine.WorkItemCompletion.Normal);
                try{ Thread.sleep(_sleepTime);}
                catch(InterruptedException ie){ie.printStackTrace();}
            }
            numIter ++;
        }
        _engine.cancelCase(_id, null);
        s = _engine.getCasesForSpecification(_specification.getSpecificationID());
//        System.out.println("3: " + _id);
        assertFalse("s = " + s, s.contains(_id));
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
