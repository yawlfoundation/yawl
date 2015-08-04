package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:13:27
 * 
 */
public class TestYInputCondition extends TestCase{
    private YInputCondition _invalidInputCondition;

    public TestYInputCondition(String name){
        super(name);
    }

    public void setUp(){
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);

        YNet deadNet = new YNet("aNetName", spec);
        _invalidInputCondition = new YInputCondition("ic1", "input", deadNet);
        _invalidInputCondition.addPostset(new YFlow(_invalidInputCondition, new YCondition("c2", deadNet)));
        _invalidInputCondition.addPreset(new YFlow(new YAtomicTask("at1", YTask._AND, YTask._AND, deadNet), _invalidInputCondition));
    }

    public void testInvalidInputCondition(){
        YVerificationHandler handler = new YVerificationHandler();
        _invalidInputCondition.verify(handler);
        if(handler.getMessageCount() != 2) {
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail("Should receive 2 error messages, but didn't.");
        }
    }
}
