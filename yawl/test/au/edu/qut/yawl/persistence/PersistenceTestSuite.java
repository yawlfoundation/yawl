/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import au.edu.qut.yawl.persistence.dao.DAOPersistenceTestSuite;
import au.edu.qut.yawl.persistence.engine.EngineTestSuite;
import au.edu.qut.yawl.persistence.managed.TestDataContext;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class PersistenceTestSuite extends TestSuite{

	/**
	 * Constructor for AuthenticationTestSuite.
	 * @param name
	 */
    public PersistenceTestSuite(String name){
    	super(name);
    }


	/**
	 * Adds all of the testing classes to the suite to run all tests.
	 */
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTest(DAOPersistenceTestSuite.suite());
        suite.addTest(EngineTestSuite.suite());
        suite.addTestSuite(TestDataParsing.class);
        suite.addTestSuite(TestYCompositeTask.class);
        suite.addTestSuite(TestYExternalTask.class);
        suite.addTestSuite(TestYNet.class);
        suite.addTestSuite(TestYSpecification.class);
        suite.addTestSuite(TestHibernateMarshal.class);
        suite.addTestSuite(TestDataContext.class);
        //suite.addTestSuite(TestJaxbMarshal.class);
        //return
        return suite;
    }

    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
}
