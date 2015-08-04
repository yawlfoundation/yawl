/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 17/04/2003
 * Time: 14:41:14
 * 
 */
public class ExceptionTestSuite extends TestSuite{
    public ExceptionTestSuite(String name)
    {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYConnectivityException.class);
        suite.addTestSuite(TestYSyntaxException.class);

        return suite;
    }
}
