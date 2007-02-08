/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.AbstractTransactionalTestCase;
import au.edu.qut.yawl.unmarshal.YMarshal;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 29/05/2003
 * Time: 16:19:02
 * 
 */
public class TestSimpleExecutionUseCases extends AbstractTransactionalTestCase {
    private YIdentifier _caseId;
    private YWorkItemRepository _workItemRepository;
    private AbstractEngine _engine;

    public TestSimpleExecutionUseCases(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    	super.setUp();
        URL fileURL = getClass().getResource("ImproperCompletion.xml");
		File yawlXMLFile = new File(fileURL.getFile());
        _workItemRepository = YWorkItemRepository.getInstance();
        YSpecification specification = null;
        specification = (YSpecification) YMarshal.
                        unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
        _engine =  EngineFactory.createYEngine();
        _engine.addSpecifications(yawlXMLFile, false, new ArrayList());
        _caseId = _engine.startCase(null, specification.getID(), null, null);
    }


    public void testUseCase1() throws YPersistenceException {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//System.out.println("caseid: " + _caseId);
        YWorkItem item = _engine.getWorkItem(
                _caseId.toString() +
                ":" +
                "b-top");
        assertNull("The work item for b-top should not be available", item);
//        Exception f = null;
//        try {
//            _engine.startWorkItem(item.getIDString() ,"admin");
//        } catch (YAWLException e) {
//            f =e;
//        }
//        assertNotNull(f);

        item = _engine.getWorkItem(
                _caseId.toString() +
                ":" +
                "a-top");
//        f = null;
        try {
            _engine.startWorkItem(item.getIDString() ,"admin");
        } catch (YAWLException e) {
        	StringWriter sw = new StringWriter();
        	PrintWriter pw = new PrintWriter( sw );
        	e.printStackTrace( pw );
            fail( sw.toString() );
        }


        Set firedWorkItems = _workItemRepository.getFiredWorkItems();
        item = (YWorkItem) firedWorkItems.iterator().next();
        try {
            _engine.startWorkItem(item.getIDString(), "admin");
        } catch (YAWLException e) {
            fail(e.getMessage());
        }
//        assertTrue(_localWorklist.startOneWorkItemAndSetOthersToFired(item.getCaseID().toString(), "a-top"));
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestSimpleExecutionUseCases.class);
        return suite;
    }
}
