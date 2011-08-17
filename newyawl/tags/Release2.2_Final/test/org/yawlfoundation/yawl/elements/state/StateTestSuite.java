package org.yawlfoundation.yawl.elements.state;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 9/05/2003
 * Time: 15:52:04
 * 
 */
public class StateTestSuite extends TestSuite {
    public StateTestSuite(String name){
        super(name);
    }


    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYIdentifier.class);
        suite.addTestSuite(TestYMarking.class);
        suite.addTestSuite(TestYOrJoinUtils.class);
        suite.addTestSuite(TestYSetOfMarkings.class);
        return suite;
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
