package au.edu.qut.yawl.persistence;

import java.util.List;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.util.YVerificationMessage;

public class TestJaxbMarshal extends XMLTestCase {
	
	public void assertComparison(String fileName) throws Exception {
		String controlXml = StringProducerYAWL.getInstance().getXMLString(fileName, true); 
//		String testXml= StringProducerRawFile.getInstance().getXMLString(fileName, true);
		String testXml= StringProducerJaxb.getInstance().getXMLString(fileName, true);
//		System.err.println(controlXml);
//		System.out.println(testXml);
		Diff diff = new Diff(controlXml, testXml);
		assertXMLIdentical("comparing minimal", diff, diff.identical());
		assertValidation(fileName);
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
		YSpecification spec = StringProducerJaxb.getInstance().getSpecification(fileName, true);
		return spec.verify();
	}

}
