/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence;

import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 17/04/2003
 * Time: 14:30:45
 * 
 */
public class TestYCompositeTask extends TestCase{
    private YCompositeTask _myCompositeTask1;
    private YCompositeTask _myCompositeTask2;

    public TestYCompositeTask(String name){
        super(name);
    }

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException, Exception {
		File file1  = new File(getClass().getResource("YAWL_Specification1.xml").getFile());
        YSpecification specification1 = null;

        specification1 = (YSpecification)
                        YMarshal.unmarshalSpecifications(file1.getAbsolutePath()).get(0);

//        specification1 = StringProducerJaxb.getInstance().getSpecification("YAWL_Specification1.xml", true);
//        specification1.verify();
        YNet net = specification1.getRootNet();
        this._myCompositeTask1 = (YCompositeTask) net.getNetElement("c-top");
        this._myCompositeTask2 = new YCompositeTask("ct1",YCompositeTask._AND,YCompositeTask._AND,net);
    }


    public void testValidCompositeTask(){
        List messages = _myCompositeTask1.verify();
        if(messages.size() > 0){
            YMessagePrinter.printMessages(messages);
            fail(((YVerificationMessage)messages.get(0)).getMessage());
        }
    }

    public void testInvalidCompositeTask(){
        List messages = _myCompositeTask2.verify();
        if(messages.size() == 0){
            fail(_myCompositeTask2 + " should have thrown exception but didn't.");
        }
    }
}
