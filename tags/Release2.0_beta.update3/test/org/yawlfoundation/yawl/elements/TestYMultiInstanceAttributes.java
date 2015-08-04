/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YMessagePrinter;
import org.yawlfoundation.yawl.util.YVerificationMessage;
import junit.framework.TestCase;

import java.util.List;

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
        spec.setVersion(YSpecification._Beta2);

        YNet deadNet = new YNet("aNet", spec);
        _aTask = new YAtomicTask("et1", YTask._AND, YTask._OR, deadNet);
        _validMultiInstance = new YMultiInstanceAttributes(_aTask, "1", "10", "15", "static");
        _invalidMultiInstanceAttributes = new YMultiInstanceAttributes(_aTask, "0", "-1", "-1", "dynhrgs");
    }


    public void testValidStuff(){
        List messages = this._validMultiInstance.verify();
        assertEquals(this._validMultiInstance.getMinInstances(), 1);
        assertEquals(this._validMultiInstance.getMaxInstances(), 10);
        assertEquals(this._validMultiInstance.getThreshold(), 15);
        assertEquals(this._validMultiInstance.getCreationMode(), "static");
        if(messages.size() > 0){
            YMessagePrinter.printMessages(messages);
            fail(((YVerificationMessage)messages.get(0)).getMessage());
        }
    }


    public void testInvalidVerify(){
        List messages = this._invalidMultiInstanceAttributes.verify();
        if(messages.size() != 5){
            /*
                YAtomicTask:et1 _minInstances > 1
                YAtomicTask:et1._minInstances > _maxInstances
                YAtomicTask:et1._maxInstances < 1
                YAtomicTask:et1._threshold < 1
                YAtomicTask:et1._creationMode does not equal 'static' or 'dynamic'
            */
            YMessagePrinter.printMessages(messages);
            fail(((YVerificationMessage)messages.get(0)).getMessage());
        }
    }
}
