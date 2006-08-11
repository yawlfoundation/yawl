/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package operation;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Nathan Rose
 */
public class OperationTestSuite extends TestSuite{
    public OperationTestSuite(String name){
    	super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestNameAndCounter.class);
        suite.addTestSuite(TestYFlow.class);
        return suite;
    }

    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
    }
}
