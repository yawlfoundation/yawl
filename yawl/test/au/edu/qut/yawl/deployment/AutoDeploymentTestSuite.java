/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.deployment;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * 
 * @author Lachlan Aldred
 * Date: 20/10/2005
 * Time: 10:10:08
 * 
 */
public class AutoDeploymentTestSuite extends TestSuite{

    public AutoDeploymentTestSuite(String name) {
        super(name);
    }


	/**
	 * Adds all of the testing classes to the suite to run all tests.
	 */
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestAutoDeployment.class);
        //return
        return suite;
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }



}
