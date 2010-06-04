/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.unmarshal;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * 
 * Author: Lachlan Aldred
 * Date: 9/05/2003
 * Time: 15:43:58
 * This class is the property of the "YAWL Project" - a collaborative
 * research effort between Queensland University of Technology and 
 * Eindhoven University of Technology.  You are not permitted to use, modify
 * or distribute this code without the express permission of a core
 * member of the YAWL Project. 
 * This class is not "open source".  This class is not for resale,
 * or commercial application.   It is intented for research purposes only.
 * The YAWL Project or it's members will not be held liable for any damage
 * occuring as a _errorsString of using this class.
 */
public class UnmarshallerTestSuite extends TestSuite {
    public UnmarshallerTestSuite(String name){
        super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestMetaDataMarshal.class);
//        suite.addTestSuite(TestYMarshal.class);
        suite.addTestSuite(TestYMarshalB4.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
    }
}
