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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class TestPersistence extends TestCase {
//    private YIdentifier _idForTopNet;
//    private YEngine engine;
//    private YSpecification specification;
//
//    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, YStateException, YPersistenceException {
//        URL fileURL = getClass().getResource("test.xml");
//        File yawlXMLFile = new File(fileURL.getFile());
//        specification = (YSpecification) YMarshal.
//                    unmarshalSpecifications(yawlXMLFile.getAbsolutePath()).get(0);
//        engine = YEngine.getInstance(true);
//        engine.loadSpecification(specification);
//        
//    }
//
//    public void testStore() throws InterruptedException, YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
//        YPersistenceManager pmgr = new YPersistenceManager(YEngine.getPMSessionFactory());
//        pmgr.startTransactionalSession();
//        pmgr.storeObject(specification);
//        pmgr.commit();
//    }
//
//
//
//    public static void main(String args[]) {
//        TestRunner runner = new TestRunner();
//        runner.doRun(suite());
//        System.exit(0);
//    }
//
//    public static Test suite() {
//        TestSuite suite = new TestSuite();
//        suite.addTestSuite(TestPersistence.class);
//        return suite;
//    }
}
