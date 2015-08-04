package org.yawlfoundation.yawl.exceptions;

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
