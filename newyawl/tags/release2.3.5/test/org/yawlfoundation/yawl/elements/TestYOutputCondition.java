package org.yawlfoundation.yawl.elements;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.YVerificationHandler;

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
        spec.setVersion(YSchemaVersion.Beta2);
        YNet deadNet = new YNet("aNetName", spec);
        _invalidOutputCondition = new YOutputCondition("ic1", "input", deadNet);
        _invalidOutputCondition.addPostset(new YFlow(_invalidOutputCondition, new YCondition("c2", deadNet)));
        _invalidOutputCondition.addPostset(new YFlow(_invalidOutputCondition, new YAtomicTask("at1", YTask._AND, YTask._AND, deadNet)));
    }


    public void testInvalidInputCondition(){
        YVerificationHandler handler = new YVerificationHandler();
        _invalidOutputCondition.verify(handler);
        /*
            OutputCondition:ic1 postset must be empty: [YCondition:c2, YAtomicTask:at1]
            OutputCondition:ic1 The preset size must be > 0
        */
        if(handler.getMessageCount() != 2){
            fail("Should receive 2 error messages, but didn't ( messages size == " + handler.getMessageCount());
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
