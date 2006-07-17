/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import au.edu.qut.yawl.authentication.AuthenticationTestSuite;
import au.edu.qut.yawl.elements.ElementsTestSuite;
import au.edu.qut.yawl.elements.data.DataTestSuite;
import au.edu.qut.yawl.elements.state.StateTestSuite;
import au.edu.qut.yawl.engine.EngineTestSuite;
import au.edu.qut.yawl.exceptions.ExceptionTestSuite;
import au.edu.qut.yawl.logging.LoggingTestSuite;
import au.edu.qut.yawl.persistence.PersistenceTestSuite;
import au.edu.qut.yawl.schema.SchemaTestSuite;
import au.edu.qut.yawl.unmarshal.UnmarshallerTestSuite;
import au.edu.qut.yawl.util.UtilTestSuite;

import com.nexusbpm.services.NexusServiceTestSuite;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 9/05/2003
 * Time: 15:33:26
 * 
 */
public class TestAllYAWLSuites extends TestSuite{

    public TestAllYAWLSuites(String name){
        super(name);
    }


    public static Test suite(){
        TestSuite suite = new TestSuite();
//        suite.addTest(AdminToolTestSuite.suite());
        suite.addTest(ElementsTestSuite.suite());
        suite.addTest(DataTestSuite.suite());
        suite.addTest(StateTestSuite.suite());
        suite.addTest(EngineTestSuite.suite());
        suite.addTest(ExceptionTestSuite.suite());
        suite.addTest(LoggingTestSuite.suite());
        suite.addTest(PersistenceTestSuite.suite());
        suite.addTest(SchemaTestSuite.suite());
        suite.addTest(UnmarshallerTestSuite.suite());
        suite.addTest(UtilTestSuite.suite());
        suite.addTest(au.edu.qut.yawl.swingWorklist.WorklistTestSuite.suite());
        suite.addTest(au.edu.qut.yawl.worklist.WorklistTestSuite.suite());
        suite.addTest(AuthenticationTestSuite.suite());
        suite.addTest(NexusServiceTestSuite.suite());
        return suite;
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
