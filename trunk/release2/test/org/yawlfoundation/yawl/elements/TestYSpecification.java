package org.yawlfoundation.yawl.elements;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 
 * Author: Lachlan Aldred
 * Date: 17/04/2003
 * Time: 14:35:09
 * 
 */
public class TestYSpecification extends TestCase {
    private YSpecification _goodSpecification;
    private YSpecification _badSpecification;
    private YSpecification _infiniteLoops;
    private YSpecification _originalSpec;
    private YSpecification spec;
    private String validType1;
    private String validType2;
    private String validType3;
    private String validType4;

    private YVerificationHandler handler = new YVerificationHandler();


    public TestYSpecification(String name) {
        super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        File specificationFile = new File(YMarshal.class.getResource("MakeRecordings.xml").getFile());
        List specifications = null;
        specifications = YMarshal.unmarshalSpecifications(StringUtil.fileToString(specificationFile.getAbsolutePath()));

        _originalSpec = (YSpecification) specifications.iterator().next();
        File file1 = new File(getClass().getResource("GoodNetSpecification.xml").getFile());
        File file2 = new File(getClass().getResource("BadNetSpecification.xml").getFile());
        File file3 = new File(getClass().getResource("infiniteDecomps.xml").getFile());
        _goodSpecification = (YSpecification) YMarshal.unmarshalSpecifications(StringUtil.fileToString(file1.getAbsolutePath())).get(0);
        _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(StringUtil.fileToString(file2.getAbsolutePath())).get(0);
        _infiniteLoops = (YSpecification) YMarshal.unmarshalSpecifications(StringUtil.fileToString(file3.getAbsolutePath())).get(0);
        spec = new YSpecification("something");
        spec.setSchema("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>");
    }


    public void testGoodNetVerify() {
        handler.reset();
        _goodSpecification.verify(handler);
        if (handler.hasErrors() || handler.getWarnings().size() != 2) {
            /*
            Warning:The decompositon(I) is not being used in this specification.
            Warning:The net (Net:leaf-d) may complete without any generated work.  Check the empty tasks linking from i to o.
            */
            fail(handler.getMessages().get(0).getMessage());
        }
    }


    public void testBadSpecVerify() {
        handler.reset();
        _badSpecification.verify(handler);
        if (handler.getMessageCount() != 5) {
            /*
            Error:CompositeTask:c-top is not on a backward directed path from i to o.
            Error:ExternalCondition:c1-top is not on a backward directed path from i to o.
            Error:ExternalCondition:c2-top is not on a backward directed path from i to o.
            Error:InputCondition:i-leaf-c preset must be empty: [AtomicTask:h-leaf-c]
            Error:AtomicTask:h-leaf-c [error] any flow into an InputCondition (InputCondition:i-leaf-c) is not allowed.
            */
            for (YVerificationMessage msg : handler.getMessages()) {
                System.out.println(msg);
            }
            fail(handler.getMessages().get(0).getMessage());
        }
    }

    public void testSpecWithLoops() {
        handler.reset();
        _infiniteLoops.verify(handler);
        /*
        Warning:The decompositon(f) is not being used in this specification.
        Error:The element (CompositeTask:d.1) plays a part in an inifinite loop/recursion in which no work items may be created.
        Warning:The net (Net:f) may complete without any generated work.  Check the empty tasks linking from i to o.
        Warning:The net (Net:e) may complete without any generated work.  Check the empty tasks linking from i to o.
        */
        assertTrue(handler.getMessages().get(0).getMessage(), handler.getMessageCount() == 4);
    }


    public void testDataStructure() {
        YNet root = _originalSpec.getRootNet();
        YTask recordTask = (YTask) root.getNetElement("record");
        assertTrue(recordTask != null);
        YNet recordNet = (YNet) recordTask.getDecompositionPrototype();
        assertTrue(recordNet != null);
        YTask prepare = (YTask) recordNet.getNetElement("prepare");
        assertTrue(prepare._net == recordNet);
    }


    public void testClonedDataStructure() {
        YNet rootNet = _originalSpec.getRootNet();
        YTask recordTask = (YTask) rootNet.getNetElement("record");
        assertTrue(recordTask != null);
        YNet recordNet = (YNet) recordTask.getDecompositionPrototype();
        assertTrue(recordNet != null);
        YTask prepare = (YTask) recordNet.getNetElement("prepare");
        assertTrue(prepare._net == recordNet);

        YNet clonedRootNet = (YNet) rootNet.clone();
        YTask clonedRecordTask = (YTask) clonedRootNet.getNetElement("record");
        assertNotSame(clonedRecordTask, recordTask);
        YNet clonedRecordNet = (YNet) recordNet.clone();
        assertNotSame(clonedRecordNet, recordNet);
        YTask prepareClone = (YTask) clonedRecordNet.getNetElement("prepare");
        assertSame(prepareClone._net, clonedRecordNet);
        assertSame(prepareClone._mi_active._myTask, prepareClone);
    }


    /**
     * Test specs ability to correctly handle valid data types.
     */
    public void testValidDataTypesInSpecification() {
        //Error:Specifications must have a root net.
        handler.reset();
        spec.verify(handler);
        assertTrue(handler.getMessageCount() == 1);
    }


    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        runner.doRun(suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestYSpecification.class);
        return suite;
    }

}
