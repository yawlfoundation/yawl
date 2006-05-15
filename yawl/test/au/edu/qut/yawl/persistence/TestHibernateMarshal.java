package au.edu.qut.yawl.persistence;

import java.util.List;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.util.YVerificationMessage;


public class TestHibernateMarshal extends XMLTestCase  {

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
    
    protected void assertComparison(String testComment, String testFilename) throws Exception {
    	String controlXml = StringProducerRawFile.getInstance().getXMLString(testFilename, true);
    	String testXml = StringProducerHibernate.getInstance().getXMLString(testFilename, true);
    	System.out.println(testXml);

		Diff diff = new Diff(controlXml, testXml);
		assertXMLEqual(testComment, diff, diff.identical());
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
		return (List<YVerificationMessage>) spec.verify();
	}

}
