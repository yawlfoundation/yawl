/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.schema;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 6/08/2004
 * Time: 16:47:21
 * 
 */
public class SchemaTestSuite extends TestSuite{
    public SchemaTestSuite(String name)
    {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestXSD4YAWLBuilder.class);
        suite.addTestSuite(TestXMLToolsForYAWL.class);
        return suite;
    }
}