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
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.JDOMException;

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


    public TestYSpecification(String name) {
    	super(name);
    }


    public void setUp() throws YSchemaBuildingException, YSyntaxException, JDOMException, IOException {
        File specificationFile = new File(YMarshal.class.getResource("MakeRecordings.xml").getFile());
        List specifications = null;
        specifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());

        _originalSpec = (YSpecification) specifications.iterator().next();
        File file1 = new File(getClass().getResource("GoodNetSpecification.xml").getFile());
        File file2 = new File(getClass().getResource("BadNetSpecification.xml").getFile());
        File file3 = new File(getClass().getResource("infiniteDecomps.xml").getFile());
        _goodSpecification = (YSpecification) YMarshal.unmarshalSpecifications(file1.getAbsolutePath()).get(0);
        _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(file2.getAbsolutePath()).get(0);
        _infiniteLoops = (YSpecification) YMarshal.unmarshalSpecifications(file3.getAbsolutePath()).get(0);
        spec = new YSpecification("something");
    }


    public void testGoodNetVerify() {
        List messages = _goodSpecification.verify();
        if (!YVerificationMessage.containsNoErrors(messages) || messages.size() != 2) {
            /*
            Warning:The decompositon(I) is not being used in this specification.
            Warning:The net (Net:leaf-d) may complete without any generated work.  Check the empty tasks linking from i to o.
            */
            fail(YMessagePrinter.getMessageString(messages));
        }
    }


    public void testBadSpecVerify() {
        List messages = _badSpecification.verify();
        if (messages.size() != 5) {
            /*
            Error:CompositeTask:c-top is not on a backward directed path from i to o.
            Error:ExternalCondition:c1-top is not on a backward directed path from i to o.
            Error:ExternalCondition:c2-top is not on a backward directed path from i to o.
            Error:InputCondition:i-leaf-c preset must be empty: [AtomicTask:h-leaf-c]
            Error:AtomicTask:h-leaf-c [error] any flow into an InputCondition (InputCondition:i-leaf-c) is not allowed.
            */
            fail(YMessagePrinter.getMessageString(messages));
        }
    }

    public void testSpecWithLoops() {
        List messages = _infiniteLoops.verify();
        /*
        Warning:The decompositon(f) is not being used in this specification.
        Error:The element (CompositeTask:d.1) plays a part in an inifinite loop/recursion in which no work items may be created.
        Warning:The net (Net:f) may complete without any generated work.  Check the empty tasks linking from i to o.
        Warning:The net (Net:e) may complete without any generated work.  Check the empty tasks linking from i to o.
        */
        assertTrue(YMessagePrinter.getMessageString(messages), messages.size() == 4);
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
        assertTrue(spec.verify().size() == 1);
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
