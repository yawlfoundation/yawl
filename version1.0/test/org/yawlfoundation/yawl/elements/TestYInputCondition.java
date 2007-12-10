/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YMessagePrinter;
import junit.framework.TestCase;

import java.util.List;

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
        spec.setVersion(YSpecification._Beta2);

        YNet deadNet = new YNet("aNetName", spec);
        _invalidInputCondition = new YInputCondition("ic1", "input", deadNet);
        _invalidInputCondition.setPostset(new YFlow(_invalidInputCondition, new YCondition("c2", deadNet)));
        _invalidInputCondition.setPreset(new YFlow(new YAtomicTask("at1", YTask._AND, YTask._AND, deadNet), _invalidInputCondition));
    }

    public void testInvalidInputCondition(){
        List messages = _invalidInputCondition.verify();
        if(messages.size() != 2){
            YMessagePrinter.printMessages(messages);
            fail("Should recieve 2 error messages, but didn't.");
        }
    }
}
