package org.yawlfoundation.yawl.logging;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.yawlfoundation.yawl.engine.YEngine;

public class YawlServletTestNextIdNew extends TestCase {

    public YawlServletTestNextIdNew(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testNextId() {
        YEventLogger yawllog = YEventLogger.getInstance();

        String x = YEngine.getInstance().getNextCaseNbr(null);

        String y = YEngine.getInstance().getNextCaseNbr(null);

        String z = YEngine.getInstance().getNextCaseNbr(null);

//        System.out.println("HERE: " + x + " " + y + " " + z);

        int xd = new Integer(x).intValue();

        int yd = new Integer(y).intValue();
        int delta = yd - xd;

        if (delta != 1) {
            fail();
        }

        int zd = new Integer(z).intValue();

        delta = zd - yd;

        if (delta != 1) {
            fail();
        }

    }


    public static Test suite() {

        TestSuite suite = new TestSuite(YawlServletTestNextIdNew.class);

        return suite;
    }

    /**
     * Runs the test case.
     */
    public static void main(String args[]) {

        junit.textui.TestRunner.run(suite());
    }

}
