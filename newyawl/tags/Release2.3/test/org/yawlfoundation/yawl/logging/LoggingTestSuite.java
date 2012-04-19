package org.yawlfoundation.yawl.logging;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 17/04/2003
 * Time: 14:41:14
 * 
 */
public class LoggingTestSuite extends TestSuite{
    public LoggingTestSuite(String name)
    {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(YawlServletTestNextIdNew.class);
        return suite;
    }
}
