/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.worklist;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/02/2004
 * Time: 14:22:08
 * 
 */
public class WorklistTestSuite extends TestSuite{
    public WorklistTestSuite(String name){
        super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestWorklistController.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
    }
}
