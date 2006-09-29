/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.logging;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import au.edu.qut.yawl.events.YawlEventLogger;

public class YawlServletTestNextIdNew extends TestCase {

    public YawlServletTestNextIdNew(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testNextId() {
        YawlEventLogger yawllog = YawlEventLogger.getInstance();

        String x = yawllog.getNextCaseId();

        String y = yawllog.getNextCaseId();

        String z = yawllog.getNextCaseId();

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
