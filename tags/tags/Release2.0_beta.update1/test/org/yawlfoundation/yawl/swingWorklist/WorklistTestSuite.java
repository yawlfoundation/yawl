/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.swingWorklist;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 9/05/2003
 * Time: 16:00:43
 * 
 */
public class WorklistTestSuite extends TestSuite{
    public WorklistTestSuite(String name){
        super(name);
    }


    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestWorklistTableModel.class);
        suite.addTestSuite(TestYWorkAvailablePanel.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());

    }
}
