package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;

/**
 * @author aldredl
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestYMultiInstanceAttributes extends TestCase{
    private YTask _aTask;
    private YMultiInstanceAttributes _validMultiInstance;
    private YMultiInstanceAttributes _invalidMultiInstanceAttributes;


    public void setUp(){
        YSpecification spec = new YSpecification("");
        spec.setVersion(YSchemaVersion.Beta2);

        YNet deadNet = new YNet("aNet", spec);
        _aTask = new YAtomicTask("et1", YTask._AND, YTask._OR, deadNet);
        _validMultiInstance = new YMultiInstanceAttributes(_aTask, "1", "10", "15", "static");
        _invalidMultiInstanceAttributes = new YMultiInstanceAttributes(_aTask, "0", "-1", "-1", "dynhrgs");
    }


    public void testValidStuff(){
        YVerificationHandler handler = new YVerificationHandler();
        _validMultiInstance.verify(handler);
        assertEquals(this._validMultiInstance.getMinInstances(), 1);
        assertEquals(this._validMultiInstance.getMaxInstances(), 10);
        assertEquals(this._validMultiInstance.getThreshold(), 15);
        assertEquals(this._validMultiInstance.getCreationMode(), "static");
        if(handler.hasMessages()){
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail(handler.getMessages().get(0).getMessage());
        }
    }


    public void testInvalidVerify(){
        YVerificationHandler handler = new YVerificationHandler();
        _invalidMultiInstanceAttributes.verify(handler);
        if(handler.getMessageCount() != 5){
            /*
                YAtomicTask:et1 _minInstances > 1
                YAtomicTask:et1._minInstances > _maxInstances
                YAtomicTask:et1._maxInstances < 1
                YAtomicTask:et1._threshold < 1
                YAtomicTask:et1._creationMode does not equal 'static' or 'dynamic'
            */
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail(handler.getMessages().get(0).getMessage());
        }
    }
}
