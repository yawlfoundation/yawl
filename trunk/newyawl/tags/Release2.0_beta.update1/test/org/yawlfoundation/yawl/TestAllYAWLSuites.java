/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl;

import org.yawlfoundation.yawl.admintool.AdminToolTestSuite;
import org.yawlfoundation.yawl.authentication.AuthenticationTestSuite;
import org.yawlfoundation.yawl.elements.ElementsTestSuite;
import org.yawlfoundation.yawl.elements.state.StateTestSuite;
import org.yawlfoundation.yawl.engine.EngineTestSuite;
import org.yawlfoundation.yawl.exceptions.ExceptionTestSuite;
import org.yawlfoundation.yawl.logging.LoggingTestSuite;
import org.yawlfoundation.yawl.schema.SchemaTestSuite;
import org.yawlfoundation.yawl.unmarshal.UnmarshallerTestSuite;
import org.yawlfoundation.yawl.util.UtilTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

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
        suite.addTest(AdminToolTestSuite.suite());
        suite.addTest(ElementsTestSuite.suite());
        suite.addTest(StateTestSuite.suite());
        suite.addTest(EngineTestSuite.suite());
        suite.addTest(ExceptionTestSuite.suite());
        suite.addTest(LoggingTestSuite.suite());
        suite.addTest(SchemaTestSuite.suite());
        suite.addTest(UnmarshallerTestSuite.suite());
        suite.addTest(UtilTestSuite.suite());
        suite.addTest(org.yawlfoundation.yawl.swingWorklist.WorklistTestSuite.suite());
        suite.addTest(org.yawlfoundation.yawl.worklist.WorklistTestSuite.suite());
        suite.addTest(AuthenticationTestSuite.suite());
        return suite;
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
