package org.yawlfoundation.yawl.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 9/05/2003
 * Time: 16:00:43
 * 
 */
public class UtilTestSuite extends TestSuite{
    public UtilTestSuite(String name){
        super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
        return suite;
    }
}
