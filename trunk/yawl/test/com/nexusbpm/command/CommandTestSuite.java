/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * 
 * @author Dean Mao
 * @created Aug 4, 2006
 */
public class CommandTestSuite extends TestSuite{
	/**
	 * Constructor for NexusCommandTestSuite.
	 * @param name
	 */
    public CommandTestSuite(String name){
    	super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CreateSpecificationTest.class);
        suite.addTestSuite(CopyNetTest.class);
        suite.addTestSuite(RenameElementTest.class);
        suite.addTestSuite(CreateFlowTest.class);
        return suite;
    }

    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
    }
}
