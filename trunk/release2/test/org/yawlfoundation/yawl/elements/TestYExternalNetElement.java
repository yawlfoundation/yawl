package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YMessagePrinter;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import junit.framework.TestCase;

import java.util.List;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 28/04/2003
 * Time: 11:12:39
 * 
 */
public class TestYExternalNetElement extends TestCase{
    private YExternalNetElement _aCondition;
    private YExternalNetElement _aTask;

    public TestYExternalNetElement(String name){
        super(name);
    }

    public void setUp(){
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);
        YNet deadNet = new YNet("aNetName", spec);
        _aCondition = new YCondition("c1", deadNet);
        _aTask = new YAtomicTask("et1", YTask._AND, YTask._AND, deadNet);
        YFlow f= new YFlow(_aCondition, _aTask);
        _aCondition.addPostset(f);
        f = new YFlow(_aTask, _aCondition);
        _aTask.addPostset(f);
    }


    public void testFlowStuff(){
        assertTrue(_aTask.getPostsetElement("c1").equals(_aCondition));
        assertTrue(_aCondition.getPostsetElement("et1").equals(_aTask));
        assertTrue(_aTask.getPresetElement("c1").equals(_aCondition));
        assertTrue(_aCondition.getPresetElement("et1").equals(_aTask));
        List messages = _aTask.verify();
        if(messages.size() > 0){
            YMessagePrinter.printMessages(messages);
            fail((String)messages.get(0));
        }
    }
}
