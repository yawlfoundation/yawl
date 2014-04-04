package org.yawlfoundation.yawl.resourcing;

import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

/**
 *
 */

public class ResourcingTestSuite extends TestSuite {

    public ResourcingTestSuite(String name){
        super(name);
    }

    public static Test suite(){
        TestSuite suite = new TestSuite();
//        suite.addTestSuite(TestResourceSpecXML.class);
//        suite.addTestSuite(TestGetSelectors.class);
//        suite.addTestSuite(TestHibernate.class);
        suite.addTestSuite(TestDB.class);
//        suite.addTestSuite(TestJDBC.class);
/*        suite.addTestSuite(TestParseXML.class);*/
        return suite;
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
    }
}
