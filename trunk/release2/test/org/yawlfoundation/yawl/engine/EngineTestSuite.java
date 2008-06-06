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

/**
 * 
 * Author: Lachlan Aldred
 * Date: 9/05/2003
 * Time: 15:57:52
 * 
 */
public class EngineTestSuite extends TestCase{
    public EngineTestSuite(String name){
        super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestCaseCancellation.class);
        suite.addTestSuite(TestEngineAgainstABeta4Spec.class);
        suite.addTestSuite(TestEngineAgainstImproperCompletionOfASubnet.class);
        suite.addTestSuite(TestEngineSystem1.class);
        suite.addTestSuite(TestEngineSystem2.class);
        suite.addTestSuite(TestImproperCompletion.class);
        suite.addTestSuite(TestOrJoin.class);
        suite.addTestSuite(TestRestServiceMethods.class);
        suite.addTestSuite(TestSimpleExecutionUseCases.class);
        suite.addTestSuite(TestYNetRunner.class);
        suite.addTestSuite(TestYWorkItem.class);
        suite.addTestSuite(TestYWorkItemID.class);
        suite.addTestSuite(TestYWorkItemRepository.class);
        return suite;
    }

    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
