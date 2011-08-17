package org.yawlfoundation.yawl.authentication;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class AuthenticationTestSuite extends TestSuite{

	/**
	 * Constructor for AuthenticationTestSuite.
	 * @param name
	 */
    public AuthenticationTestSuite(String name){
    	super(name);
    }


	/**
	 * Adds all of the testing classes to the suite to run all tests.
	 */
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestConnections.class);
        //return
        return suite;
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
