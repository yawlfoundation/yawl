/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.events;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import au.edu.qut.yawl.engine.domain.TestYWorkItemID;
import au.edu.qut.yawl.engine.domain.TestYWorkItemRepository;
import au.edu.qut.yawl.engine.interfce.TestEngineGateway;
import au.edu.qut.yawl.engine.interfce.TestEngineGatewaySpecifications;
import au.edu.qut.yawl.engine.interfce.TestEngineGatewayUserFunctionality;
import au.edu.qut.yawl.engine.interfce.TestEngineGatewayYAWLServices;

/**
 * 
 * Author: Matthew Sandoz
 * Date: 10/25/2006
 * 
 */
public class EventsTestSuite extends TestCase{
    public EventsTestSuite(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(HsqlTest.class);
        suite.addTestSuite(JmsProviderTest.class);
        return suite;
    }

    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
