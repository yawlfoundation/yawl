/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


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
