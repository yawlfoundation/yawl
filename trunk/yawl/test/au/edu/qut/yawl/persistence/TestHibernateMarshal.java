/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import java.awt.geom.Point2D;
import java.util.List;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.AbstractHibernateDAOTestCase;
import au.edu.qut.yawl.util.YVerificationMessage;

import com.nexusbpm.editor.persistence.YDecompositionEditorExtension;

public class TestHibernateMarshal extends AbstractHibernateDAOTestCase {
	public void setUp() throws Exception{
		super.setUp();
	}
	
	public void testENEMinimal() throws Exception {
    	assertComparison("Comparing a minimal spec", "TestENEMinimal.xml");
    }

    public void testFlowMinimal() throws Exception {
    	assertComparison("Comparing a minimal spec", "TestFlowMinimal.xml");
    }
    
    public void testMDMetaData() throws Exception {
    	assertComparison("comparing metadata", "TestMetaDataDetailed.xml");
    }

    public void testMDMinimal() throws Exception {
    	assertComparison("comparing a minimal metadata", "TestMetaDataMinimal.xml");
    }

    public void testDecompLocalVariable() throws Exception {
    	assertComparison("comparing local variable", "TestDecompLocalVariable.xml");
    }
    
    public void testDecompOutputParameters() throws Exception {
    	assertComparison("comparing output parameter", "TestDecompOutputParameters.xml");
    }
    
    public void testDecompInputParameters() throws Exception {
    	assertComparison("Comparing input parameter", "TestDecompInputParameters.xml");
    }

    public void testDecompMinimal() throws Exception {
    	assertComparison("Comparing a minimal spec", "TestDecompMinimal.xml");
    }
    
    public void testGateway() throws Exception {
    	assertComparison("Comparing a complex spec with a gateway", "TestGateway.xml");
    }
	
//    public void testSpecImportedNet() throws Exception {
//    	// Ask Lachlan for an imported net example
//    	throw new Exception("This test will fail until we have a test xml file for imported net!");
//    	//assertComparison("comparing specification with imported net", "TestSpecImportedNet.xml");
//    }

    public void testSpecAttributes() throws Exception {
    	assertComparison("comparing input params", "TestSpecAttributes.xml");
    }
    
    public void testSpecMetaData() throws Exception {
    	assertComparison("comparing metadata", "TestSpecMetaData.xml");
    }
	
    public void testBigTest() throws Exception {
    	assertComparison("big test", "TestMakeRecordingsBigTest.xml");
    }

    public void testSpecMinimal() throws Exception {
    	assertComparison("comparing a minimal spec", "TestSpecMinimal.xml");
    }
    
    public void testSpecEmbeddedExtensions() throws Exception {
		YSpecification spec = StringProducerHibernate.getInstance().getSpecification("TestSpecEmbeddedExtensions.xml", true);
		YDecomposition decomp = spec.getDecomposition("OverseeMusic");
		YDecompositionEditorExtension decompExten = new YDecompositionEditorExtension(decomp);
		assertEquals(
				"Extension must present a value",
				decompExten.getCenterPoint(), 
				new Point2D.Double(100, 100)
		);
		decompExten.setCenterPoint(new Point2D.Double(200, 200));
		assertEquals(
				"Extension must be modifiable",
				decompExten.getCenterPoint(), 
				new Point2D.Double(200, 200)
		);
    }
    
    protected void assertComparison(String testComment, String testFilename) throws Exception {
    	String controlXml = StringProducerRawFile.getInstance().getXMLString(testFilename, true);
    	String testXml = StringProducerHibernate.getInstance().getXMLString(testFilename, true);
		Diff diff = new Diff(controlXml, testXml);
		XMLTestCase c = new XMLTestCase();
		c.assertXMLEqual(testComment, diff, false);
		assertValidation(testFilename);
    }

	public void assertValidation(String fileName) throws Exception{
		List<YVerificationMessage> l = getValidationList(fileName);
		for (YVerificationMessage message:  l) {
 			System.out.println(fileName + ":" + message.getMessage());
		}
		for (YVerificationMessage message:  l) {
 			assertFalse("Error in validation:" + message.getMessage(), YVerificationMessage.ERROR_STATUS.equals(message.getStatus()));
		}
	}

	public List<YVerificationMessage> getValidationList(String fileName) throws Exception {
		YSpecification spec = StringProducerHibernate.getInstance().getSpecification(fileName, true);
		YSpecification rspec = StringProducerYAWL.getInstance().getSpecification(fileName, true);
//		showSpec(spec);
//		showSpec(rspec);
		return (List<YVerificationMessage>) spec.verify();
	}

	public void showSpec(YSpecification spec) {
		for (YDecomposition decomp: spec.getDecompositions()) {
			System.err.println("Decomp:" + decomp);
			if (decomp instanceof YNet) {
				for (YExternalNetElement elem: ((YNet) decomp).getNetElements()) {
					System.err.println("  Element:" + elem);
					for (YFlow flow: elem.getPresetFlows()) {
						System.err.println("    PREFlow:" + flow);
					}
					for (YFlow flow: elem.getPostsetFlows()) {
						System.err.println("    POSFlow:" + flow);
					}
				}
			}
		}
	}
	
	
}
