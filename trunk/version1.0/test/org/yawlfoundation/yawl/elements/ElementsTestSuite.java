/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

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
public class ElementsTestSuite extends TestSuite{

	/**
	 * Constructor for AuthenticationTestSuite.
	 * @param name
	 */
    public ElementsTestSuite(String name){
    	super(name);
    }


	/**
	 * Adds all of the testing classes to the suite to run all tests.
	 */
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestDataParsing.class);
        suite.addTestSuite(TestYAtomicTask.class);
        suite.addTestSuite(TestYCompositeTask.class);
        suite.addTestSuite(TestYExternalCondition.class);
        suite.addTestSuite(TestYExternalNetElement.class);
        suite.addTestSuite(TestYExternalTask.class);
        suite.addTestSuite(TestYFlowsInto.class);
        suite.addTestSuite(TestYInputCondition.class);
        suite.addTestSuite(TestYMultiInstanceAttributes.class);
        suite.addTestSuite(TestYNet.class);
        suite.addTestSuite(TestYNetElement.class);
        suite.addTestSuite(TestYOutputCondition.class);
        suite.addTestSuite(TestYSpecification.class);
        //return
        return suite;
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
