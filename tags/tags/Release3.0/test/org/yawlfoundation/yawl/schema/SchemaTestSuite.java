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
        suite.addTestSuite(TestSchemaHandler.class);
        suite.addTestSuite(TestSchemaHandlerValidation.class);
        return suite;
    }
}