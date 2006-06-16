package au.edu.qut.yawl.persistence;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.ValidationEventCollector;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.sun.xml.bind.IDResolver;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import au.edu.qut.yawl.elements.SpecificationSet;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMetaData;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.jaxb.ControlTypeCodeType;
import au.edu.qut.yawl.jaxb.FlowsIntoType;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YVerificationMessage;
import junit.framework.TestCase;

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
