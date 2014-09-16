package org.yawlfoundation.yawl.elements;

import junit.framework.TestCase;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.io.File;
import java.io.IOException;

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

    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
		File file1  = new File(getClass().getResource("YAWL_Specification1.xml").getFile());
        YSpecification specification1 = null;

        specification1 = (YSpecification)
                        YMarshal.unmarshalSpecifications(StringUtil.fileToString(file1.getAbsolutePath())).get(0);

        YNet net = specification1.getRootNet();
        this._myCompositeTask1 = (YCompositeTask) net.getNetElement("c-top");
        this._myCompositeTask2 = new YCompositeTask("ct1",YCompositeTask._AND,YCompositeTask._AND,net);
    }


    public void testValidCompositeTask(){
        YVerificationHandler handler = new YVerificationHandler();
        _myCompositeTask1.verify(handler);
        if (handler.hasMessages()) {
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail((handler.getMessages().get(0)).getMessage());
        }
    }

    public void testInvalidCompositeTask(){
        YVerificationHandler handler = new YVerificationHandler();
        _myCompositeTask2.verify(handler);
        if (handler.hasMessages()) {
            fail(_myCompositeTask2 + " should have thrown exception but didn't.");
        }
    }
}
