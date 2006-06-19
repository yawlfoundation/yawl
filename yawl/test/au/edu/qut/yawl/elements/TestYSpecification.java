/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.jdom.JDOMException;

import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;

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
    private YSpecification _decompAttributeSpec;
    private YSpecification spec;
//    private String validType1;
//    private String validType2;
//    private String validType3;
//    private String validType4;


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
        File file4 = new File(getClass().getResource("DecompositionAttributeSpec.xml").getFile());
        _goodSpecification = (YSpecification) YMarshal.unmarshalSpecifications(file1.getAbsolutePath()).get(0);
        _badSpecification = (YSpecification) YMarshal.unmarshalSpecifications(file2.getAbsolutePath()).get(0);
        _infiniteLoops = (YSpecification) YMarshal.unmarshalSpecifications(file3.getAbsolutePath()).get(0);
        _decompAttributeSpec = (YSpecification) YMarshal.unmarshalSpecifications(file4.getAbsolutePath()).get(0);
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
    
    /**
     * Tests that the XML can write a decomposition with attributes besides the
     * default attributes (the id and xsi:type).
     */
    public void testDecompositionAttributeSpec() {
    	String str = _decompAttributeSpec.toXML();
    	str = "<specificationSet xmlns=\"http://www.citi.qut.edu.au/yawl\" " +
		"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
		"version=\"Beta 7.1\" xsi:schemaLocation=\"http://www.citi.qut.edu.au/yawl" +
		" d:/yawl/schema/YAWL_SchemaBeta6.xsd\">" + str + "</specificationSet>";
    	try {
    		List<YSpecification> specs = YMarshal.unmarshalSpecifications(
    				str, "TestYSpecification.testDecompositionAttributeSpecB" );
    		YSpecification spec = specs.get( 0 );
    		Iterator<YDecomposition> iter = spec._decompositions.iterator();
    		
    		boolean attr1found = false;
    		boolean attr2found = false;
    		
    		while( iter.hasNext() ) {
    			YDecomposition decomp = iter.next();
    			if( decomp.getId().equals( "SignOff" ) ) {
    				if( decomp.getAttribute( "foo" ) != null ) {
    					assertTrue( decomp.getAttribute( "foo" ).equals( "bar" ) );
    					attr1found = true;
    				}
    				if( decomp.getAttribute( "baz" ) != null ) {
    					assertTrue( decomp.getAttribute( "baz" ).equals( "boz" ) );
    					attr2found = true;
    				}
    			}
    		}
    		
    		assertTrue( attr1found );
    		assertTrue( attr2found );
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
    }
    
    public void testDecompositionAttributeSpecB() {
    	_decompAttributeSpec.setName( null );
    	_decompAttributeSpec.setDocumentation( null );
    	String str = _decompAttributeSpec.toXML();
    	str = "<specificationSet xmlns=\"http://www.citi.qut.edu.au/yawl\" " +
    		"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
    		"version=\"Beta 7.1\" xsi:schemaLocation=\"http://www.citi.qut.edu.au/yawl" +
    		" d:/yawl/schema/YAWL_SchemaBeta6.xsd\">" + str + "</specificationSet>";
    	try {
    		List<YSpecification> specs = YMarshal.unmarshalSpecifications(
    				str, "TestYSpecification.testDecompositionAttributeSpecB" );
    		assertTrue( specs.get( 0 ).getName() == null );
    		assertTrue( specs.get( 0 ).getDocumentation() == null );
    	}
    	catch( Exception e ) {
    		StringWriter sw = new StringWriter();
    		sw.write( e.toString() + "\n" );
    		e.printStackTrace(new PrintWriter(sw));
    		fail( sw.toString() );
    	}
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
    
    public void testSpecBetaVersion() {
    	spec.setBetaVersion("beta3");
    	assertTrue( spec.getBetaVersion().equals( YSpecification._Beta3 ) );
    	try {
    		spec.setBetaVersion( "invalid version string that should get rejected" );
    		fail( "Setting an invalid version should throw an exception." );
    	}
    	catch( IllegalArgumentException e ) {
    		// proper exception was thrown
    	}
    }
    
    public void testDecompositions() {
    	YSpecification spec = new YSpecification( "" );
    	spec._decompositions.add(new YDecomposition( "self", spec));
    	assertNull( spec.getDecomposition( "nonexistent" ) );
    	assertNotNull( spec.getDecomposition( "self" ) );
    	assertTrue( spec.getDecomposition( "self" ).getSpecification() == spec );
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
