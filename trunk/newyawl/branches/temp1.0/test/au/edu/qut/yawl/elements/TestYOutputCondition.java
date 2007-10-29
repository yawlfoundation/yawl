/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.util.YMessagePrinter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.List;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:14:55
 * 
 */
public class TestYOutputCondition extends TestCase{

    private YOutputCondition _invalidOutputCondition;

    public TestYOutputCondition(String name){
        super(name);
    }


    public void setUp(){
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSpecification._Beta2);
        YNet deadNet = new YNet("aNetName", spec);
        _invalidOutputCondition = new YOutputCondition("ic1", "input", deadNet);
        _invalidOutputCondition.setPostset(new YFlow(_invalidOutputCondition, new YCondition("c2", deadNet)));
        _invalidOutputCondition.setPostset(new YFlow(_invalidOutputCondition, new YAtomicTask("at1", YTask._AND, YTask._AND, deadNet)));
    }


    public void testInvalidInputCondition(){
        List messages = _invalidOutputCondition.verify();
        /*
            OutputCondition:ic1 postset must be empty: [YCondition:c2, YAtomicTask:at1]
            OutputCondition:ic1 The preset size must be > 0
        */
        if(messages.size() != 2){
            YMessagePrinter.printMessages(messages);
            fail("Should recieve 2 error messages, but didn't ( messages size == " + messages.size());
        }
    }


    public static void main(String args[]){
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }
    public static Test suite(){
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYOutputCondition.class);
        return suite;
    }
}
